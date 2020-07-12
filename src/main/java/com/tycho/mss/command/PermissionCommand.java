package com.tycho.mss.command;

import com.tycho.mss.Context;
import com.tycho.mss.ServerManager;
import com.tycho.mss.module.permission.Role;
import com.tycho.mss.util.Utils;

import java.util.List;

public class PermissionCommand extends Command {

    public PermissionCommand(){
        super("permission");
    }

    @Override
    public void execute(String player, Context context, String... parameters) throws Exception {
        if (parameters.length < 1){
            throw new InvalidParametersException();
        }

        if ("list".equals(parameters[0])){
            final List<Role> roles = context.getPermissionsManager().getRoles();
            if (roles.isEmpty()){
                context.tellraw(player, Utils.createText("You haven't created any roles yet!", "white"));
            }else{
                for (Role role : roles){
                    context.tellraw(player, Utils.createText(role.getName(), "green"));
                    if (role.getCommands().isEmpty()){
                        context.tellraw(player, Utils.createText("<No Permissions>", "white"));
                    }else{
                        //List commands
                        final StringBuilder stringBuilder = new StringBuilder();
                        for (Class<? extends Command> cls : role.getCommands()){
                            final Command command = findCommand(context, cls);
                            if (command != null){
                                stringBuilder.append(command.getCommand()).append(", ");
                            }
                        }
                        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                        context.tellraw(player, Utils.createText(stringBuilder.toString(), "white"));
                    }

                    //List players
                    final List<String> players = context.getPermissionsManager().getPlayers(role);
                    if (players.isEmpty()){
                        context.tellraw(player, Utils.createText("<No Players>", "white"));
                    }else{
                        final StringBuilder stringBuilder = new StringBuilder();
                        for (String p : players){
                            stringBuilder.append(p).append(", ");
                        }
                        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                        context.tellraw(player, Utils.createText(stringBuilder.toString(), "dark_aqua"));
                    }

                }
            }
        }else if ("create".equals(parameters[0])){
            if (parameters.length < 2) throw new InvalidParametersException();
            context.getPermissionsManager().addRole(new Role(parameters[1]));
        }else if ("delete".equals(parameters[0])){
            if (parameters.length < 2) throw new InvalidParametersException();
            context.getPermissionsManager().removeRole(findRole(context, parameters[1]));
        }else if ("role".equals(parameters[0])){
            if (parameters.length < 4) throw new InvalidParametersException();

            //Find the role
            final Role role = findRole(context, parameters[1]);
            if (role == null) throw new InvalidParametersException();

            //Find command
            final Command command = findCommand(context, parameters[3]);
            if (command == null) throw new InvalidParametersException();

            if ("add".equals(parameters[2])){
                role.getCommands().add(command.getClass());
                ServerManager.save();
            }else if ("remove".equals(parameters[2])){
                role.getCommands().remove(command.getClass());
                ServerManager.save();
            }else{
                throw new InvalidParametersException();
            }
        }else if ("assign".equals(parameters[0])){
            if (parameters.length < 3) throw new InvalidParametersException();
            final Role role = findRole(context, parameters[1]);
            if (role == null) throw new InvalidParametersException();
            context.getPermissionsManager().assign(parameters[2], role);
        }else if ("unassign".equals(parameters[0])){
            if (parameters.length < 3) throw new InvalidParametersException();
            final Role role = findRole(context, parameters[1]);
            if (role == null) throw new InvalidParametersException();
            context.getPermissionsManager().unassign(parameters[2], role);
        }else{
            throw new InvalidParametersException();
        }
    }

    @Override
    public String getFormat() {
        return "list | create <role> | delete <role> | role <role> (add | remove) <command> | assign <role> <player> | unassign <role> <player>";
    }

    @Override
    public String getDescription() {
        return "Change player permissions.";
    }

    private Command findCommand(final Context context, final Class<? extends Command> cls){
        for (Command command : context.getCustomCommands()){
            if (command.getClass().equals(cls)) return command;
        }
        return null;
    }

    private Command findCommand(final Context context, final String name){
        for (Command command : context.getCustomCommands()){
            if (command.getCommand().equals(name)) return command;
        }
        return null;
    }

    private Role findRole(final Context context, final String name){
        for (Role role : context.getPermissionsManager().getRoles()){
            if (role.getName().equals(name)) return role;
        }
        return null;
    }
}
