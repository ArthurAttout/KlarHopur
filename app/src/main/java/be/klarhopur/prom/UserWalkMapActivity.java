package be.klarhopur.prom;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.model.Direction;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class UserWalkMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveStartedListener {

    public static final String VISITED_POIS = "VISITED_POIS";

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Marker myMarker;
    private View bottomSheetView;
    private View multiplierView;
    private int poiPassed = 0;

    // INTENT TO GET BACK
    private LatLng origin;
    private LatLng destination;
    private ArrayList<PointOfInterest> pointsOfInterests;
    private String direction;
    private ArrayList<String> visitedPois = new ArrayList<String>();

    private double length;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.NoActionBar);
        setContentView(R.layout.activity_user_walk_map);

        origin =  getIntent().getParcelableExtra("origin");
        pointsOfInterests =  getIntent().getParcelableArrayListExtra("pointsOfInterest");

        direction =  getIntent().getStringExtra("polyline");
        fab = findViewById(R.id.cameraActionButton);

        bottomSheetView = findViewById(R.id.bottomSheetLayout);
        bottomSheetView.setVisibility(View.INVISIBLE);
        multiplierView = findViewById(R.id.multiplierConstraint);
        multiplierView.setVisibility(View.INVISIBLE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent myIntent = new Intent(UserWalkMapActivity.this, CameraActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        // Add a marker in Sydney and move the camera
        //LatLng marche = new LatLng(50.248100, 5.339977);
        //myMarker = googleMap.addMarker(new MarkerOptions().position(marche));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.addMarker(new MarkerOptions()
                .title("Départ")
                .position(origin));

        for (PointOfInterest pointOfInterest : pointsOfInterests) {
            mMap.addMarker(new MarkerOptions()
                    .title(pointOfInterest.getName())
                    .position(pointOfInterest.getLatLng()));
        }

        mMap.addMarker(new MarkerOptions()
                .title("Arrivée")
                .position(destination)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        PolylineOptions options = new PolylineOptions();
        options.addAll(Utils.decodePoly(direction));
        mMap.addPolyline(options);

        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        if (marker.getTitle().equals("Arrivée")) {
            // TODO launch final activity
            Intent intent = new Intent(this, FinishWalkActivity.class);

            intent.putExtra("destination",destination);
            intent.putExtra("length",length);
            intent.putExtra("origin",origin);
            intent.putExtra(VISITED_POIS, pointsOfInterests);

            startActivity(intent);
        } else {

            bottomSheetView.setVisibility(View.VISIBLE);
            View v = findViewById(R.id.cameraActionButton);
            v.setVisibility(View.INVISIBLE);

            View v2 = findViewById(R.id.navBarConstraintLayout);
            //v2.setLayoutParams(new ConstraintLayout.LayoutParams());
            ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) v2.getLayoutParams();
            newLayoutParams.topMargin = 71;
            v2.setLayoutParams(newLayoutParams);

            TextView tv1 = (TextView)findViewById(R.id.poiName);
            tv1.setText(marker.getTitle());
            TextView tv2 = (TextView)findViewById(R.id.poiAddress);

            for (PointOfInterest pointOfInterest : pointsOfInterests) {
                if(pointOfInterest.getName().equals(marker.getTitle())){
                    tv2.setText(pointOfInterest.getAddress());
                    if(poiPassed == 0){
                        multiplierView.setVisibility(View.VISIBLE);
                    }
                    if(!visitedPois.contains(marker.getTitle())) {
                        visitedPois.add(marker.getTitle());
                        poiPassed++;
                        TextView multiplierTxt = (TextView) findViewById(R.id.multiplierText);
                        multiplierTxt.setText("x" + poiPassed);
                    }
                }
            }

        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        if(bottomSheetView.getVisibility() == View.VISIBLE) {
            hideBottomPopup();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCameraMoveStarted(int reason) {

        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            hideBottomPopup();
        } /*else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            Toast.makeText(this, "The user tapped something on the map.",
                    Toast.LENGTH_SHORT).show();
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            Toast.makeText(this, "The app moved the camera.",
                    Toast.LENGTH_SHORT).show();
        }*/
    }

    private void hideBottomPopup() {
        bottomSheetView.setVisibility(View.INVISIBLE);
        View v = findViewById(R.id.cameraActionButton);
        v.setVisibility(View.VISIBLE);

        View v2 = findViewById(R.id.navBarConstraintLayout);
        //v2.setLayoutParams(new ConstraintLayout.LayoutParams());
        ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) v2.getLayoutParams();
        newLayoutParams.topMargin = 0;
        v2.setLayoutParams(newLayoutParams);
    }
}
