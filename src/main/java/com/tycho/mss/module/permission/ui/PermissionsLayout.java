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

    @Override
    public void onPageHidden() {

    }

    private ServerShell serverShell;

    @Override
    public void attach(ServerShell serverShell) {
        this.serverShell = serverShell;
        if (serverShell != null) {
            Platform.runLater(() -> {
                roles_list_view.getItems().clear();
                for (Role role : serverShell.getServerConfiguration().getPermissionsManager().getRoles()) {
                    roles_list_view.getItems().add(role);
                }
                roles_list_view.getSelectionModel().select(0);
            });
        }
    }

    @Override
    public void detach(ServerShell serverShell) {
        this.serverShell = null;
        Platform.runLater(() -> {
            roles_list_view.getItems().clear();
        });
    }

    @FXML
    private void initialize() {
        roles_list_view.setCellFactory(param -> new RoleListCell(serverShell, serverShell.getServerConfiguration().getPermissionsManager()));
        roles_list_view.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            players_list_view.getItems().clear();
            players_list_view.getItems().addAll(serverShell.getServerConfiguration().getPermissionsManager().getPlayers(newValue));
        });

        players_list_view.setCellFactory(param -> new PlayerListCell(roles_list_view.getSelectionModel().getSelectedItem(), serverShell.getServerConfiguration().getPermissionsManager()));

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
                    editRoleLayout.setServerShell(serverShell);
                    final Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
                    stage.setScene(scene);
                    stage.showAndWait();
                    final Role role = editRoleLayout.getRole();
                    if (role != null){
                        serverShell.getServerConfiguration().getPermissionsManager().addRole(role);
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
                    final Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
                    stage.setScene(scene);
                    stage.showAndWait();
                    if (addPlayerLayout.getPlayer() != null){
                        serverShell.getServerConfiguration().getPermissionsManager().assign(addPlayerLayout.getPlayer(), roles_list_view.getSelectionModel().getSelectedItem());
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
            if (serverShell != null){
                for (Role role : serverShell.getServerConfiguration().getPermissionsManager().getRoles()){
                    roles_list_view.getItems().add(role);
                }
            }
            roles_list_view.getSelectionModel().select(0);
        });
    }
}
