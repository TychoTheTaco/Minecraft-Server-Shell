package com.tycho.mss;

public class Module {

    private final String title;

    private ServerShellUser controller;

    public Module(final String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public ServerShellUser getController() {
        return controller;
    }

    public void setController(ServerShellUser controller) {
        this.controller = controller;
    }
}
