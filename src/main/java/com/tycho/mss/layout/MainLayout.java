package com.tycho.mss.layout;

import com.tycho.mss.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

public class MainLayout {

    @FXML
    private ListView<Module> module_list_view;

    @FXML
    private TextArea console;

    @FXML
    private TextField console_input;

    private ServerShell serverShell;

    @FXML
    private void initialize() {
        //Modules
        module_list_view.setCellFactory(param -> new ModuleListCell());
        module_list_view.getItems().add(new Module("Dashboard"));
        module_list_view.getItems().add(new Module("Players"));
        module_list_view.getItems().add(new Module("Console"));
        module_list_view.getItems().add(new Module("Configuration"));

        //Console output
        console.setEditable(false);

        //Console input
        console_input.setOnAction(event -> {
            if (serverShell != null) {
                try {
                    serverShell.execute(console_input.getText());
                    console_input.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                Platform.runLater(() -> {
                    console.appendText(message);
                    console.appendText("\n");
                });
            }
        });
    }
}
