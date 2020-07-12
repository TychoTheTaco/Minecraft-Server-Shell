package com.tycho.mss.layout;

import com.tycho.mss.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Comparator;

public class MainLayout {

    @FXML
    private ListView<MenuItem> menu_items_list_view;

    @FXML
    private MiniDashboard mini_dashboard;

    @FXML
    private BorderPane container;

    @FXML
    private Label server_name_label;

    @FXML
    private Label server_version_label;

    @FXML
    private Node icon;

    @FXML
    private void initialize() {
        loadMenuItems();
        menu_items_list_view.getSelectionModel().select(getMenuItemIndex("Console"));

        //Mini dashboard
        mini_dashboard.managedProperty().bind(mini_dashboard.visibleProperty());

        icon.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MinecraftServerManager.setPage("server_list");
            }
        });
    }

    private void loadMenuItems(){
        menu_items_list_view.setCellFactory(param -> new MenuListCell());
        try {
            menu_items_list_view.getItems().add(new MenuItem("Players", "players_layout"));
            menu_items_list_view.getItems().add(new MenuItem("Console", "console_layout"));
            menu_items_list_view.getItems().add(new MenuItem("Configuration", "configuration_layout"));
            menu_items_list_view.getItems().add(new MenuItem("Custom Commands", "custom_commands_layout"));
            menu_items_list_view.getItems().add(new MenuItem("Backups", "backups_layout"));
            menu_items_list_view.getItems().add(new MenuItem("Permissions", "permissions_layout"));
            menu_items_list_view.getItems().sort(Comparator.comparing(MenuItem::getTitle));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (MenuItem menuItem : menu_items_list_view.getItems()) {
            if (menuItem.getLoader().getController() instanceof StatusHost){
                ((StatusHost) menuItem.getLoader().getController()).getStatusManager().addStatusChangedListener((previous, status) -> menu_items_list_view.refresh());
            }
        }

        menu_items_list_view.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ((Page) newValue.getLoader().getController()).onPageSelected();
            container.setCenter(newValue.getNode());
            newValue.getNode().setId(newValue.getTitle());
            if (oldValue != null) ((Page) oldValue.getLoader().getController()).onPageHidden();
        });
    }

    public void onHidden() {
        mini_dashboard.onPageHidden();
        for (MenuItem menuItem : menu_items_list_view.getItems()) {
            ((Page) menuItem.getLoader().getController()).onPageHidden();
        }
    }

    public void triggerOnHide(){
        if (container.getCenter() != null){
            MenuItem item = menu_items_list_view.getItems().get(getMenuItemIndex(container.getCenter().getId()));
            if (item.getLoader().getController() instanceof Page) ((Page) item.getLoader().getController()).onPageHidden();
        }
    }

    public void onVisible(){
        if (container.getCenter() != null){
            MenuItem item = menu_items_list_view.getItems().get(getMenuItemIndex(container.getCenter().getId()));
            if (item.getLoader().getController() instanceof Page) ((Page) item.getLoader().getController()).onPageSelected();
        }
    }

    private int getMenuItemIndex(final String title) {
        for (MenuItem menuItem : menu_items_list_view.getItems()) {
            if (menuItem.getTitle().equals(title)) return menu_items_list_view.getItems().indexOf(menuItem);
        }
        return -1;
    }

    public void setServerShell(ServerShell serverShell) {
        //Update modules
        for (MenuItem menuItem : menu_items_list_view.getItems()){
            if (menuItem.getLoader().getController() instanceof ServerShellConnection){
                ((ServerShellConnection) menuItem.getLoader().getController()).getServerShellContainer().setServerShell(serverShell);
            }
        }

        //Update mini dashboard
        if (serverShell == null){
            mini_dashboard.setVisible(false);
        }else{
            mini_dashboard.setVisible(true);
        }
        mini_dashboard.getServerShellContainer().setServerShell(serverShell);

        //Update top left
        if (serverShell != null){
            server_name_label.setText(serverShell.getServerConfiguration().getName());
            server_version_label.setText(serverShell.getServerConfiguration().getMinecraftVersion());
        }
    }
}
