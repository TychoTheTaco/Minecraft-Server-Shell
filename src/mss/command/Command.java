package mss.command;

import mss.ServerShell;

public abstract class Command {

    public abstract String execute(final ServerShell serverShell, final String... parameters) throws Exception;

    public abstract String getCommand();
}
