package com.tycho.mss;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerManager {

    private static final Path SAVE_PATH = MinecraftServerManager.PRIVATE_DIR.resolve("servers.json");

    private static final Map<UUID, ServerConfiguration> configurations = new HashMap<>();

    private static final Map<UUID, ServerShell> shells = new HashMap<>();

    public static void init(){
        try {
            final String string = new String(Files.readAllBytes(SAVE_PATH));
            final JSONObject root = (JSONObject) new JSONParser().parse(string);

            final JSONArray array = (JSONArray) root.get("servers");
            for (Object object : array){
                add(new ServerConfiguration((JSONObject) object));
            }
        } catch (NoSuchFileException e){
            //Ignore
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static void save(){
        try {
            Files.createDirectories(SAVE_PATH.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(SAVE_PATH.toFile()))) {
            final JSONObject root = new JSONObject();
            final JSONArray array = new JSONArray();
            for (ServerConfiguration configuration : configurations.values()) {
                array.add(configuration.toJson());
            }
            root.put("servers", array);
            bufferedWriter.write(root.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void add(final ServerConfiguration configuration){
        configurations.put(configuration.getId(), configuration);
    }

    public static Map<UUID, ServerConfiguration> getConfigurations() {
        return configurations;
    }

    public static Map<UUID, ServerShell> getShells() {
        return shells;
    }

    public static ServerShell getOrCreate(ServerConfiguration configuration){
        if (shells.containsKey(configuration.getId())){
            return shells.get(configuration.getId());
        }
        final ServerShell shell = new ServerShell(configuration);
        shells.put(configuration.getId(), shell);
        return shell;
    }

    public static void stopAll(){
        for (ServerShell shell : shells.values()){
            shell.stop();
        }
    }
}
