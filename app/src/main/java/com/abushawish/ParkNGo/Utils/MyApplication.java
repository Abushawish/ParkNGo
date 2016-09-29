package com.abushawish.ParkNGo.Utils;

/**
 * Created by Moe on 16-03-11.
 */

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.abushawish.ParkNGo.Activity.LoginActivity;
import com.abushawish.ParkNGo.Activity.MainActivity;
import com.abushawish.ParkNGo.Activity.ParkedActivity;
import com.abushawish.ParkNGo.Utils.Obdapi.commands.engine.RPMCommand;
import com.abushawish.ParkNGo.Utils.Obdapi.commands.protocol.EchoOffCommand;
import com.abushawish.ParkNGo.Utils.Obdapi.commands.protocol.LineFeedOffCommand;
import com.abushawish.ParkNGo.Utils.Obdapi.commands.protocol.SelectProtocolCommand;
import com.abushawish.ParkNGo.Utils.Obdapi.commands.protocol.TimeoutCommand;
import com.abushawish.ParkNGo.Utils.Obdapi.commands.temperature.AmbientAirTemperatureCommand;
import com.abushawish.ParkNGo.Utils.Obdapi.enums.ObdProtocols;
import com.abushawish.ParkNGo.Utils.Obdapi.exceptions.UnableToConnectException;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

public class MyApplication extends Application {

	private AsyncHttpClient client = new AsyncHttpClient();
	private static BeaconManager beaconManager;
	private static Region region;
	private static Beacon parkedBeacon;
	private static BluetoothSocket socket;
	private SharedPreferences prefs;


	@Override
	public void onCreate() {
		super.onCreate();

		prefs = getSharedPreferences(Constants.PARKPREFERENCES,
				MODE_PRIVATE);

		//Connect to the OBD if needed
		if (socket == null || !socket.isConnected()) {
			socket = connectOBD();
		}

		beaconManager = new BeaconManager(getApplicationContext());
		region = new Region("ranged region",
				UUID.fromString(Constants.BLUETOOTH_BEACON_UUID), null, null);

		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				beaconManager.startRanging(region);
			}
		});

		//Checks the distance of the Bluetooth beacon, if within range and engine is off
		//then car is considered parked
		beaconManager.setRangingListener(new BeaconManager
				.RangingListener() {
			@Override
			public void onBeaconsDiscovered(Region region, List<Beacon> list) {
				boolean officially_parked = prefs.getBoolean("officiallyparked", false);
				if (!list.isEmpty()) {
					for (Beacon beacon : list) {
						if (Utils.computeAccuracy(beacon) < Constants.DISTANCE_OF_BEACON && !officially_parked && isCarEngingOff(socket)) {
							parkedBeacon = beacon;
							Intent i = new Intent(MyApplication.this, ParkedActivity.class);
							i.putExtra("spot_num", beacon.getMajor());
							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							MyApplication.this.startActivity(i);
						} else if (parkedBeacon != null && Utils.computeAccuracy(parkedBeacon) >
								Constants.DISTANCE_OF_BEACON && officially_parked) {
							leftParking();
						}
					}
				} else if (officially_parked) {
					//If user already parked and there are no beacons then considered leaving
					leftParking();
				}
			}
		});
	}

	private void leftParking() {
		SharedPreferences prefs = getSharedPreferences(Constants.PARKPREFERENCES, MODE_PRIVATE);
		String license_plate = prefs.getString("license_plate", null);

		if (license_plate == null) {
			Intent i = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(i);
		}

		HashMap<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("license_plate", license_plate);

		leftParkHttp(paramMap);

	}

	//Checking the car engine, if it's off it means the car is parked
	private boolean isCarEngingOff(final BluetoothSocket socket) {
		//Initing the OBD here
		if (socket.isConnected()) {
			try {
				new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
				new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
				new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
				new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
				new AmbientAirTemperatureCommand().run(socket.getInputStream(), socket.getOutputStream());
				final RPMCommand engineRpmCommand = new RPMCommand();

				//Observing the RPMs of the vehicle through the OBD
				try {
					engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
					return true;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return true;
				}

				//Engine is off, meaning parked
				if (engineRpmCommand.getCalculatedResult().equals("0")) {
					return true;
				} else {
					return false;
				}
			} catch (UnableToConnectException e) {
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return true;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return true;
			}
		}

		return true;
	}

	//Connect to the Bluetooth OBD
	private BluetoothSocket connectOBD() {
		//Connect the bluetooth (OBD)
		String deviceAddress = Constants.OBD_BLUETOOTH_MAC;
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
		UUID uuid = UUID.fromString(Constants.OBD_BLUETOOTH_UUID);
		BluetoothSocket socket = null;
		try {
			socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
			socket.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return socket;
	}

	//Used to display notifications to the user
	private void showNotification(String title, String message) {
		Intent notifyIntent = new Intent(this, MainActivity.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
				new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification.Builder(this)
				.setSmallIcon(android.R.drawable.ic_dialog_info)
				.setContentTitle(title)
				.setContentText(message)
				.setAutoCancel(true)
				.setContentIntent(pendingIntent)
				.build();
		notification.defaults |= Notification.DEFAULT_SOUND;
		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1, notification);
	}

	//Inform the server that the car has left the parking spot
	private void leftParkHttp(HashMap<String, String> paramMap) {

		final RequestParams params = new RequestParams(paramMap);

		client.post(getAbsoluteUrl("/leftparking"), params, new
				TextHttpResponseHandler() {


					@Override
					public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
						System.out.println("Server says: responseString = " + responseString);
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers, String responseString) {

						if (responseString.contains("Success")) {
							SharedPreferences.Editor editor = prefs.edit();
							editor.putBoolean("officiallyparked", false);
							editor.commit();
							System.out.println("prefs.getString() after commit = " + prefs
									.getBoolean
											("officiallyparked", false));
							showNotification("Park-N-Go", responseString.substring(responseString.lastIndexOf(",") + 1));

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