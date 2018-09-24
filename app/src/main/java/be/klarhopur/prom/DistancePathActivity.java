package be.klarhopur.prom;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class DistancePathActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private BottomSheetBehavior mBottomSheetBehavior;
    private TextView textViewTime;
    private ImageView backArrow;
    private TextView textViewDistance;
    private TextView textViewViaPOI;
    private GoogleMap mMap;
    private Location lastKnownLocation;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_distance_path);

        View bottomSheet = findViewById( R.id.bottom_sheet );
        FloatingActionButton button = findViewById( R.id.prom_floatingActionButton );


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(300);  // 0 if you don't want to show the bottom sheet on the starting activity.
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        textViewTime = findViewById(R.id.bottom_time_text_view);
        backArrow = findViewById(R.id.back_arrow);
        textViewDistance = findViewById(R.id.bottom_distance_text_view);
        textViewViaPOI = findViewById(R.id.bottom_via_poi_text_view);


        textViewDistance = findViewById(R.id.bottom_distance_text_view);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
        if(mMap != null)
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lastKnownLocation != null) onLocationChanged(lastKnownLocation);

        LatLng currentLatLng = lastKnownLocation == null ?
                new LatLng(50.224812, 5.344703) :
                new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
    }
}
