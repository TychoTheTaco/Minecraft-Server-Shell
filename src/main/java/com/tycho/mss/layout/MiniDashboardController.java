package com.tycho.mss.layout;

import com.tycho.mss.BackupTask;
import com.tycho.mss.MenuPage;
import com.tycho.mss.Player;
import com.tycho.mss.ServerShell;
import com.tycho.mss.util.Preferences;
import com.tycho.mss.util.UiUpdater;
import com.tycho.mss.util.Utils;
import easytasks.ITask;
import easytasks.TaskAdapter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;

import java.io.File;

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
    private Button create_backup_button;

    private final UiUpdater uiUpdater = new UiUpdater(1000) {
        @Override
        protected void onUiUpdate() {
            updateUptime();
        }
    };

    @FXML
    private void initialize() {
        start_stop_button.setOnAction(event -> {
            if (getServerShell().getState() == ServerShell.State.ONLINE){
                getServerShell().stop();
            }else if (getServerShell().getState() == ServerShell.State.OFFLINE){
                getServerShell().startOnNewThread();
            }
        });

        create_backup_button.setOnAction(event -> {
            create_backup_button.setDisable(true);

            final File backupDirectory = Preferences.getBackupDirectory();
            if (backupDirectory == null){
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please specify a backup directory in the settings!", ButtonType.OK);
                alert.show();
                create_backup_button.setDisable(false);
                return;
            }

            final BackupTask backupTask = new BackupTask(new File((String) Preferences.getPreferences().get("server_jar")).getParentFile(), new File(backupDirectory + File.separator + System.currentTimeMillis() + ".zip"));
            backupTask.addTaskListener(new TaskAdapter(){
                @Override
                public void onTaskStopped(ITask task) {
                    create_backup_button.setDisable(false);
                }
            });
            backupTask.startOnNewThread();
        });
    }

    @Override
    public void setServerShell(ServerShell serverShell) {
        super.setServerShell(serverShell);
        if (serverShell != null){
            Platform.runLater(() -> {
                updateStatus();
                updateUptime();
                updatePlayerCount();
                updateStartStopButton();
            });

            serverShell.addEventListener(new ServerShell.EventAdapter(){
                @Override
                public void onServerStarting() {
                    Platform.runLater(() -> {
                        updateStatus();
                        updateStartStopButton();
                    });
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
            });
            uiUpdater.startOnNewThread();
        }
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
        if (getServerShell().getState() == ServerShell.State.ONLINE){
            uptime_label.setText(Utils.formatTimeStopwatch(getServerShell().getUptime(), 2));
        }else{
            uptime_label.setText("");
        }
    }

    private void updateStartStopButton(){
        switch (getServerShell().getState()){
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
}
