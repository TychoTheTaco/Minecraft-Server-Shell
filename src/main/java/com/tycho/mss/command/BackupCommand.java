package com.tycho.mss.command;

import com.tycho.mss.BackupTask;
import com.tycho.mss.ServerShell;
import com.tycho.mss.util.Preferences;
import com.tycho.mss.util.Utils;
import easytasks.ITask;
import easytasks.TaskAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;

public class BackupCommand extends Command {

    @Override
    public void execute(String player, ServerShell serverShell, String... parameters) throws Exception {
        if (parameters.length < 1) {
            throw new InvalidParametersException();
        }

        if ("create".equals(parameters[0])) {
            if (Preferences.getBackupDirectory() == null){
                final JSONObject root = Utils.createText("Cannot create backup: No backup directory is specified in the settings!", "red");
                serverShell.tellraw(player, root);
                return;
            }
            final BackupTask backupTask = new BackupTask(new File((String) Preferences.getPreferences().get("server_jar")).getParentFile(), new File(Preferences.getBackupDirectory() + File.separator + System.currentTimeMillis() + ".zip"));
            backupTask.addTaskListener(new TaskAdapter() {
                @Override
                public void onTaskStarted(ITask task) {
                    JSONObject root = Utils.createText("Creating backup...", "white");
                    try {
                        serverShell.tellraw(player, root);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onTaskStopped(ITask task) {
                    JSONObject root;
                    if (backupTask.isSuccessful()) {
                        System.out.println("BACKUP SUCCESSFUL");
                        root = Utils.createText("Backup Successful!", "green");
                    } else {
                        System.out.println("BACKUP UNSUCESSFUL");
                        root = Utils.createText("Backup Unsuccessful!", "red");
                    }
                    try {
                        serverShell.tellraw(player, root);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            backupTask.startOnNewThread();
        }else if ("restore".equals(parameters[0])){
            System.out.println("Restore");
        }else if ("list".equals(parameters[0])){
            System.out.println("Restore");
        }
    }

    @Override
    public String getCommand() {
        return "backup";
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Create a backup of the server.";
    }
}
