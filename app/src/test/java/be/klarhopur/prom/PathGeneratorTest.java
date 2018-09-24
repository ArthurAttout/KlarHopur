package be.klarhopur.prom;

import com.akexorcist.googledirection.model.RoutePolyline;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import static org.junit.Assert.*;

public class PathGeneratorTest {

    @Test
    public void generationIsNotEmpty(){
        Utils.getRouteFromAtoB(
            new LatLng(50.23413,5.344769),
            new LatLng(50.246785,5.336868),
            new Utils.PathComputedCallback() {
                @Override
                public void onPathComputed(RoutePolyline polyline) {
                    assertTrue(polyline != null);
                    assertTrue(!polyline.getRawPointList().equals(""));
                }
            }
        );
    }
}
