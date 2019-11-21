package com.tycho.mss.command;


import com.tycho.mss.ServerShell;
import com.tycho.mss.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public abstract class Command {

    /**
     * Execute the command.
     * @param player The player that executed the command. This will be {@code null} if the server is executing it directly.
     * @param serverShell A reference to the server shell.
     * @param parameters The parameters for this command.
     * @return A string that will be printed to the chat. If this is {@code null}, no message will be printed.
     * @throws Exception Commands may throw any exceptions to indicate an error. The error message will be whispered to the user who executed this command.
     */
    public abstract void execute(final String player, final ServerShell serverShell, final String... parameters) throws Exception;

    public abstract String getCommand();

    public abstract String getFormat();

    public abstract String getDescription();

    public class InvalidParametersException extends Exception{

        public JSONObject getJson(){
            final JSONObject root = Utils.createText("Invalid Parameters! ", "red");
            final JSONArray extra = new JSONArray();
            extra.add(Utils.createText("Expected format:\n", "gray"));
            extra.add(Utils.createText(getCommand(), "green"));
            extra.add(Utils.createText(" " + getFormat(), "white"));
            root.put("extra", extra);
            return root;
        }
    }
}
