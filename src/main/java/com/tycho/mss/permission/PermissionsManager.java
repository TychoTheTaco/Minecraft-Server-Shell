package com.tycho.mss.permission;

import com.tycho.mss.Player;
import com.tycho.mss.ServerShell;
import com.tycho.mss.command.Command;
import netscape.javascript.JSObject;
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
import java.util.*;

import static com.tycho.mss.MinecraftServerShell.PRIVATE_DIR;

public class PermissionsManager {

    private final Path permissionsFile;

    private final Map<Role, Set<String>> permissions = new HashMap<>();

    private final Map<String, Set<String>> special = new HashMap<>();

    public PermissionsManager(final Path directory) {
        this.permissionsFile = PRIVATE_DIR.resolve("permissions.json");
    }

    public void load() {
        try {
            final String string = new String(Files.readAllBytes(permissionsFile));
            final JSONObject root = (JSONObject) new JSONParser().parse(string);

            //Permissions
            final JSONArray permissionsArray = (JSONArray) root.get("permissions");
            for (Object roleObject : permissionsArray){
                final Role role = new Role((JSONObject) roleObject);
                final JSONArray playersArray = (JSONArray) ((JSONObject) roleObject).get("players");
                if (playersArray.isEmpty()){
                    addRole(role);
                }else{
                    for (Object playerObject : playersArray){
                        assignNoSave((String) playerObject, role);
                    }
                }
            }
        } catch (NoSuchFileException e){
            //Ignore
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        System.out.println("AFTER LOAD: " + this.permissions);
    }

    public void addRole(final Role role){
        addRoleNoSave(role);
        save();
    }

    public void addRoleNoSave(final Role role){
        if (!isUnique(role)){
            System.out.println("ROLE IS NOT UNIQUE: " + role);
            return;
        }
        this.permissions.put(role, new HashSet<>());
    }

    private boolean isUnique(final Role role){
        for (Role r : this.permissions.keySet()){
            if (r.getName().equals(role.getName())) return false;
        }
        return true;
    }

    public void removeRole(final Role role){
        this.permissions.remove(role);
        save();
    }

    public void save() {
        System.out.println("SAVE: " + permissions);

        try {
            Files.createDirectories(permissionsFile.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(permissionsFile.toFile()))) {
            final JSONObject root = new JSONObject();
            final JSONArray permissionsArray = new JSONArray();
            for (Role role : permissions.keySet()) {
                final JSONObject roleObject = role.toJson();
                final JSONArray playersArray = new JSONArray();
                for (String player : permissions.get(role)) {
                    playersArray.add(player);
                }
                roleObject.put("players", playersArray);
                permissionsArray.add(roleObject);
            }
            root.put("permissions", permissionsArray);
            bufferedWriter.write(root.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAuthorized(final String player, final Command command) {
        final List<Role> roles = getRoles(player);
        for (Role role : roles) {
            if (role.getCommands().contains(command.getClass())) return true;
        }
        return false;
    }

    private List<Role> getRoles(final String player) {
        final List<Role> roles = new ArrayList<>();
        for (Role role : permissions.keySet()) {
            if (permissions.get(role).contains(player)) roles.add(role);
        }
        return roles;
    }

    public List<Role> getRoles(){
        return new ArrayList<>(permissions.keySet());
    }

    public List<String> getPlayers(final Role role){
        return new ArrayList<>(this.permissions.getOrDefault(role, new HashSet<>()));
    }

    public void assign(final String player, final Role role) {
        assignNoSave(player, role);
        save();
    }

    public void assignNoSave(final String player, final Role role) {
        if (!permissions.containsKey(role) && !isUnique(role)){
            System.out.println("ROLE IS NOT UNIQUE: " + role);
            return;
        }
        permissions.computeIfAbsent(role, k -> new HashSet<>());
        permissions.get(role).add(player);
    }

    public void unassign(final String player, final Role role) {
        permissions.computeIfAbsent(role, k -> new HashSet<>());
        permissions.get(role).remove(player);
        save();
    }
}
