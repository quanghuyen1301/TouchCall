package github.com.touchcall;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//import com.example.soundlaucher.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AddUserActivity extends Activity {
	public final static String TAG = "hqbui";
	private static String mFileNameSound = null;
	private static String mFileNamePic = null;
	public UserDB UserDB;
	SQLiteDatabase db;
	Button Recorder;
	Button Play;
	private MediaRecorder mRecorder = null;
	private MediaPlayer mPlayer = null;
	public String appPath;
	static String appPath2;
	String mCurrentPhotoPath;
	static final int REQUEST_TAKE_PHOTO = 1;
	private static final int SELECT_PHOTO = 100;
	Button SelectPic;
	Button mOk;
	TextView mUser;
	TextView mPhone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_user);
		db = openOrCreateDatabase("UserDB", Context.MODE_PRIVATE, null);
		UserDB = new UserDB(db);
		appPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
		appPath2 = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
		debug(TAG, appPath);
		mUser = (TextView) findViewById(R.id.UserName);
		mPhone = (TextView) findViewById(R.id.PhoneNumber);
		Recorder = (Button) findViewById(R.id.Recorder);
		Recorder.setOnClickListener(new OnClickListener() {
			boolean mStartRecording = true;

			@Override
			public void onClick(View view) {
				if (checktextviewempty()) {
					mFileNameSound = appPath + mPhone.getText() + ".3gp";
					onRecord(mStartRecording);
					if (mStartRecording) {
						Recorder.setText("Stop");
					} else {
						Recorder.setText("Start");
					}
					mStartRecording = !mStartRecording;
				}
			}
		});
		Play = (Button) findViewById(R.id.Play);
		Play.setOnClickListener(new OnClickListener() {
			boolean mStartPlaying = true;

			@Override
			public void onClick(View view) {
				onPlay(mStartPlaying);

			}
		});

		SelectPic = (Button) findViewById(R.id.BrowmPic);

		SelectPic.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (checktextviewempty()) {
					mFileNamePic = appPath + mPhone.getText() + ".jpg";
					Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
					photoPickerIntent.setType("image/*");
					startActivityForResult(photoPickerIntent, SELECT_PHOTO);
				}
			}
		});
		mOk = (Button) findViewById(R.id.Ok);
		mOk.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				UserDB.AddUser(mUser.getText().toString(), mPhone.getText().toString(), mFileNameSound, mFileNamePic);
				UserDB.Show();
				finish();
			}
		});
	}

	boolean checktextviewempty() {
		if (mUser.getText().toString().equals("")) {
			mUser.setError("Please enter User Name");
			return false;
		}
		if (mPhone.getText().toString().equals("")) {
			mPhone.setError("Please enter Phone Number");
			return false;
		}
		return true;
	}

	private void onPlay(boolean start) {
		if (mFileNameSound == null) {
			debug(TAG,"Please recorder voice");
		} else {
			if (start) {
				startPlaying();
			} else {
				stopPlaying();
			}
		}
	}

	private void startPlaying() {
		Play.setText("Stop");
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mFileNameSound);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			debug(TAG, "prepare() failed");
		}

	}

	private void stopPlaying() {
		Play.setText("Start");
		mPlayer.release();
		mPlayer = null;
	}

	private void onRecord(boolean start) {
		if (start) {
			startRecording();
		} else {
			stopRecording();
		}
	}

	private void startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileNameSound);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			debug(TAG, "prepare() failed");
		}

		mRecorder.start();
	}

	private void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 90:
			if (resultCode == RESULT_OK) {
				Bundle res = data.getExtras();
				String result = res.getString("results");
//				Log.d("FIRST", "result:" + result);
			}

			break;
		case SELECT_PHOTO:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				InputStream imageStream = null;
				try {

					imageStream = getContentResolver().openInputStream(selectedImage);
					Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
					OutputStream fOut = null;
					File file = new File(mFileNamePic);
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

	public void debug(String Tag,String smg) {
		//Log.e(TAG, smg);
		//Toast.makeText(getApplicationContext(), smg, Toast.LENGTH_LONG).show();
	}
}
