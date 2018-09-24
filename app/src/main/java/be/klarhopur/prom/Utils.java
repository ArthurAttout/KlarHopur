package be.klarhopur.prom;

import android.support.annotation.NonNull;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.RoutePolyline;
import com.akexorcist.googledirection.request.DirectionDestinationRequest;
import com.akexorcist.googledirection.request.DirectionRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utils {

    public static final double MAX_DISTANCE_METERS = 10000;
    public static final double MAX_N = 4;
    public static final String API_KEY = "AIzaSyCoowU0YF5vZO787raRq9zm7AF-r9FsrA4";

    public static void getRouteFromAtoB(final LatLng origin, final LatLng destination, final PathComputedCallback callback){
        final DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseDatabase
            .child("poi")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String,Map<String,Object>> value = (HashMap<String, Map<String, Object>>) dataSnapshot.getValue();
                    ArrayList<PointOfInterest> pointsOfInterest = new ArrayList<>();
                    for (Map.Entry<String, Map<String, Object>> stringMapEntry : value.entrySet()) {
                        PointOfInterest pointOfInterest = PointOfInterest.fromDataSnapshot(stringMapEntry.getKey(),stringMapEntry.getValue());
                        pointsOfInterest.add(pointOfInterest);
                    }

                    //Sort by boost
                    Collections.sort(pointsOfInterest, new Comparator<PointOfInterest>() {
                        @Override
                        public int compare(PointOfInterest o1, PointOfInterest o2) {
                        if(o1.getBoost() < o2.getBoost()) return 1;
                        if(o1.getBoost() > o2.getBoost()) return -1;
                        return 0;
                        }
                    });

                    selectOnlyClosePoints(pointsOfInterest, origin, destination);
                    selectRandomNFirstElements(pointsOfInterest);

                    createRouteBuilder(pointsOfInterest,origin,destination,callback);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
    }

    private static void createRouteBuilder(ArrayList<PointOfInterest> pointsOfInterest, LatLng origin, LatLng destination, final PathComputedCallback callback) {
        DirectionDestinationRequest builder = GoogleDirection.withServerKey(API_KEY).from(origin);
        for (PointOfInterest pointOfInterest : pointsOfInterest) {
            builder = builder.and(pointOfInterest.getLatLng());
        }
        DirectionRequest request = builder.to(destination);
        request.transportMode(TransportMode.WALKING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if(direction.isOK()) {
                            callback.onPathComputed(direction.getRouteList().get(0).getOverviewPolyline());
                        } else {
                            throw new IllegalStateException(direction.getErrorMessage());
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        throw new IllegalStateException(t);
                    }
                });
    }

    private static void selectOnlyClosePoints(ArrayList<PointOfInterest> pointsOfInterest, LatLng origin, LatLng destination) {
        Iterator<PointOfInterest> iterator = pointsOfInterest.iterator();
        while(iterator.hasNext()){
            PointOfInterest next = iterator.next();

            if(calculateDistanceFromPoints(next.getLatLng(),origin) > MAX_DISTANCE_METERS || calculateDistanceFromPoints(next.getLatLng(),destination) > MAX_DISTANCE_METERS){
                iterator.remove();
            }
        }
    }

    private static void selectRandomNFirstElements(ArrayList<PointOfInterest> pointsOfInterest) {

        Iterator<PointOfInterest> iterator = pointsOfInterest.iterator();
        int i = 0;
        while(iterator.hasNext()){

            PointOfInterest next = iterator.next();
            if(i > MAX_N || Math.random() > next.getBoost()){
                iterator.remove();
            }
            i++;
        }
    }

    public static void getRoute(LatLng currentPosition, double distanceKilometers, PathComputedCallback callback){
        throw new UnsupportedOperationException("not implemented yet");
    }

    public interface PathComputedCallback{
        void onPathComputed(RoutePolyline polyline);
    }

    public static double calculateDistanceFromPoints(LatLng origin, LatLng destination){

        // http://www.movable-type.co.uk/scripts/latlong.html
        double lat1 = origin.latitude;
        double lon1 = origin.longitude;

        double lat2 = destination.latitude;
        double lon2 = destination.longitude;

        double R = 6371e3; // earth radius in meters
        double phi1 = lat1 * (Math.PI / 180);
        double phi2 = lat2 * (Math.PI / 180);
        double deltaPhi = (lat2 - lat1) * (Math.PI / 180);
        double deltaLambda = (lon2 - lon1) * (Math.PI / 180);

        double a = (Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)) +
                    ((Math.cos(phi1) * Math.cos(phi2)) * (Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return R * c; // in meters
    }

    /**
     * Method to decode polyline points
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    public static List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
