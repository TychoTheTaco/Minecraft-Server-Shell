package com.tycho.mss;

import easytasks.Task;

import java.io.File;

public class BackupTask extends Task {

    private final File file;

    public BackupTask(final File file) {
        this.file = file;
    }

    @Override
    protected void run() {

    }
}
