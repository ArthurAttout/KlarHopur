package be.klarhopur.prom;

import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class AtoBPathActivity extends AppCompatActivity implements View.OnClickListener {

    private BottomSheetBehavior mBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_ato_bpath);

        View bottomSheet = findViewById( R.id.bottom_sheet );
        FloatingActionButton button = (FloatingActionButton) findViewById( R.id.prom_floatingActionButton );


        button.setOnClickListener(this);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(300);  // 0 if you don't want to show the bottom sheet on the starting activity.
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prom_floatingActionButton:
                // run next activity
                break;
        }
    }
}
