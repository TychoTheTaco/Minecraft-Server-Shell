package com.tycho.mss;

import easytasks.Task;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class DownloadFileTask extends Task {

    private final String url;

    private final Path destination;

    private HttpURLConnection connection;

    private boolean finished = false;

    public DownloadFileTask(final String url, final Path destination) {
        this.url = url;
        this.destination = destination;
    }

    @Override
    protected void run() throws IOException {
        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        //Get response code
        if (connection.getResponseCode() == 200) {
            Files.createDirectories(destination.getParent());
            System.out.println("A");
            Files.copy(connection.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("B");
            connection.getInputStream().close();
            finished = true;
        } else {
            throw new IOException("Connection failed with HTTP response code " + connection.getResponseCode());
        }
    }

    @Override
    public void stop() {
        super.stop();
        connection.disconnect();
        if (!finished){
            onTaskCancelled();
        }
    }

    @Override
    protected void onTaskCancelled() {
        System.out.println("CANCELED");
        //Delete any partially downloaded data
        try {
            Files.deleteIfExists(destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
