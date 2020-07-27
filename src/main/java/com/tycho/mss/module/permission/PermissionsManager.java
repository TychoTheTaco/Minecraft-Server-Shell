package com.tycho.mss.module.permission;

import com.tycho.mss.ServerManager;
import com.tycho.mss.command.Command;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class PermissionsManager {

    private final Map<Role, Set<String>> permissions = new HashMap<>();

    private final Map<String, Set<String>> special = new HashMap<>();

    public PermissionsManager(){

    }

    public PermissionsManager(final JSONObject jsonObject) {
        if (jsonObject.containsKey("permissions")){
            final JSONArray permissionsArray = (JSONArray) jsonObject.get("permissions");
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
        }
    }

    public void addRole(final Role role){
        if (!isUnique(role)){
            //System.out.println("ROLE IS NOT UNIQUE: " + role);
            return;
        }
        this.permissions.put(role, new HashSet<>());
        ServerManager.save();
    }

    private boolean isUnique(final Role role){
        for (Role r : this.permissions.keySet()){
            if (r.getName().equals(role.getName())) return false;
        }
        return true;
    }

    public void removeRole(final Role role){
        this.permissions.remove(role);
    }

    public JSONObject toJson(){
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
        return root;
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

    private void assignNoSave(final String player, final Role role) {
        if (!permissions.containsKey(role) && !isUnique(role)){
            //System.out.println("ROLE IS NOT UNIQUE: " + role);
            return;
        }
        permissions.computeIfAbsent(role, k -> new HashSet<>());
        permissions.get(role).add(player);
    }

    public void assign(final String player, final Role role){
        assignNoSave(player, role);
        ServerManager.save();
    }

    public void unassign(final String player, final Role role) {
        permissions.computeIfAbsent(role, k -> new HashSet<>());
        permissions.get(role).remove(player);
        ServerManager.save();
    }
}
