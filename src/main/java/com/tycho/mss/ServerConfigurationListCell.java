package com.tycho.mss;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.nio.file.Files;

public class ServerConfigurationListCell extends GridPane {

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

            //Get Minecraft version from JAR
            if (Files.exists(serverConfiguration.getJar())){
                final String version = serverConfiguration.getMinecraftVersion();
                server_version_label.setText(version == null ? "Unknown version" : version);
            }else{
                setStyle("-fx-background-color: -red;");
                server_version_label.setText("Server JAR not found!");
            }
        });
    }
}
