package com.abushawish.ParkNGo.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.abushawish.ParkNGo.R;
import com.abushawish.ParkNGo.Utils.Constants;
import com.devmarvel.creditcardentry.library.CreditCard;
import com.devmarvel.creditcardentry.library.CreditCardForm;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Moe on 16-04-11.
 */
public class SignupActivity extends AppCompatActivity {

	//Using AsyncHttpClient for simpler HTTP requests
	private AsyncHttpClient client = new AsyncHttpClient();
	private ProgressDialog progressDialog;
	private SharedPreferences.Editor sharedpreferences;

	//Using ButterKnife library for simpler UI
	@InjectView(R.id.input_name)
	EditText _nameText;
	@InjectView(R.id.input_plate)
	EditText _licenseText;
	@InjectView(R.id.input_email)
	EditText _emailText;
	@InjectView(R.id.input_password)
	EditText _passwordText;
	@InjectView(R.id.btn_signup)
	Button _signupButton;
	@InjectView(R.id.link_login)
	TextView _loginLink;
	@InjectView(R.id.credit_card_form)
	CreditCardForm _creditCardForm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		ButterKnife.inject(this);

		sharedpreferences = getSharedPreferences(Constants.PARKPREFERENCES, Context.MODE_PRIVATE).edit();

		_signupButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				signup();
			}
		});

		_loginLink.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Finish the registration screen and return to the Login activity
				finish();
			}
		});

		_licenseText.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
	}

	//Called when the user signs up
	private void signup() {

		if (!validate()) {
			onSignupFailed();
			return;
		}

		_signupButton.setEnabled(true);

		progressDialog = new ProgressDialog(SignupActivity.this,
				R.style.AppTheme_Dark_Dialog);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("Creating Account...");
		progressDialog.show();

		CreditCard card = _creditCardForm.getCreditCard();
		final String name = _nameText.getText().toString();
		final String lplate = _licenseText.getText().toString();
		final String email = _emailText.getText().toString();
		final String password = _passwordText.getText().toString();
		String ccNum = card.getCardNumber();
		int ccMonth = card.getExpMonth();
		int ccYear = card.getExpYear() + 2000;
		String ccSecurity = card.getSecurityCode();

		//Create a stripe card with the given info to be used with Stripe API
		Card stripeCard = new Card(ccNum, ccMonth, ccYear, ccSecurity);
		stripeCard.setName(name);

		Stripe stripe;
		try {
			stripe = new Stripe(Constants.P_KEY_STRIPE);
			stripe.createToken(
					stripeCard,
					new TokenCallback() {
						public void onSuccess(Token token) {
							HashMap<String, String> paramMap = new HashMap<String, String>();
							paramMap.put("license_plate", lplate);
							paramMap.put("fname", name);
							paramMap.put("email", email);
							paramMap.put("password", password);
							paramMap.put("stripeToken", token.getId());

							sharedpreferences.putString("license_plate", lplate);
							sharedpreferences.putString("password", password);

							signupUserHttp(paramMap);
						}

						public void onError(Exception error) {
							// Show localized error message
							Toast.makeText(SignupActivity.this,
									error.getLocalizedMessage(),
									Toast.LENGTH_LONG
							).show();
						}
					}
			);
		} catch (AuthenticationException e) {
			e.printStackTrace();
		}

	}

	//Called when the sign up is succesful
	private void onSignupSuccess() {
		sharedpreferences.commit();
		_signupButton.setEnabled(true);
		progressDialog.dismiss();
		setResult(RESULT_OK, null);
		finish();
	}

	//If sign up fails
	private void onSignupFailed() {
		Toast.makeText(getBaseContext(), "Sign up failed", Toast.LENGTH_LONG).show();
		sharedpreferences.clear();
		sharedpreferences.commit();
		_signupButton.setEnabled(true);
	}

	//Check if name, license plate, email and password are valid entries
	private boolean validate() {
		boolean valid = true;

		String name = _nameText.getText().toString();
		String lplate = _licenseText.getText().toString();
		String email = _emailText.getText().toString();
		String password = _passwordText.getText().toString();

		if (name.isEmpty() || name.length() < 3) {
			_nameText.setError("at least 3 characters");
			valid = false;
		} else {
			_nameText.setError(null);
		}

		if (lplate.isEmpty() || lplate.length() < 3) {
			_licenseText.setError("enter valid license plate");
			valid = false;
		} else {
			_licenseText.setError(null);
		}

		if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			_emailText.setError("enter a valid email address");
			valid = false;
		} else {
			_emailText.setError(null);
		}

		if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
			_passwordText.setError("between 4 and 40 alphanumeric characters");
			valid = false;
		} else {
			_passwordText.setError(null);
		}

		if (!_creditCardForm.isCreditCardValid()) {
			valid = false;
		}

		return valid;
	}

	//Sends POST request to the server to signup
	private void signupUserHttp(HashMap<String, String> paramMap) {

		final RequestParams params = new RequestParams(paramMap);

		client.post(getAbsoluteUrl("/newAccount"), params, new
				TextHttpResponseHandler() {

					@Override
					public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

					}

					@Override
					public void onSuccess(int statusCode, Header[] headers, String responseString) {
						if (responseString.equalsIgnoreCase("Success")) {
							onSignupSuccess();
						} else {
							onSignupFailed();
						}
					}
				});
	}

	//Get the route URL
	private static String getAbsoluteUrl(String relativeUrl) {
		return Constants.BASE_SERVER_URL + relativeUrl;
	}
}