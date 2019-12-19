package com.tycho.mss.permission;

import com.tycho.mss.command.Command;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Role {

    private final String name;

    private Set<Class<? extends Command>> commands = new HashSet<>();

    public Role(final String name, Class<? extends Command>... classes) {
        this.name = name;
        this.commands.addAll(Arrays.asList(classes));
    }

    public Role(final JSONObject jsonObject){
        this.name = (String) jsonObject.get("name");
        for (Object object : (JSONArray) jsonObject.get("commands")){
            try {
                this.commands.add((Class<? extends Command>) Class.forName("com.tycho.mss.command." + object));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public String getName() {
        return name;
    }

    public Set<Class<? extends Command>> getCommands() {
        return commands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public JSONObject toJson(){
        final JSONObject root = new JSONObject();
        root.put("name", name);
        final JSONArray commandsArray = new JSONArray();
        for (Class<? extends Command> cls : this.commands){
            commandsArray.add(cls.getSimpleName());
        }
        root.put("commands", commandsArray);
        return root;
    }

    @Override
    public String toString() {
        return "Role{" +
                "name='" + name + '\'' +
                ", commands=" + commands +
                '}';
    }
}
