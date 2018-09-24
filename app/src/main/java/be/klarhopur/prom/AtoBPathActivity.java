package be.klarhopur.prom;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AtoBPathActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private BottomSheetBehavior mBottomSheetBehavior;
    private EditText editTextStartPoint;
    private EditText editTextEndPoint;
    private ImageButton imageButtonInfo;

    private TextView textViewTime;
    private ImageView backArrow;
    private TextView textViewDistance;
    private TextView textViewViaPOI;
    private GoogleMap mMap;
    private RecyclerView recyclerViewPOI;
    private Location lastKnownLocation;
    private ImageButton imageButtonDone;

    private LocationManager locationManager;
    private Direction direction;
    private ArrayList<PointOfInterest> pointsOfInterest;
    private LatLng origin;
    private LatLng destination;

    private Marker markerStart;
    private Marker markerEnd;

    private String startID;
    private String endID;

    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_ato_bpath);

        View bottomSheet = findViewById( R.id.bottom_sheet );
        FloatingActionButton button = findViewById( R.id.prom_floatingActionButton );

        imageButtonInfo = findViewById(R.id.imageButtonInfo);
        recyclerViewPOI = findViewById(R.id.recyclerViewPOI);

        editTextStartPoint = findViewById(R.id.start_search_bar);
        editTextStartPoint.setKeyListener(null);

        editTextEndPoint = findViewById(R.id.end_search_bar);
        editTextEndPoint.setKeyListener(null);

        backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        geocoder = new Geocoder(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(direction == null || pointsOfInterest == null || origin == null || destination == null) return;

            Intent intent = new Intent(getBaseContext(), UserWalkMapActivity.class);
            Bundle b = new Bundle();

            b.putParcelable("origin",origin);
            b.putParcelable("destination",destination);
            b.putString("polyline",direction.getRouteList().get(0).getOverviewPolyline().getRawPointList());
            b.putParcelableArrayList("pointsOfInterest",pointsOfInterest);
            b.putParcelable("direction",direction);

            intent.putExtras(b);
            startActivity(intent);
            }
        });
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(300);  // 0 if you don't want to show the bottom sheet on the starting activity.
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        textViewTime = findViewById(R.id.bottom_time_text_view);
        recyclerViewPOI = findViewById(R.id.recyclerViewPOI);
        backArrow = findViewById(R.id.back_arrow);
        textViewDistance = findViewById(R.id.bottom_distance_text_view);
        textViewViaPOI = findViewById(R.id.bottom_via_poi_text_view);
        textViewDistance = findViewById(R.id.bottom_distance_text_view);
        imageButtonDone = findViewById(R.id.imageButtonDone);

        imageButtonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.getRouteFromAtoB(markerStart.getPosition(),markerEnd.getPosition(), new Utils.PathComputedCallback() {
                    @Override
                    public void onPathComputed(Direction direction, List<PointOfInterest> pointsOfInterests, LatLng origin, LatLng destination) {
                        PolylineOptions options = new PolylineOptions();
                        options.addAll(Utils.decodePoly(direction.getRouteList().get(0).getOverviewPolyline().getRawPointList()));
                        mMap.addPolyline(options);
                        updateViews(direction,pointsOfInterests,origin,destination);
                    }
                });
            }
        });

        imageButtonInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(AtoBPathActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(AtoBPathActivity.this);
                }
                builder.setTitle("Infos")
                    .setMessage("Faites glisser les marqueurs pour déterminer vos points de départ et d'arrivée.")
                    .show();
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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prom_floatingActionButton:
                // run next activity
                break;
        }
    }

    private void updateViews(Direction direction, List<PointOfInterest> pointsOfInterests, LatLng origin, LatLng destination) {

        this.direction = direction;
        this.pointsOfInterest = new ArrayList<>(pointsOfInterests);
        this.origin = origin;
        this.destination = destination;

        mMap.addMarker(new MarkerOptions()
                .title("Départ")
                .position(origin));

        for (PointOfInterest pointOfInterest : pointsOfInterests) {
            mMap.addMarker(new MarkerOptions()
                    .title(pointsOfInterests.indexOf(pointOfInterest)+1 + " - " + pointOfInterest.getName())
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lastKnownLocation != null) onLocationChanged(lastKnownLocation);

        LatLng currentLatLng = lastKnownLocation == null ?
                new LatLng(50.224812, 5.344703) :
                new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());

        LatLng latLngDestination = new LatLng(currentLatLng.latitude + 0.01,currentLatLng.longitude);

        markerStart = mMap.addMarker(new MarkerOptions()
                .title("Départ")
                .draggable(true)
                .position(currentLatLng));
        startID = markerStart.getId();

        markerEnd = mMap.addMarker(new MarkerOptions()
                .title("Arrivée")
                .draggable(true)
                .position(latLngDestination));
        endID = markerEnd.getId();

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}

            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                try {
                    List<Address> fromLocation = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                    Address address = fromLocation.get(0);

                   if(marker.getId().equals(startID))
                       editTextStartPoint.setText(address.getAddressLine(0));
                   else
                       editTextEndPoint.setText(address.getAddressLine(0));


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
