package com.tycho.mss;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.effect.*;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.io.IOException;

public class MenuListCell extends ListCell<MenuItem> {

    @FXML
    private Label title;

    @FXML
    private ImageView error_icon;

    private final Effect errorEffect;
    private final Effect warningEffect;

    public MenuListCell() {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/menu_item_list_cell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Apply red tint
        errorEffect = new Blend(
                BlendMode.SRC_ATOP,
                new ColorAdjust(0, 0, 0, 0),
                new ColorInput(
                        0,
                        0,
                        error_icon.getImage().getWidth(),
                        error_icon.getImage().getHeight(),
                        CustomColor.RED
                )
        );
        warningEffect = new Blend(
                BlendMode.SRC_ATOP,
                new ColorAdjust(0, 0, 0, 0),
                new ColorInput(
                        0,
                        0,
                        error_icon.getImage().getWidth(),
                        error_icon.getImage().getHeight(),
                        Color.YELLOW
                )
        );

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
            if (item.getLoader().getController() instanceof StatusHost){
                switch (((StatusHost) item.getLoader().getController()).getStatusManager().getStatus()) {
                    case ERROR:
                        error_icon.setEffect(errorEffect);
                        error_icon.setVisible(true);
                        break;

                    case WARNING:
                        error_icon.setEffect(warningEffect);
                        error_icon.setVisible(true);
                        break;
                }
            }

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}
