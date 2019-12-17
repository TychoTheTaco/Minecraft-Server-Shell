package com.tycho.mss.command;

import com.tycho.mss.PlayerDatabaseManager;
import com.tycho.mss.ServerShell;
import com.tycho.mss.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationCommand extends Command {

    private static final Pattern POSITION_PATTERN = Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}] \\[Server thread\\/INFO]: (?<player>.+) has the following entity data: \\[(?<x>-?\\d+)\\.\\d+d, (?<y>-?\\d+)\\.\\d+d, (?<z>-?\\d+)\\.\\d+d]");

    public LocationCommand(){
        super("location");
    }

    @Override
    public void execute(String player, ServerShell serverShell, String... parameters) throws Exception {
        if (parameters.length < 1){
            throw new InvalidParametersException();
        }

        //Determine action
        final String type = parameters[0];
        if ("save".equals(type)){
            //Get player position
            final Matcher matcher = serverShell.awaitResult("data get entity " + player + " Pos", POSITION_PATTERN);
            final int x = Integer.parseInt(matcher.group("x"));
            final int y = Integer.parseInt(matcher.group("y"));
            final int z = Integer.parseInt(matcher.group("z"));

            //Notes
            final StringBuilder stringBuilder = new StringBuilder();
            for (int i = 1; i < parameters.length; i++){
                stringBuilder.append(parameters[i]).append(' ');
            }

            serverShell.getPlayer(player).getSavedLocations().add(new SavedLocation(x, y, z, stringBuilder.toString().trim()));
            PlayerDatabaseManager.save(serverShell.getPlayer(player));

            final JSONObject root = Utils.createText("Location saved!", "green");
            serverShell.tellraw(player, root);
        }else if ("list".equals(type)){
            final JSONObject root = Utils.createText("Saved Locations:\n", "white");
            final JSONArray extra = new JSONArray();

            final List<SavedLocation> savedLocations = serverShell.getPlayer(player).getSavedLocations();
            for (int i = 0; i < savedLocations.size(); i++){
                extra.add("[" + i + "]: (");
                extra.add(Utils.createText(String.valueOf(savedLocations.get(i).getX()), "yellow"));
                extra.add(Utils.createText(", ", "white"));
                extra.add(Utils.createText(String.valueOf(savedLocations.get(i).getY()), "yellow"));
                extra.add(Utils.createText(", ", "white"));
                extra.add(Utils.createText(String.valueOf(savedLocations.get(i).getZ()), "yellow"));
                extra.add(Utils.createText("): ", "white"));
                extra.add(Utils.createText(savedLocations.get(i).getDescription(), "green"));
                extra.add(Utils.createText("\n", "white"));
            }
            extra.remove(extra.size() - 1);

            root.put("extra", extra);
            serverShell.tellraw(player, root);
        }else if ("remove".equals(type)){
            if (parameters.length < 2){
                throw new InvalidParametersException();
            }

            try {
                final int index = Integer.parseInt(parameters[1]);
                serverShell.getPlayer(player).getSavedLocations().remove(index);
                final JSONObject root = Utils.createText("Location removed!", "green");
                serverShell.tellraw(player, root);
                PlayerDatabaseManager.save(serverShell.getPlayer(player));
            }catch (NumberFormatException e){
                throw new InvalidParametersException();
            }
        }
    }

    @Override
    public String getFormat() {
        return "<save [<notes>] | list | remove <index>>";
    }

    @Override
    public String getDescription() {
        return "Saves your current location along with a note so you can find it again later.";
    }
}
