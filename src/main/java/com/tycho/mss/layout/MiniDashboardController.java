package com.tycho.mss.layout;

import com.tycho.mss.MenuPage;
import com.tycho.mss.Player;
import com.tycho.mss.ServerShell;
import com.tycho.mss.util.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;

public class MiniDashboardController extends MenuPage{

    @FXML
    private Label status_label;

    @FXML
    private Label uptime_label;

    @FXML
    private Label player_count_label;

    @FXML
    private Button start_stop_button;

    @FXML
    private void initialize() {
        start_stop_button.setOnAction(event -> getServerShell().stop());
    }

    @Override
    public void setServerShell(ServerShell serverShell) {
        super.setServerShell(serverShell);

        updateStatus();
        updateUptime();
        updatePlayerCount();

        serverShell.addEventListener(new ServerShell.EventAdapter(){
            @Override
            public void onServerStarting() {
                Platform.runLater(() -> updateStatus());
            }

            @Override
            public void onServerStarted() {
                Platform.runLater(() -> updateStatus());
            }

            @Override
            public void onServerStopping() {
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
        });
        new Thread(new UiUpdater()).start();
    }

    private void updateStatus(){
        switch (getServerShell().getState()){
            case STARTING:
                this.status_label.setText("Starting server...");
                this.status_label.setTextFill(Paint.valueOf("white"));
                break;

            case ONLINE:
                this.status_label.setText("Server online");
                this.status_label.setTextFill(Paint.valueOf("#45d151"));
                break;

            case STOPPING:
                this.status_label.setText("Stopping server...");
                this.status_label.setTextFill(Paint.valueOf("white"));
                break;

            case OFFLINE:
                this.status_label.setText("Server offline");
                this.status_label.setTextFill(Paint.valueOf("#d14545"));
                break;
        }
    }

    private void updatePlayerCount(){
        this.player_count_label.setText(getServerShell().getPlayers().size() + " / " + getServerShell().getProperties().get("max-players") + " Players");
    }

    private void updateUptime(){
        uptime_label.setText(Utils.formatTimeStopwatch(getServerShell().getUptime(), 2));
    }

    private class UiUpdater implements Runnable{

        private boolean isRunning = false;

        @Override
        public void run() {
            this.isRunning = true;
            while (isRunning){
                //Update UI
                Platform.runLater(MiniDashboardController.this::updateUptime);

                //Sleep
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        public void stop(){
            this.isRunning = false;
        }
    }
}
