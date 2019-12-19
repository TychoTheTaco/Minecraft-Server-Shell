package com.tycho.mss;

import com.tycho.mss.permission.PermissionsManager;
import com.tycho.mss.permission.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class PlayerListCell extends ListCell<String> {

    @FXML
    private Label username;

    @FXML
    private HBox buttons;

    @FXML
    private Button remove_button;

    public PlayerListCell(final Role role, final PermissionsManager permissionsManager) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/player_list_cell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();

            buttons.managedProperty().bind(buttons.visibleProperty());
            buttons.setVisible(false);
            hoverProperty().addListener((observable, oldValue, newValue) -> {
                buttons.setVisible(newValue);
            });

            remove_button.setOnAction(event -> {
                permissionsManager.unassign(getItem(), role);
                getListView().getItems().remove(getItem());
            });
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty){
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }else{
            username.setText(item);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}
