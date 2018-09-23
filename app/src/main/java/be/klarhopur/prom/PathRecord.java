package be.klarhopur.prom;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

public class PathRecord {

    private LatLng destination;
    private LatLng origin;
    private double distanceKilometer;
    private String title;
    private String imageURL;
    private double points;
    private HashMap<String,PointOfInterest> pointsOfInterest;

    public PathRecord(LatLng origin, LatLng destination, double distanceKilometer, String title, String imageURL, double points) {
        this.destination = destination;
        this.origin = origin;
        this.distanceKilometer = distanceKilometer;
        this.title = title;
        this.imageURL = imageURL;
        this.points = points;
        this.pointsOfInterest = pointsOfInterest;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public LatLng getOrigin() {
        return origin;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setOrigin(LatLng origin) {
        this.origin = origin;
    }

    public double getDistanceKilometer() {
        return distanceKilometer;
    }

    public void setDistanceKilometer(double distanceKilometer) {
        this.distanceKilometer = distanceKilometer;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public HashMap<String, PointOfInterest> getPointsOfInterest() {
        return pointsOfInterest;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPointsOfInterest(HashMap<String, PointOfInterest> pointsOfInterest) {
        this.pointsOfInterest = pointsOfInterest;
    }

    public static PathRecord fromDataSnapshot(Map<String, Object> data) {
        double latitudeOrigin = (double) ((Map)data.get("origin")).get("latitude");
        double longitudeOrigin = (double) ((Map)data.get("origin")).get("longitude");

        double latitudeDestination = (double) ((Map)data.get("destination")).get("latitude");
        double longitudeDestination = (double) ((Map)data.get("destination")).get("longitude");

        long length = (long) data.get("length");
        long points = (long) data.get("points");

        String title = (String) data.get("title");
        String imageURL = (String) data.get("urlimg");

        return new PathRecord(
                new LatLng(latitudeOrigin,longitudeOrigin),
                new LatLng(latitudeDestination,longitudeDestination),
                length,
                title, imageURL, points);
    }
}
