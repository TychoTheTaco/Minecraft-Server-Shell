package com.tycho.mss;

import java.util.UUID;

public class Player {

    private final String username;

    private UUID id;

    private final String ipAddress;

    public Player(final String username, final String ipAddress) {
        this.username = username;
        this.ipAddress = ipAddress;
    }

    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
