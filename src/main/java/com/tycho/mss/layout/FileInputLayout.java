package com.tycho.mss.layout;

import com.tycho.mss.CustomColor;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
    private HBox button_container;

    @FXML
    private StackPane error_icon_container;

    @FXML
    private Button button;

    @FXML
    private ImageView error_icon;

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    private final FileChooser fileChooser = new FileChooser();

    private boolean isDirectory = false;

    private Path path;

    private final Tooltip errorTooltip = new Tooltip("Invalid Path");

    @FXML
    private void initialize() {
        error_icon.setEffect(new Blend(
                BlendMode.SRC_ATOP,
                new ColorAdjust(0, 0, 0, 0),
                new ColorInput(
                        0,
                        0,
                        error_icon.getImage().getWidth(),
                        error_icon.getImage().getHeight(),
                        CustomColor.RED
                )
        ));
        Tooltip.install(error_icon_container, errorTooltip);

        this.input.textProperty().addListener((observable, oldValue, newValue) -> {
            final StringBuilder stringBuilder = new StringBuilder();
            try {
                path = Paths.get(input.getText());
                if (validator.isValid(path, stringBuilder)){
                    this.input.getStyleClass().removeAll("invalid_input");
                    button_container.getChildren().remove(error_icon_container);
                    if (onPathChangedListener != null) onPathChangedListener.onPathChanged(path);
                }else{
                    throw new InvalidPathException("", "invalid");
                }
            }catch (InvalidPathException e){
                this.input.getStyleClass().add("invalid_input");
                if (!button_container.getChildren().contains(error_icon_container)){
                    button_container.getChildren().add(0, error_icon_container);
                }
                errorTooltip.setText(e.getReason().equals("invalid") ? stringBuilder.toString() : "Invalid Path!");
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
            if (validator.isValid(path, null)){
                setPath(path);
            }
        });
    }

    public Path getPath(){
        return path;
    }

    public boolean isValid(){
        if (this.input.getText().trim().length() == 0) return false;
        if (validator != null && !validator.isValid(getPath(), null)) return false;
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
        boolean isValid(final Path path, final StringBuilder stringBuilder){
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
