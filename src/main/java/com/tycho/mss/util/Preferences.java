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

    private static final String PREF_SERVER_JAR = "server_jar";
    private static final String PREF_LAUNCH_OPTIONS = "launch_options";
    private static final String PREF_BACKUP_DIR = "backup_directory";

    public static void load(){
        if (Files.notExists(PREFERENCES_FILE)){
            System.out.println("Preferences file does not exist! Creating a new one with defaults...");

            //Try to find a server JAR in the current directory
            for (Path path : Paths.get(System.getProperty("user.dir"))){
                System.out.println("PATH: " + path);
                //TODO: Check potential server jar for META_INF?
                final String name = path.getFileName().toString();
                if (name.contains("server") && name.endsWith("jar")){
                    setServerJar(path);
                }
            }

            //Set default values
            setLaunchOptions("");

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

    public static Path getServerJar(){
        final String string = (String) preferences.get(PREF_SERVER_JAR);
        if (string == null) return null;
        return Paths.get(string);
    }

    public static void setServerJar(final Path path){
        preferences.put(PREF_SERVER_JAR, path.toString());
    }

    public static String[] getLaunchOptions(){
        return ((String) preferences.get(PREF_LAUNCH_OPTIONS)).split(" ");
    }

    public static void setLaunchOptions(final String string){
        preferences.put(PREF_LAUNCH_OPTIONS, string);
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
