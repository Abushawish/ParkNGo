package com.abushawish.ParkNGo.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.abushawish.ParkNGo.R;
import com.abushawish.ParkNGo.Utils.Constants;
import com.estimote.sdk.SystemRequirementsChecker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

	private AsyncHttpClient client = new AsyncHttpClient();
	private ListView parkingListView;
	private ArrayList parkingList;
	private ArrayAdapter parkingListAdapter;

	//If user already logged in, allow him to view parking history, else open login/sign up activity
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SharedPreferences prefs = getSharedPreferences(Constants.PARKPREFERENCES, MODE_PRIVATE);
		String license_plate = prefs.getString("license_plate", null);

		if (license_plate == null) {
			Intent i = new Intent(getApplicationContext(), LoginActivity.class);
			finish();
			startActivity(i);
		}

		HashMap<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("license_plate", license_plate);

		listOfParkingHttp(paramMap);

		parkingList = new ArrayList();
		parkingListView = (ListView) findViewById(R.id.listView);
		parkingListAdapter = new ArrayAdapter(getApplicationContext(),
				android.R.layout.simple_list_item_1, parkingList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView text = (TextView) view.findViewById(android.R.id.text1);
				text.setTextColor(Color.WHITE);
				return view;
			}
		};
		parkingListView.setAdapter(parkingListAdapter);

	}

	//Sends a POST to the server requesting the list of parking history of the users
	private void listOfParkingHttp(HashMap<String, String> paramMap) {
		final RequestParams params = new RequestParams(paramMap);

		client.post(getAbsoluteUrl("/listofparking"), params, new
				JsonHttpResponseHandler() {

					@Override
					public void onFailure(int statusCode, Header[] headers, Throwable
							throwable, JSONArray response) {

					}

					@Override
					public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
						try {
							String details;

							for (int i = 0; i < response.length(); i++) {
								JSONObject jsonobject = response.getJSONObject(i);
								String spot_num = jsonobject.getString("spot_num");
								long time_in = jsonobject.getLong("time_in");
								long time_out;
								float total_charge;
								Date date = new Date ();

								if (jsonobject.isNull("time_out") == false) {
									time_out = jsonobject.getLong("time_out");
									total_charge = (float) jsonobject.getInt("total_charge");

									date.setTime((long)time_out);

									details = "On " + date + " paid $" + total_charge/100 + " at " +
											"parking lot " + spot_num;
								} else {
									date.setTime((long)time_in);
									details = "Pending parking began " + date +
											" at parking lot " + spot_num;
								}
								parkingList.add(details);
							}
							parkingListAdapter.notifyDataSetChanged();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

				});
	}

	//Get the route URL
	private static String getAbsoluteUrl(String relativeUrl) {
		return Constants.BASE_SERVER_URL + relativeUrl;
	}

	@Override
	protected void onResume() {
		super.onResume();

		SystemRequirementsChecker.checkWithDefaultDialogs(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
