package com.tycho.mss;

import com.tycho.mss.command.Command;
import com.tycho.mss.command.SavedLocation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.tycho.mss.MinecraftServerShell.PRIVATE_DIR;

public class PermissionsManager {

    private static final Path DATABASE = PRIVATE_DIR.resolve("permissions.json");

    public static void save(final List<ServerShell.PermissionGroup> permissionGroups, final Map<String, ServerShell.PermissionGroup> permissions){
        createIfNotExists();
        try {
            final JSONObject root = new JSONObject();

            //Save groups
            final JSONArray permissionGroupsArray = new JSONArray();
            for (ServerShell.PermissionGroup permissionGroup : permissionGroups){
                final JSONObject permissionGroupObject = new JSONObject();
                permissionGroupObject.put("name", permissionGroup.getName());

                final JSONArray array = new JSONArray();
                for (Class<? extends Command> command : permissionGroup.getCommands()){
                    array.add(command);
                }
                permissionGroupObject.put("commands", array);

                permissionGroupsArray.add(permissionGroupObject);
            }

            //Save players
            for (String player : permissions.keySet()){
                final JSONObject playerObject = new JSONObject();



                root.put(player, playerObject);
            }


            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(DATABASE.toFile()));
            bufferedWriter.write(root.toString());
            bufferedWriter.close();
        }catch (IOException /*| ParseException*/ e){
            e.printStackTrace();
        }
    }

    private static void createIfNotExists(){
        if (!Files.exists(DATABASE)){
            DATABASE.toFile().getParentFile().mkdirs();
            try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(DATABASE.toFile()))){
                bufferedWriter.write(new JSONObject().toString());
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void get(final Player player){
        createIfNotExists();
        try {
            final String string = new String(Files.readAllBytes(DATABASE));
            final JSONObject players = (JSONObject) new JSONParser().parse(string);

            System.out.println("PLAYERS: " + players);
            final JSONObject target = (JSONObject) players.get(player.getId().toString());
            System.out.println("TARGET: " + target);
            if (target == null) return;

            final JSONArray savedLocationsArray = (JSONArray) target.get("savedLocations");
            for (Object object : savedLocationsArray){
                final JSONObject location = (JSONObject) object;
                player.getSavedLocations().add(new SavedLocation(
                        ((Long) location.get("x")).intValue(),
                        ((Long) location.get("y")).intValue(),
                        ((Long) location.get("z")).intValue(),
                        (String) location.get("description"))
                );
            }

        }catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }
}
