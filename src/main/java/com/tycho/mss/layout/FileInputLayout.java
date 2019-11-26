package com.tycho.mss.layout;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;

public class FileInputLayout {

    @FXML
    private TextField input;

    @FXML
    private Button button;

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    private final FileChooser fileChooser = new FileChooser();

    private File file;

    private boolean isDirectory = false;

    @FXML
    private void initialize() {
        this.button.setOnAction(event -> {
            final File file;
            if (isDirectory){
                file = directoryChooser.showDialog(((Node) event.getTarget()).getScene().getWindow());
            }else{
                //fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("RAR archives", "*.rar"));
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

    public void setFile(File file) {
        this.file = file;
        if (file != null) this.input.setText(file.getAbsolutePath());
    }

    public void setIsDirectory(final boolean isDirectory){
        this.isDirectory = isDirectory;
    }
}
