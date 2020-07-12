package com.tycho.mss.layout;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileInputLayout extends ValidatedTextField {

    @FXML
    private Pane button_container;

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    private final FileChooser fileChooser = new FileChooser();

    private boolean isDirectory = false;

    public FileInputLayout(){
        super();

        final Button button = new Button();
        button.getStyleClass().add("folder_button");
        button.setMnemonicParsing(false);
        button.setMinWidth(24);
        button_container.getChildren().add(button);

        setValidator(new PathValidator() {
            @Override
            protected boolean isPathValid(Path path, StringBuilder invalidReason) {
                return true;
            }
        });

        //Folder button
        button.setOnAction(event -> {
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
            if (getValidator().isTextValid(file.getAbsolutePath(), null)){
                setPath(path);
            }
        });
    }

    @Override
    protected void onTextChanged(String oldText, String newText) {
        super.onTextChanged(oldText, newText);
        if (onPathChangedListener != null) onPathChangedListener.onPathChanged(getPath());
    }

    public void addExtensionFilter(final FileChooser.ExtensionFilter filter){
        fileChooser.getExtensionFilters().add(filter);
    }

    public Path getPath(){
        if (isValid()){
            return Paths.get(getText());
        }
        return null;
    }

    public void setPath(final Path path) {
        if (path == null){
            setText("");
        }else{
            setText(path.toString());
        }
    }

    public void setIsDirectory(final boolean isDirectory){
        this.isDirectory = isDirectory;
    }

    public interface OnPathChangedListener{
        void onPathChanged(final Path path);
    }

    public static abstract class PathValidator extends Validator{
        @Override
        protected boolean isTextValid(String string, StringBuilder invalidReason) {
            Path path;
            try {
                path = Paths.get(string);
            }catch (InvalidPathException e){
                invalidReason.append("Invalid Path!");
                return false;
            }

            //The path name cannot end in a period
            if (path.getFileName() != null && path.getFileName().toString().endsWith(".")){
                invalidReason.append("Invalid Path!");
                return false;
            }

            return super.isTextValid(string, invalidReason) && isPathValid(path, invalidReason);
        }

        protected abstract boolean isPathValid(final Path path, final StringBuilder invalidReason);
    }

    private OnPathChangedListener onPathChangedListener;

    public void setOnPathChangedListener(OnPathChangedListener onPathChangedListener) {
        this.onPathChangedListener = onPathChangedListener;
    }
}
