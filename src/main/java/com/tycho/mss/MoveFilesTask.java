package com.tycho.mss;

import easytasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MoveFilesTask extends Task {

    private final Path source;

    private final Path destination;

    private boolean isSuccessful = false;

    public MoveFilesTask(final Path source, final Path destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    protected void run() {
        try {
            Files.walk(source)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            Files.move(path, destination.resolve(source.relativize(path)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }
}
