package com.tycho.mss.module.backup;

import com.tycho.mss.util.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupListCell extends ListCell<Path> {

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
                final Alert alert;
                String lastModified = "Unknown";
                try {
                    lastModified = SIMPLE_DATE_FORMAT.format(Files.getLastModifiedTime(getItem()).toMillis());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to restore the backup created on " + lastModified + " ?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    //MinecraftServerManager.getServerShell().restore(getItem());
                    throw new RuntimeException("Not implemented!");
                }
            });

            delete_button.setOnAction(event -> {
                final Alert alert;
                String lastModified = "Unknown";
                try {
                    lastModified = SIMPLE_DATE_FORMAT.format(Files.getLastModifiedTime(getItem()).toMillis());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete the backup created on " + lastModified + " ?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    final Path path = getItem();
                    try {
                        Files.delete(path);
                        getListView().getItems().remove(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(Path item, boolean empty) {
        super.updateItem(item, empty);
        if (empty){
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }else{
            try {
                dateCreated.setText(SIMPLE_DATE_FORMAT.format(new Date(Files.getLastModifiedTime(item).toMillis())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                size.setText(Utils.humanReadableByteCount(Files.size(item), true));
            } catch (IOException e) {
                e.printStackTrace();
            }
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}
