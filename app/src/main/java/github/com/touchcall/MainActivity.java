package github.com.touchcall;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public final static String TAG = "Touch Call";
	private GestureDetector GD;
	private ImageView pic;
	TextView mText;
	private SensorManager mSensorManager = null;
	private Sensor mSensor;
	SQLiteDatabase db;
	public UserDB UserDB;
	public String appPath;
	Cursor c;
	public int UserCount=0;
	private MediaPlayer mPlayer = null;
	public String PhoneNumber;
	public int LockLaucher;
	public int clean;
	public int BatInfo = 0;
	public int BatInfoL = 0;
	public SensorEventListener proximitySensorEventListener;
	private static String mFileNameImgBG = null;
	public int PlayTimeNowFow = 0;
	private static final int IntenImgBGID = 123;
	// private AudioRecorder mAudioRecorder;
	private GestureDetector.SimpleOnGestureListener SOGL = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float veloccityX, float veloccityY) {
			int maxcount;
			float x1 = e1.getX();
			float x2 = e2.getX();
			float distanceX = Math.abs(x1 - x2);
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			int height = metrics.heightPixels;
			int width = metrics.widthPixels;

			debug(TAG, "" + height + "x" + width);
			debug(TAG, "distanceX " + distanceX);

			c = db.rawQuery("SELECT * FROM UserDB", null);
			maxcount = c.getCount();
			debug(TAG, "onFling" + maxcount);
			if (distanceX < width / 4)
				return true;

			if (x1 > x2) { // Rigth to left 1 -->// 2
				UserCount++;
				debug(TAG,"UserCount+ = " + UserCount + "Max = " + maxcount);
				if (UserCount >= maxcount) {
					UserCount = -1;
					
					ShowPicture(mFileNameImgBG, pic);
					setTitle(R.string.app_name);
					mText.setText("");
					
					new Thread(new Runnable() {
						public void run() {
							PlayTimeNow();
						}
					}).start();
					return true;
				}
			}

			if (x1 < x2) { // left to right 1 <-- // 2
				UserCount--;
				debug(TAG,"UserCount- = " + UserCount + "Max = " + maxcount);
				if (UserCount < 0) {
					UserCount = maxcount;
					ShowPicture(mFileNameImgBG, pic);
					setTitle(R.string.app_name);
					mText.setText("");
					new Thread(new Runnable() {
						public void run() {
							PlayTimeNow();
						}
					}).start();
					return true;
				}
			}
			debug(TAG,"UserCount = " + UserCount + "Max = " + maxcount);
			if (c.getCount() > 0) {
				if (mSensorManager != null)
					mSensorManager.unregisterListener(proximitySensorEventListener);
				debug(TAG, "UserCount " + UserCount);
				c.moveToPosition(UserCount);
				ShowPicture(c.getString(3), pic);
				setTitle(c.getString(1));
				stopPlaying();
				startPlaying(c.getString(2));
				mText.setText(c.getString(0));
				PhoneNumber = c.getString(1);
			}

			// SystemClock.sleep(100);
			return true;
		}

	};

	@Override
	public void onPause() {
		PlayTimeNowFow = 0;
		// if(LockLaucher == 1) {
		debug(TAG, "ON PASUE");
		if (mSensorManager != null)
			mSensorManager.unregisterListener(proximitySensorEventListener);
		// Intent intent = new Intent(this, MainActivity.class);
		// startActivityForResult(intent,90);
		super.onPause(); // Always call the superclass method first
		// finish();
		// }
		// else
		// {
		// super.onPause(); // Always call the superclass method first
		// }
	}
	protected void onStop() {
		super.onStop();  // Always call the superclass method first
//		 Intent intent = new Intent(this, MainActivity.class);
//		 startActivityForResult(intent,90);
	}
	@Override
	public void onResume() {

		// mPlayer2.release();
		
		if (mSensorManager != null)
			mSensorManager.unregisterListener(proximitySensorEventListener);
		debug(TAG, "ON onResume");
		ShowPicture(mFileNameImgBG, pic);
		setTitle(R.string.app_name);
		mText.setText("");
		PhoneNumber = "NULL";
		super.onResume(); // Always call the superclass method first
		// Intent intent = new Intent(this, MainActivity.class);
		// startActivityForResult(intent,90);
		// finish();
		// // Get the Camera instance as the activity achieves full user focus
		// if (mCamera == null) {
		// initializeCamera(); // Local method to handle camera init
		// }

	}

	public void PlayTimeNow() {
		//ShowPicture(mFileNameImgBG, pic);
		
		if (mSensorManager != null)
			mSensorManager.unregisterListener(proximitySensorEventListener);
		maxvolume();
		if(PlayTimeNowFow == 1)
			return;
		PlayTimeNowFow = 1;
		Date todayDate = new Date();
		int hours = todayDate.getHours();
		int minutes = todayDate.getMinutes();
		MediaPlayer mPlayer2 = null;

		if(PlayTimeNowFow == 0)
			return;
		
		mPlayer2 = MediaPlayer.create(this, R.raw.time);
		mPlayer2.start();
		while (mPlayer2.isPlaying()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mPlayer2.release();

		if(PlayTimeNowFow == 0)
			return;
		if(hours > 12)
			hours = hours - 12;
		mPlayer2 = SetRawSound(hours, mPlayer2);
		mPlayer2.start();
		while (mPlayer2.isPlaying()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if(PlayTimeNowFow == 0)
			return;
		mPlayer2 = MediaPlayer.create(this, R.raw.h);
		mPlayer2.start();
		while (mPlayer2.isPlaying()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mPlayer2.release();
		
		if(PlayTimeNowFow == 0)
			return;

		if(minutes > 10)
		{
			int hightm = minutes/10;
			if(hightm == 1){
				//Play muoi 
				
				mPlayer2 = SetRawSound(10, mPlayer2);
				mPlayer2.start();
				while (mPlayer2.isPlaying()) {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				mPlayer2.release();
			}
			if(hightm > 1){
				mPlayer2 = SetRawSound(hightm, mPlayer2);
				mPlayer2.start();
				while (mPlayer2.isPlaying()) {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				mPlayer2.release();
				
				mPlayer2 = MediaPlayer.create(this, R.raw.muoi);;
				mPlayer2.start();
				while (mPlayer2.isPlaying()) {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				mPlayer2.release();
			}
			
		}
		
		mPlayer2 = SetRawSound(minutes % 10, mPlayer2);
		mPlayer2.start();
		while (mPlayer2.isPlaying()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mPlayer2.release();
		
		if(PlayTimeNowFow == 0)
			return;
		
		mPlayer2 = MediaPlayer.create(this, R.raw.m);
		mPlayer2.start();
		while (mPlayer2.isPlaying()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mPlayer2.release();

		PlayTimeNowFow = 0;
	}

	public MediaPlayer SetRawSound(int id, MediaPlayer mPlayer2) {
		if (id == 0) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n0);
		}
		if (id == 1) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n1);
		}
		if (id == 2) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n2);
		}
		if (id == 3) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n3);
		}
		if (id == 4) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n4);
		}
		if (id == 5) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n5);
		}
		if (id == 6) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n6);
		}
		if (id == 7) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n7);
		}
		if (id == 8) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n8);
		}
		if (id == 9) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n9);
		}
		if (id == 10) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n10);
		}
			
		if (id == 11) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n11);
		}
		if (id == 12) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n12);
		}
		if (id == 13) {
			mPlayer2 = MediaPlayer.create(this, R.raw.n13);
		}
		
		return mPlayer2;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		GD.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GD = new GestureDetector(this, SOGL);

		setContentView(R.layout.activity_main);
		mFileNameImgBG = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "ImgBg.jpg";
		disableRotation(this);
		UserCount = -1;
		LockLaucher = 0;

		appPath = getApplicationContext().getFilesDir().getAbsolutePath();
		db = openOrCreateDatabase("UserDB", Context.MODE_PRIVATE, null);
		UserDB = new UserDB(db);
		pic = (ImageView) findViewById(R.id.imageView1);
		ShowPicture(mFileNameImgBG, pic);
		mText = (TextView) findViewById(R.id.textView1);

		mText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

		registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

	}

	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			int level = intent.getIntExtra("level", 0);
			// TODO: Preform action based upon battery level
