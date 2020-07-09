package com.tycho.mss.layout;

import com.tycho.mss.MenuItem;
import com.tycho.mss.MenuListCell;
import com.tycho.mss.MenuPage;
import com.tycho.mss.ServerShell;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Comparator;

public class MainLayout {

    @FXML
    private ListView<MenuItem> menu_items_list_view;

    @FXML
    private Pane miniDashboard;

    @FXML
    private MiniDashboardController miniDashboardController;

    @FXML
    private BorderPane container;

    private ServerShell serverShell;

    @FXML
    private void initialize() {
        loadMenuItems();
        menu_items_list_view.getSelectionModel().select(getMenuItemIndex("Console"));

        //Mini dashboard
        miniDashboard.managedProperty().bind(miniDashboard.visibleProperty());
    }

    private void loadMenuItems(){
        menu_items_list_view.setCellFactory(param -> new MenuListCell());
        try {
            //menu_items_list_view.getItems().add(new MenuItem("Dashboard", "dashboard_layout"));
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
            ((MenuPage) menuItem.getLoader().getController()).addStatusChangedListener((previous, status) -> menu_items_list_view.refresh());
        }

        menu_items_list_view.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ((MenuPage) newValue.getLoader().getController()).onPageSelected();
            container.setCenter(newValue.getNode());
            if (oldValue != null) ((MenuPage) oldValue.getLoader().getController()).onPageHidden();
        });
    }

    public void onHidden() {
        miniDashboardController.onPageHidden();
        for (MenuItem menuItem : menu_items_list_view.getItems()) {
            ((MenuPage) menuItem.getLoader().getController()).onPageHidden();
        }
    }

    private int getMenuItemIndex(final String title) {
        for (MenuItem menuItem : menu_items_list_view.getItems()) {
            if (menuItem.getTitle().equals(title)) return menu_items_list_view.getItems().indexOf(menuItem);
        }
        return -1;
    }

    public void setServerShell(ServerShell serverShell) {
        this.serverShell = serverShell;

        //Update modules
        for (MenuItem menuItem : menu_items_list_view.getItems()){
            ((MenuPage) menuItem.getLoader().getController()).setServerShell(serverShell);
        }

        //Update mini dashboard
        if (serverShell == null){
            miniDashboard.setVisible(false);
        }else{
            miniDashboard.setVisible(true);
        }
        miniDashboardController.setServerShell(serverShell);
    }
}
