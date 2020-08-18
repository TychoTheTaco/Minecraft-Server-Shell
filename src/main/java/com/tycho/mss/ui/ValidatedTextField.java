package com.tycho.mss.ui;

import com.tycho.mss.CustomColor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class ValidatedTextField extends StackPane{

    @FXML
    private TextField input;

    @FXML
    private StackPane error_icon_container;

    @FXML
    private ImageView error_icon;

    private static final String DEFAULT_ERROR_TOOLTIP = "Invalid Input";
    private final Tooltip tooltip = new Tooltip(DEFAULT_ERROR_TOOLTIP);

    private Pane container;

    private boolean isValid = false;

    public ValidatedTextField(){
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/validated_text_field.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        Tooltip.install(error_icon_container, tooltip);

        container = (Pane) error_icon_container.getParent();

        this.input.textProperty().addListener((observable, oldValue, newValue) -> {
            checkValidity();
            onTextChanged(oldValue, newValue);
        });
        checkValidity();
    }

    protected void onTextChanged(final String oldText, final String newText){

    }

    private void checkValidity(){
        final StringBuilder invalidReason = new StringBuilder();

        final boolean previousValidState = isValid;

        if (validator.isTextValid(input.getText(), invalidReason)){
            isValid = true;
            this.input.getStyleClass().removeAll("invalid_input");
            container.getChildren().remove(error_icon_container);
        }else{
            isValid = false;
            this.input.getStyleClass().add("invalid_input");
            if (!container.getChildren().contains(error_icon_container)){
                container.getChildren().add(0, error_icon_container);
            }
            tooltip.setText(invalidReason.length() > 0 ? invalidReason.toString() : DEFAULT_ERROR_TOOLTIP);
        }

        //Check if valid state changed
        if (previousValidState != isValid){
            if (onValidStateChangeListener != null) onValidStateChangeListener.onValidStateChange(isValid);
        }
    }

    public boolean isValid() {
        return isValid;
    }

    public void setText(final String string){
        this.input.setText(string);
    }

    public String getText(){
        return this.input.getText();
    }

    public static class Validator{
        protected boolean isTextValid(final String string, final StringBuilder invalidReason){
            return true;
        }
    }

    private Validator validator = new Validator();

    public void setValidator(Validator validator) {
        if (validator == null) this.validator = new Validator();
        this.validator = validator;

        checkValidity();
    }

    protected Validator getValidator(){
        return this.validator;
    }

    public interface OnValidStateChangeListener{
        void onValidStateChange(final boolean isValid);
    }

    private OnValidStateChangeListener onValidStateChangeListener;

    public void setOnValidStateChangeListener(OnValidStateChangeListener onValidStateChangeListener) {
        this.onValidStateChangeListener = onValidStateChangeListener;
        if (onValidStateChangeListener != null) onValidStateChangeListener.onValidStateChange(isValid);
    }
}
