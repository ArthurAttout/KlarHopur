package be.klarhopur.prom;


import com.google.android.gms.maps.model.LatLng;

import java.util.Map;

public class PointOfInterest {

    private String address;
    private String urlImage;
    private LatLng latLng;
    private String ID;
    private Boolean approved;
    private double boost;
    private String qrCode;
    private String name;

    public PointOfInterest(String address, String uri, LatLng latLng, String id, Boolean approved, double boost, String qrCode, String name) {
        this.address = address;
        this.urlImage = uri;
        this.latLng = latLng;
        ID = id;
        this.approved = approved;
        this.boost = boost;
        this.qrCode = qrCode;
        this.name = name;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public double getBoost() {
        return boost;
    }

    public void setBoost(double boost) {
        this.boost = boost;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getName() {
        return name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static PointOfInterest fromDataSnapshot(String key, Map<String,Object> data){

        Boolean approved = (Boolean) data.get("approved");
        String qrcode = (String)data.get("qrcode");
        String ownerID = (String)data.get("uid");
        String urlImg = (String)data.get("urlimg");
        String userFriendlyAddress = (String)data.get("userfriendlyaddress");
        String name = (String)data.get("name");

        double latitude = 0;
        double longitude = 0;

        try{
            latitude = (double)data.get("latitude");
            longitude = (double)data.get("longitude");

            latitude = (long)data.get("latitude");
            longitude = (long)data.get("longitude");
        }
        catch(ClassCastException e){}

        double boost = 0;
        try{
            boost = (double) data.get("boost");
            boost = (long) data.get("boost");
        }
        catch(ClassCastException e){}

        return new PointOfInterest(userFriendlyAddress,urlImg, new LatLng(latitude,longitude), key,approved,boost,qrcode, name);
    }


}
