package com.tycho.mss;

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

import static com.tycho.mss.MinecraftServerManager.PRIVATE_DIR;

public class PlayerDatabase {

    private final Path database;

    public PlayerDatabase(final Path directory){
        this.database = directory.resolve(".mss").resolve("players.json");
    }

    public void save(final Player player){
        createIfNotExists();
        try {
            final String string = new String(Files.readAllBytes(database));
            final JSONObject players = (JSONObject) new JSONParser().parse(string);
            players.put(player.getId().toString(), player.toJsonObject());

            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(database.toFile()));
            bufferedWriter.write(players.toString());
            bufferedWriter.close();
        }catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }

    private void createIfNotExists(){
        if (!Files.exists(database)){
            database.toFile().getParentFile().mkdirs();
            try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(database.toFile()))){
                bufferedWriter.write(new JSONObject().toString());
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Load player data from the database into this player object
     * @param player
     */
    public void load(final Player player){
        createIfNotExists();
        try {
            final String string = new String(Files.readAllBytes(database));
            final JSONObject players = (JSONObject) new JSONParser().parse(string);

            System.out.println("PLAYERS: " + players);
            final JSONObject target = (JSONObject) players.get(player.getId().toString());
            System.out.println("TARGET: " + target);
            if (target == null) return;

            player.load(target);
        }catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }
}
