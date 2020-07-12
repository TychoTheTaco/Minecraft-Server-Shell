package com.tycho.mss.layout;

import com.tycho.mss.Page;
import com.tycho.mss.StatusContainer;
import com.tycho.mss.StatusHost;
import com.tycho.mss.module.backup.BackupListCell;
import com.tycho.mss.module.backup.MoveFilesTask;
import com.tycho.mss.util.Preferences;
import easytasks.ITask;
import easytasks.Task;
import easytasks.TaskAdapter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BackupsLayout implements Page, StatusHost {

    @FXML
    private FileInputLayout backup_directory_input;

    @FXML
    private ListView<Path> backups_list_view;

    @FXML
    private Button save_button;

    @FXML
    private Label no_backups_found_label;

    @FXML
    private Label loading_label;

    private StatusContainer statusContainer = new StatusContainer();

    @FXML
    private void initialize() {
        backup_directory_input.setIsDirectory(true);
        backup_directory_input.setPath(Preferences.getBackupDirectory());
        backup_directory_input.setOnPathChangedListener(new FileInputLayout.OnPathChangedListener() {
            @Override
            public void onPathChanged(Path path) {
                save_button.setDisable(false);
            }
        });
        backup_directory_input.setValidator(new FileInputLayout.PathValidator() {
            @Override
            protected boolean isPathValid(Path path, StringBuilder invalidReason) {
                System.out.println("RESOLVE: " + Paths.get(System.getProperty("user.dir")).resolve(path));
                if (path.toString().length() == 0){
                    return false;
                }
                return true;
            }
        });
        backup_directory_input.setOnValidStateChangeListener(new ValidatedTextField.OnValidStateChangeListener() {
            @Override
            public void onValidStateChange(boolean isValid) {
                if (isValid){
                    statusContainer.setStatus(StatusContainer.Status.OK);
                }else{
                    statusContainer.setStatus(StatusContainer.Status.WARNING);
                }
            }
        });

        save_button.setDisable(true);
        save_button.setOnAction(event -> {
            //If this is a different backup directory we need to move existing backups to the new location
            final Path oldBackupDirectory = Preferences.getBackupDirectory();
            if (!backup_directory_input.getPath().equals(oldBackupDirectory)) {
                if (oldBackupDirectory != null && Files.exists(oldBackupDirectory) && oldBackupDirectory.iterator().hasNext()) {
                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to move all existing backups to the new location?", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES) {
                        moveBackupDirectory(oldBackupDirectory, backup_directory_input.getPath());
                    }
                }

                //Update location
                Preferences.setBackupDirectory(Paths.get(System.getProperty("user.dir")).resolve(backup_directory_input.getPath()));
                Preferences.save();
                refreshBackupsList();
            }

            save_button.setDisable(true);
        });

        backups_list_view.setCellFactory(param -> new BackupListCell());

        refreshBackupsList();
    }

    @Override
    public void onPageSelected() {
        refreshBackupsList();
    }

    @Override
    public void onPageHidden() {

    }

    private void moveBackupDirectory(final Path source, final Path destination){
        //Move files to new location
        final MoveFilesTask moveFilesTask = new MoveFilesTask(source, destination);
        final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Restoring backup...", ButtonType.CANCEL);

        alert.setOnCloseRequest(event -> {
            if (moveFilesTask.getState() != Task.State.STOPPED) event.consume();
        });

        moveFilesTask.addTaskListener(new TaskAdapter(){
            @Override
            public void onTaskStopped(ITask task) {
                Platform.runLater(alert::close);
            }
        });

        alert.show();
        moveFilesTask.startOnNewThread();
    }

    private void refreshBackupsList() {
        loading_label.setVisible(true);
        no_backups_found_label.setVisible(false);
        backups_list_view.getItems().clear();
        new Thread(() -> {
            final Path backupsDirectory = Preferences.getBackupDirectory();
            final List<Path> backups = new ArrayList<>();
            if (backupsDirectory != null && Files.exists(backupsDirectory)) {
                try {
                    Files.walk(backupsDirectory).filter(path -> path.getFileName().toString().endsWith("zip")).forEach(backups::add);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            backups.sort((a, b) -> {
                try {
                    return -Long.compare(Files.getLastModifiedTime(a).toMillis(), Files.getLastModifiedTime(b).toMillis());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return 0;
            });

            Platform.runLater(() -> {
                backups_list_view.getItems().addAll(backups);
                loading_label.setVisible(false);

                if (!backups_list_view.getItems().isEmpty() && no_backups_found_label.getParent() != null){
                    ((GridPane) no_backups_found_label.getParent()).getChildren().remove(no_backups_found_label);
                }
            });

        }).start();
    }

    @Override
    public StatusContainer getStatusManager() {
        return statusContainer;
    }
}
