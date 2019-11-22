package com.tycho.mss;

import java.util.UUID;

public class Player {

    private final String username;

    private UUID id;

    private final String ipAddress;

    private long onConnectTime;

    public Player(final String username, final String ipAddress) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.onConnectTime = System.currentTimeMillis();
    }

    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public long getOnConnectTime() {
        return onConnectTime;
    }
}
