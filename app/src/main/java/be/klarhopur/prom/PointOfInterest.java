package be.klarhopur.prom;

public class PointOfInterest {

    private String name;
    private String address;
    private String uri;

    public PointOfInterest(String name, String address, String uri) {
        this.name = name;
        this.address = address;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
