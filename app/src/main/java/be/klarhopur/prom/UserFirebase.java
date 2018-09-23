package be.klarhopur.prom;

import java.util.HashMap;

public class UserFirebase {

    private String email;
    private String username;
    private int role;
    private HashMap<String,Path> pathsHistory;


    public UserFirebase() {

    }

    public UserFirebase(String email, String username, int role, HashMap<String, Path> pathsHistory) {
        this.email = email;
        this.username = username;
        this.role = role;
        this.pathsHistory = pathsHistory;
    }

    public HashMap<String, Path> getPathsHistory() {
        return pathsHistory;
    }

    public void setPathsHistory(HashMap<String, Path> pathsHistory) {
        this.pathsHistory = pathsHistory;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
