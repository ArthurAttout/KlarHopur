package be.klarhopur.prom;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

// TODO post path history to Firebase

public class FinishWalkActivity extends AppCompatActivity {

    private ArrayList<PointOfInterest> pointsOfInterests;
    private LatLng destination;
    private double length;
    private LatLng origin;
    private int points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resultat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        destination =  getIntent().getParcelableExtra("destination");
        length = getIntent().getDoubleExtra("length",1);
        origin =  getIntent().getParcelableExtra("origin");
        pointsOfInterests =  getIntent().getParcelableArrayListExtra("VISITED_POIS");
        points = getIntent().getIntExtra("points",10);

        if(destination != null && origin != null && pointsOfInterests != null){
            TextView textViewPoints = findViewById(R.id.textViewPoints);
            TextView textViewDistance = findViewById(R.id.textViewDistance);

            textViewDistance.setText(String.format("%.2f km", length/1000));
            textViewPoints.setText(String.valueOf(points) + " points");

            HashMap<String,Object> map = new HashMap<>();
            HashMap<String,Boolean> mapPoi = new HashMap<>();
            for (PointOfInterest pointOfInterest : pointsOfInterests) {
                mapPoi.put(pointOfInterest.getID(),true);
            }

            map.put("destination",destination);
            map.put("length",length/1000);
            map.put("origin",origin);
            map.put("poi",mapPoi);
            map.put("points",points);
            map.put("title","Balade auto-générée");
            map.put("urlimg","https://prom.longree.be/img/Capture2.png");

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
            firebaseDatabase.child("users")
                    .child(user.getUid())
                    .child("path_history")
                    .child(UUID.randomUUID().toString())
                    .setValue(map);
        }

        findViewById(R.id.floatingActionButtonHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerViewPOI = findViewById(R.id.visitedPoiRecycler);
        recyclerViewPOI.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPOI.setAdapter(new POIAdapter(new ArrayList<>(pointsOfInterests)));
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

    }

}
