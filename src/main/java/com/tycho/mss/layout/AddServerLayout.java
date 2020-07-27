package com.tycho.mss.layout;

import com.tycho.mss.ServerConfiguration;
import com.tycho.mss.ServerManager;
import com.tycho.mss.ServerShell;
import com.tycho.mss.util.Utils;
import easytasks.ITask;
import easytasks.Task;
import easytasks.TaskAdapter;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AddServerLayout extends VBox {

    @FXML
    private ValidatedTextField server_name_input;

    @FXML
    private RadioButton auto_download_jar_button;

    @FXML
    private RadioButton custom_jar_button;

    @FXML
    private Pane download_option_container;

    ////////////////////////////// Auto download //////////////////////////////

    @FXML
    private ProgressIndicator progress_indicator;

    @FXML
    private ComboBox<String> minecraft_version_input;

    @FXML
    private FileInputLayout server_directory_input;

    ////////////////////////////// Custom Jar //////////////////////////////

    @FXML
    private FileInputLayout custom_jar_input;

    @FXML
    private Button create_server_button;

    private Node currentNode;

    private AndValidGroup form = new AndValidGroup();
    AndValidGroup downloadJarGroup = new AndValidGroup();
    OrValidGroup radio = new OrValidGroup();
    AndValidGroup customJarGroup = new AndValidGroup();

    public AddServerLayout() {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/edit_server_layout.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        downloadJarGroup.add(new Valid() {
            @Override
            public boolean isValid() {
                return server_directory_input.isValid();
            }
        });

        customJarGroup.add(new Valid() {
            @Override
            public boolean isValid() {
                return custom_jar_input.isValid();
            }
        });
        form.add(radio);
        form.add(new Valid() {
            @Override
            public boolean isValid() {
                return server_name_input.isValid();
            }
        });

        //Server name
        server_name_input.setValidator(new ValidatedTextField.Validator() {
            @Override
            protected boolean isTextValid(String string, StringBuilder invalidReason) {
                if (string.length() == 0) {
                    invalidReason.append("Name cannot be empty!");
                    return false;
                }
                return super.isTextValid(string, invalidReason);
            }
        });
        server_name_input.setOnValidStateChangeListener(new ValidatedTextField.OnValidStateChangeListener() {
            @Override
            public void onValidStateChange(boolean isValid) {
                create_server_button.setDisable(!form.isValid());
            }
        });

        create_server_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                final boolean autoAcceptEula = true;

                final MultiStepProgressView multiStepProgressView = new MultiStepProgressView();

                //Download server JAR if we have to
                if (auto_download_jar_button.isSelected()) {
                    final MultiStepProgressView.MultipartTask.Task downloadServerJarTask = new MultiStepProgressView.MultipartTask.Task("Downloading server JAR") {
                        @Override
                        public void run() {
                            final String versionCode = minecraft_version_input.getValue();
                            Path serverJarPath = null;
                            for (Object object : versions) {
                                if (((JSONObject) object).get("id").equals(versionCode)) {
                                    final JSONObject result = sendRequest((String) ((JSONObject) object).get("url"));
                                    serverJarPath = Paths.get(System.getProperty("user.dir")).resolve(server_directory_input.getPath()).resolve("server.jar");
                                    final DownloadJarTask downloadJarTask = new DownloadJarTask((String) ((JSONObject) ((JSONObject) result.get("downloads")).get("server")).get("url"), serverJarPath);
                                    downloadJarTask.start();
                                    break;
                                }
                            }

                            if (serverJarPath == null) {
                                System.err.println("ERROR DOWNLOADING JAR!");
                            }
                            setOutput(serverJarPath);
                        }
                    };
                    downloadServerJarTask.addTaskListener(new TaskAdapter(){
                        @Override
                        public void onTaskFailed(ITask task, Exception exception) {
                            Platform.runLater(() -> {
                                final Alert alert = new Alert(Alert.AlertType.ERROR, "Could not find a server JAR for this version of Minecraft! Please try a different version.", ButtonType.OK);
                                alert.showAndWait();
                            });
                        }
                    });
                    multiStepProgressView.addTask(downloadServerJarTask);
                    multiStepProgressView.addTask(new MultiStepProgressView.MultipartTask.Task("Generating server properties") {
                        @Override
                        public void run() {
                            //Save server configuration
                            final ServerConfiguration serverConfiguration = new ServerConfiguration(server_name_input.getText(), (Path) getInput());
                            ServerManager.add(serverConfiguration);
                            ServerManager.save();

                            if (autoAcceptEula) {
                                try {
                                    //Decline EULA so that the server will stop after creating server.properties
                                    createAndSetEula(server_directory_input.getPath(), false);

                                    //Start server to generate properties
                                    final ServerShell serverShell = new ServerShell(serverConfiguration);
                                    serverShell.start();

                                    //Accept EULA
                                    createAndSetEula(server_directory_input.getPath(), true);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else if (custom_jar_button.isSelected()) {
                    multiStepProgressView.addTask(new MultiStepProgressView.MultipartTask.Task("Generating server properties") {
                        @Override
                        public void run() {
                            //Save server configuration
                            final ServerConfiguration serverConfiguration = new ServerConfiguration(server_name_input.getText(), custom_jar_input.getPath());
                            ServerManager.add(serverConfiguration);
                            ServerManager.save();

                            if (autoAcceptEula) {
                                final Path serverDirectory = custom_jar_input.getPath().getParent();
                                try {
                                    //Decline EULA so that the server will stop after creating server.properties
                                    createAndSetEula(serverDirectory, false);

                                    //Start server to generate properties
                                    final ServerShell serverShell = new ServerShell(serverConfiguration);
                                    serverShell.start();

                                    //Accept EULA
                                    createAndSetEula(serverDirectory, true);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }

                //Show progress dialog
                final Stage stage = (Stage) getScene().getWindow();
                stage.setResizable(false);
                stage.setOnCloseRequest(Event::consume);

                final Scene scene = new Scene(multiStepProgressView);
                scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
                stage.setScene(scene);
                stage.sizeToScene();
                stage.centerOnScreen();

                //Start
                multiStepProgressView.start(() -> Platform.runLater(stage::close));
            }
        });

        download_option_container.getChildren().forEach(new Consumer<Node>() {
            @Override
            public void accept(Node node) {
                node.setVisible(false);
                node.setManaged(false);
            }
        });

        auto_download_jar_button.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) setOption("download");
            }
        });

        custom_jar_button.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) setOption("custom");
            }
        });

        server_directory_input.setIsDirectory(true);
        server_directory_input.setValidator(new FileInputLayout.PathValidator() {
            @Override
            protected boolean isPathValid(Path path, StringBuilder invalidReason) {
                System.out.println(path.toAbsolutePath() + " VS " + Paths.get(System.getProperty("user.dir")));

                if (Files.exists(path) && Files.isDirectory(path)) {
                    try {
                        if (Files.list(path).count() > 0) {
                            invalidReason.append("Directory must be empty!");
                            return false;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }
        });
        server_directory_input.setOnValidStateChangeListener(new ValidatedTextField.OnValidStateChangeListener() {
            @Override
            public void onValidStateChange(boolean isValid) {
                create_server_button.setDisable(!form.isValid());
            }
        });

        custom_jar_input.setOnValidStateChangeListener(new ValidatedTextField.OnValidStateChangeListener() {
            @Override
            public void onValidStateChange(boolean isValid) {
                create_server_button.setDisable(!form.isValid());
            }
        });

        auto_download_jar_button.setSelected(true);

        //Download official Minecraft version manifest
        new Thread(() -> {
            final JSONObject result = sendRequest("https://launchermeta.mojang.com/mc/game/version_manifest.json");
            if (result != null){
                final List<String> versionCodes = new ArrayList<>();
                versions = (JSONArray) result.get("versions");
                if (versions != null){
                    for (Object object : versions) {
                        try{
                            //Ignore Minecraft versions 1.2.4 and below because they don't have a server JAR available for download.
                            final LocalDateTime CUTOFF_RELEASE_DATE = LocalDateTime.parse("2012-03-29T22:00:00+00:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            final String releaseTimeString = (String) ((JSONObject) object).get("releaseTime");
                            final LocalDateTime localDateTime = LocalDateTime.parse(releaseTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            if (localDateTime.isBefore(CUTOFF_RELEASE_DATE)){
                                continue;
                            }

                            versionCodes.add((String) ((JSONObject) object).get("id"));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    Platform.runLater(() -> {
                        minecraft_version_input.getItems().addAll(versionCodes);
                        minecraft_version_input.getSelectionModel().selectFirst();
                        progress_indicator.setVisible(false);
                        progress_indicator.setManaged(false);
                    });
                }else{
                    //TODO: Error condition
                }
            }else{
                //TODO: Error condition
            }
        }).start();

        create_server_button.setDisable(true);

    }

    private void createAndSetEula(final Path serverDirectory, final boolean accept) throws IOException {
        final PrintStream printStream = new PrintStream(Files.newOutputStream(serverDirectory.resolve(Paths.get("eula.txt"))));
        printStream.println("eula=" + (accept ? "true" : "false"));
        printStream.close();
    }

    private JSONArray versions;

    private void setOption(final String option) {
        final String id;
        if (option.equals("download")) {
            id = "option_download_jar";
            radio.remove(customJarGroup);
            radio.add(downloadJarGroup);
        } else {
            id = "option_custom_jar";
            radio.remove(downloadJarGroup);
            radio.add(customJarGroup);
        }
        if (currentNode != null) {
            currentNode.setVisible(false);
            currentNode.setManaged(false);
        }
        download_option_container.getChildren().forEach(new Consumer<Node>() {
            @Override
            public void accept(Node node) {
                if (node.getId().equals(id)) {
                    node.setVisible(true);
                    node.setManaged(true);
                    currentNode = node;
                }
            }
        });
    }

    private JSONObject sendRequest(final String url) {
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            //Get response code
            if (connection.getResponseCode() == 200) {
                return Utils.readStreamAsJson(connection.getInputStream());
            } else {
                System.out.println("ERROR RESPONSE CODE: " + connection.getResponseCode());
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private interface Valid {
        boolean isValid();
    }

    private class AndValidGroup implements Valid {

        private final List<Valid> items = new ArrayList<>();

        public void add(final Valid item) {
            if (!items.contains(item)) items.add(item);
        }

        public void remove(final Valid item) {
            items.remove(item);
        }

        @Override
        public boolean isValid() {
            for (Valid item : items) {
                if (!item.isValid()) {
                    return false;
                }
            }
            return true;
        }
    }

    private class OrValidGroup implements Valid {

        private final List<Valid> items = new ArrayList<>();

        public void add(final Valid item) {
            if (!items.contains(item)) items.add(item);
        }

        public void remove(final Valid item) {
            items.remove(item);
        }

        @Override
        public boolean isValid() {
            for (Valid item : items) {
                if (item.isValid()) {
                    return true;
                }
            }
            return false;
        }
    }

    private class DownloadJarTask extends Task {

        private final String url;

        private final Path destination;

        public DownloadJarTask(final String url, final Path destination) {
            this.url = url;
            this.destination = destination;
            System.out.println("DESTINATION: " + destination);
        }

        @Override
        protected void run() {
            try {
                final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                //Get response code
                if (connection.getResponseCode() == 200) {

                    try {
                        Files.createDirectories(destination.getParent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Files.copy(connection.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                    connection.getInputStream().close();
                } else {
                    System.out.println("ERROR RESPONSE CODE: " + connection.getResponseCode());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
