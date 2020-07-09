package com.tycho.mss;

import com.tycho.mss.command.SavedLocation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player {

    private final UUID id;

    private final String username;

    private final String ipAddress;

    private final long sessionStartTime;

    private long totalPlaytime = 0;

    private final List<SavedLocation> savedLocations = new ArrayList<>();

    public Player(final UUID id, final String username, final String ipAddress) {
        this.id = id;
        this.username = username;
        this.ipAddress = ipAddress;
        this.sessionStartTime = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public long getTotalPlaytime(){
        return totalPlaytime + getSessionTime();
    }

    public void load(final JSONObject jsonObject){
        totalPlaytime = (long) jsonObject.getOrDefault("playtime", 0);

        final JSONArray savedLocationsArray = (JSONArray) jsonObject.get("savedLocations");
        for (Object object : savedLocationsArray){
            final JSONObject location = (JSONObject) object;
            savedLocations.add(new SavedLocation(
                    ((Long) location.get("x")).intValue(),
                    ((Long) location.get("y")).intValue(),
                    ((Long) location.get("z")).intValue(),
                    (String) location.get("description"))
            );
        }
    }

    public long getSessionTime(){
        return System.currentTimeMillis() - sessionStartTime;
    }

    public long getPing(){
        try {
            final long start = System.nanoTime();
            final boolean isReachable = InetAddress.getByName(this.ipAddress).isReachable(3000);
            return isReachable ? (System.nanoTime() - start) / 1000000 : -1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<SavedLocation> getSavedLocations() {
        return savedLocations;
    }

    public JSONObject toJsonObject(){
        final JSONObject root = new JSONObject();
        root.put("id", this.id.toString());
        root.put("username", this.username);
        root.put("playtime", getTotalPlaytime());

        //Saved locations
        final JSONArray savedLocationsArray = new JSONArray();
        for (SavedLocation savedLocation : this.savedLocations){
            final JSONObject savedLocationObject = new JSONObject();
            savedLocationObject.put("x", savedLocation.getX());
            savedLocationObject.put("y", savedLocation.getY());
            savedLocationObject.put("z", savedLocation.getZ());
            savedLocationObject.put("description", savedLocation.getDescription());
            savedLocationsArray.add(savedLocationObject);
        }
        root.put("savedLocations", savedLocationsArray);

        return root;
    }
}
