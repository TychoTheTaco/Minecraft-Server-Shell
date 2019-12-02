package com.tycho.mss.command;

import com.tycho.mss.Player;
import com.tycho.mss.ServerShell;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuideCommand extends Command {

    private static final Pattern POSITION_PATTERN = Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}] \\[Server thread\\/INFO]: (?<player>.+) has the following entity data: \\[(?<x>-?\\d+)\\.\\d+d, (?<y>-?\\d+)\\.\\d+d, (?<z>-?\\d+)\\.\\d+d]");

    private Thread thread;

    @Override
    public void execute(String player, ServerShell serverShell, String... parameters) throws Exception {
        if (parameters.length > 0){
            if ("stop".equals(parameters[0])){
                if (this.thread != null) thread.interrupt();
                this.thread = null;
                return;
            }
        }
        //Get player position
        Matcher matcher = serverShell.awaitResult("data get entity " + player + " Pos", POSITION_PATTERN);
        final int x = Integer.parseInt(matcher.group("x"));
        final int y = Integer.parseInt(matcher.group("y"));
        final int z = Integer.parseInt(matcher.group("z"));

        //Determine destination
        /*final Player destination = serverShell.getPlayer(parameters[0]);
        matcher = serverShell.awaitResult("data get entity " + destination + " Pos", POSITION_PATTERN);
        final int dx = Integer.parseInt(matcher.group("x"));
        final int dy = Integer.parseInt(matcher.group("y"));
        final int dz = Integer.parseInt(matcher.group("z"));*/

        final int dx = 0;
        final int dy = 0;
        final int dz = 0;

        final double direction = Math.atan2(dz - z, dx - x);
        if (thread != null) thread.interrupt();
        thread = new Thread(() -> {
            while (true){
                for (int i = 1; i < 10; i++){
                    try {
                        serverShell.execute("execute at TychoTheTaco run particle composter ~" + (i * Math.cos(direction)) + " ~" + 0 + " ~" + (i * Math.sin(direction)) + " 0 0 0 0 1 normal");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(100);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public String getCommand() {
        return "guide";
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Guides you to another player or location";
    }
}
