package com.tycho.mss.module.permission.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddPlayerLayout {

    @FXML
    private TextField player_text_field;

    private String player;

    @FXML
    private void initialize() {
        player_text_field.setOnAction(event -> {
            player = player_text_field.getText();
            ((Stage) player_text_field.getScene().getWindow()).close();
        });
    }

    public String getPlayer() {
        return player;
    }
}
