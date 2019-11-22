package com.tycho.mss.layout;

import com.tycho.mss.Player;
import com.tycho.mss.ServerShell;
import com.tycho.mss.MenuPage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardLayout extends MenuPage {

    @FXML
    private Label status_label;

    @FXML
    private Label player_count_label;

    @FXML
    private void initialize() {

    }

    @Override
    public void setServerShell(ServerShell serverShell) {
        super.setServerShell(serverShell);
        serverShell.addEventListener(new ServerShell.EventListener() {
            @Override
            public void onServerStarting() {
                Platform.runLater(() -> updateStatus());
            }

            @Override
            public void onServerIOready() {

            }

            @Override
            public void onServerStarted() {
                Platform.runLater(() -> updateStatus());
            }

            @Override
            public void onServerStopped() {
                Platform.runLater(() -> updateStatus());
            }

            @Override
            public void onPlayerConnected(Player player) {
                Platform.runLater(() -> updatePlayerCount());
            }

            @Override
            public void onPlayerDisconnected(Player player) {
                Platform.runLater(() -> updatePlayerCount());
            }

            @Override
            public void onOutput(String message) {
            }
        });
        updatePlayerCount();
        updateStatus();
    }

    private void updateStatus(){
        switch (getServerShell().getState()){
            case STARTING:
                this.status_label.setText("Starting server...");
                break;

            case ONLINE:
                this.status_label.setText("Server online");
                break;

            case STOPPING:
                this.status_label.setText("Stopping server...");
                break;

            case OFFLINE:
                this.status_label.setText("Server offline");
                break;
        }
    }

    private void updatePlayerCount(){
        this.player_count_label.setText(getServerShell().getPlayers().size() + " / MAX Players connected");
    }
}
