package com.abushawish.ParkNGo.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.abushawish.ParkNGo.R;
import com.abushawish.ParkNGo.Utils.Constants;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class ParkedActivity extends AppCompatActivity {

	private AsyncHttpClient client = new AsyncHttpClient();
	private int spot_num;
	private TextView parkingSpot;
	private TextView cancelLabel;
	private TextView chargedIn;

	//Automatically pops up, but allow user to confirm the parking
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parked);

		parkingSpot = (TextView) findViewById(R.id.parkingSpot);
		cancelLabel = (TextView) findViewById(R.id.cancelLabel);
		chargedIn = (TextView) findViewById(R.id.chargedIn);

		Intent intent = getIntent();
		if (intent != null) {
			spot_num = intent.getIntExtra("spot_num", 0);
		}

		parkingSpot.setText(spot_num + "");

		chargedIn.setText("Click here to confirm");
		chargedIn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {


				SharedPreferences prefs = getSharedPreferences(Constants.PARKPREFERENCES, MODE_PRIVATE);
				String license_plate = prefs.getString("license_plate", null);
				boolean officiallyparked = prefs.getBoolean("officiallyparked", false);

				if (license_plate == null) {
					Intent i = new Intent(getApplicationContext(), LoginActivity.class);
					finish();
					startActivity(i);
				}

				HashMap<String, String> paramMap = new HashMap<String, String>();
				paramMap.put("license_plate", license_plate);

				if (!officiallyparked) {
					paramMap.put("spot_num", spot_num + "");
					parkedHttp(paramMap);
				}
			}
		});


		cancelLabel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

	//Send a POST to the server when user succesfully parks
	private void parkedHttp(HashMap<String, String> paramMap) {

		final RequestParams params = new RequestParams(paramMap);

		client.post(getAbsoluteUrl("/parked"), params, new
				TextHttpResponseHandler() {


					@Override
					public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

					}

					@Override
					public void onSuccess(int statusCode, Header[] headers, String responseString) {

						if (responseString.equalsIgnoreCase("Success")) {
							SharedPreferences.Editor editor = getSharedPreferences(Constants.PARKPREFERENCES,
									MODE_PRIVATE).edit();
							editor.putBoolean("officiallyparked", true);
							if (editor.commit()) {
								finish();
							}
						} else {
							System.out.println("Server says: responseString = " + responseString);
						}

					}

				});
	}

	//Get the route URL
	private static String getAbsoluteUrl(String relativeUrl) {
		return Constants.BASE_SERVER_URL + relativeUrl;
	}
}
