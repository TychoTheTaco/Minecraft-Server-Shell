package com.tycho.mss.layout;

import com.tycho.mss.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainLayout {

    @FXML
    private ListView<Module> module_list_view;

    @FXML
    private BorderPane container;

    private ServerShell serverShell;

    @FXML
    private void initialize() {
        //Modules
        module_list_view.setCellFactory(param -> new ModuleListCell());
        module_list_view.getItems().add(new Module("Dashboard"));
        module_list_view.getItems().add(new Module("Players"));
        module_list_view.getItems().add(new Module("Console"));
        module_list_view.getItems().add(new Module("Configuration"));

        final Node[] nodes = new Node[4];
        final String[] ids = new String[]{"dashboard_layout", "players_layout", "console_module_layout", "configuration_layout"};
        for (int i = 0; i < nodes.length; i++){
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/" + ids[i] + ".fxml"));
            try {
                nodes[i] = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        module_list_view.setOnMouseClicked(event -> {
            final Module module = module_list_view.getSelectionModel().getSelectedItem();
            switch (module.getTitle()){
                case "Dashboard":
                    container.setCenter(nodes[0]);
                    break;

                case "Players":
                    container.setCenter(nodes[1]);
                    break;

                case "Console":
                    container.setCenter(nodes[2]);
                    break;

                case "Configuration":
                    container.setCenter(nodes[3]);
                    break;
            }
        });
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

            }

            @Override
            public void onPlayerDisconnected(Player player) {

            }

            @Override
            public void onOutput(String message) {
            }
        });
    }
}
