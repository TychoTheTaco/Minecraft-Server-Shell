package com.tycho.mss;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.io.IOException;

public class MenuListCell extends ListCell<MenuItem> {

    @FXML
    private Label title;

    @FXML
    private ImageView error_icon;

    public MenuListCell() {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/module_list_cell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Apply red tint
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
    }

    @Override
    protected void updateItem(MenuItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        } else {
            title.setText(item.getTitle());
            error_icon.setVisible(false);
            switch (((MenuPage) item.getLoader().getController()).getStatus()) {
                case ERROR:
                    error_icon.setVisible(true);
                    break;
            }

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}
