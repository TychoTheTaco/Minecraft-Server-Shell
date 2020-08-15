package com.tycho.mss.command;

import org.json.simple.JSONObject;

public class SavedLocation {

    private final int x;

    private final int y;

    private final int z;

    private final String description;

    private String dimension;

    public SavedLocation(final int x, final int y, final int z, final String description) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.description = description;
    }

    public SavedLocation(final JSONObject jsonObject){
        x = (int) ((long) jsonObject.get("x"));
        y = (int) ((long) jsonObject.get("y"));
        z = (int) ((long) jsonObject.get("z"));
        description = (String) jsonObject.get("description");
        dimension = (String) jsonObject.get("dimension");
    }

    public JSONObject toJson(){
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("x", x);
        jsonObject.put("y", y);
        jsonObject.put("z", z);
        jsonObject.put("description", description);
        jsonObject.put("dimension", dimension);
        return jsonObject;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getDescription() {
        return description;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }
}
