package com.tycho.mss.command;


import com.tycho.mss.Context;
import com.tycho.mss.util.Utils;
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
     * @param player The player that is executing the command. This will be {@code null} if the server is executing it directly.
     * @param context The context in which this command is being executed. This provides access to all of the server interactions.
     * @param parameters The parameters for this command.
     * @throws Exception Commands may throw any exceptions to indicate an error. If an exception occurs, the error message will be whispered to the user who executed this command.
     */
    public abstract void execute(final String player, final Context context, final String... parameters) throws Exception;

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
