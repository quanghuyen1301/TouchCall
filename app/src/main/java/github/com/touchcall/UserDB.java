package github.com.touchcall;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

public class UserDB {
	public final static String TAG = "hqbui";
	SQLiteDatabase db;
	public int lock;

	public UserDB(SQLiteDatabase db) {
		debug(TAG, "Open USERBD");
		// SQLiteDatabase.openOrCreateDatabase("UserDB", null);
		this.db = db;
		db.execSQL("CREATE TABLE IF NOT EXISTS UserDB(UserName VARCHAR,PhoneNumber VARCHAR,UserVoice VARCHAR,UserPic VARCHAR);");
	}

	public void AddUser(String UserName, String PhoneNumber, String UserVoice, String UserPic) {
		debug(TAG, "Open AddUser");
		Cursor c = db.rawQuery("SELECT * FROM UserDB WHERE PhoneNumber='" + PhoneNumber + "'", null);
		if (c.moveToFirst()) {
			// db.execSQL("DELETE FROM UserDB WHERE PhoneNumber='"+PhoneNumber+"'");
		} else {
			db.execSQL("INSERT INTO UserDB VALUES('" + UserName + "','" + PhoneNumber + "','" + UserVoice + "','" + UserPic + "');");
		}
	}

	public void DelUser(String PhoneNumber) {

		db.execSQL("DELETE FROM UserDB  WHERE PhoneNumber='" + PhoneNumber + "'");
	}

	public void Show() {
		debug(TAG, "Open Show");
		Cursor c = db.rawQuery("SELECT * FROM UserDB", null);
		if (c.getCount() == 0) {
			debug(TAG, "UserDB Empty");
			return;
		}
		while (c.moveToNext()) {
			debug(TAG, "UserName = " + c.getString(0) + " PhoneNumber = " + c.getString(1) + " UserVoice = " + c.getString(2) + " UserPic = " + c.getString(3));
		}
	}
	public void debug(String Tag,String smg) {
		//Log.e(TAG, smg);
	}
}
