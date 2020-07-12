package com.tycho.mss.layout;

import com.tycho.mss.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConsoleLayout implements ServerShellConnection, Page {

    @FXML
    private TextArea console;

    @FXML
    private TextField console_input;

    @FXML
    private Region offline_overlay;

    private final ServerShellContainer serverShellContainer = new ServerShellContainer(){
        @Override
        public void setServerShell(ServerShell serverShell) {
            super.setServerShell(serverShell);
            if (serverShell != null){
                serverShell.addEventListener(new ServerShell.EventAdapter() {

                    @Override
                    public void onServerStarting() {
                        Platform.runLater(() -> {
                            offline_overlay.setVisible(false);
                        });
                    }

                    @Override
                    public void onServerStarted() {
                        Platform.runLater(() -> {
                            console_input.setDisable(false);
                            console_input.setText("");
                        });
                    }

                    @Override
                    public void onServerStopping() {
                        Platform.runLater(() -> {
                            console_input.setDisable(true);
                        });
                    }

                    @Override
                    public void onServerStopped() {
                        Platform.runLater(() -> {
                            offline_overlay.setVisible(true);
                        });
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

            //Console input
            console_input.setDisable(serverShell == null || serverShell.getState() != ServerShell.State.ONLINE);
        }
    };

    private static final List<String> dictionary = new ArrayList<>();
    static{
        dictionary.add("say");
        dictionary.add("minecraft:");
    }

    private final List<String> commandHistory = new ArrayList<>();

    private int commandHistoryIndex = -1;

    @FXML
    private void initialize() {
        offline_overlay.managedProperty().bind(offline_overlay.visibleProperty());

        //Console input
        console_input.setOnAction(event -> {
            if (serverShellContainer.getServerShell() != null) {
                try {
                    //Add command to history
                    commandHistory.add(console_input.getText());
                    commandHistoryIndex = commandHistory.size();

                    //Execute command
                    serverShellContainer.getServerShell().execute(console_input.getText());
                    console_input.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //Control command history
        console_input.setOnKeyPressed(event -> {
            switch (event.getCode()){
                case UP:
                    if (commandHistoryIndex > 0){
                        console_input.setText(commandHistory.get(--commandHistoryIndex));
                        console_input.end();
                    }
                    event.consume();
                    break;

                case DOWN:
                    if (commandHistoryIndex < commandHistory.size() - 1){
                        console_input.setText(commandHistory.get(++commandHistoryIndex));
                        console_input.end();
                    }
                    event.consume();
                    break;

                case TAB: //Handle autocomplete
                    final String[] split = console_input.getText().split(" +");
                    final String word = split[split.length - 1];
                    console_input.appendText(autocomplete(word));
                    event.consume();
                    break;
            }
        });
    }

    @Override
    public void onPageSelected() {
        Platform.runLater(() -> {
            console.setScrollTop(Double.MAX_VALUE);
        });
    }

    @Override
    public void onPageHidden() {

    }

    private String autocomplete(final String input){
        if (input.length() <= 0) return "";

        if (serverShellContainer.getServerShell() != null){
            for (Player player : serverShellContainer.getServerShell().getPlayers()){
                if (player.getUsername().startsWith(input)){
                    return player.getUsername().replaceFirst(input, "");
                }
            }
        }

        for (String string : dictionary){
            if (string.startsWith(input)){
                return string.replaceFirst(input, "");
            }
        }

        return "";
    }

    @Override
    public ServerShellContainer getServerShellContainer() {
        return serverShellContainer;
    }
}
