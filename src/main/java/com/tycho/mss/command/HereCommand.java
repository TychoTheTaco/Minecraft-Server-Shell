package com.tycho.mss.command;

import com.tycho.mss.Colors;
import com.tycho.mss.Context;
import com.tycho.mss.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HereCommand extends Command {

    private static final Pattern POSITION_PATTERN = Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}] \\[Server thread\\/INFO]: (?<player>.+) has the following entity data: \\[(?<x>-?\\d+)\\.\\d+d, (?<y>-?\\d+)\\.\\d+d, (?<z>-?\\d+)\\.\\d+d]");

    public HereCommand(){
        super("here");
    }

    @Override
    public void execute(String player, Context context, String... parameters) throws Exception {
        //Get player position
        final Matcher matcher = context.awaitMatch("data get entity " + player + " Pos", POSITION_PATTERN).requiresPlayersOnline(player).waitFor();
        final int x = Integer.parseInt(matcher.group("x"));
        final int y = Integer.parseInt(matcher.group("y"));
        final int z = Integer.parseInt(matcher.group("z"));

        //Spawn firework rocket
        context.execute("execute at " + player + " run summon minecraft:firework_rocket ~ ~3 ~ {LifeTime:30,FireworksItem:{id:firework_rocket,Count:1,tag:{Fireworks:{Flight:2,Explosions:[{Type:1,Flicker:1,Trail:1,Colors:[I;11743532,15435844,15790320],FadeColors:[I;8073150,12801229]}]}}}}");

        //Apply glowing effect
        context.execute("effect give " + player + " minecraft:glowing 5");

        //Display player's position
        final JSONObject root = Utils.createText(player, Colors.PLAYER_COLOR);
        final JSONArray extra = new JSONArray();
        extra.add(Utils.createText(" is at (", "white"));

        //Player position
        extra.add(Utils.createText(String.valueOf(x), "yellow"));
        extra.add(Utils.createText(", ", "white"));
        extra.add(Utils.createText(String.valueOf(y), "yellow"));
        extra.add(Utils.createText(", ", "white"));
        extra.add(Utils.createText(String.valueOf(z), "yellow"));
        extra.add(Utils.createText(")", "white"));

        //Notes
        if (parameters.length > 0){
            extra.add(Utils.createText(" Notes:", "white"));
            for (String string : parameters){
                extra.add(Utils.createText(' ' + string, "green"));
            }
        }

        root.put("extra", extra);
        context.tellraw("@a", root);
    }

    @Override
    public String getFormat() {
        return "[<notes>]";
    }

    @Override
    public String getDescription() {
        return "Spawn a firework at your location and print your coordinates in chat.";
    }
}
