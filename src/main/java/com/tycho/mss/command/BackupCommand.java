package com.tycho.mss.command;

import com.tycho.mss.*;
import com.tycho.mss.util.Preferences;
import com.tycho.mss.util.UiUpdater;
import com.tycho.mss.util.Utils;
import easytasks.ITask;
import easytasks.TaskAdapter;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BackupCommand extends Command {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

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
            serverShell.tellraw("@a", Utils.createText("Saving world...", Colors.STATUS_MESSAGE_COLOR));
            serverShell.awaitResult("save-all flush", Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}] \\[Server thread\\/INFO]: Saved the game$"));
            serverShell.tellraw("@a", Utils.createText("Creating backup...", Colors.STATUS_MESSAGE_COLOR));

            final BackupTask backupTask = new BackupTask(new File((String) Preferences.getPreferences().get("server_jar")).getParentFile().toPath(), new File(Preferences.getBackupDirectory() + File.separator + System.currentTimeMillis() + ".zip").toPath());
            final UiUpdater progressUpdater = new UiUpdater(3000) {

                private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##%");

                @Override
                protected void onUiUpdate() {
                    serverShell.tellraw("@a", Utils.createText(DECIMAL_FORMAT.format(backupTask.getProgress()), Colors.STATUS_MESSAGE_COLOR));
                }
            };
            backupTask.addTaskListener(new TaskAdapter() {

                @Override
                public void onTaskStarted(ITask task) {
                    progressUpdater.startOnNewThread();
                }

                @Override
                public void onTaskStopped(ITask task) {
                    JSONObject root;
                    try {
                        progressUpdater.stopAndWait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (backupTask.isSuccessful()) {
                        root = Utils.createText("Backup successful!", "green");
                    } else {
                        root = Utils.createText("Backup failed!", "red");
                    }
                    serverShell.tellraw(player, root);
                }
            });
            backupTask.startOnNewThread();
        }else if ("restore".equals(parameters[0])){
            if (parameters.length < 2) throw new InvalidParametersException();

            try {
                final int index = Integer.parseInt(parameters[1]);
                final Path backup = getBackups().get(index);

                //serverShell.tellraw(player, Utils.createText("Are you sure you want to restore the backup from " + backup.toFile().getName() + "?","white"));
                serverShell.restore(backup);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }else if ("list".equals(parameters[0])){
            final Path backupsDirectory = Preferences.getBackupPath();
            if (backupsDirectory == null){
                return;
            }
            final List<Path> backups = getBackups();
            for (int i = 0; i < backups.size(); i++){
                serverShell.tellraw(player, Utils.createText("[" + i + "]: " + SIMPLE_DATE_FORMAT.format(new Date(backups.get(i).toFile().lastModified())), "white"));
            }
        }else{
            throw new InvalidParametersException();
        }
    }

    @Override
    public String getFormat() {
        return "create | restore <index> | list";
    }

    @Override
    public String getDescription() {
        return "Create or restore a backup of the server.";
    }

    private List<Path> getBackups() throws IOException {
        return Files.walk(Preferences.getBackupPath()).filter(Files::isRegularFile).sorted(Comparator.comparing(a -> ((Path) a).toFile().getName()).reversed()).collect(Collectors.toList());
    }
}
