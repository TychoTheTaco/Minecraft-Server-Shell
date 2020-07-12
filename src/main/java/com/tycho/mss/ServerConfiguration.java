package com.tycho.mss;

import org.json.simple.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ServerConfiguration {

    private final UUID id;

    private String name;

    private Path jar;

    public ServerConfiguration(final UUID id, final String name, final Path jar){
        this.id = id;
        this.name = name;
        this.jar = jar;
    }

    public ServerConfiguration(final String name, final Path jar){
        this(UUID.randomUUID(), name, jar);
    }

    public ServerConfiguration(final JSONObject json){
        this.id = UUID.fromString((String) json.get("id"));
        this.name = (String) json.get("name");
        this.jar = Paths.get((String) json.get("jar"));
    }

    public JSONObject toJson(){
        final JSONObject root = new JSONObject();
        root.put("id", id.toString());
        root.put("name", name);
        root.put("jar", jar.toString());
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
}
