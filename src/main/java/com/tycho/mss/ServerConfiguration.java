package com.tycho.mss;

import com.tycho.mss.module.permission.PermissionsManager;
import com.tycho.mss.util.Utils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ServerConfiguration {

    private final UUID id;

    //TODO: Last started time

    private String name;

    private Path jar;

    private String launchOptions = "";

    private final PermissionsManager permissionsManager;

    private String minecraftVersion = null;

    private Path backupDirectory = null;

    public ServerConfiguration(final UUID id, final String name, final Path jar){
        this.id = id;
        this.name = name;
        this.jar = jar;
        this.permissionsManager = new PermissionsManager();
    }

    public ServerConfiguration(final String name, final Path jar){
        this(UUID.randomUUID(), name, jar);
    }

    public ServerConfiguration(final JSONObject json){
        this.id = UUID.fromString((String) json.get("id"));
        this.name = (String) json.get("name");
        this.jar = Paths.get((String) json.get("jar"));
        this.permissionsManager = new PermissionsManager((JSONObject) json.get("permissions"));

        try {
            this.backupDirectory = Paths.get((String) json.get("backupDirectory"));
        }catch (Exception e){
            System.err.println("Failed to get preference: backupDirectory");
            this.backupDirectory = null;
        }
    }

    public JSONObject toJson(){
        final JSONObject root = new JSONObject();
        root.put("id", id.toString());
        root.put("name", name);
        root.put("jar", jar.toString());
        root.put("launch_options", launchOptions);
        root.put("permissions", permissionsManager.toJson());
        if (backupDirectory != null) root.put("backupDirectory", backupDirectory.toString());
        return root;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Path getJar() {
        return jar;
    }

    public String getLaunchOptions() {
        return launchOptions;
    }

    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    public String getMinecraftVersion() {
        //Try to read minecraft version from server JAR file
        if (minecraftVersion == null){
            try {
                final JarURLConnection connection = (JarURLConnection) new URL("jar:file:/" + this.jar.toAbsolutePath().toString() + "!/version.json").openConnection();
                final JSONObject jsonObject = Utils.readStreamAsJson(connection.getInputStream());
                minecraftVersion = (String) jsonObject.get("id");
            }catch (IOException | ParseException e){
                return "Unknown";
            }
        }
        return minecraftVersion;
    }
}
