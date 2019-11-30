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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
            Preferences.setBackupDirectory(backupDirectoryInputController.getFile());
            Preferences.save();
        });

        backups_list_view.setCellFactory(param -> new BackupListCell());

        refresh();
    }

    @Override
    public void onPageSelected() {
        refresh();
    }

    private void refresh(){
        backups_list_view.getItems().clear();
        final File backupsDirectory = Preferences.getBackupDirectory();
        if (backupsDirectory != null){
            for (File file : backupsDirectory.listFiles()){
                if (file.getName().endsWith("zip")){
                    backups_list_view.getItems().add(file);
                }
            }
        }
    }
}
