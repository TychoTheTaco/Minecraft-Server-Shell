package com.tycho.mss.layout;

import com.tycho.mss.BackupListCell;
import com.tycho.mss.MenuPage;
import com.tycho.mss.MoveFilesTask;
import com.tycho.mss.util.Preferences;
import easytasks.ITask;
import easytasks.Task;
import easytasks.TaskAdapter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AddPlayerLayout {

    @FXML
    private TextField player_text_field;

    private Stage stage;

    private String player;

    @FXML
    private void initialize() {
        player_text_field.setOnAction(event -> {
            player = player_text_field.getText();
            stage.close();
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public String getPlayer() {
        return player;
    }
}
