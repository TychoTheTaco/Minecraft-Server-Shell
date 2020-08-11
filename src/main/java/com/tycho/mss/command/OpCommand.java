package com.tycho.mss.command;

import com.tycho.mss.Context;

/**
 * This command makes the target player an operator (op). This is useful because it allows the player to become an operator without requiring access to the server console.
 */
public class OpCommand extends Command {

    public OpCommand() {
        super("op");
    }

    @Override
    public void execute(String player, Context context, String... parameters) throws Exception {
        context.execute("op " + player);
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Make yourself an operator!";
    }
}
