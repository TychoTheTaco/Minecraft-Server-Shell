package com.tycho.mss;

import com.tycho.mss.util.Utils;
import easytasks.Task;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;

import java.io.File;
import java.io.IOException;

public class RestoreBackupTask extends Task {

    private final File source;

    private final File destination;

    private boolean isSuccessful = false;

    public RestoreBackupTask(final File source, final File destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    protected void run() {
        try {
            //Delete all files in destination directory
            Utils.clean(destination);

            //Unzip backup into server directory
            Utils.unzip(source, destination);

            this.isSuccessful = true;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }
}
