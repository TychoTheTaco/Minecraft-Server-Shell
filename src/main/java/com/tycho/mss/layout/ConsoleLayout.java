package com.tycho.mss.layout;

import com.tycho.mss.MenuPage;
import com.tycho.mss.Player;
import com.tycho.mss.ServerShell;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConsoleLayout extends MenuPage {

    @FXML
    private TextArea console;

    @FXML
    private TextField console_input;

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
            if (getServerShell() != null) {
                try {
                    //Add command to history
                    commandHistory.add(console_input.getText());
                    commandHistoryIndex = commandHistory.size() - 1;

                    //Execute command
                    getServerShell().execute(console_input.getText());
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
    public void setServerShell(ServerShell serverShell) {
        super.setServerShell(serverShell);
        if (serverShell != null){
            serverShell.addEventListener(new ServerShell.EventAdapter() {
                @Override
                public void onServerIoReady() {
                    console_input.setDisable(false);
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

    private String autocomplete(final String input){
        if (input.length() <= 0) return "";

        if (getServerShell() != null){
            for (Player player : getServerShell().getPlayers()){
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
}
