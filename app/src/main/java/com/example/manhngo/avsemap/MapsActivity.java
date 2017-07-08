package com.example.manhngo.avsemap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.manhngo.avsemap.Interaction.Comment_Room;
import com.example.manhngo.avsemap.Interaction.LoginActivity;
import com.example.manhngo.avsemap.Interaction.RegisterActivity;
import com.example.manhngo.avsemap.Modules_FindingDirection.DirectionFinder;
import com.example.manhngo.avsemap.Modules_FindingDirection.DirectionFinderListener;
import com.example.manhngo.avsemap.Modules_FindingDirection.Route;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.example.manhngo.avsemap.Convert.convertLatTngTo;
import static com.example.manhngo.avsemap.Convert.convertStringTo;


public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, DirectionFinderListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener, GoogleMap.OnCircleClickListener, ChildEventListener{


    String TAG = "MapsActivity";
    private static final int REQUES_CODE_LOCATION = 10;
    private GoogleMap mMap;
    ControlMap controlMap;
    private Context context;

    private ProgressDialog progressDialog;
    private String stringTypeStuck;

    Boolean isSearch;
    private FloatingActionButton fabMyLocation;
    private FloatingActionButton fabTraffic;
    private FloatingActionButton fabStuck;
    private FloatingActionButton fabMapType;
    private FloatingActionButton fabInformationUser;


    OnCircle onCircle;
    LatLng findLatLng;
    Marker findMaker;
    String strFindAddress;

    String strMyLocation;
    LatLng myLatLng;

    Convert convert;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Circle> stuckListCircle = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();

    private ArrayList<Marker> markerArrayList = new ArrayList<>();

    private DatabaseReference root;
    private FirebaseAuth firebaseAuth;

    private String userName = "Client";
    private int numberCircle = 0;

    public boolean internet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        context = getApplicationContext();

        Log.d("MapsActivity", "onCreate");
        convert = new Convert();

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            userName = firebaseAuth.getCurrentUser().getDisplayName();
        }

        for (int i = 0; i < stuckListCircle.size(); i++) {
            stuckListCircle.get(i).remove();
        }

        root = FirebaseDatabase.getInstance().getReference();

        fabMapType = (FloatingActionButton) findViewById(R.id.fab_MapType);
        fabTraffic = (FloatingActionButton) findViewById(R.id.fab_Traffic);
        fabStuck = (FloatingActionButton) findViewById(R.id.fab_Wanted);
        fabInformationUser = (FloatingActionButton) findViewById(R.id.fab_InformationUser);

        fabInformationUser.setOnClickListener(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getLatLng());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MapsActivity", "onStart");
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            internet = true;
        }
        else{
            internet = false;
        }
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("MapsActivity", "onPostResume");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, 300, 0, 0);
        Log.d("MapsActivity", "onMapReady");
        controlMap = new ControlMap(mMap, context, MapsActivity.this);
        onCircle = new OnCircle(mMap, context);
        controlMap.setContext(MapsActivity.this);
        controlMap.setOnCircle(onCircle, root, address1, null, findMaker, strFindAddress, userName, convert);
        controlMap.setInternet(internet);
        root.addChildEventListener(this);

        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCircleClickListener(this);

        controlMap.setFabMapType(fabMapType);
        controlMap.setFabTraffic(fabTraffic);
        controlMap.setFabStuck(fabStuck);
        controlMap.setControll();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.INTERNET,
                    }, 10);

        }else {
            mMap.setMyLocationEnabled(true);
            if (controlMap.updateMyLocation() != null) {
                myLatLng = controlMap.updateMyLocation();
            }
            if (myLatLng != null) {

                if(convert.getAddressStringFromLatLng(myLatLng.latitude, myLatLng.longitude, MapsActivity.this) != null)
                    strMyLocation = convert.getAddressStringFromLatLng(myLatLng.latitude, myLatLng.longitude, MapsActivity.this);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 16));
                Log.d("Move", "Location");
            } else {
                myLatLng = controlMap.updateMyLocation();
            }
        }
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            //((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            //((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(20);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));
            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    Address address1;

    @Override
    public void onMapLongClick(LatLng latLng) {
//        if (mMap == null) {
//            Log.d("onMapLong", "mMap = null");
//            return;
//        }
//        if (findMaker != null) {
//            findMaker.remove();
//            Log.d("onMapLong", "remove marker");
//        }
//        Log.d("onMapLong", "set marker");
//        strFindAddress = convert.getAddressStringFromLatLng(latLng.latitude, latLng.longitude, MapsActivity.this);
//        address1 = convert.getAddressFromLatLng(latLng.latitude, latLng.longitude, MapsActivity.this);
//
//        controlMap.setOnCircle(onCircle, root, address1, latLng, findMaker, strFindAddress, userName, convert);
//        controlMap.showCustomDialogBox(this);
        for (int i = 0; i < destinationMarkers.size(); i++) {
            destinationMarkers.get(i).remove();
        }
        for (int i = 0; i < originMarkers.size(); i++) {
            originMarkers.get(i).remove();
        }
        Address address = convert.getAddressFromLatLng(latLng.latitude, latLng.longitude, this);
        findMaker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(address.getAddressLine(0).toString()));
        findMaker.showInfoWindow();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        Log.d("MA-onSearch", address.getAddressLine(0).toString());
        try {
            new DirectionFinder(MapsActivity.this, strMyLocation, address.getAddressLine(0).toString()).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));


    }


    private void showAlerDialogWasSignedIn(final FirebaseAuth firebaseAuth) {

        if (wasSignedIn(firebaseAuth) == false) {
            showAlerDiaglogNotSigned();
        } else {
            showAlerDialogSigned(firebaseAuth);
        }
    }

    private boolean wasSignedIn(final FirebaseAuth firebaseAuth) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            return false;
        }
        return true;
    }

    private void showAlerDiaglogNotSigned() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setMessage(R.string.notSigned)
                .setNeutralButton(R.string.login, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                    }
                })
                .setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(MapsActivity.this, RegisterActivity.class));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        builder.show();
    }

    private void showAlerDialogSigned(final FirebaseAuth firebaseAuth) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

        builder.setMessage("Username: " + firebaseAuth.getCurrentUser().getDisplayName() + "\n"
                +          "Email: " + firebaseAuth.getCurrentUser().getEmail())
                .setPositiveButton(R.string.signout, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        firebaseAuth.signOut();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        builder.show();
    }



    public void onSearch(String searchTerm) {

        Address address = convert.getAddressFromString(searchTerm, MapsActivity.this);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
        findMaker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(address.getFeatureName().toString()));
        findMaker.showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        Log.d("MA-onSearch", address.getFeatureName().toString());
        try {
            new DirectionFinder(MapsActivity.this, strMyLocation, searchTerm).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        showAlerDialogWasSignedIn(firebaseAuth);
    }


    @Override
    public void onCircleClick(Circle circle) {

        if (wasSignedIn(firebaseAuth)) {
            Intent intent = new Intent(getApplicationContext(), Comment_Room.class);
            intent.putExtra("room_name", convertLatTngTo(circle.getCenter()));
            intent.putExtra("user_name", firebaseAuth.getCurrentUser().getDisplayName());
            startActivity(intent);
        } else {
            showAlerDiaglogNotSigned();
        }
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {

        if (wasSignedIn(firebaseAuth)) {
            Intent intent = new Intent(getApplicationContext(), Comment_Room.class);
            intent.putExtra("room_name", convertLatTngTo(marker.getPosition()));
            intent.putExtra("user_name", firebaseAuth.getCurrentUser().getDisplayName());
            startActivity(intent);
        } else {
            showAlerDiaglogNotSigned();
        }
        return true;
    }


    private void append_Circle_Stuck(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getKey().toString() != null && dataSnapshot.child("Type").getValue() != null) {

            boolean exist = false;
            for (int i = 0; i < stuckListCircle.size(); i++) {
                if (convertStringTo(dataSnapshot.getKey().toString()).equals(stuckListCircle.get(i).getCenter())) {
                    exist = true;
                }
            }
            if (exist == false) {
                Circle circle = onCircle.drawCircle(convertStringTo(dataSnapshot.getKey().toString()), dataSnapshot.child("Type").getValue().toString());
                stuckListCircle.add(circle);
                numberCircle++;
                Log.d("DrawCircle", "Draw " + numberCircle);

            }

        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        append_Circle_Stuck(dataSnapshot);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        append_Circle_Stuck(dataSnapshot);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUES_CODE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    }
                    mMap.setMyLocationEnabled(true);
                    if (controlMap.updateMyLocation() != null) {
                        myLatLng = controlMap.updateMyLocation();
                    }
                    if (myLatLng != null) {
                        strMyLocation = convert.getAddressStringFromLatLng(myLatLng.latitude, myLatLng.longitude, MapsActivity.this);

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 16));
                        Log.d("Move", "Location");
                    } else {
                        myLatLng = controlMap.updateMyLocation();
                    }
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }

    }


}
