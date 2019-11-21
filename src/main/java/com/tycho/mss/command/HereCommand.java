package com.tycho.mss.command;

import com.tycho.mss.ServerShell;
import com.tycho.mss.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HereCommand extends Command {

    private static final Pattern POSITION_PATTERN = Pattern.compile("\\[Server thread\\/INFO]: (?<player>.+) has the following entity data: \\[(?<x>-?\\d+)\\.\\d+d, (?<y>-?\\d+)\\.\\d+d, (?<z>-?\\d+)\\.\\d+d]");

    @Override
    public void execute(String player, ServerShell serverShell, String... parameters) throws Exception {
        //Get player position
        final Matcher matcher = serverShell.awaitResult("data get entity " + player + " Pos", POSITION_PATTERN);
        final int x = Integer.parseInt(matcher.group("x"));
        final int y = Integer.parseInt(matcher.group("y"));
        final int z = Integer.parseInt(matcher.group("z"));

        //Spawn firework rocket
        serverShell.execute("summon minecraft:firework_rocket " + x + " " + (y + 3) + " " + z + " {LifeTime:30,FireworksItem:{id:firework_rocket,Count:1,tag:{Fireworks:{Flight:2,Explosions:[{Type:1,Flicker:1,Trail:1,Colors:[I;11743532,15435844,15790320],FadeColors:[I;8073150,12801229]}]}}}}");

        //Display player's position
        final JSONObject root = Utils.createText(player + " is at (", "white");
        final JSONArray extra = new JSONArray();
        extra.add(Utils.createText(String.valueOf(x), "yellow"));
        extra.add(Utils.createText(", ", ""));
        extra.add(Utils.createText(String.valueOf(y), "yellow"));
        extra.add(Utils.createText(", ", ""));
        extra.add(Utils.createText(String.valueOf(z), "yellow"));
        extra.add(Utils.createText(")", ""));
        root.put("extra", extra);
        serverShell.tellraw("@a", root);
    }

    @Override
    public String getCommand() {
        return "here";
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Spawn a firework at your location and print your coordinates in chat.";
    }
}
