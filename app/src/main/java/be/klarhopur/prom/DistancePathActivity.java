package be.klarhopur.prom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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

public class DistancePathActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private BottomSheetBehavior mBottomSheetBehavior;
    private TextView textViewTime;
    private ImageView backArrow;
    private TextView textViewDistance;
    private EditText distanceEditText;
    private TextView textViewViaPOI;
    private GoogleMap mMap;
    private RecyclerView recyclerViewPOI;
    private Location lastKnownLocation;
    private LocationManager locationManager;

    private Direction direction;
    private ArrayList<PointOfInterest> pointsOfInterest;
    private LatLng origin;
    private LatLng destination;


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
                if(direction == null || pointsOfInterest == null || origin == null || destination == null) return;

                Intent intent = new Intent(getBaseContext(), UserWalkMapActivity.class);
                Bundle b = new Bundle();

                b.putParcelable("origin",origin);
                b.putParcelable("destination",origin);
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
        distanceEditText = findViewById(R.id.distance_search_bar);

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

        final boolean[] addedSuffix = {false};
        final String SUFFIX = " km";
        distanceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // if the only text is the suffix
                if(s.toString().equals(SUFFIX)){
                    distanceEditText.setText(""); // clear the text
                    return;
                }

                // If there is text append on SUFFIX as long as it is not there
                // move cursor back before the suffix
                if(s.length() > 0 && !s.toString().contains(SUFFIX) && !s.toString().equals(SUFFIX)){
                    String text = s.toString().concat(SUFFIX);
                    distanceEditText.setText(text);
                    distanceEditText.setSelection(text.length() - SUFFIX.length());
                    addedSuffix[0] = true; // flip the addedSuffix flag to true
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0){
                    addedSuffix[0] = false; // reset the addedSuffix flag
                }
            }
        });

        distanceEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @SuppressLint("MissingPermission")
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                String query = String.valueOf(distanceEditText.getText());
                if(query.equals(""))
                    return true;

                double value = Double.valueOf(query.substring(0,query.length()-3)); //remove suffix
                if(value > 30){
                    Toast.makeText(DistancePathActivity.this, "Cette distance n'est pas supportée", Toast.LENGTH_SHORT).show();
                    return true;
                }

                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng currentLatLng = lastKnownLocation == null ?
                    new LatLng(50.224812, 5.344703) :
                    new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                Utils.getRouteForDistance(currentLatLng,value*1000, new Utils.PathComputedCallback() {
                    @Override
                    public void onPathComputed(Direction direction, List<PointOfInterest> pointsOfInterests, LatLng origin, LatLng destination) {
                        PolylineOptions options = new PolylineOptions();
                        options.addAll(Utils.decodePoly(direction.getRouteList().get(0).getOverviewPolyline().getRawPointList()));
                        mMap.addPolyline(options);
                        updateViews(direction,pointsOfInterests,origin,destination);
                    }
                });

                InputMethodManager inputManager =
                        (InputMethodManager) DistancePathActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(
                        DistancePathActivity.this.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                return true;
            }
        });
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
    }
}
