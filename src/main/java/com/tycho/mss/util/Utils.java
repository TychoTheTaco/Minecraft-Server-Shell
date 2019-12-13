package com.tycho.mss.util;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {

    public static JSONObject createText(final String text, final String color) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", text);
        jsonObject.put("color", color);
        return jsonObject;
    }

    public static JSONObject createText(final String... data) {
        final JSONObject root = new JSONObject();
        root.put("text", data[0]);
        root.put("color", data[1]);

        if (data.length > 2) {
            final JSONObject extras = new JSONObject();
            for (int i = 2; i < data.length; i += 2) {
                extras.put("text", data[i]);
                extras.put("color", data[i + 1]);
            }
            root.put("extra", extras);
        }

        return root;
    }

    public static String formatTimeHuman(final long millis, final int precision) {
        if (millis == -1) {
            return "infinite time";
        }

        if (precision < 1) {
            return "";
        }

        final int milliseconds = (int) (millis % 1000);
        final int seconds = (int) ((millis / 1000) % 60);
        final int minutes = (int) ((millis / (1000 * 60)) % 60);
        final int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
        final int days = (int) ((millis / (1000 * 60 * 60 * 24)) % 7);

        final int[] times = new int[]{milliseconds, seconds, minutes, hours, days};
        final String[] parts = new String[]{milliseconds + " ms", seconds + " second", minutes + " minute", hours + " hour", days + " day"};

        final StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (int i = times.length - 1; i >= 0; i--) {
            if (times[i] > 0) {
                stringBuilder.append(parts[i]);
                if (i != 0 && times[i] != 1) {
                    stringBuilder.append('s');
                }
                stringBuilder.append(' ');
                count++;
                if (count == precision) {
                    break;
                }
            } else if (stringBuilder.length() > 0) {
                break;
            }
        }

        if (stringBuilder.length() == 0) {
            stringBuilder.append(parts[0]);
        }

        return stringBuilder.toString().trim();
    }

    public static String formatTimeStopwatch(final long millis, final int precision) {
        final int seconds = (int) ((millis / 1000) % 60);
        final int minutes = (int) ((millis / (1000 * 60)) % 60);
        final int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
        final int days = (int) ((millis / (1000 * 60 * 60 * 24)) % 7);

        final int[] times = new int[]{seconds, minutes, hours, days};

        final StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (int i = times.length - 1; i >= 0; i--) {
            if (times[i] > 0 || i < precision) {
                stringBuilder.append(String.format("%02d", times[i]));
                stringBuilder.append(':');
                count++;
                if (count == precision) {
                    break;
                }
            } else if (stringBuilder.length() > 0) {
                break;
            }
        }

        return stringBuilder.toString().substring(0, stringBuilder.length() - 1).trim();
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "KMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static void deleteDirectory(final Path directory) throws IOException {
        if (Files.notExists(directory)) return;
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void unzip(final File source, final File destination) throws IOException {
        // create output directory if it doesn't exist
        if (!destination.exists()) destination.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        fis = new FileInputStream(source);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry ze = zis.getNextEntry();
        while (ze != null) {
            String fileName = ze.getName();
            File newFile = new File(destination + File.separator + fileName);
            System.out.println("Unzipping to " + newFile.getAbsolutePath());
            //create directories for sub directories in zip
            new File(newFile.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            //close this ZipEntry
            zis.closeEntry();
            ze = zis.getNextEntry();
        }
        //close last ZipEntry
        zis.closeEntry();
        zis.close();
        fis.close();
    }
}
