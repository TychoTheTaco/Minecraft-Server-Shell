package com.tycho.mss.layout;

import com.tycho.mss.MenuListCell;
import com.tycho.mss.MenuPage;
import com.tycho.mss.ServerShell;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Comparator;

public class MainLayout {

    @FXML
    private ListView<MenuItem> module_list_view;

    @FXML
    private Pane miniDashboard;

    @FXML
    private MiniDashboardController miniDashboardController;

    @FXML
    private BorderPane container;

    private ServerShell serverShell;

    public class MenuItem{

        private final String title;

        private final FXMLLoader loader;

        private final Node node;

        public MenuItem(final String title, final String layout) throws IOException {
            this.title = title;
            this.loader = new FXMLLoader(getClass().getResource("/layout/" + layout + ".fxml"));
            this.node = this.loader.load();
        }

        public String getTitle() {
            return title;
        }

        public FXMLLoader getLoader() {
            return loader;
        }

        public Node getNode() {
            return node;
        }
    }

    @FXML
    private void initialize() {
        //Modules
        module_list_view.setCellFactory(param -> new MenuListCell());
        try {
            //module_list_view.getItems().add(new MenuItem("Dashboard", "dashboard_layout"));
            module_list_view.getItems().add(new MenuItem("Players", "players_layout"));
            module_list_view.getItems().add(new MenuItem("Console", "console_layout"));
            module_list_view.getItems().add(new MenuItem("Configuration", "configuration_layout"));
            module_list_view.getItems().add(new MenuItem("Custom Commands", "custom_commands_layout"));
            module_list_view.getItems().add(new MenuItem("Backups", "backups_layout"));
            module_list_view.getItems().sort(Comparator.comparing(MenuItem::getTitle));
        }catch (IOException e){
            e.printStackTrace();
        }

        for (MenuItem menuItem : module_list_view.getItems()){
            ((MenuPage) menuItem.getLoader().getController()).addStatusChangedListener((previous, status) -> module_list_view.refresh());
        }

        module_list_view.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ((MenuPage) newValue.getLoader().getController()).onPageSelected();
            container.setCenter(newValue.getNode());
            if (oldValue != null) ((MenuPage) oldValue.getLoader().getController()).onPageHidden();
        });

        module_list_view.getSelectionModel().select(getMenuItemIndex("Console"));

        //Mini dashboard
        miniDashboard.managedProperty().bind(miniDashboard.visibleProperty());
    }

    public void onHidden(){
        miniDashboardController.onPageHidden();
        for (MenuItem menuItem : module_list_view.getItems()){
            ((MenuPage) menuItem.getLoader().getController()).onPageHidden();
        }
    }

    private int getMenuItemIndex(final String title){
        for (MenuItem menuItem : module_list_view.getItems()){
            if (menuItem.getTitle().equals(title)) return module_list_view.getItems().indexOf(menuItem);
        }
        return -1;
    }

    public void setServerShell(ServerShell serverShell) {
        this.serverShell = serverShell;

        //Update modules
        for (MenuItem menuItem : module_list_view.getItems()){
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
