package com.example.manhngo.avsemap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static com.example.manhngo.avsemap.Convert.convertLatTngTo;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;

/**
 * Created by Manh Ngo on 11/30/2016.
 */

public class ControlMap extends Activity implements View.OnClickListener {

    public void setmMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

    private static final int REQUES_CODE_LOCATION = 10;

    private GoogleMap mMap;
    private Context context;
    private Activity activity;

    private FloatingActionButton fabMyLocation;
    private FloatingActionButton fabTraffic;
    private FloatingActionButton fabStuck;
    private FloatingActionButton fabMapType;

    private LatLng myLatLng;
    private Marker touchLMarker;
    private String touchLString = "MNH";
    private Address touchLAddress;
    private Circle circle;

    public void setInternet(Boolean internet) {
        this.internet = internet;
    }

    private Boolean internet;

    private boolean myLocation = true;
    private boolean myTraffic = false;
    private boolean myStuck = false;
    private boolean myMapType = true; // true: normall || false: satelite

    GPSTracker gpsTracker;


    DatabaseReference root;
    LatLng latLng;
    Address address;
    Marker findMaker;
    String strFindAddress;
    OnCircle onCircle;
    String userName;

    public void setConvert(Convert convert) {
        this.convert = convert;
    }


    Convert convert;

    public void setOnCircle(OnCircle onCircle, DatabaseReference root, Address address, LatLng latLng
            , Marker findMaker, String strFindAddress, String userName, Convert convert) {
        this.onCircle = onCircle;
        this.root = root;
        this.address = address;
        this.latLng = latLng;
        this.findMaker = findMaker;
        this.strFindAddress = strFindAddress;
        this.userName = userName;
        this.convert = convert;
    }


