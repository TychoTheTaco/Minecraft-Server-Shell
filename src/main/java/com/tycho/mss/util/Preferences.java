package com.tycho.mss.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Preferences {

    private JSONObject preferences = new JSONObject();

    private static final File PREFERENCES_FILE = new File(System.getProperty("user.dir") + File.separator + "mss_config.json");

    public void load(){
        try {
            final String string = new String(Files.readAllBytes(Paths.get(PREFERENCES_FILE.getAbsolutePath())));
            final JSONObject root = (JSONObject) new JSONParser().parse(string);
            this.preferences = root;
        }catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }

    public void save(){
        try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(PREFERENCES_FILE))){
            bufferedWriter.write(this.preferences.toString());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public JSONObject getPreferences() {
        return preferences;
    }
}
