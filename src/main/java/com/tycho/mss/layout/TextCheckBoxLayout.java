package com.tycho.mss.layout;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class TextCheckBoxLayout {

    @FXML
    private CheckBox check_box;

    @FXML
    private Label label;

    @FXML
    private void initialize() {
       final Parent root = label.getParent();
       root.setOnMouseClicked(new EventHandler<MouseEvent>() {
           @Override
           public void handle(MouseEvent event) {
               check_box.setSelected(!check_box.isSelected());
           }
       });
    }

    public void setText(final String string){
        this.label.setText(string);
    }

    public boolean isSelected(){
        return check_box.isSelected();
    }

    public void setSelected(final boolean selected){
        this.check_box.setSelected(selected);
    }
}
