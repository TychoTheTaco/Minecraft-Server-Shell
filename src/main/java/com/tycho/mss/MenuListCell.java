package com.tycho.mss;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class MenuListCell extends ListCell<MenuItem> {

    @FXML
    private Label title;

    public MenuListCell() {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/module_list_cell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(MenuItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty){
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }else{
            title.setText(item.getTitle());
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}
