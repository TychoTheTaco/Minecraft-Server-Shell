package com.tycho.mss.layout;

import com.tycho.mss.Player;
import com.tycho.mss.PlayerListCell;
import com.tycho.mss.ServerShell;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class ConfigurationLayout {

    @FXML
    private ListView<Player> players_list;

    private ServerShell serverShell;

    @FXML
    private void initialize() {
        //Player list
        players_list.setCellFactory(param -> new PlayerListCell());
    }

    public void setServerShell(ServerShell serverShell) {
        this.serverShell = serverShell;
        this.serverShell.addEventListener(new ServerShell.EventListener() {
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
                Platform.runLater(() -> players_list.getItems().add(player));
            }

            @Override
            public void onPlayerDisconnected(Player player) {
                Platform.runLater(() -> players_list.getItems().remove(player));
            }

            @Override
            public void onOutput(String message) {
            }
        });
    }
}
