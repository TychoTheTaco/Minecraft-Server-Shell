package com.tycho.mss.layout;

import com.tycho.mss.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.omg.CORBA.ARG_IN;

import java.util.*;
import java.util.function.Consumer;

public class ServerListLayout implements Page {

    @FXML
    private TilePane servers_tile_pane;

    @FXML
    private ScrollPane scroll_pane;

    @FXML
    private void initialize() {
        servers_tile_pane.maxWidthProperty().bind(scroll_pane.widthProperty().subtract(16));

        //Load servers
        refreshServerList();
    }

    @Override
    public void onPageSelected() {
        refreshServerList();
    }

    @Override
    public void onPageHidden() {

    }

    private void refreshServerList(){
        //TODO: Instead of refreshing everything, only add/remove/update items that have changed

        //Sort configurations
        final List<ServerConfiguration> configurations = new ArrayList<>();
        for (UUID uuid : ServerManager.getConfigurations().keySet()){
            configurations.add(ServerManager.getConfigurations().get(uuid));
        }
        configurations.sort(Comparator.comparing(ServerConfiguration::getName));

        servers_tile_pane.getChildren().forEach(new Consumer<Node>() {
            @Override
            public void accept(Node node) {
                if (node instanceof ServerConfigurationListCell) ((ServerConfigurationListCell) node).setServerConfiguration(null);
            }
        });

        servers_tile_pane.getChildren().clear();
        for (ServerConfiguration configuration : configurations){
            final ServerConfigurationListCell cell = new ServerConfigurationListCell();
            cell.setServerConfiguration(configuration);
            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY){
                    MinecraftServerManager.setServer(configuration);
                }
            });
            servers_tile_pane.getChildren().add(cell);
        }

        //Add server item
        final AddNewServerListItem addNewServerListItem = new AddNewServerListItem();
        addNewServerListItem.setOnMouseClicked(event -> {
            final AddServerLayout addServerLayout = new AddServerLayout();
            final Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("New Server");

            final Scene scene = new Scene(addServerLayout);
            scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

            refreshServerList();
        });
        servers_tile_pane.getChildren().add(addNewServerListItem);
    }
}
