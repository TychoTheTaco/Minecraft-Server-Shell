package com.tycho.mss.layout;

import com.tycho.mss.CommandListCell;
import com.tycho.mss.RoleListCell;
import com.tycho.mss.ServerShell;
import com.tycho.mss.command.Command;
import com.tycho.mss.command.HelpCommand;
import com.tycho.mss.permission.Role;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EditRoleLayout {

    @FXML
    private TextField role_name_text_field;

    @FXML
    private ListView<Command> commands_list_view;

    @FXML
    private Button ok_button;

    private Stage stage;

    private Role role;

    @FXML
    private void initialize() {
        commands_list_view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        commands_list_view.setCellFactory(param -> new CommandListCell());

        ok_button.setOnAction(event -> stage.close());
    }

    public void setServerShell(final ServerShell serverShell){
        commands_list_view.getItems().setAll(serverShell.getCustomCommands());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Role getRole(){
        final List<Class<? extends Command>> commands = new ArrayList<>();
        for (Command command : commands_list_view.getSelectionModel().getSelectedItems()){
            commands.add(command.getClass());
        }

        if (role != null){
            role.setName(role_name_text_field.getText());
            role.getCommands().clear();
            role.getCommands().addAll(commands);
            return role;
        }

        return new Role(role_name_text_field.getText(), commands);
    }

    public void setRole(final Role role){
        this.role = role;
        this.role_name_text_field.setText(role.getName());
        for (Command command : commands_list_view.getItems()){
            if (role.getCommands().contains(command.getClass())){
                commands_list_view.getSelectionModel().select(command);
                //commands_list_view.getSelectionModel().getSelectedItems().add(command);
            }
        }
    }
}
