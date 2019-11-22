package com.tycho.mss.layout;

import com.tycho.mss.MenuListCell;
import com.tycho.mss.MenuPage;
import com.tycho.mss.ServerShell;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainLayout {

    @FXML
    private ListView<MenuPage> module_list_view;

    @FXML
    private BorderPane container;

    private ServerShell serverShell;

    @FXML
    private void initialize() {
        //Modules
        module_list_view.setCellFactory(param -> new MenuListCell());

        final Node[] nodes = new Node[4];
        final String[] ids = new String[]{"dashboard_layout", "players_layout", "console_module_layout", "configuration_layout"};
        for (int i = 0; i < ids.length; i++){
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/" + ids[i] + ".fxml"));
            try {
                nodes[i] = loader.load();
                module_list_view.getItems().add(loader.getController());
                //module_list_view.getItems().get(i).setController(loader.getController());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        module_list_view.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<MenuPage>() {
            @Override
            public void changed(ObservableValue<? extends MenuPage> observable, MenuPage oldValue, MenuPage newValue) {
                switch (newValue.getTitle()){
                    case "Dashboard":
                        container.setCenter(nodes[0]);
                        break;

                    case "Players":
                        container.setCenter(nodes[1]);
                        break;

                    case "Console":
                        container.setCenter(nodes[2]);
                        break;

                    case "Configuration":
                        container.setCenter(nodes[3]);
                        break;
                }
            }
        });

        module_list_view.getSelectionModel().select(0);
    }

    public void setServerShell(ServerShell serverShell) {
        this.serverShell = serverShell;
        for (MenuPage menuPage : module_list_view.getItems()){
            menuPage.setServerShell(serverShell);
        }
    }
}
