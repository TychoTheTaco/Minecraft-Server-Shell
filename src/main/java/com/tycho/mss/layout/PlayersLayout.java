package com.tycho.mss.layout;

import com.tycho.mss.Player;
import com.tycho.mss.ServerShell;
import com.tycho.mss.MenuPage;
import com.tycho.mss.util.Utils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PlayersLayout extends MenuPage {

    @FXML
    private TableView<Player> players_table_view;

    private final UiUpdater uiUpdater = new UiUpdater();

    @FXML
    private void initialize() {
        //Username
        final TableColumn<Player, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setPrefWidth(150);
        players_table_view.getColumns().add(usernameColumn);

        //IP Address
        final TableColumn<Player, String> ipAddressColumn = new TableColumn<>("IP Address");
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        ipAddressColumn.setPrefWidth(130);
        players_table_view.getColumns().add(ipAddressColumn);

        //Session Time
        final TableColumn<Player, String> sessionTimeColumn = new TableColumn<>("Session Time");
        sessionTimeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(Utils.formatTimeHuman(System.currentTimeMillis() - param.getValue().getOnConnectTime(), 2)));
        sessionTimeColumn.setPrefWidth(200);
        players_table_view.getColumns().add(sessionTimeColumn);

        players_table_view.getItems().add(new Player("TychoTheTaco", "192.168.1.7"));

        new Thread(this.uiUpdater).start();
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

    private class UiUpdater implements Runnable{

        private boolean isRunning = false;

        @Override
        public void run() {
            this.isRunning = true;
            while (isRunning){
                //Update UI
                players_table_view.refresh();

                //Sleep
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        public void stop(){
            this.isRunning = false;
        }
    }

    @Override
    public String getTitle() {
        return "Players";
    }
}
