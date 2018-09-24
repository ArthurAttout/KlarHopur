package be.klarhopur.prom;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Intent intent = getIntent();
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.camera_container, CameraFragment.newInstance(this))
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
