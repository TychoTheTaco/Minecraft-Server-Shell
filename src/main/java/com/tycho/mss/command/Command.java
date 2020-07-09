package com.tycho.mss.command;


import com.tycho.mss.ServerShell;
import com.tycho.mss.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.regex.Pattern;

public abstract class Command {

    private final String command;

    private final Pattern pattern;

    public Command(final String command){
        this(command, Pattern.compile("^" + command + "(?: (?<parameters>[^ ].*))?$"));
    }

    public Command(final String command, final Pattern pattern){
        this.command = command;
        this.pattern = pattern;
    }

    /**
     * Execute the command.
     * @param player The player that executed the command. This will be {@code null} if the server is executing it directly.
     * @param serverShell A reference to the server shell.
     * @param parameters The parameters for this command.
     * @return A string that will be printed to the chat. If this is {@code null}, no message will be printed.
     * @throws Exception Commands may throw any exceptions to indicate an error. The error message will be whispered to the user who executed this command.
     */
    public abstract void execute(final String player, final ServerShell serverShell, final String... parameters) throws Exception;

    public String getCommand() {
        return command;
    }

    public abstract String getFormat();

    public abstract String getDescription();

    public Pattern getPattern() {
        return pattern;
    }

    public class InvalidParametersException extends Exception{

        public JSONObject getJson(){
            return Utils.createText("Invalid Parameters! ", "red", "Expected format:\n", "gray", getCommand(), "green", " " + getFormat(), "white");
        }
    }
}
