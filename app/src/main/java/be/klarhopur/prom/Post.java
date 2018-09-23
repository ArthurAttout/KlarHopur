package be.klarhopur.prom;

public class Post {
    private String nom;
    private String adresse;
    private String img;
    public Post()
    {
    }
    public Post(String nom, String adresse) {
        this.nom = nom;
        this.adresse = adresse;
    }
    public String getAdresse() {
        return adresse;
    }

    public String getImg() {
        return img;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getNom() {
        return nom;
    }

    public void setName(String nom) {
        this.nom = nom;
    }
}
