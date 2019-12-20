package com.tycho.mss;

import com.tycho.mss.command.Command;
import com.tycho.mss.permission.Role;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class CommandListCell extends ListCell<Command> {

    @FXML
    private Label command_name_label;

    @FXML
    private Label description_label;

    @FXML
    private CheckBox toggle_button;

    public CommandListCell() {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/command_list_cell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();

            /*addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                toggle_button.setSelected(!toggle_button.isSelected());
            });*/

            selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    toggle_button.setSelected(newValue);
                }
            });

            addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (isEmpty()) {
                    return ;
                }

                int index = getIndex() ;
                if (getListView().getSelectionModel().getSelectedIndices().contains(index)) {
                    getListView().getSelectionModel().clearSelection(index);
                } else {
                    getListView().getSelectionModel().select(index);
                }

                getListView().requestFocus();

                e.consume();
            });
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(Command item, boolean empty) {
        super.updateItem(item, empty);
        if (empty){
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }else{
            command_name_label.setText(item.getCommand());
            description_label.setText(item.getDescription());
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }

    public boolean isChecked(){
        return toggle_button.isSelected();
    }
}
