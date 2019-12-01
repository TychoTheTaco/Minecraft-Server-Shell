package com.tycho.mss.command;

public class SavedLocation {

    private final int x;

    private final int y;

    private final int z;

    private final String description;

    public SavedLocation(final int x, final int y, final int z, final String description) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.description = description;
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
}
