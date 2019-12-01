package com.tycho.mss.layout;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileInputLayout {

    @FXML
    private TextField input;

    @FXML
    private Button button;

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    private final FileChooser fileChooser = new FileChooser();

    private boolean isDirectory = false;

    @FXML
    private void initialize() {
        this.input.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValid()){
                this.input.getStyleClass().removeAll("invalid_input");
            }else{
                this.input.getStyleClass().add("invalid_input");
            }
        });
        this.button.setOnAction(event -> {
            final File file;
            if (isDirectory){
                file = directoryChooser.showDialog(((Node) event.getTarget()).getScene().getWindow());
            }else{
                file = fileChooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
            }

            if (file != null){
                setFile(file);
            }
        });
    }

    public File getFile(){
        return new File(input.getText());
    }

    public boolean isValid(){
        if (this.input.getText().trim().length() == 0) return false;
        if (validator != null && !validator.isValid(getFile())) return false;
        return Files.exists(Paths.get(this.input.getText()));
    }

    public void setFile(File file) {
        if (file == null){
            this.input.setText("");
        }else{
            this.input.setText(file.getAbsolutePath());
        }
    }

    public void setIsDirectory(final boolean isDirectory){
        this.isDirectory = isDirectory;
    }

    public interface Validator{
        boolean isValid(final File file);
    }

    private Validator validator;

    public void setValidator(Validator validator) {
        this.validator = validator;
    }
}
