package com.tycho.mss.layout;

import com.tycho.mss.*;
import com.tycho.mss.module.backup.BackupTask;
import com.tycho.mss.util.Preferences;
import com.tycho.mss.util.UiUpdater;
import com.tycho.mss.util.Utils;
import easytasks.ITask;
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

public class MiniDashboard extends GridPane implements Page, ServerShellConnection {

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

    private final ServerShellContainer serverShellContainer = new ServerShellContainer(){

        @Override
        public void setServerShell(ServerShell serverShell) {
            super.setServerShell(serverShell);
            System.out.println("SET SERVER SHELL: " + serverShell);
            if (serverShell != null) {
                Platform.runLater(() -> {
                    System.out.println("UPDATE");
                    updateStatus();
                    updateUptime();
                    updatePlayerCount();
                    updateStartStopButton();
                });

                serverShell.addEventListener(new ServerShell.EventAdapter() {
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
    };

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
            if (serverShellContainer.getServerShell() == null || serverShellContainer.getServerShell().getState() == ServerShell.State.OFFLINE){
                serverShellContainer.getServerShell().startOnNewThread();
            }else if (serverShellContainer.getServerShell().getState() == ServerShell.State.ONLINE){
                serverShellContainer.getServerShell().stop();
            }
        });

        //Set up "Create Backup" button
        create_backup_button.setOnAction(event -> {

            //Disable the button
            create_backup_button.setDisable(true);
            final String initialButtonText = create_backup_button.getText();
            create_backup_button.setText("0 %");

            //Make sure a backup directory is specified in the settings
            final Path backupDirectory = Preferences.getBackupDirectory();
            if (backupDirectory == null){
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please specify a backup directory in the settings!", ButtonType.OK);
                alert.show();
                create_backup_button.setDisable(false);
                create_backup_button.setText("Create Backup");
                return;
            }

            //Create backup task and UI updater for button
            final BackupTask backupTask = new BackupTask(new File((String) Preferences.getPreferences().get("server_jar")).getParentFile().toPath(), new File(backupDirectory + File.separator + System.currentTimeMillis() + ".zip").toPath());
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
        Platform.runLater(() -> create_backup_button.setPrefWidth(create_backup_button.getWidth()));
    }

    @Override
    public ServerShellContainer getServerShellContainer() {
        return serverShellContainer;
    }

    @Override
    public void onPageSelected() {

    }

    @Override
    public void onPageHidden() {
        uiUpdater.stop();
    }

    private void updateStatus(){
        System.out.println("STATE: " + serverShellContainer.getServerShell().getState());
        switch (serverShellContainer.getServerShell().getState()){
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
        this.player_count_label.setText(serverShellContainer.getServerShell().getPlayers().size() + " / " + serverShellContainer.getServerShell().getProperties().get("max-players") + " Players");
    }

    private void updateUptime(){
        if (serverShellContainer.getServerShell().getState() == ServerShell.State.ONLINE){
            uptime_label.setText(Utils.formatTimeStopwatch(serverShellContainer.getServerShell().getUptime(), 3));
        }else{
            uptime_label.setText("");
        }
    }

    private void updateStartStopButton(){
        switch (serverShellContainer.getServerShell().getState()){
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
