package com.tycho.mss.layout;

import com.tycho.mss.*;
import com.tycho.mss.module.backup.BackupTask;
import com.tycho.mss.util.UiUpdater;
import com.tycho.mss.util.Utils;
import easytasks.ITask;
import easytasks.Task;
import easytasks.TaskAdapter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;

public class MiniDashboard extends GridPane implements Page, ServerShellConnection, ServerShell.EventListener {

    @FXML
    private Label status_label;

    @FXML
    private Label uptime_label;

    @FXML
    private Label player_count_label;

    @FXML
    private Button start_stop_button;

    @FXML
    private Button create_backup_button;

    private final UiUpdater uiUpdater = new UiUpdater(1000) {
        @Override
        protected void onUiUpdate() {
            updateUptime();
        }
    };

    public MiniDashboard(){
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/mini_dashboard_layout.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Set up "start/stop" button
        start_stop_button.setOnAction(event -> {
            if (serverShell == null || serverShell.getState() == ServerShell.State.OFFLINE){
                try {
                    serverShell.startOnNewThread();
                }catch (RuntimeException e){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to start server: " + e.getMessage(), ButtonType.OK);
                    alert.showAndWait();
                }
            }else if (serverShell.getState() == ServerShell.State.ONLINE){
                serverShell.stop();
            }
        });

        //Set up "Create Backup" button
        create_backup_button.setOnAction(event -> {

            //Disable the button
            create_backup_button.setDisable(true);
            final String initialButtonText = create_backup_button.getText();
            create_backup_button.setText("0 %");

            //Make sure a backup directory is specified in the settings
            final Path backupDirectory = serverShell.getServerConfiguration().getBackupDirectory();
            if (backupDirectory == null){
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please specify a backup directory in the settings!", ButtonType.OK);
                alert.show();
                create_backup_button.setDisable(false);
                create_backup_button.setText("Create Backup");
                return;
            }

            //Create backup task and UI updater for button
            final BackupTask backupTask = new BackupTask(serverShell.getServerConfiguration().getJar().getParent(), new File(backupDirectory + File.separator + System.currentTimeMillis() + ".zip").toPath());
            final UiUpdater backupButtonUpdater = new UiUpdater(250) {

                private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##%");

                @Override
                protected void onUiUpdate() {
                    create_backup_button.setText(DECIMAL_FORMAT.format(backupTask.getProgress()));
                }
            };
            backupTask.addTaskListener(new TaskAdapter(){
                @Override
                public void onTaskStarted(ITask task) {
                    backupButtonUpdater.startOnNewThread();
                }

                @Override
                public void onTaskStopped(ITask task) {
                    try {
                        backupButtonUpdater.stopAndWait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Platform.runLater(() -> {
                        create_backup_button.setText(initialButtonText);
                        create_backup_button.setDisable(false);
                    });
                }
            });
            backupTask.startOnNewThread();
        });
    }

    private ServerShell serverShell;

    @Override
    public void attach(ServerShell serverShell) {
        this.serverShell = serverShell;

        if (serverShell != null) {
            Platform.runLater(() -> {
                updateStatus();
                updateUptime();
                updatePlayerCount();
                updateStartStopButton();
            });

            serverShell.addEventListener(this);

            if (uiUpdater.getState() == Task.State.NOT_STARTED){
                uiUpdater.startOnNewThread();
            }else{
                uiUpdater.resume();
            }
        }
    }

    @Override
    public void detach(ServerShell serverShell) {
        this.serverShell = null;
        if (serverShell != null){
            serverShell.removeEventListener(this);
            uiUpdater.pause();
        }
    }

    @Override
    public void onPageSelected() {

    }

    @Override
    public void onPageHidden() {
        uiUpdater.stop();
    }

    private void updateStatus(){
        switch (serverShell.getState()){
            case STARTING:
                this.status_label.setText("Starting Server...");
                this.status_label.setTextFill(Paint.valueOf("white"));
                break;

            case ONLINE:
                this.status_label.setText("Server Online");
                this.status_label.setTextFill(Paint.valueOf("#45d151"));
                break;

            case STOPPING:
                this.status_label.setText("Stopping Server...");
                this.status_label.setTextFill(Paint.valueOf("white"));
                break;

            case OFFLINE:
                this.status_label.setText("Server Offline");
                this.status_label.setTextFill(CustomColor.RED);
                break;
        }
    }

    private void updatePlayerCount(){
        if (serverShell.getProperties().containsKey("max-players")){
            player_count_label.setVisible(true);
            this.player_count_label.setText(serverShell.getPlayers().size() + " / " + serverShell.getProperties().get("max-players") + " Players");
        }else{
            player_count_label.setVisible(false);
        }
    }

    private void updateUptime(){
        if (serverShell.getState() == ServerShell.State.ONLINE){
            uptime_label.setText(Utils.formatTimeStopwatch(serverShell.getUptime(), 3));
        }else{
            uptime_label.setText("");
        }
    }

    private void updateStartStopButton(){
        switch (serverShell.getState()){
            case OFFLINE:
                start_stop_button.setDisable(false);
                start_stop_button.setText("Start");
                start_stop_button.getStyleClass().remove("stop_button");
                start_stop_button.getStyleClass().add("start_button");
                break;

            case STARTING:
                start_stop_button.setDisable(true);
                break;

            case ONLINE:
                start_stop_button.setDisable(false);
                start_stop_button.setText("Stop");
                start_stop_button.getStyleClass().remove("start_button");
                start_stop_button.getStyleClass().add("stop_button");
                break;

            case STOPPING:
                start_stop_button.setDisable(true);
                break;
        }
    }

    @Override
    public void onServerStarting() {
        Platform.runLater(() -> {
            updateStatus();
            updateStartStopButton();
        });
    }

    @Override
    public void onServerIoReady() {

    }

    @Override
    public void onServerStarted() {
        Platform.runLater(() -> {
            updateStatus();
            updateStartStopButton();
            updateUptime();
        });
    }

    @Override
    public void onServerStopping() {
        Platform.runLater(() -> {
            updateStatus();
            updateUptime();
            updateStartStopButton();
        });
    }

    @Override
    public void onServerStopped() {
        Platform.runLater(() -> {
            updateStatus();
            updatePlayerCount();
            updateUptime();
            updateStartStopButton();
        });
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
}
