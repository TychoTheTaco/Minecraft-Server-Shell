package com.tycho.mss;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class ServerConfigurationListCell extends GridPane {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy   hh:mm:ss a");

    @FXML
    private Label server_name_label;

    @FXML
    private Label server_version_label;

    private ServerConfiguration serverConfiguration;

    public ServerConfigurationListCell() {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/server_configuration_list_cell.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setServerConfiguration(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
        Platform.runLater(() -> {
            server_name_label.setText(serverConfiguration.getName());
            server_version_label.setText("1.16.1");
        });
    }
}
