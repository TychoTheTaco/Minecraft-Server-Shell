package com.tycho.mss.layout;

import com.tycho.mss.MenuPage;
import com.tycho.mss.util.Preferences;
import com.tycho.mss.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.Date;

public class BackupsLayout extends MenuPage {

    @FXML
    private TableView<File> backups_table_view;

    @FXML
    private void initialize() {
        backups_table_view.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        //Date
        final TableColumn<File, String> dateColumn = new TableColumn<>("Date Created");
        dateColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(DateFormat.getDateInstance().format(new Date(param.getValue().lastModified()))));
        //dateColumn.setPrefWidth(150);
        backups_table_view.getColumns().add(dateColumn);

        //Size
        final TableColumn<File, String> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(Utils.humanReadableByteCount(param.getValue().length(), true)));
        //sizeColumn.setPrefWidth(200);
        backups_table_view.getColumns().add(sizeColumn);

        final Preferences preferences = new Preferences();
        preferences.load();
        final File backupsDirectory = preferences.getBackupDirectory();
        if (backupsDirectory != null && backupsDirectory.exists()){
            for (File file : backupsDirectory.listFiles((dir, name) -> name.endsWith("zip"))){
                backups_table_view.getItems().add(file);
            }
        }
    }

}