    private boolean mShowPermissionDeniedDialog = false;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String stringTypeStuck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    public ControlMap(GoogleMap googleMap, Context context, Activity activity) {
        this.mMap = googleMap;
        this.context = context;
        this.activity = activity;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setFabMyLocation(FloatingActionButton fabMyLocation) {
        this.fabMyLocation = fabMyLocation;
    }

    public void setFabTraffic(FloatingActionButton fabTraffic) {
        this.fabTraffic = fabTraffic;
    }

    public void setFabStuck(FloatingActionButton fabStuck) {
        this.fabStuck = fabStuck;
    }

    public void setFabMapType(FloatingActionButton fabMapType) {
        this.fabMapType = fabMapType;
    }


    public void setControll() {
        fabMapType.setOnClickListener(this);
        //fabMyLocation.setOnClickListener(this);
        fabTraffic.setOnClickListener(this);
        fabStuck.setOnClickListener(this);


    }


    private boolean checkReady() {
        if (mMap == null) {
            Log.d("checkReady", String.valueOf(R.string.map_not_ready));
            return false;
        }
        return true;
    }

    public LatLng updateMyLocation() {
        LatLng latLng = null;
        if (!checkReady()) {
            return null;
        }

        if (!myLocation) {
            mMap.setMyLocationEnabled(false);
            Log.d("Load traffic", "ACB");
            return null;
        }

        // Enable the location layer. Request the location permission if needed.
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            gpsTracker = new GPSTracker(context, activity);
            if (gpsTracker.canGetLocation()) {
                Log.d("MapsActivity", "canGetLocation");
                Log.d("MA-Location",
                        "la" + gpsTracker.getLatitude() + ", " + gpsTracker.getLongitude());

                latLng = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
            } else {
                Log.d("MapsActivity", "can't get location");
            }
            Log.d("Set Coordinate", "ACB");
        } else {
            // Uncheck the box until the layer has been enabled and request missing permission.
            myLocation = false;
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.INTERNET,
                    }, 10);

        }
        return latLng;
    }

    private void updateTraffic() {
        if (!checkReady()) {
            return;
        }
        mMap.setTrafficEnabled(myTraffic);
    }

    private void updateMapType() {
        // No toast because this can also be called by the Android framework in onResume() at which
        // point mMap may not be ready yet.
        if (!checkReady()) {
            return;
        }
        if (myMapType) {
            mMap.setMapType(MAP_TYPE_NORMAL);
        } else {
            mMap.setMapType(MAP_TYPE_SATELLITE);
        }
    }

    private void updateLocationStuck() {
        latLng = updateMyLocation();
        if (myStuck) {
            if (latLng == null) {
                Log.d("GPS", "Chua dinh vi");
            } else {
                //strFindAddress = convert.getAddressStringFromLatLng(latLng.latitude,latLng.longitude, context);
                 address = convert.getAddressFromLatLng(latLng.latitude, latLng.longitude, context);
                showCustomDialogBox(context);
            }
        } else {
            if (circle != null) {
                circle.remove();
            }
        }

    }

    Dialog customDialog;

    public void showCustomDialogBox(Context context) {

        customDialog = new Dialog(context);
        customDialog.setTitle("Custom Dialog Title");
        // match customDialog with custom dialog layout
        customDialog.setContentView(R.layout.type_stuck);

        (customDialog.findViewById(R.id.btn_Stuck)).setOnClickListener(this);
        (customDialog.findViewById(R.id.btn_Police)).setOnClickListener(this);
        (customDialog.findViewById(R.id.btn_Accident)).setOnClickListener(this);
        (customDialog.findViewById(R.id.btn_Construct)).setOnClickListener(this);
        (customDialog.findViewById(R.id.btn_Flooding)).setOnClickListener(this);


        customDialog.show();

    }


    public void onClick(View view) {
        if (view == fabMapType) {
            myMapType = !myMapType;
            updateMapType();
        } else if (view == fabMyLocation) {
            myLocation = !myLocation;
            updateMyLocation();
        } else if (view == fabTraffic) {
            myTraffic = !myTraffic;
            updateTraffic();
        } else if (view == fabStuck) {
            myStuck = !myStuck;
            if(internet) {
                //we are connected to a network
                updateLocationStuck();
            }
            else{
                showDialogNotInternet();
            }
        }
        // Type Stuck
        else if (view.getId() == R.id.btn_Police) {
            stringTypeStuck = TypeStuck.POLICE;
            updateServer();
            DrawOnMap();
            customDialog.dismiss();
        } else if (view.getId() == R.id.btn_Stuck) {
            stringTypeStuck = TypeStuck.STUCK;
            updateServer();
            DrawOnMap();
            customDialog.dismiss();
        } else if (view.getId() == R.id.btn_Accident) {
            stringTypeStuck = TypeStuck.ACCIDENT;
            updateServer();
            DrawOnMap();
            customDialog.dismiss();
        } else if (view.getId() == R.id.btn_Construct) {
            stringTypeStuck = TypeStuck.CONSTRUCT;
            updateServer();
            DrawOnMap();
            customDialog.dismiss();
        } else if (view.getId() == R.id.btn_Flooding) {
            stringTypeStuck = TypeStuck.FLOODING;
            updateServer();
            DrawOnMap();
            customDialog.dismiss();
        }
    }

    public void updateServer() {
        root = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> cmMap = new HashMap<>();
        cmMap.put(convertLatTngTo(latLng), address.getAddressLine(0));
        root.updateChildren(cmMap);
        cmMap.clear();
        // update type
        root = root.child(convertLatTngTo(latLng));
        cmMap.put("Name", userName);
        cmMap.put("Type", stringTypeStuck);
        root.updateChildren(cmMap);
    }

    private void showDialogNotInternet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage("You're not connected network! \n You can't use this feature!")
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        builder.show();
    }

    public void DrawOnMap() {
        onCircle.drawCircle(latLng, stringTypeStuck);
        Log.d("OnMapLong-Address", address.getAddressLine(0));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUES_CODE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }

    }


}


