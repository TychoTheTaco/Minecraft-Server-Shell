package com.tycho.mss.layout;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class EditServerLayout extends VBox {

    @FXML
    private ValidatedTextField server_name_input;

    public EditServerLayout(){
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/edit_server_layout.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
