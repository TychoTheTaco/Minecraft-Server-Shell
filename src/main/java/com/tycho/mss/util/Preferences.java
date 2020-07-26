package com.tycho.mss.util;

import com.tycho.mss.MinecraftServerManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Preferences {

    private static JSONObject preferences = new JSONObject();

    private static final Path PREFERENCES_FILE = MinecraftServerManager.PRIVATE_DIR.resolve("preferences.json");

    public static void load(){
        if (Files.notExists(PREFERENCES_FILE)){
            System.out.println("Preferences file does not exist! Creating a new one with defaults...");

            try {
                Files.createDirectories(PREFERENCES_FILE.getParent());
            } catch (IOException e) {
                e.printStackTrace();
            }

            save();
        }

        //Load preferences
        try {
            final String string = new String(Files.readAllBytes(PREFERENCES_FILE));
            final JSONObject root = (JSONObject) new JSONParser().parse(string);
            preferences = root;
        }catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }

    public static void save(){
        try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(PREFERENCES_FILE.toFile()))){
            bufferedWriter.write(preferences.toString());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static JSONObject getPreferences() {
        return preferences;
    }
}
