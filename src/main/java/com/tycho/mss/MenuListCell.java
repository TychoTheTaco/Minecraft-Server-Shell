package com.tycho.mss;

import com.tycho.mss.layout.MainLayout;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class MenuListCell extends ListCell<MainLayout.MenuItem> {

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
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(MainLayout.MenuItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty){
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }else{
            title.setText(item.getTitle());
            error_icon.setVisible(false);
            switch (((MenuPage) item.getLoader().getController()).getStatus()){
                case ERROR:
                    error_icon.setVisible(true);
                    break;
            }

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}
