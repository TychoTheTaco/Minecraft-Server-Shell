package com.tycho.mss.command;

import com.tycho.mss.ServerShell;

public class PermissionCommand extends Command {

    public PermissionCommand(){
        super("authorize");
    }

    @Override
    public void execute(String player, ServerShell serverShell, String... parameters) throws Exception {
        if (parameters.length != 3){
            throw new InvalidParametersException();
        }

        final String targetPlayer = parameters[1];

        //Determine target command
        Command targetCommand = null;
        for (Command command : serverShell.getCustomCommands()){
            if (command.getCommand().equals(parameters[2])){
                targetCommand = command;
            }
        }
        if (targetCommand == null) throw new InvalidParametersException();

        //Determine action
        /*switch (parameters[0]){
            case "a":
                serverShell.authorize(targetPlayer, targetCommand);
                break;

            case "d":
                serverShell.deauthorize(targetPlayer, targetCommand);
                break;
        }*/
    }

    @Override
    public String getFormat() {
        return "<a | d> <player> <command>";
    }

    @Override
    public String getDescription() {
        return "Change player permissions";
    }
}
