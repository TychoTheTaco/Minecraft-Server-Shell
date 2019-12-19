package com.tycho.mss;

import com.tycho.mss.permission.Role;
import com.tycho.mss.util.Utils;
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

public class RoleListCell extends ListCell<Role> {

    @FXML
    private Label name;

    @FXML
    private GridPane grid_pane;

    @FXML
    private HBox buttons;

    @FXML
    private Button edit_button;

    @FXML
    private Button delete_button;

    public RoleListCell() {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/role_list_cell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();

            buttons.managedProperty().bind(buttons.visibleProperty());
            buttons.setVisible(false);
            hoverProperty().addListener((observable, oldValue, newValue) -> {
                buttons.setVisible(newValue);
            });

            edit_button.setOnAction(event -> {
                System.out.println("EDIT ROLE");
            });

            delete_button.setOnAction(event -> {
                System.out.println("DELETE ROLE");
            });
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(Role item, boolean empty) {
        super.updateItem(item, empty);
        if (empty){
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }else{
            name.setText(item.getName());
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}
