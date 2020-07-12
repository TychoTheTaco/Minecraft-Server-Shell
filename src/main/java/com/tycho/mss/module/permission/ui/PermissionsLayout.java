package com.tycho.mss.module.permission.ui;

import com.tycho.mss.*;
import com.tycho.mss.module.permission.Role;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PermissionsLayout implements Page, ServerShellConnection {

    @FXML
    private ListView<Role> roles_list_view;

    @FXML
    private ListView<String> players_list_view;

    @FXML
    private Button add_role_button;

    @FXML
    private Button add_player_button;

    private ServerShellContainer serverShellContainer = new ServerShellContainer(){
        @Override
        public void setServerShell(ServerShell serverShell) {
            super.setServerShell(serverShell);
            if (serverShell != null){
                for (Role role : serverShell.getPermissionsManager().getRoles()){
                    roles_list_view.getItems().add(role);
                }
            }
            Platform.runLater(() -> roles_list_view.getSelectionModel().select(0));
        }
    };

    @Override
    public void onPageHidden() {

    }

    @Override
    public ServerShellContainer getServerShellContainer() {
        return serverShellContainer;
    }

    @FXML
    private void initialize() {
        roles_list_view.setCellFactory(param -> new RoleListCell(serverShellContainer.getServerShell(), serverShellContainer.getServerShell().getPermissionsManager()));
        roles_list_view.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            players_list_view.getItems().clear();
            players_list_view.getItems().addAll(serverShellContainer.getServerShell().getPermissionsManager().getPlayers(newValue));
        });

        players_list_view.setCellFactory(param -> new PlayerListCell(roles_list_view.getSelectionModel().getSelectedItem(), serverShellContainer.getServerShell().getPermissionsManager()));

        add_role_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/add_role_layout.fxml"));
                    Parent root = fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Add Role");
                    final EditRoleLayout editRoleLayout = fxmlLoader.getController();
                    editRoleLayout.setStage(stage);
                    editRoleLayout.setServerShell(serverShellContainer.getServerShell());
                    final Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
                    stage.setScene(scene);
                    stage.showAndWait();
                    final Role role = editRoleLayout.getRole();
                    if (role != null){
                        serverShellContainer.getServerShell().getPermissionsManager().addRole(role);
                        roles_list_view.getItems().add(role);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });

        add_player_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/add_player_layout.fxml"));
                    Parent root = fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Add Player");
                    final AddPlayerLayout addPlayerLayout = fxmlLoader.getController();
                    addPlayerLayout.setStage(stage);
                    final Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
                    stage.setScene(scene);
                    stage.showAndWait();
                    if (addPlayerLayout.getPlayer() != null){
                        serverShellContainer.getServerShell().getPermissionsManager().assign(addPlayerLayout.getPlayer(), roles_list_view.getSelectionModel().getSelectedItem());
                        players_list_view.getItems().add(addPlayerLayout.getPlayer());
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onPageSelected() {
        Platform.runLater(() -> {
            roles_list_view.getItems().clear();
            if (serverShellContainer.getServerShell() != null){
                for (Role role : serverShellContainer.getServerShell().getPermissionsManager().getRoles()){
                    roles_list_view.getItems().add(role);
                }
            }
            roles_list_view.getSelectionModel().select(0);
        });
    }
}
