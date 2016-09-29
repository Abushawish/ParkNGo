package com.abushawish.ParkNGo.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.abushawish.ParkNGo.R;
import com.abushawish.ParkNGo.Utils.Constants;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Moe on 16-04-11.
 */
public class LoginActivity extends AppCompatActivity {

	//Using AsyncHttpClient for simpler HTTP requests
	private AsyncHttpClient client = new AsyncHttpClient();
	private static final int REQUEST_SIGNUP = 0;
	private ProgressDialog progressDialog;
	private SharedPreferences.Editor sharedpreferences;

	//Using ButterKnife library for simpler UI
	@InjectView(R.id.input_plate) EditText _licenseText;
	@InjectView(R.id.input_password) EditText _passwordText;
	@InjectView(R.id.btn_login)
	Button _loginButton;
	@InjectView(R.id.link_signup)
	TextView _signupLink;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = getSharedPreferences(Constants.PARKPREFERENCES, MODE_PRIVATE);
		String license_plate = prefs.getString("license_plate", null);

		if (license_plate != null) {
			Intent i = new Intent(getApplicationContext(), MainActivity.class);
			finish();
			startActivity(i);
		}

		sharedpreferences = getSharedPreferences(Constants.PARKPREFERENCES, Context.MODE_PRIVATE).edit();

		setContentView(R.layout.activity_login);
		ButterKnife.inject(this);

		_loginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				login();
			}
		});

		_signupLink.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Start the Signup activity
				Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
				startActivityForResult(intent, REQUEST_SIGNUP);
			}
		});
	}

	//Attempting to login when the user enters the information
	public void login() {

		//If the login information is not valid
		if (!validate()) {
			onLoginFailed();
			return;
		}

		progressDialog = new ProgressDialog(LoginActivity.this,
				R.style.AppTheme_Dark_Dialog);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("Authenticating...");
		progressDialog.show();

		String lplate = _licenseText.getText().toString();
		String password = _passwordText.getText().toString();

		HashMap<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("license_plate", lplate);
		paramMap.put("password", password);

		//Put given information in sharedPrefs, will commit later when we succeed
		sharedpreferences.putString("license_plate", lplate);
		sharedpreferences.putString("password", password);

		loginUserHttp(paramMap);
	}

	//Sends POST request to the server to login
	private void loginUserHttp(HashMap<String, String> paramMap) {

		final RequestParams params = new RequestParams(paramMap);

		client.post(getAbsoluteUrl("/login"), params, new
				TextHttpResponseHandler() {

					@Override
					public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

					}

					@Override
					public void onSuccess(int statusCode, Header[] headers, String responseString) {

						if (responseString.equalsIgnoreCase("Success")) {
							onLoginSuccess();
						} else {
							onLoginFailed();
						}

					}

				});
	}

	//If signup succeeds go to MainAcitivity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SIGNUP) {
			if (resultCode == RESULT_OK) {
				Intent i = new Intent(getApplicationContext(), MainActivity.class);
				finish();
				startActivity(i);
			}
		}
	}

	//Disable going back to the MainActivity
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
		sharedpreferences.clear();
		sharedpreferences.commit();
	}

	//When login succeeds go to mainActivity
	private void onLoginSuccess() {
		_loginButton.setEnabled(true);
		progressDialog.dismiss();
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		finish();
		startActivity(i);
		sharedpreferences.commit();
	}

	//If login failed, allow to retry
	private void onLoginFailed() {
		Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
		progressDialog.dismiss();
		_loginButton.setEnabled(true);
		sharedpreferences.clear();
		sharedpreferences.commit();
	}

	//Check if license plate and password are valid entries
	private boolean validate() {
		boolean valid = true;

		String licensePlate = _licenseText.getText().toString();
		String password = _passwordText.getText().toString();

		if (licensePlate.isEmpty()) {
			_licenseText.setError("enter a valid email address");
			valid = false;
		} else {
			_licenseText.setError(null);
		}

		if (password.isEmpty() || password.length() < 4 || password.length() > 40) {
			_passwordText.setError("between 4 and 40 alphanumeric characters");
			valid = false;
		} else {
			_passwordText.setError(null);
		}

		return valid;
	}

	//Get the route URL
	private static String getAbsoluteUrl(String relativeUrl) {
		return Constants.BASE_SERVER_URL + relativeUrl;
	}
}