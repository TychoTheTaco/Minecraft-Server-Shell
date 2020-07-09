package com.tycho.mss.command;


import com.tycho.mss.Colors;
import com.tycho.mss.ServerShell;
import com.tycho.mss.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Comparator;
import java.util.List;

public class HelpCommand extends Command {

    public HelpCommand(){
        super("help");
    }

    @Override
    public void execute(String player, ServerShell serverShell, String... parameters) throws Exception {
        if (parameters.length > 0){
            //Find target command
            Command target = null;
            for (Command command : serverShell.getCustomCommands()){
                if (command.getCommand().equals(parameters[0])){
                    target = command;
                    break;
                }
            }

            if (target != null){
                final JSONObject root = Utils.createText("", "gray");
                final JSONArray extras = new JSONArray();

                extras.add(Utils.createText(target.getCommand(), Colors.COMMAND_COLOR));
                extras.add(Utils.createText(": " + target.getDescription(), ""));
                extras.add(Utils.createText("\n", ""));
                extras.add(Utils.createText("Usage:\n", "white"));
                extras.add(Utils.createText(target.getCommand(), Colors.COMMAND_COLOR));
                extras.add(Utils.createText(" " + target.getFormat(), "gray"));

                //Display text to user
                root.put("extra", extras);
                serverShell.tellraw(player, root);
            }else{
                final JSONObject root = Utils.createText("Unknown command: ", "red");
                final JSONArray extras = new JSONArray();
                extras.add(Utils.createText(parameters[0], "white"));
                root.put("extra", extras);
                serverShell.tellraw(player, root);
            }
        }else{
            //Create root object with standard formatting. All extras will inherit these properties.
            final JSONObject root = Utils.createText("", "gray");
            final JSONArray extras = new JSONArray();

            //Create title text
            final JSONObject title = Utils.createText("You have access to the following commands:\n", "white");
            title.put("bold", false);
            extras.add(title);

            //Sort commands alphabetically
            final List<Command> commands = serverShell.getCustomCommands();
            commands.sort(Comparator.comparing(Command::getCommand));

            //List commands and descriptions
            for (Command command : commands){
                if (serverShell.getPermissionsManager().isAuthorized(player, command)){
                    extras.add(Utils.createText(command.getCommand(), Colors.COMMAND_COLOR));
                    extras.add(Utils.createText(": " + command.getDescription(), ""));
                    extras.add(Utils.createText("\n", ""));
                }
            }
            extras.remove(extras.size() - 1);

            //Add extras to root object
            root.put("extra", extras);

            //Display text to user
            serverShell.tellraw(player, root);
        }
    }

    @Override
    public String getFormat() {
        return "[<command>]";
    }

    @Override
    public String getDescription() {
        return "Shows all the commands you can execute.";
    }
}
