package com.tycho.mss;

public abstract class MenuPage {

    private ServerShell serverShell;

    public ServerShell getServerShell() {
        return serverShell;
    }

    public void setServerShell(ServerShell serverShell) {
        this.serverShell = serverShell;
    }
}
