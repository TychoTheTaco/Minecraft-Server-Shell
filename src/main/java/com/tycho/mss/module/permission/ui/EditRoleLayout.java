package com.tycho.mss.module.permission.ui;

import com.tycho.mss.ServerShell;
import com.tycho.mss.command.Command;
import com.tycho.mss.layout.TextCheckBoxLayout;
import com.tycho.mss.layout.ValidatedTextField;
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
    private ValidatedTextField roleNameTextFieldController;

    @FXML
    private ListView<Command> commands_list_view;

    @FXML
    private Button ok_button;

    @FXML
    private TextCheckBoxLayout auto_assign_check_boxController;

    private Role role;

    @FXML
    private void initialize() {
        auto_assign_check_boxController.setText("Automatically assign this role to new players.");

        commands_list_view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        commands_list_view.setCellFactory(param -> new CommandListCell());

        ok_button.setOnAction(event -> ((Stage) ok_button.getScene().getWindow()).close());

        roleNameTextFieldController.setValidator(new ValidatedTextField.Validator(){
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
        roleNameTextFieldController.setOnValidStateChangeListener(new ValidatedTextField.OnValidStateChangeListener() {
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
        for (Command command : commands_list_view.getItems()){
            if (command.getCommand().equals("help")){
                commands_list_view.getSelectionModel().select(command);
                break;
            }
        }
    }

    public Role getRole(){
        if (!roleNameTextFieldController.isValid()){
            return null;
        }

        final List<Class<? extends Command>> commands = new ArrayList<>();
        for (Command command : commands_list_view.getSelectionModel().getSelectedItems()){
            commands.add(command.getClass());
        }

        if (role != null){
            role.setName(roleNameTextFieldController.getText().trim());
            role.getCommands().clear();
            role.getCommands().addAll(commands);
            role.setAutoAssign(auto_assign_check_boxController.isSelected());
            return role;
        }

        return new Role(roleNameTextFieldController.getText().trim(), commands, auto_assign_check_boxController.isSelected());
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
        auto_assign_check_boxController.setSelected(role.isAutoAssign());
    }
}
