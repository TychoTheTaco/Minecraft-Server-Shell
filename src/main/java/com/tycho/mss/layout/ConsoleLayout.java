package com.tycho.mss.layout;

import com.tycho.mss.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ConsoleLayout extends MenuPage {

    @FXML
    private TextArea console;

    @FXML
    private TextField console_input;

    @FXML
    private void initialize() {
        //Console output
        console.setEditable(false);

        //Console input
        console_input.setOnAction(event -> {
            if (getServerShell() != null) {
                try {
                    getServerShell().execute(console_input.getText());
                    console_input.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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

    @Override
    public String getTitle() {
        return "Console";
    }
}
