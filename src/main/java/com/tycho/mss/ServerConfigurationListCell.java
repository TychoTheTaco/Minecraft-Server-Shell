package com.tycho.mss;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.effect.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.nio.file.Files;

public class ServerConfigurationListCell extends GridPane implements ServerShellConnection{

    @FXML
    private Label server_name_label;

    @FXML
    private Label server_version_label;

    @FXML
    private ImageView state_icon;

    private ServerConfiguration serverConfiguration;

    private Effect serverOnlineEffect;
    private Effect yellowEffect;
    private Effect serverOfflineEffect;

    public ServerConfigurationListCell() {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/server_configuration_list_cell.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        serverOnlineEffect = new Blend(
                BlendMode.SRC_ATOP,
                new ColorAdjust(0, 0, 0, 0),
                new ColorInput(
                        0,
                        0,
                        state_icon.getImage().getWidth(),
                        state_icon.getImage().getHeight(),
                        CustomColor.GREEN
                )
        );

        yellowEffect = new Blend(
                BlendMode.SRC_ATOP,
                new ColorAdjust(0, 0, 0, 0),
                new ColorInput(
                        0,
                        0,
                        state_icon.getImage().getWidth(),
                        state_icon.getImage().getHeight(),
                        CustomColor.YELLOW
                )
        );

        serverOfflineEffect = new Blend(
                BlendMode.SRC_ATOP,
                new ColorAdjust(0, 0, 0, 0),
                new ColorInput(
                        0,
                        0,
                        state_icon.getImage().getWidth(),
                        state_icon.getImage().getHeight(),
                        CustomColor.RED
                )
        );
    }

    public void setServerConfiguration(ServerConfiguration serverConfiguration) {
        //Detach from previous configuration
        if (this.serverConfiguration != null){
            detach(ServerManager.getShells().get(this.serverConfiguration.getId()));
        }

        this.serverConfiguration = serverConfiguration;

        //Attach to new configuration
        if (this.serverConfiguration != null){
            final ServerShell serverShell = ServerManager.getShells().get(serverConfiguration.getId());
            Platform.runLater(() -> {
                server_name_label.setText(serverConfiguration.getName());

                //Server status
                if (serverShell == null){
                    state_icon.setEffect(serverOfflineEffect);
                }else{
                    switch (serverShell.getState()){
                        case STARTING:
                            state_icon.setEffect(yellowEffect);
                            break;

                        case ONLINE:
                            state_icon.setEffect(serverOnlineEffect);
                            break;

                        case STOPPING:
                            state_icon.setEffect(yellowEffect);
                            break;

                        case OFFLINE:
                            state_icon.setEffect(serverOfflineEffect);
                            break;
                    }
                }

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

    private final ServerShell.EventListener eventListener = new ServerShell.EventAdapter(){
        @Override
        public void onServerStarting() {
            state_icon.setEffect(serverOnlineEffect);
        }

        @Override
        public void onServerStarted() {
            state_icon.setEffect(serverOnlineEffect);
        }

        @Override
        public void onServerStopping() {
            state_icon.setEffect(serverOfflineEffect);
        }

        @Override
        public void onServerStopped() {
            state_icon.setEffect(serverOfflineEffect);
        }
    };

    @Override
    public void attach(ServerShell serverShell) {
        if (serverShell == null){
            state_icon.setEffect(serverOfflineEffect);
        }else{
            serverShell.addEventListener(eventListener);
            switch (serverShell.getState()){
                case STARTING:
                case ONLINE:
                    state_icon.setEffect(serverOnlineEffect);
                    break;

                case STOPPING:
                case OFFLINE:
                    state_icon.setEffect(serverOfflineEffect);
                    break;
            }
        }
    }

    @Override
    public void detach(ServerShell serverShell) {
        if (serverShell != null) serverShell.removeEventListener(eventListener);
    }
}
