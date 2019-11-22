package com.tycho.mss.layout;

import com.tycho.mss.Player;
import com.tycho.mss.PlayerListCell;
import com.tycho.mss.ServerShell;
import com.tycho.mss.ServerShellUser;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class PlayersLayout extends ServerShellUser {

    @FXML
    private TableView players_table_view;

    @FXML
    private void initialize() {
        //Username
        final TableColumn<String, Player> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        players_table_view.getColumns().add(usernameColumn);

        //IP Address
        final TableColumn<String, Player> ipAddressColumn = new TableColumn<>("IP Address");
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        players_table_view.getColumns().add(ipAddressColumn);

        players_table_view.getItems().add(new Player("test", "address"));
    }

    @Override
    public void setServerShell(ServerShell serverShell) {
        super.setServerShell(serverShell);
        serverShell.addEventListener(new ServerShell.EventListener() {
            @Override
            public void onServerStarting() {

            }

            @Override
            public void onServerIOready() {

            }

            @Override
            public void onServerStarted() {

            }

            @Override
            public void onServerStopped() {

            }

            @Override
            public void onPlayerConnected(Player player) {
                Platform.runLater(() -> players_table_view.getItems().add(player));
            }

            @Override
            public void onPlayerDisconnected(Player player) {
                Platform.runLater(() -> players_table_view.getItems().remove(player));
            }

            @Override
            public void onOutput(String message) {
            }
        });
    }
}
