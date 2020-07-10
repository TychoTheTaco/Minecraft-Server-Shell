package com.tycho.mss.module.permission.ui;

import com.tycho.mss.ServerShell;
import com.tycho.mss.command.Command;
import com.tycho.mss.layout.ValidatedTextFieldLayout;
import com.tycho.mss.module.permission.Role;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EditRoleLayout {

    @FXML
    private ValidatedTextFieldLayout roleNameTextFieldController;

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

        roleNameTextFieldController.setValidator(new ValidatedTextFieldLayout.Validator(){
            @Override
            protected boolean isTextValid(String string, StringBuilder invalidReason) {
                if (string.trim().length() == 0){
                    invalidReason.append("Name cannot be blank!");
                    return false;
                }

                //TODO: Check for name conflicts with existing roles

                return true;
            }
        });
        roleNameTextFieldController.setOnValidStateChangeListener(new ValidatedTextFieldLayout.OnValidStateChangeListener() {
            @Override
            public void onValidStateChange(boolean isValid) {
                if (isValid){
                    ok_button.setDisable(false);
                }else{
                    ok_button.setDisable(true);
                }
            }
        });
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
            role.setName(roleNameTextFieldController.getText());
            role.getCommands().clear();
            role.getCommands().addAll(commands);
            return role;
        }

        return new Role(roleNameTextFieldController.getText(), commands);
    }

    public void setRole(final Role role){
        this.role = role;
        roleNameTextFieldController.setText(role.getName());
        for (Command command : commands_list_view.getItems()){
            if (role.getCommands().contains(command.getClass())){
                commands_list_view.getSelectionModel().select(command);
                //commands_list_view.getSelectionModel().getSelectedItems().add(command);
            }
        }
    }
}
