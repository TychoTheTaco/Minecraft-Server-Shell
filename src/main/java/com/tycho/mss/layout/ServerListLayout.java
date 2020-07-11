package com.tycho.mss.layout;

import com.tycho.mss.AddNewListItem;
import com.tycho.mss.MinecraftServerManager;
import com.tycho.mss.ServerConfigurationListCell;
import javafx.fxml.FXML;
import javafx.scene.layout.TilePane;

public class ServerListLayout {

    @FXML
    private TilePane servers_tile_pane;

    @FXML
    private void initialize() {
        servers_tile_pane.getChildren().add(new ServerConfigurationListCell());
        servers_tile_pane.getChildren().add(new ServerConfigurationListCell());
        servers_tile_pane.getChildren().add(new ServerConfigurationListCell());

        //Add server item
        final AddNewListItem addNewListItem = new AddNewListItem();
        addNewListItem.setOnMouseClicked(event -> MinecraftServerManager.setPage("main"));
        servers_tile_pane.getChildren().add(addNewListItem);
    }

}
