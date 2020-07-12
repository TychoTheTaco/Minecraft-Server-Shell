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
import java.nio.file.Paths;

public class Preferences {

    private static JSONObject preferences = new JSONObject();

    private static final Path PREFERENCES_FILE = MinecraftServerManager.PRIVATE_DIR.resolve("preferences.json");

    private static final String PREF_BACKUP_DIR = "backup_directory";

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

    public static Path getBackupDirectory(){
        final String string = (String) preferences.get(PREF_BACKUP_DIR);
        if (string == null) return null;
        return Paths.get(string);
    }

    public static void setBackupDirectory(final Path directory){
        preferences.put(PREF_BACKUP_DIR, directory.toString());
    }
}
