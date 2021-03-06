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
import java.util.Optional;

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
                for (MenuItem menuItem : menu_items_list_view.getItems()){
                    if (menuItem.getLoader().getController() instanceof ServerShellConnection){
                        ((ServerShellConnection) menuItem.getLoader().getController()).detach(serverShell);
                    }
                }
                mini_dashboard.detach(serverShell);
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

    private ServerShell serverShell;

    public void setServerShell(ServerShell serverShell) {
        //Detach from old server shell
        if (this.serverShell != null){
            for (MenuItem menuItem : menu_items_list_view.getItems()){
                if (menuItem.getLoader().getController() instanceof ServerShellConnection){
                    ((ServerShellConnection) menuItem.getLoader().getController()).detach(serverShell);
                }
            }
        }

        this.serverShell = serverShell;

        //Attach to new server shell
        if (this.serverShell != null){
            for (MenuItem menuItem : menu_items_list_view.getItems()){
                if (menuItem.getLoader().getController() instanceof ServerShellConnection){
                    ((ServerShellConnection) menuItem.getLoader().getController()).attach(serverShell);
                }
            }
        }

        //Update mini dashboard
        mini_dashboard.setVisible(serverShell != null);
        mini_dashboard.attach(serverShell);

        //Update top left
        if (serverShell != null){
            server_name_label.setText(serverShell.getServerConfiguration().getName());
            final String version = serverShell.getServerConfiguration().getMinecraftVersion();
            server_version_label.setText(version == null ? "Unknown version" : version);
        }
    }
}
