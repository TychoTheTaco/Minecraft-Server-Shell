package mss.command;

import mss.ServerShell;

public abstract class Command {

    /**
     * Execute the command.
     * @param player The player that executed the command. This will be {@code null} if the server is executing it directly.
     * @param serverShell A reference to the server shell.
     * @param parameters The parameters for this command.
     * @return A string that will be printed to the chat. If this is {@code null}, no message will be printed.
     * @throws Exception Commands may throw any exceptions to indicate an error. The error message will be whispered to the user who executed this command.
     */
    public abstract String execute(final String player, final ServerShell serverShell, final String... parameters) throws Exception;

    public abstract String getCommand();

    public abstract String getFormat();

    public abstract String getDescription();

    public class InvalidParametersException extends Exception{
        @Override
        public String getMessage() {
            return "§cInvalid parameters!§7 Expected format: !" + getCommand() + " " + getFormat();
        }
    }
}
