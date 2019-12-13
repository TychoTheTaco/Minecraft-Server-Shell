package com.tycho.mss.command;

import com.tycho.mss.BackupTask;
import com.tycho.mss.ServerShell;
import com.tycho.mss.util.Preferences;
import com.tycho.mss.util.UiUpdater;
import com.tycho.mss.util.Utils;
import easytasks.ITask;
import easytasks.TaskAdapter;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class BackupCommand extends Command {

    public BackupCommand() {
        super("backup");
    }

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

            //Save the world
            serverShell.tellraw("@a", Utils.createText("Saving world...", "dark_aqua"));
            serverShell.awaitResult("save-all flush", Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}] \\[Server thread\\/INFO]: Saved the game$"));
            serverShell.tellraw("@a", Utils.createText("Creating backup...", "dark_aqua"));

            final BackupTask backupTask = new BackupTask(new File((String) Preferences.getPreferences().get("server_jar")).getParentFile().toPath(), new File(Preferences.getBackupDirectory() + File.separator + System.currentTimeMillis() + ".zip").toPath());
            final UiUpdater backupButtonUpdater = new UiUpdater(1000) {

                private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##%");

                @Override
                protected void onUiUpdate() {
                    try {
                        serverShell.tellraw("@a", Utils.createText(DECIMAL_FORMAT.format(backupTask.getProgress()), "dark_aqua"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            backupTask.addTaskListener(new TaskAdapter() {

                @Override
                public void onTaskStarted(ITask task) {
                    backupButtonUpdater.startOnNewThread();
                }

                @Override
                public void onTaskStopped(ITask task) {
                    JSONObject root;
                    try {
                        backupButtonUpdater.stopAndWait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (backupTask.isSuccessful()) {
                        root = Utils.createText("Backup successful!", "green");
                    } else {
                        root = Utils.createText("Backup failed!", "red");
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
    public String getFormat() {
        return "create | restore | list";
    }

    @Override
    public String getDescription() {
        return "Create a backup of the server.";
    }
}
