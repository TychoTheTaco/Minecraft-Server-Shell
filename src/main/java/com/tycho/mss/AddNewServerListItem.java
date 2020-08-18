package com.tycho.mss;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class AddNewServerListItem extends GridPane {

    public AddNewServerListItem() {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/add_new_server_list_item.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
