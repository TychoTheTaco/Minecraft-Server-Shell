package mss.command;

import mss.ServerShell;

public class HelpCommand extends Command {
    @Override
    public String execute(String player, ServerShell serverShell, String... parameters) throws Exception {
        serverShell.execute("msg " + player + " §fYou have access to the following commands:");
        for (Command command : serverShell.getCustomCommands()){
            serverShell.msg(player, "§r§a" + command.getCommand() + "§7: " + command.getDescription());
        }
        return null;
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
