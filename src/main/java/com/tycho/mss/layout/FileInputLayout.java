package com.tycho.mss.layout;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
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

    private Path path;

    @FXML
    private void initialize() {
        this.input.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                path = Paths.get(input.getText());
                if (validator.isValid(path)){
                    this.input.getStyleClass().removeAll("invalid_input");
                    if (onPathChangedListener != null) onPathChangedListener.onPathChanged(path);
                }else{
                    throw new InvalidPathException("", "");
                }
            }catch (InvalidPathException e){
                this.input.getStyleClass().add("invalid_input");
            }
        });

        //Folder button
        this.button.setOnAction(event -> {
            final File file;
            if (isDirectory){
                file = directoryChooser.showDialog(((Node) event.getTarget()).getScene().getWindow());
            }else{
                file = fileChooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
            }

            //If the user canceled, don't do anything
            if (file == null){
                return;
            }

            final Path path = file.toPath();
            if (validator.isValid(path)){
                setPath(path);
            }
        });
    }

    public Path getPath(){
        return path;
    }

    public boolean isValid(){
        if (this.input.getText().trim().length() == 0) return false;
        if (validator != null && !validator.isValid(getPath())) return false;
        return Files.exists(Paths.get(this.input.getText()));
    }

    public void setPath(final Path path) {
        this.path = path;
        if (path == null){
            this.input.setText("");
        }else{
            this.input.setText(path.toString());
        }
        if (onPathChangedListener != null) onPathChangedListener.onPathChanged(path);
    }

    public void setIsDirectory(final boolean isDirectory){
        this.isDirectory = isDirectory;
    }

    public static class Validator{
        boolean isValid(final Path path){
            return true;
        }
    }

    public interface OnPathChangedListener{
        void onPathChanged(final Path path);
    }

    private OnPathChangedListener onPathChangedListener;

    public void setOnPathChangedListener(OnPathChangedListener onPathChangedListener) {
        this.onPathChangedListener = onPathChangedListener;
    }

    private Validator validator = new Validator();

    public void setValidator(Validator validator) {
        this.validator = validator;
    }
}
