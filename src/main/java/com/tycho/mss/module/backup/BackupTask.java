package com.tycho.mss.module.backup;

import easytasks.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupTask extends Task {

    private final Path source;

    private final Path destination;

    private boolean isSuccessful = false;

    final List<Path> failed = new ArrayList<>();

    public BackupTask(final Path source, final Path destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    protected void run() {
        try {
            //Get list of target files
            final List<Path> files = Files.walk(source).filter(Files::isRegularFile).collect(Collectors.toList());

            //Create ZIP file
            Files.createDirectories(destination.getParent());
            final ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(destination));
            for (int i = 0; i < files.size(); i++){
                ZipEntry zipEntry = new ZipEntry(source.relativize(files.get(i)).toString());
                try {
                    zipOutputStream.putNextEntry(zipEntry);
                    Files.copy(files.get(i), zipOutputStream);
                    zipOutputStream.closeEntry();
                } catch (IOException e) {
                    failed.add(files.get(i));
                    System.err.println("FAILED TO COPY: " + files.get(i) + " REASON: " + e.getMessage());
                }
                setProgress((float) (i + 1) / files.size());
            }
            zipOutputStream.close();

            this.isSuccessful = failed.isEmpty();
            System.out.println("Saved backup to " + destination.toAbsolutePath());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public Path getSource() {
        return source;
    }

    public Path getDestination() {
        return destination;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public List<Path> getFailed() {
        return failed;
    }
}
