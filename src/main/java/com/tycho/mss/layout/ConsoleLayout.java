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

public class ConsoleLayout implements ServerShellConnection, Page, ServerShell.EventListener {

    @FXML
    private TextArea console;

    @FXML
    private TextField console_input;

    private ServerShell serverShell;

    @Override
    public void attach(ServerShell serverShell) {
        this.serverShell = serverShell;
        if (serverShell != null){
            serverShell.addEventListener(this);

            //Console input
            switch (serverShell.getState()){
                case STARTING:
                    onServerStarting();
                    break;

                case ONLINE:
                    onServerStarted();
                    break;

                case STOPPING:
                    onServerStopping();
                    break;

                case OFFLINE:
                    onServerStopped();
                    break;
            }
        }
    }

    @Override
    public void detach(ServerShell serverShell) {
        this.serverShell = serverShell;
        if (serverShell != null){
            serverShell.removeEventListener(this);
        }
    }

    private static final List<String> dictionary = new ArrayList<>();
    static{
        dictionary.add("say");
        dictionary.add("minecraft:");
    }

    private final List<String> commandHistory = new ArrayList<>();

    private int commandHistoryIndex = -1;

    @FXML
    private void initialize() {
        //Console input
        console_input.setOnAction(event -> {
            if (serverShell != null) {
                try {
                    //Add command to history
                    commandHistory.add(console_input.getText());
                    commandHistoryIndex = commandHistory.size();

                    //Execute command
                    serverShell.execute(console_input.getText());
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

        if (serverShell != null){
            for (Player player : serverShell.getPlayers()){
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
    public void onServerStarting() {
        Platform.runLater(() -> {
            console_input.setDisable(true);
            console_input.setText("Console input is disabled while the server is starting.");
        });
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
            console_input.setDisable(false);
            console_input.setText("");
        });
    }

    @Override
    public void onServerStopping() {
        Platform.runLater(() -> {
            console_input.setDisable(true);
            console_input.setText("Console input is disabled while the server is stopping.");
        });
    }

    @Override
    public void onServerStopped() {
        Platform.runLater(() -> {
            console_input.setDisable(true);
            console_input.setText("Console input is disabled while the server is offline.");
        });
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
            //Clear console if this is a new start
            //TODO: Make this a user controlled option
            if (message.equals("[Minecraft Server Manager] Starting server...")){
                console.clear();
            }
            console.appendText(message);
            console.appendText("\n");
        });
    }

}
