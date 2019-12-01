package com.tycho.mss.layout;

import com.tycho.mss.BackupListCell;
import com.tycho.mss.MenuPage;
import com.tycho.mss.util.Preferences;
import com.tycho.mss.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class BackupsLayout extends MenuPage {

    @FXML
    private FileInputLayout backupDirectoryInputController;

    @FXML
    private ListView<File> backups_list_view;

    @FXML
    private Button save_button;

    @FXML
    private void initialize() {
        backupDirectoryInputController.setIsDirectory(true);
        backupDirectoryInputController.setFile(Preferences.getBackupDirectory());

        save_button.setOnAction(event -> {
            //If this is a different backup directory we need to move existing backups to the new location
            final File oldBackupDirectory = Preferences.getBackupDirectory();
            if (oldBackupDirectory != backupDirectoryInputController.getFile()) {
                if (oldBackupDirectory != null && oldBackupDirectory.list().length > 0) {
                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to move all existing backups to the new location?", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES) {
                        //Move files to new location
                        try {
                            System.out.println("Moving...");
                            final Path oldBackupPath = Paths.get(oldBackupDirectory.getAbsolutePath());
                            Files.walk(oldBackupPath)
                                    .filter(path -> !Files.isDirectory(path))
                                    .forEach(path -> {
                                        System.out.println(oldBackupPath.relativize(path));
                                        try {
                                            System.out.println("MOVE " + path + " TO " + Paths.get(backupDirectoryInputController.getFile().getAbsolutePath(), path.getFileName().toString()));
                                            Files.move(path, Paths.get(backupDirectoryInputController.getFile().getAbsolutePath(), path.getFileName().toString()));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Done moving.");
                    }
                }

                //Update location
                Preferences.setBackupDirectory(backupDirectoryInputController.getFile());
                Preferences.save();
                refresh();
            }
        });

        backups_list_view.setCellFactory(param -> new BackupListCell());

        refresh();
    }

    @Override
    public void onPageSelected() {
        refresh();
    }

    private void refresh() {
        backups_list_view.getItems().clear();
        final File backupsDirectory = Preferences.getBackupDirectory();
        if (backupsDirectory != null) {
            for (File file : backupsDirectory.listFiles()) {
                if (file.getName().endsWith("zip")) {
                    backups_list_view.getItems().add(file);
                }
            }
        }
    }
}