//			debug(TAG, "mBatInfoReceiver level " + level);
			if (isPhonePluggedIn(getBaseContext())) {
				debug(TAG, "mBatInfoReceiver isPhonePluggedIn " + level);
				return;
			}
			if (level == 15) {
				maxvolume();
				// Bao het pin
				if (BatInfo == 0) {
					new Thread(new Runnable() {
						public void run() {
							LowBatInfo();
						}
					}).start();
					// buzzz
					BatInfo++;
				}

			}
			if (level == 5) {
				maxvolume();
				if (BatInfoL != 2) {
					new Thread(new Runnable() {
						public void run() {
							LowBatInfo();
						}
					}).start();
					BatInfoL++;
				}
			}
		}
	};

	public static boolean isPhonePluggedIn(Context context) {
		boolean charging = false;

		final Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean batteryCharge = status == BatteryManager.BATTERY_STATUS_CHARGING;

		int chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

		if (batteryCharge)
			charging = true;
		if (usbCharge)
			charging = true;
		if (acCharge)
			charging = true;

		return charging;
	}

	public void LowBatInfo() {
		MediaPlayer mPlayer3;
		mPlayer3 = MediaPlayer.create(this, R.raw.lowb);
		mPlayer3.start();
		while (mPlayer3.isPlaying()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mPlayer3.release();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		debug(TAG,"requestCode" + requestCode);
		switch (requestCode) {
		case 90:
			if (resultCode == RESULT_OK) {
				Bundle res = data.getExtras();
				String result = res.getString("param_result");
//				Log.d("FIRST", "result:" + result);
			}
			break;
			
		case IntenImgBGID:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				InputStream imageStream = null;
				try {

					imageStream = getContentResolver().openInputStream(selectedImage);
					Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
					OutputStream fOut = null;
					File file = new File(mFileNameImgBG);
					fOut = new FileOutputStream(file);

					DisplayMetrics displaymetrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
					int height = displaymetrics.heightPixels;
					int width = displaymetrics.widthPixels;

					int imgh = yourSelectedImage.getHeight();
					int imgw = yourSelectedImage.getWidth();

					int count = 1;
					while (true) {
						if (imgh / count <= height) {
							break;
						}
						count++;
					}

					yourSelectedImage = Bitmap.createScaledBitmap(yourSelectedImage, imgw / count, imgh / count, false);

					// yourSelectedImage.getHeight()

					yourSelectedImage.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
					fOut.flush();
					fOut.close();

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void ShowPicture(String fileName, ImageView pic) {
		File f = new File(fileName);
		FileInputStream is = null;
		try {
			is = new FileInputStream(f);
			Bitmap bm = BitmapFactory.decodeStream(is, null, null);
			pic.setImageBitmap(bm);
			// pic.getHeight();

		} catch (FileNotFoundException e) {
			File file = new File(mFileNameImgBG);
			if(file.exists()) {
				try {
					is = new FileInputStream(file);
					Bitmap bm = BitmapFactory.decodeStream(is, null, null);
					pic.setImageBitmap(bm);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					pic.setImageResource(R.drawable.android_icon);
				}
			}else{
			pic.setImageResource(R.drawable.android_icon);
			}
			
			return;
		}

		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		f.exists();

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		debug(TAG, "Key Back buttion " + keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			debug(TAG, "Key Back buttion \n");
		}
		return false;// super.onKeyDown(keyCode, event);
	}

	public void maxvolume()
	{
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
	}
	private void startPlaying(String mFileNameSound) {

		PlayTimeNowFow = 0;
		maxvolume();
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mFileNameSound);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			debug(TAG, "prepare() failed");
		}
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		proximitySensorEventListener = new SensorEventListener() {

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				debug(TAG, "onAccuracyChanged ");
			}

			@Override
			public void onSensorChanged(SensorEvent event) {
				// TODO Auto-generated method stub
				if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
					debug(TAG, "Proximity Sensor Reading:" + String.valueOf(event.values[0]));
					if ((event.values[0]) != 0) {
						debug(TAG, "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD = 0   " + event.values[0]);
						return;
					}
					mSensorManager.unregisterListener(this);
					stopPlaying();

					if (!PhoneNumber.equals("NULL")) {
						String uri = "tel:" + PhoneNumber;
						Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
						startActivity(callIntent);
					}

				}
			}

		};
		mSensorManager.registerListener(proximitySensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	private void stopPlaying() {
		if (mPlayer != null) {
			// if(mPlayer.isPlaying())
			{
				mPlayer.stop();
				mPlayer.release();
				mPlayer = null;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_addnewuser) {
			ActionAddNewUserClick();
		}
		if (id == R.id.ListUser) {
			ListUserClick();
		}
		if (id == R.id.imagebackground) {
			imagebackground();
		}
		if (id == R.id.about) {

			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("About");
			builder.setMessage(R.string.about);
			builder.create();
			builder.show();

		}

		return super.onOptionsItemSelected(item);
	}

	public void imagebackground()
	{
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, IntenImgBGID);
	}
 
	public void LockLaucherClick() {
		if (LockLaucher == 0)
			LockLaucher = 1;
		else
			LockLaucher = 0;
	}

	public void ListUserClick() {
		debug(TAG,"ListUserClick");
		Intent intent = new Intent(this, ListUserActivity.class);
		startActivityForResult(intent, 99);
	}

	public void ActionAddNewUserClick() {
		debug(TAG,"ActionAddNewUserClick");
		Intent intent = new Intent(this, AddUserActivity.class);
		startActivityForResult(intent, 90);
	}



	private static void disableRotation(Activity activity) {
		final int orientation = activity.getResources().getConfiguration().orientation;
		final int rotation = activity.getWindowManager().getDefaultDisplay().getOrientation();

		// Copied from Android docs, since we don't have these values in Froyo
		// 2.2
		int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8;
		int SCREEN_ORIENTATION_REVERSE_PORTRAIT = 9;

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
			SCREEN_ORIENTATION_REVERSE_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			SCREEN_ORIENTATION_REVERSE_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		}

		if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
			if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
		} else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
			if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			} else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			}
		}
	}

	
	private static void enableRotation(Activity activity) {
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	public void debug(String Tag,String smg) {
		//Log.e(TAG, smg);
		//Toast.makeText(getApplicationContext(), smg, Toast.LENGTH_LONG).show();
	}
}
