package be.klarhopur.prom;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RandomPathActivity extends AppCompatActivity implements View.OnClickListener,OnMapReadyCallback, LocationListener {

    private BottomSheetBehavior mBottomSheetBehavior;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private EnumTypeRandomPath typeEnum;
    private Location lastKnownLocation;
    private TextView textViewTime;
    private TextView textViewDistance;
    private RecyclerView recyclerViewPOI;
    private TextView textViewViaPOI;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_random_path);

        int type = getIntent().getExtras().getInt("type",-1);
        typeEnum = EnumTypeRandomPath.values()[type];

        View bottomSheet = findViewById( R.id.bottom_sheet );
        textViewTime = findViewById(R.id.bottom_time_text_view);
        backArrow = findViewById(R.id.back_arrow);
        recyclerViewPOI = findViewById(R.id.recyclerViewPOI);
        textViewDistance = findViewById(R.id.bottom_distance_text_view);
        textViewViaPOI = findViewById(R.id.bottom_via_poi_text_view);

        FloatingActionButton button = (FloatingActionButton) findViewById( R.id.prom_floatingActionButton );

        textViewDistance = findViewById(R.id.bottom_distance_text_view);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        button.setOnClickListener(this);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(300);  // 0 if you don't want to show the bottom sheet on the starting activity.
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prom_floatingActionButton:
                // run next activity
                break;
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
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lastKnownLocation != null) onLocationChanged(lastKnownLocation);

        LatLng currentLatLng = lastKnownLocation == null ?
                new LatLng(50.224812, 5.344703) :
                new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());

        switch (typeEnum) {
            case SHORT:
                handleShortPath(currentLatLng);
                break;
            case MEDIUM:
                handleMediumPath(currentLatLng);
                break;
            case LONG:
                handleLongPath(currentLatLng);
                break;
        }
    }

    private void handleLongPath(LatLng currentLatLng) {

        Utils.getRouteForDistance(
            currentLatLng,
            10000,
            new Utils.PathComputedCallback() {
                @Override
                public void onPathComputed(Direction direction, List<PointOfInterest> pointsOfInterests, LatLng origin, LatLng destination) {
                    PolylineOptions options = new PolylineOptions();
                    options.addAll(Utils.decodePoly(direction.getRouteList().get(0).getOverviewPolyline().getRawPointList()));
                    mMap.addPolyline(options);

                    updateViews(direction,pointsOfInterests,origin,destination);
                }
            }
        );
    }

    private void updateViews(Direction direction, List<PointOfInterest> pointsOfInterests, LatLng origin, LatLng destination) {

        mMap.addMarker(new MarkerOptions()
                .title("Départ")
                .position(origin));

        for (PointOfInterest pointOfInterest : pointsOfInterests) {
            mMap.addMarker(new MarkerOptions()
                    .title(pointsOfInterests.indexOf(pointOfInterest) + " - " + pointOfInterest.getName())
                    .position(pointOfInterest.getLatLng()));
        }

        mMap.addMarker(new MarkerOptions()
                .title("Arrivée")
                .position(destination)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


        int totalSeconds = 0;
        double totalDistanceMeters = 0;

        for (Leg leg : direction.getRouteList().get(0).getLegList()) {
            totalSeconds += Integer.parseInt(leg.getDuration().getValue());
            totalDistanceMeters += Integer.parseInt(leg.getDistance().getValue());
        }

        String outTime;
        if(totalSeconds < 3600){
            outTime = String.format("%d min",
                    TimeUnit.SECONDS.toMinutes(totalSeconds)
            );
        }
        else
        {
            outTime = String.format("%d h, %d min",
                    TimeUnit.SECONDS.toHours(totalSeconds),
                    TimeUnit.SECONDS.toMinutes(totalSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(totalSeconds))
            );
        }
        textViewTime.setText(outTime);

        String outDistance;
        if(totalDistanceMeters > 1000){
            outDistance = totalDistanceMeters/1000 + "km";
        }
        else
        {
            outDistance = totalDistanceMeters + "m";
        }

        textViewDistance.setText(outDistance);
        textViewViaPOI.setText(String.format("via %d points d'intérêt", pointsOfInterests.size()));

        recyclerViewPOI.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPOI.setAdapter(new POIAdapter(new ArrayList<>(pointsOfInterests)));
    }

    private void handleMediumPath(LatLng currentLatLng) {
        Utils.getRouteForDistance(
            currentLatLng,
            6000,
            new Utils.PathComputedCallback() {
                @Override
                public void onPathComputed(Direction direction, List<PointOfInterest> pointsOfInterests, LatLng origin, LatLng destination) {
                    PolylineOptions options = new PolylineOptions();
                    options.addAll(Utils.decodePoly(direction.getRouteList().get(0).getOverviewPolyline().getRawPointList()));
                    mMap.addPolyline(options);
                    updateViews(direction,pointsOfInterests,origin,destination);
                }
            }
        );
    }

    private void handleShortPath(LatLng currentLatLng) {
        Utils.getRouteForDistance(
            currentLatLng,
            2500,
            new Utils.PathComputedCallback() {
                @Override
                public void onPathComputed(Direction direction, List<PointOfInterest> pointsOfInterests, LatLng origin, LatLng destination) {
                    PolylineOptions options = new PolylineOptions();
                    options.addAll(Utils.decodePoly(direction.getRouteList().get(0).getOverviewPolyline().getRawPointList()));
                    mMap.addPolyline(options);
                    updateViews(direction,pointsOfInterests,origin,destination);
                }
            }
        );
    }


}
