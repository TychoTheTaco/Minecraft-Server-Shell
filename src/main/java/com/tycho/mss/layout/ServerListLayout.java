package com.tycho.mss.layout;

import com.tycho.mss.AddNewListItem;
import com.tycho.mss.MinecraftServerManager;
import com.tycho.mss.ServerConfigurationListCell;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
        addNewListItem.setOnMouseClicked(event -> {

            MinecraftServerManager.setPage("main");

            final EditServerLayout editServerLayout = new EditServerLayout();
            final Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("New Server");

            //final AddPlayerLayout addPlayerLayout = fxmlLoader.getController();
            //addPlayerLayout.setStage(stage);

            final Scene scene = new Scene(editServerLayout);
            scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
                /*if (addPlayerLayout.getPlayer() != null){
                    serverShellContainer.getServerShell().getPermissionsManager().assign(addPlayerLayout.getPlayer(), roles_list_view.getSelectionModel().getSelectedItem());
                    players_list_view.getItems().add(addPlayerLayout.getPlayer());
                }*/
        });
        servers_tile_pane.getChildren().add(addNewListItem);
    }

}
