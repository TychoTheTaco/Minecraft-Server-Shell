package com.tycho.mss.layout;

import com.tycho.mss.*;
import com.tycho.mss.command.Command;
import com.tycho.mss.permission.Role;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PermissionsLayout extends MenuPage {

    @FXML
    private ListView<Role> roles_list_view;

    @FXML
    private ListView<String> players_list_view;

    @FXML
    private void initialize() {
        roles_list_view.setCellFactory(param -> new RoleListCell());
        roles_list_view.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        roles_list_view.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            players_list_view.getItems().clear();
            players_list_view.getItems().addAll(getServerShell().getPermissionsManager().getPlayers(newValue));
        });

        players_list_view.setCellFactory(param -> new PlayerListCell());
    }

    @Override
    public void setServerShell(ServerShell serverShell) {
        super.setServerShell(serverShell);
        if (serverShell != null){
            for (Role role : serverShell.getPermissionsManager().getRoles()){
                roles_list_view.getItems().add(role);
            }
        }
    }
}
