package com.tycho.mss.layout;

import com.tycho.mss.MenuPage;
import com.tycho.mss.ServerShell;
import com.tycho.mss.command.Command;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class CustomCommandsLayout extends MenuPage {

    @FXML
    private TableView<Command> custom_commands_table_view;

    @FXML
    private void initialize() {
        //Command Name
        final TableColumn<Command, String> commandNameColumn = new TableColumn<>("Command");
        commandNameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getCommand()));
        commandNameColumn.setPrefWidth(150);
        custom_commands_table_view.getColumns().add(commandNameColumn);

        //Description
        final TableColumn<Command, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setSortable(false);
        descriptionColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getDescription()));
        descriptionColumn.prefWidthProperty().bind(custom_commands_table_view.widthProperty().subtract(commandNameColumn.widthProperty()).subtract(2));
        custom_commands_table_view.getColumns().add(descriptionColumn);

        //IP Address
        /*final TableColumn<Player, String> ipAddressColumn = new TableColumn<>("IP Address");
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        ipAddressColumn.setPrefWidth(130);
        players_table_view.getColumns().add(ipAddressColumn);

        final HashMap<Player, CachedStats> cache = new HashMap<>();*/
    }

    @Override
    public void setServerShell(ServerShell serverShell) {
        super.setServerShell(serverShell);

        for (Command command : serverShell.getCustomCommands()){
            custom_commands_table_view.getItems().add(command);
        }
    }
}
