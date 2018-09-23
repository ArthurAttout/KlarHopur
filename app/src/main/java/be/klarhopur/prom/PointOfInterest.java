package be.klarhopur.prom;


import java.util.Map;

public class PointOfInterest {

    private String address;
    private String urlImage;
    private String ID;
    private Boolean approved;
    private double boost;
    private String qrCode;
    private String name;

    public PointOfInterest(String address, String uri, String id, Boolean approved, double boost, String qrCode, String name) {
        this.address = address;
        this.urlImage = uri;
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

    public void setName(String name) {
        this.name = name;
    }

    public static PointOfInterest fromDataSnapshot(Map<String,Object> data){


        String ID = (String) data.get("ID");
        Boolean approved = (Boolean) data.get("approved");
        long boost = (long) data.get("boost");
        String qrcode = (String)data.get("qrcode");
        String ownerID = (String)data.get("uid");
        String urlImg = (String)data.get("urlimg");
        String userFriendlyAddress = (String)data.get("userfriendlyaddress");
        String name = (String)data.get("name");

        return new PointOfInterest(userFriendlyAddress,urlImg,ID,approved,boost,qrcode, name);
    }
}
