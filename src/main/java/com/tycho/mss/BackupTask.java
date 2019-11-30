package com.tycho.mss;

import com.tycho.mss.util.Utils;
import easytasks.Task;

import java.io.File;
import java.io.IOException;

public class BackupTask extends Task {

    private final File source;

    private final File destination;

    private boolean isSuccessful = false;

    public BackupTask(final File source, final File destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    protected void run() {
        try {
            Utils.zip(source.getAbsolutePath(), destination.getAbsolutePath());
            this.isSuccessful = true;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }
}
