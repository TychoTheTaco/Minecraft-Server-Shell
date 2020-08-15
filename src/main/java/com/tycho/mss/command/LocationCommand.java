package com.tycho.mss.command;

import com.tycho.mss.Context;
import com.tycho.mss.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationCommand extends Command {

    public LocationCommand(){
        super("location");
    }

    @Override
    public void execute(String player, Context context, String... parameters) throws Exception {
        if (parameters.length < 1){
            throw new InvalidParametersException();
        }

        //Determine action
        final String type = parameters[0];
        if ("save".equals(type)){
            //Get player position
            Matcher matcher = context.awaitMatch("data get entity " + player + " Pos", getPositionPattern(player)).waitFor();
            final int x = (int) Double.parseDouble(matcher.group("x"));
            final int y = (int) Double.parseDouble(matcher.group("y"));
            final int z = (int) Double.parseDouble(matcher.group("z"));

            //Get player dimension
            matcher = context.awaitMatch("data get entity " + player + " Dimension", getDimensionPattern(player)).waitFor();
            final String dimension = matcher.group("dimension");

            //Notes
            final StringBuilder stringBuilder = new StringBuilder();
            for (int i = 1; i < parameters.length; i++){
                stringBuilder.append(parameters[i]).append(' ');
            }

            final SavedLocation savedLocation = new SavedLocation(x, y, z, stringBuilder.toString().trim());
            savedLocation.setDimension(dimension);

            context.getPlayer(player).getSavedLocations().add(savedLocation);
            context.getPlayerDatabaseManager().save(context.getPlayer(player));

            final JSONObject root = Utils.createText("Location saved!", "green");
            context.tellraw(player, root);
        }else if ("list".equals(type)){
            final List<SavedLocation> savedLocations = context.getPlayer(player).getSavedLocations();
            if (savedLocations.isEmpty()){
                context.tellraw(player, Utils.createText("You haven't saved any locations!", "white"));
            }else{
                final JSONObject root = Utils.createText("", "white");
                final JSONArray extra = new JSONArray();
                for (int i = 0; i < savedLocations.size(); i++){
                    //Index
                    extra.add("[" + (i + 1) + "] ");

                    //Dimension
                    final String dimension = savedLocations.get(i).getDimension();
                    extra.add("[");
                    if (dimension == null){
                        extra.add(Utils.createText("DIM ?", "gray"));
                    }else if ("overworld".equals(dimension)){
                        extra.add(Utils.createText("Overworld", "green"));
                    }else if ("the_nether".equals(dimension)){
                        extra.add(Utils.createText("Nether", "red"));
                    }else if ("the_end".equals(dimension)){
                        extra.add(Utils.createText("End", "yellow"));
                    }
                    extra.add("]");

                    //Position
                    extra.add(" (");
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
                context.tellraw(player, root);
            }
        }else if ("remove".equals(type)){
            if (parameters.length < 2){
                throw new InvalidParametersException();
            }

            try {
                final int index = Integer.parseInt(parameters[1]) - 1;
                context.getPlayer(player).getSavedLocations().remove(index);
                final JSONObject root = Utils.createText("Location removed!", "green");
                context.tellraw(player, root);
                context.getPlayerDatabaseManager().save(context.getPlayer(player));
            }catch (NumberFormatException e){
                throw new InvalidParametersException();
            }
        }else{
            throw new InvalidParametersException();
        }
    }

    @Override
    public String getFormat() {
        return "save [<notes>] | list | remove <index>";
    }

    @Override
    public String getDescription() {
        return "Saves your current location along with a note so you can find it again later.";
    }

    private static Pattern getPositionPattern(final String username){
        return Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}] \\[Server thread\\/INFO]: " + username + " has the following entity data: \\[(?<x>-?\\d+\\.\\d+)d, (?<y>-?\\d+\\.\\d+)d, (?<z>-?\\d+\\.\\d+)d]");
    }

    private static Pattern getDimensionPattern(final String username){
        return Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}] \\[Server thread\\/INFO]: " + username + " has the following entity data: \"\\w+:(?<dimension>\\w+)\"$");
    }
}
