package com.tycho.mss.layout;

import com.tycho.mss.MenuPage;
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
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BackupsLayout extends MenuPage {

    @FXML
    private FileInputLayout backupDirectoryInputController;

    @FXML
    private ListView<Path> backups_list_view;

    @FXML
    private Button save_button;

    @FXML
    private Label no_backups_found_label;

    @FXML
    private void initialize() {
        backupDirectoryInputController.setIsDirectory(true);
        backupDirectoryInputController.setPath(Preferences.getBackupDirectory());
        backupDirectoryInputController.setOnPathChangedListener(new FileInputLayout.OnPathChangedListener() {
            @Override
            public void onPathChanged(Path path) {
                save_button.setDisable(false);
            }
        });

        save_button.setDisable(true);
        save_button.setOnAction(event -> {
            //If this is a different backup directory we need to move existing backups to the new location
            final Path oldBackupDirectory = Preferences.getBackupDirectory();
            if (!backupDirectoryInputController.getPath().equals(oldBackupDirectory)) {
                if (oldBackupDirectory != null && Files.exists(oldBackupDirectory) && oldBackupDirectory.iterator().hasNext()) {
                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to move all existing backups to the new location?", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES) {
                        moveBackupDirectory(oldBackupDirectory, backupDirectoryInputController.getPath());
                    }
                }

                //Update location
                Preferences.setBackupDirectory(backupDirectoryInputController.getPath());
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
        backups_list_view.getItems().clear();
        final Path backupsDirectory = Preferences.getBackupDirectory();
        if (backupsDirectory != null && Files.exists(backupsDirectory)) {
            try {
                Files.walk(backupsDirectory).filter(new Predicate<Path>() {
                    @Override
                    public boolean test(Path path) {
                        return path.getFileName().toString().endsWith("zip");
                    }
                }).forEach(new Consumer<Path>() {
                    @Override
                    public void accept(Path path) {
                        backups_list_view.getItems().add(path);
                    }
                });
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        backups_list_view.getItems().sort((a, b) -> {
            try {
                return -Long.compare(Files.getLastModifiedTime(a).toMillis(), Files.getLastModifiedTime(b).toMillis());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        });

        if (!backups_list_view.getItems().isEmpty() && no_backups_found_label.getParent() != null){
            ((GridPane) no_backups_found_label.getParent()).getChildren().remove(no_backups_found_label);
        }
    }
}
