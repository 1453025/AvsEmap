package com.example.manhngo.avsemap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

public class GPSTracker extends Service implements LocationListener {

	private static final int REQUES_CODE_LOCATION = 10;
	private final Context context;

	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;
	Activity activity;
	Location location;

	double latitude;
	double longitude;

	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

	protected LocationManager locationManager;

	public GPSTracker(Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
		getLocation();
	}

//	@Override
//	public void onCreate() {
//		super.onCreate();
//		activity = (Activity) context;
//	}

	public Location getLocation() {
		try {
			if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
					PackageManager.PERMISSION_GRANTED &&
					ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
							PackageManager.PERMISSION_GRANTED) {

				ActivityCompat.requestPermissions(activity, new String[]{
						Manifest.permission.ACCESS_FINE_LOCATION,
						Manifest.permission.ACCESS_COARSE_LOCATION}, REQUES_CODE_LOCATION);
			}
			locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				Log.d("GPSTracker", "No GPS & No Network");
			} else {
				this.canGetLocation = true;

				if (isNetworkEnabled) {
					Log.d("GPSTracker", "Use Network");
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

						if (location != null) {

							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}

				}

				if (isGPSEnabled) {
					Log.d("GPSTracker", "Use GPS");
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

						if (locationManager != null) {
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}


	public void stopUsingGPS() {
		if (locationManager != null) {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
					PackageManager.PERMISSION_GRANTED &&
					ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
							PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(activity, new String[]{
						Manifest.permission.ACCESS_FINE_LOCATION,
						Manifest.permission.ACCESS_COARSE_LOCATION}, REQUES_CODE_LOCATION);
			}
			locationManager.removeUpdates(GPSTracker.this);
		}
	}
	
	public double getLatitude() {
		if(location != null) {
			latitude = location.getLatitude();
		}
		return latitude;
	}
	
	public double getLongitude() {
		if(location != null) {
			longitude = location.getLongitude();
		}
		
		return longitude;
	}
	
	public boolean canGetLocation() {
		return this.canGetLocation;
	}
	
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		
		alertDialog.setTitle("GPS is settings");
		
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
		
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				context.startActivity(intent);
			}
		});
		
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		alertDialog.show();
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}


	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case REQUES_CODE_LOCATION: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					getLocation();

					Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
				}
				return;
			}
		}

	}

}
