package com.tycho.mss;

import com.tycho.mss.util.Utils;
import easytasks.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RestoreBackupTask extends Task {

    private final File backup;

    private final Path destination;

    private boolean isSuccessful = false;

    public RestoreBackupTask(final File backup, final Path destination) {
        this.backup = backup;
        this.destination = destination;
    }

    @Override
    protected void run() {
        try {
            //Delete world folder
            final Path worldDirectory = destination.resolve("world");
            Utils.deleteDirectory(worldDirectory);

            //Extract world from backup
            final ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(backup));
            final byte[] buffer = new byte[1024];
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final String name = entry.getName();
                if (name.startsWith("world" + File.separator)){
                    final Path path = worldDirectory.resolve(Paths.get(name.replaceFirst(Pattern.quote("world" + File.separator), "")));

                    //Create parent directories
                    Files.createDirectories(path.getParent());

                    //Extract file
                    final FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.close();
                }
                zipInputStream.closeEntry();
            }
            zipInputStream.close();

            this.isSuccessful = true;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }
}
