package github.com.touchcall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ListUserActivity extends Activity {
	public final static String TAG = "Touch Call";
	public UserDB UserDB;
	SQLiteDatabase db;
	ListView listView;
	int DBposition;
	Cursor c;
	int maxcount;
	AlertDialog.Builder alertDialogBuilder;
	final Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_user);
		db = openOrCreateDatabase("UserDB", Context.MODE_PRIVATE, null);
		UserDB = new UserDB(db);
		listView = (ListView) findViewById(R.id.listView1);

		// Defined Array values to show in ListView
		c = db.rawQuery("SELECT * FROM UserDB", null);
		maxcount = c.getCount();
		String[] values = new String[maxcount];
		for (int i = 0; i < maxcount; i++) {
			c.moveToPosition(i);
			values[i] = c.getString(1);
		}

		// Define a new Adapter
		// First parameter - Context
		// Second parameter - Layout for the row
		// Third parameter - ID of the TextView to which the data is written
		// Forth - the Array of data
		alertDialogBuilder = new AlertDialog.Builder(context);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);

		// Assign adapter to ListView
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "Click ListItem Number " + position + (String) listView.getItemAtPosition(position), Toast.LENGTH_LONG).show();

				DBposition = position;

				// set title
				alertDialogBuilder.setTitle("Remove Phone Number");

				// set dialog message
				alertDialogBuilder.setMessage("Do you want delete ?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity
						UserDB.DelUser((String) listView.getItemAtPosition(DBposition));
						StartListUserActivity();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
			}
		});
	}

	void StartListUserActivity() {
		Intent intent = new Intent(this, ListUserActivity.class);
		startActivityForResult(intent, 90);
		finish();
	}

}
