package mss.command;

import mss.ServerShell;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static mss.util.Utils.createText;

public class HelpCommand extends Command {

    @Override
    public void execute(String player, ServerShell serverShell, String... parameters) throws Exception {
        //Create root object with standard formatting. All extras will inherit these properties.
        final JSONObject root = createText("", "gray");
        final JSONArray extras = new JSONArray();

        //Create title text
        final JSONObject title = createText("The server accepts the following commands:\n", "");
        title.put("bold", false);
        extras.add(title);

        //Sort commands alphabetically
        final List<Command> commands = serverShell.getCustomCommands();
        commands.sort(Comparator.comparing(Command::getCommand));

        //List commands and descriptions
        for (Command command : commands){
            extras.add(createText(command.getCommand(), "green"));
            extras.add(createText(": " + command.getDescription(), ""));
            extras.add(createText("\n", ""));
        }
        extras.remove(extras.size() - 1);

        //Add extras to root object
        root.put("extra", extras);

        //Display text to user
        serverShell.tellraw(player, root);
    }

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Shows all the commands you can execute.";
    }
}
