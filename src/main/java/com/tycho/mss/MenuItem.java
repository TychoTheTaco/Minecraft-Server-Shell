package com.tycho.mss;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class MenuItem {

    private final String title;

    private final FXMLLoader loader;

    private final Node node;

    public MenuItem(final String title, final String layout) throws IOException {
        this.title = title;
        this.loader = new FXMLLoader(getClass().getResource("/layout/" + layout + ".fxml"));
        this.node = this.loader.load();
    }

    public String getTitle() {
        return title;
    }

    public FXMLLoader getLoader() {
        return loader;
    }

    public Node getNode() {
        return node;
    }

}
