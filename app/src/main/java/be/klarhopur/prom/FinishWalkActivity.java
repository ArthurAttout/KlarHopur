package be.klarhopur.prom;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;

// TODO post path history to Firebase

public class FinishWalkActivity extends AppCompatActivity {

    private ArrayList<PointOfInterest> pointsOfInterests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resultat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pointsOfInterests =  getIntent().getParcelableArrayListExtra("VISITED_POIS");

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
