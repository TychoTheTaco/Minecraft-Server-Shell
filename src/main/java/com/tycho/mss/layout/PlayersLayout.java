package com.tycho.mss.layout;

import com.tycho.mss.*;
import com.tycho.mss.util.UiUpdater;
import com.tycho.mss.util.Utils;
import easytasks.Task;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;

import java.util.HashMap;

public class PlayersLayout implements Page, ServerShellConnection, ServerShell.EventListener {

    @FXML
    private TableView<Player> players_table_view;

    @FXML
    private Region offline_overlay;

    private final UiUpdater uiUpdater = new UiUpdater(1000) {
        @Override
        protected void onUiUpdate() {
            players_table_view.refresh();
        }
    };

    @FXML
    private void initialize() {
        offline_overlay.managedProperty().bind(offline_overlay.visibleProperty());

        players_table_view.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        players_table_view.setPlaceholder(new Label("No players online."));

        //Username
        final TableColumn<Player, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setPrefWidth(150);
        players_table_view.getColumns().add(usernameColumn);

        //Session Time
        final TableColumn<Player, String> sessionTimeColumn = new TableColumn<>("Session Time");
        sessionTimeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(Utils.formatTimeHuman(param.getValue().getSessionTime(), 2)));
        sessionTimeColumn.setPrefWidth(200);
        players_table_view.getColumns().add(sessionTimeColumn);

        //IP Address
        final TableColumn<Player, String> ipAddressColumn = new TableColumn<>("IP Address");
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        ipAddressColumn.setPrefWidth(130);
        players_table_view.getColumns().add(ipAddressColumn);

        final HashMap<Player, CachedStats> cache = new HashMap<>();

        //Ping
        final TableColumn<Player, String> pingColumn = new TableColumn<>("Ping");
        /*pingColumn.setCellValueFactory(param -> {
            if (cache.containsKey(param.getValue())){
                if (System.currentTimeMillis() - cache.get(param.getValue()).lastPingTime >= 3000){
                    new Thread(() -> {
                        final long ping = param.getValue().getPing();
                        cache.put(param.getValue(), new CachedStats(System.currentTimeMillis(), ping));
                    }).start();
                }
            }else{
                new Thread(() -> {
                    final long ping = param.getValue().getPing();
                    cache.put(param.getValue(), new CachedStats(System.currentTimeMillis(), ping));
                }).start();
            }
            return new ReadOnlyObjectWrapper<>(String.valueOf(cache.get(param.getValue()).lastPingValue) + " ms.");
        });*/
        pingColumn.setPrefWidth(100);
        players_table_view.getColumns().add(pingColumn);
    }

    @Override
    public void onPageSelected() {
        if (uiUpdater.getState() == Task.State.NOT_STARTED){
            uiUpdater.startOnNewThread();
        }else{
            uiUpdater.resume();
        }
    }

    private class CachedStats{
        private long lastPingTime;
        private long lastPingValue;

        public CachedStats(long lastPingTime, long lastPingValue) {
            this.lastPingTime = lastPingTime;
            this.lastPingValue = lastPingValue;
        }
    }

    @Override
    public void onPageHidden() {
        uiUpdater.pause();
    }

    private ServerShell serverShell;

    @Override
    public void attach(ServerShell serverShell) {
        this.serverShell = serverShell;
        if (serverShell != null){
            serverShell.addEventListener(this);
        }
    }

    @Override
    public void detach(ServerShell serverShell) {
        this.serverShell = null;
        if (serverShell != null){
            serverShell.removeEventListener(this);
        }
    }

    @Override
    public void onServerStarting() {

    }

    @Override
    public void onFailedStart() {

    }

    @Override
    public void onServerIoReady() {

    }

    @Override
    public void onServerStarted() {
        Platform.runLater(() -> {
            offline_overlay.setVisible(false);
        });
    }

    @Override
    public void onServerStopping() {

    }

    @Override
    public void onServerStopped() {
        Platform.runLater(() -> {
            players_table_view.getItems().clear();
            offline_overlay.setVisible(true);
        });
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
}
