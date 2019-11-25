package com.tycho.mss.layout;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

public class FileInputLayout {

    @FXML
    private TextField input;

    @FXML
    private Button button;

    private final FileChooser fileChooser = new FileChooser();

    @FXML
    private void initialize() {
        this.button.setOnAction(event -> {
            //fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("RAR archives", "*.rar"));
            final File file = fileChooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
            if (file != null){
                this.input.setText(file.getAbsolutePath());
            }
        });
    }

    public File getFile(){
        return new File(input.getText());
    }
}
