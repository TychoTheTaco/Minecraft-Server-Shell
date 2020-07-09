package com.tycho.mss.layout;

import com.tycho.mss.BackupListCell;
import com.tycho.mss.MenuPage;
import com.tycho.mss.MoveFilesTask;
import com.tycho.mss.util.Preferences;
import easytasks.ITask;
import easytasks.Task;
import easytasks.TaskAdapter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BackupsLayout extends MenuPage {

    @FXML
    private FileInputLayout backupDirectoryInputController;

    @FXML
    private ListView<Path> backups_list_view;

    @FXML
    private void initialize() {
        backupDirectoryInputController.setIsDirectory(true);
        backupDirectoryInputController.setPath(Preferences.getBackupDirectory());

        /*save_button.setOnAction(event -> {
            //If this is a different backup directory we need to move existing backups to the new location
            final File oldBackupDirectory = Preferences.getBackupDirectory();
            if (!backupDirectoryInputController.getFile().equals(oldBackupDirectory)) {
                if (oldBackupDirectory != null && oldBackupDirectory.exists() && oldBackupDirectory.list().length > 0) {
                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to move all existing backups to the new location?", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES) {
                        moveBackupDirectory(Paths.get(oldBackupDirectory.getAbsolutePath()), Paths.get(backupDirectoryInputController.getFile().getAbsolutePath()));
                    }
                }

                //Update location
                Preferences.setBackupDirectory(backupDirectoryInputController.getFile());
                Preferences.save();
                refreshBackupsList();
            }
        });*/

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
            for (Path path : backupsDirectory) {
                if (path.getFileName().toString().endsWith("zip")) {
                    backups_list_view.getItems().add(path);
                }
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
    }
}
