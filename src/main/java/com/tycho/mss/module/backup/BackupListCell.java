package com.tycho.mss.module.backup;

import com.tycho.mss.MinecraftServerShell;
import com.tycho.mss.util.Utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupListCell extends ListCell<File> {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy   hh:mm:ss a");

    @FXML
    private Label dateCreated;

    @FXML
    private Label size;

    @FXML
    private GridPane grid_pane;

    @FXML
    private HBox buttons;

    @FXML
    private Button restore_button;

    @FXML
    private Button delete_button;

    public BackupListCell() {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/backup_list_cell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();

            buttons.managedProperty().bind(buttons.visibleProperty());
            buttons.setVisible(false);
            hoverProperty().addListener((observable, oldValue, newValue) -> {
                buttons.setVisible(newValue);
            });

            restore_button.setOnAction(event -> {
                final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to restore the backup created on " + SIMPLE_DATE_FORMAT.format(getItem().lastModified()) + "?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    MinecraftServerShell.getServerShell().restore(Paths.get(getItem().getAbsolutePath()));
                }
            });

            delete_button.setOnAction(event -> {
                final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete the backup created on " + SIMPLE_DATE_FORMAT.format(getItem().lastModified()) + "?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    final File file = getItem();
                    if (file.delete()){
                        getListView().getItems().remove(file);
                    }
                }
            });
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);
        if (empty){
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }else{
            dateCreated.setText(SIMPLE_DATE_FORMAT.format(new Date(item.lastModified())));
            size.setText(Utils.humanReadableByteCount(item.length(), true));
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}
