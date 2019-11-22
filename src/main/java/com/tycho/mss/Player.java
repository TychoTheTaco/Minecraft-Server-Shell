package com.tycho.mss;

import java.io.IOException;
import java.net.InetAddress;
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
}
