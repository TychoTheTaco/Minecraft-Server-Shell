package com.tycho.mss.layout;

import com.tycho.mss.MenuPage;
import com.tycho.mss.util.Preferences;
import com.tycho.mss.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupsLayout extends MenuPage {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy   hh:mm:ss a");

    @FXML
    private TableView<File> backups_table_view;

    @FXML
    private void initialize() {
        backups_table_view.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        //Date
        final TableColumn<File, String> dateColumn = new TableColumn<>("Date Created");
        dateColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(SIMPLE_DATE_FORMAT.format(new Date(param.getValue().lastModified()))));
        //dateColumn.setPrefWidth(150);
        backups_table_view.getColumns().add(dateColumn);

        //Size
        final TableColumn<File, String> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(Utils.humanReadableByteCount(param.getValue().length(), true)));
        //sizeColumn.setPrefWidth(200);
        backups_table_view.getColumns().add(sizeColumn);

        refresh();
    }

    @Override
    public void onPageSelected() {
        refresh();
    }

    private void refresh(){
        backups_table_view.getItems().clear();
        final File backupsDirectory = Preferences.getBackupDirectory();
        if (backupsDirectory != null){
            for (File file : backupsDirectory.listFiles()){
                if (file.getName().endsWith("zip")){
                    backups_table_view.getItems().add(file);
                }
            }
        }
    }
}
