package com.tycho.mss.module.permission;

import com.tycho.mss.command.Command;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class Role {

    private String name;

    private Set<Class<? extends Command>> commands = new HashSet<>();

    private boolean autoAssign = false;

    public Role(final String name, Class<? extends Command>... classes) {
        this(name, Arrays.asList(classes), false);
    }

    public Role(final String name, final List<Class<? extends Command>> commands, final boolean autoAssign){
        this.name = name;
        this.commands.addAll(commands);
        this.autoAssign = autoAssign;
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

    public void setAutoAssign(boolean autoAssign) {
        this.autoAssign = autoAssign;
    }

    public boolean isAutoAssign() {
        return autoAssign;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return name.equals(role.getName());
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

    public void setName(String name) {
        this.name = name;
    }
}
