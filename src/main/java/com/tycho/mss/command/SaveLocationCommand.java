package com.tycho.mss.command;

import com.tycho.mss.ServerShell;

public class SaveLocationCommand extends Command {

    @Override
    public void execute(String player, ServerShell serverShell, String... parameters) throws Exception {

    }

    @Override
    public String getCommand() {
        return "savelocation";
    }

    @Override
    public String getFormat() {
        return "<notes>";
    }

    @Override
    public String getDescription() {
        return "Saves your current location along with a note so you can find it again later.";
    }
}
