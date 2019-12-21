package com.tycho.mss.command;

import com.tycho.mss.Colors;
import com.tycho.mss.Player;
import com.tycho.mss.ServerShell;
import com.tycho.mss.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class GiveRandomItemCommand extends Command {

    private static final Random RANDOM = new Random();

    private final List<String> ids = new ArrayList<>();

    public GiveRandomItemCommand(final File idsFile) throws IOException {
        super("mcrandom");
        final BufferedReader bufferedReader = new BufferedReader(new FileReader(idsFile));
        String line;
        while ((line = bufferedReader.readLine()) != null){
            ids.add(line);
        }
        bufferedReader.close();
    }

    @Override
    public void execute(String player, ServerShell serverShell, String... parameters) throws Exception {
        if (parameters.length < 2) throw new InvalidParametersException();

        final String targetPlayer = parameters[0];
        final int maxCount = Integer.parseInt(parameters[1]);

        //Check for player selectors
        if (targetPlayer.equals("@a")){
            final List<Player> players = serverShell.getPlayers();
            for (Player p : players){
                serverShell.tellraw("@a", give(p.getUsername(), maxCount, serverShell));
            }
            return;
        }

        serverShell.tellraw("@a", give(targetPlayer, maxCount, serverShell));
    }

    @Override
    public String getFormat() {
        return "<player> <maxCount>";
    }

    @Override
    public String getDescription() {
        return "Gives <player> up to <maxCount> random items.";
    }

    private JSONObject give(final String player, final int maxCount, final ServerShell serverShell) throws IOException {
        final String item = this.ids.get(RANDOM.nextInt(ids.size()));
        final int count = RANDOM.nextInt(maxCount) + 1;
        serverShell.execute("give " + player + " " + item + " " + count);

        //Create message
        final JSONObject root = Utils.createText("Gave ", "white");
        final JSONArray extra = new JSONArray();
        extra.add(Utils.createText(String.valueOf(count), "dark_green"));
        extra.add(Utils.createText(" " + item, Colors.ITEM_COLOR));
        extra.add(Utils.createText(" to ", ""));
        extra.add(Utils.createText(player, Colors.PLAYER_COLOR));
        root.put("extra", extra);
        return root;
    }
}
