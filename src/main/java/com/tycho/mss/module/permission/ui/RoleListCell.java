package com.tycho.mss.module.permission.ui;

import com.tycho.mss.ServerShell;
import com.tycho.mss.module.permission.PermissionsManager;
import com.tycho.mss.module.permission.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

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

    public RoleListCell(final ServerShell serverShell, final PermissionsManager permissionsManager) {
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
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/add_role_layout.fxml"));
                    Parent root = fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Edit Role");
                    final EditRoleLayout editRoleLayout = fxmlLoader.getController();
                    editRoleLayout.setServerShell(serverShell);
                    editRoleLayout.setRole(getItem());
                    final Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
                    stage.setScene(scene);
                    stage.showAndWait();
                    editRoleLayout.getRole();
                    getListView().refresh();
                    getListView().getSelectionModel().select(getItem());
                }catch (IOException e){
                    e.printStackTrace();
                }
            });

            delete_button.setOnAction(event -> {
                permissionsManager.removeRole(getItem());
                getListView().getItems().remove(getItem());
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
