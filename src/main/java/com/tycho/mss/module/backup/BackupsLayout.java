package com.tycho.mss.module.backup;

import com.tycho.mss.layout.FileInputLayout;
import com.tycho.mss.MenuPage;
import com.tycho.mss.util.Preferences;
import easytasks.ITask;
import easytasks.Task;
import easytasks.TaskAdapter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BackupsLayout extends MenuPage {

    @FXML
    private FileInputLayout backupDirectoryInputController;

    @FXML
    private ListView<File> backups_list_view;

    @FXML
    private Button save_button;

    @FXML
    private void initialize() {
        //Backup directory input
        backupDirectoryInputController.setIsDirectory(true);
        backupDirectoryInputController.setFile(Preferences.getBackupDirectory());
        setStatus(backupDirectoryInputController.isValid() ? Status.OK : Status.ERROR);

        save_button.setOnAction(event -> {
            final File oldBackupDirectory = Preferences.getBackupDirectory();
            if (!backupDirectoryInputController.getFile().equals(oldBackupDirectory)) {
                if (backupDirectoryInputController.isValid()){
                    setStatus(Status.OK);

                    //Move backups to new location
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
                }else{
                    setStatus(Status.ERROR);
                }

                refresh();
            }

            setStatus(backupDirectoryInputController.isValid() ? Status.OK : Status.ERROR);
        });

        //Backups list
        backups_list_view.setCellFactory(param -> new BackupListCell());
        refresh();
    }

    @Override
    public void onPageSelected() {
        refresh();
    }

    private void moveBackupDirectory(final Path source, final Path destination){
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

    private void refresh() {
        backups_list_view.getItems().clear();
        final File backupsDirectory = Preferences.getBackupDirectory();
        if (backupsDirectory != null && backupsDirectory.exists()) {
            for (File file : backupsDirectory.listFiles()) {
                if (file.getName().endsWith("zip")) {
                    backups_list_view.getItems().add(file);
                }
            }
        }
        backups_list_view.getItems().sort((a, b) -> -Long.compare(a.lastModified(), b.lastModified()));
    }
}
