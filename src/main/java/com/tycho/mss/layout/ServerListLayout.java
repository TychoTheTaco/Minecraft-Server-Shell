package com.tycho.mss.layout;

import com.tycho.mss.*;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.UUID;

public class ServerListLayout implements Page {

    @FXML
    private TilePane servers_tile_pane;

    @FXML
    private void initialize() {
        //Load servers
        refreshServerList();
    }

    @Override
    public void onPageSelected() {

    }

    @Override
    public void onPageHidden() {

    }

    private void refreshServerList(){
        servers_tile_pane.getChildren().clear();
        for (UUID uuid : ServerManager.getConfigurations().keySet()){
            final ServerConfigurationListCell cell = new ServerConfigurationListCell();
            cell.setServerConfiguration(ServerManager.getConfigurations().get(uuid));
            servers_tile_pane.getChildren().add(cell);
        }

        //Add server item
        final AddNewListItem addNewListItem = new AddNewListItem();
        addNewListItem.setOnMouseClicked(event -> {
            final EditServerLayout editServerLayout = new EditServerLayout();
            final Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("New Server");

            final Scene scene = new Scene(editServerLayout);
            scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

            refreshServerList();
        });
        servers_tile_pane.getChildren().add(addNewListItem);
    }
}
