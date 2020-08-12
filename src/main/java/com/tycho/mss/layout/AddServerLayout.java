package com.tycho.mss.layout;

import com.tycho.mss.DownloadFileTask;
import com.tycho.mss.ServerConfiguration;
import com.tycho.mss.ServerManager;
import com.tycho.mss.ServerShell;
import com.tycho.mss.form.AndGroup;
import com.tycho.mss.form.FormGroup;
import com.tycho.mss.form.OrGroup;
import com.tycho.mss.util.Utils;
import easytasks.ITask;
import easytasks.TaskAdapter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class AddServerLayout extends VBox {

    @FXML
    private ValidatedTextField server_name_input;

    @FXML
    private RadioButton radio_auto_download_jar;

    @FXML
    private RadioButton radio_custom_jar;

    @FXML
    private Pane download_option_container;

    ////////////////////////////// Auto download //////////////////////////////

    @FXML
    private HBox loading_versions_indicator;

    @FXML
    private Label loading_label;

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

    private JSONArray versions;

    private AndGroup form = new AndGroup();
    AndGroup downloadJarGroup = new AndGroup();
    OrGroup radio = new OrGroup();
    AndGroup customJarGroup = new AndGroup();

    public AddServerLayout() {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/add_server_layout.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        loading_versions_indicator.managedProperty().bind(loading_versions_indicator.visibleProperty());
        minecraft_version_input.managedProperty().bind(minecraft_version_input.visibleProperty());

        downloadJarGroup.add(new FormGroup() {
            @Override
            public boolean isValid() {
                return server_directory_input.isValid();
            }
        });
        downloadJarGroup.add(new FormGroup() {
            @Override
            public boolean isValid() {
                return minecraft_version_input.getSelectionModel().getSelectedIndex() != -1;
            }
        });

        customJarGroup.add(new FormGroup() {
            @Override
            public boolean isValid() {
                return custom_jar_input.isValid();
            }
        });
        form.add(radio);
        form.add(new FormGroup() {
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

                //Check for duplicate server name
                if (ServerManager.getConfiguration(server_name_input.getText().trim()) != null) {
                    invalidReason.append("Another server already has this name!");
                    return false;
                }

                return super.isTextValid(string, invalidReason);
            }
        });
        server_name_input.setOnValidStateChangeListener(isValid -> create_server_button.setDisable(!form.isValid()));

        create_server_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final MultiStepProgressView multiStepProgressView = new MultiStepProgressView();
                multiStepProgressView.addTaskListener(new TaskAdapter(){

                    private boolean failed = false;

                    @Override
                    public void onTaskFailed(ITask task, Exception exception) {
                        failed = true;
                        Platform.runLater(() -> {
                            final Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage(), ButtonType.OK);
                            alert.showAndWait();
                        });
                    }

                    @Override
                    public void onTaskStopped(ITask task) {
                        if (!failed){
                            System.err.println("Saving config");
                            final ServerConfiguration configuration = (ServerConfiguration) multiStepProgressView.getOutput();
                            ServerManager.add(configuration);
                            ServerManager.save();
                        }
                    }
                });

                final MultiStepProgressView.MultipartTask.Task generateServerPropertiesTask = new MultiStepProgressView.MultipartTask.Task("Generating server properties") {

                    private boolean onFailedStart = false;

                    @Override
                    public void run() throws Exception {
                        final Path serverJarPath = (Path) getInput();
                        System.out.println("SERVER PATH: " + serverJarPath);

                        //If there is already an EULA, decline it so that the server will stop after generating 'server.properties'
                        if (Files.exists(serverJarPath.getParent().resolve("eula.txt"))){
                            createAndSetEula(serverJarPath.getParent(), false);
                        }

                        //Start server to generate 'server.properties'
                        final ServerConfiguration serverConfiguration = new ServerConfiguration(server_name_input.getText().trim(), serverJarPath);
                        final ServerShell serverShell = new ServerShell(serverConfiguration);
                        final ServerShell.PendingPatternMatch pendingPatternMatch = serverShell.listen(Pattern.compile("\\[main/INFO\\]: You need to agree to the EULA in order to run the server. Go to eula.txt for more info."));
                        serverShell.addEventListener(new ServerShell.EventAdapter() {
                            @Override
                            public void onServerStarted() {
                                System.err.println("SERVER STARTED");
                                //Server successfully started, turn it off now
                                serverShell.stop();
                            }

                            @Override
                            public void onFailedStart() {
                                System.err.println("FAILED START");
                                onFailedStart = true;
                            }
                        });
                        serverShell.start();

                        if (onFailedStart){
                            System.out.println("RESULT: " + pendingPatternMatch.getResult());
                            //The server failed to start, check if its due to EULA
                            if (pendingPatternMatch.getResult() != null) {
                                createAndSetEula(serverJarPath.getParent(), true);
                            }else{
                                //Server failed to start for some other reason
                                throw new RuntimeException("Failed to validate server!");
                            }
                        }

                        //Verify that 'server.properties' exists
                        final Path serverPropertiesPath = serverJarPath.getParent().resolve("server.properties");
                        if (!Files.exists(serverPropertiesPath)){
                            throw new RuntimeException("Error generating 'server.properties'");
                        }

                        setOutput(serverConfiguration);
                    }

                    private void createAndSetEula(final Path serverDirectory, final boolean accept) throws IOException {
                        //TODO: Dont overwrite the pre-generated one
                        final PrintStream printStream = new PrintStream(Files.newOutputStream(serverDirectory.resolve(Paths.get("eula.txt"))));
                        printStream.println("//Generated by Minecraft Server Manager");
                        printStream.println("eula=" + (accept ? "true" : "false"));
                        printStream.close();
                    }

                    //TODO: Fix this
                    private void setEula(final Path serverDirectory, final boolean accept) throws IOException{
                        final Path eulaPath = serverDirectory.resolve("eula.txt");
                        if (Files.exists(eulaPath)){
                            final RandomAccessFile randomAccessFile = new RandomAccessFile(eulaPath.toFile(), "rw");
                            String line;
                            while ((line = randomAccessFile.readLine()) != null){
                                if (line.startsWith("eula=")){
                                    randomAccessFile.writeBytes("eula=" + accept);
                                }
                            }
                        }else{
                            final PrintStream printStream = new PrintStream(Files.newOutputStream(eulaPath));
                            printStream.println("//Generated by Minecraft Server Manager");
                            printStream.println("eula=" + accept);
                            printStream.close();
                        }
                    }
                };

                //Download server JAR if we have to
                if (radio_auto_download_jar.isSelected()) {
                    final MultiStepProgressView.MultipartTask.Task downloadServerJarTask = new MultiStepProgressView.MultipartTask.Task("Downloading server JAR") {

                        private DownloadFileTask downloadJarTask;

                        private final Object MUTEX = new Object();

                        @Override
                        public void run() throws Exception {
                            final String versionCode = minecraft_version_input.getValue();
                            Path serverJarPath = null;
                            for (Object object : versions) {
                                if (((JSONObject) object).get("id").equals(versionCode)) {
                                    final JSONObject result = sendRequest((String) ((JSONObject) object).get("url"), 10 * 1000);
                                    serverJarPath = Paths.get(System.getProperty("user.dir")).resolve(server_directory_input.getPath()).resolve("server.jar");

                                    synchronized (MUTEX) {
                                        System.err.println("SHOULD STOP: " + shouldStop());
                                        if (shouldStop()) {
                                            setCanceled(true);
                                            return;
                                        }
                                        downloadJarTask = new DownloadFileTask((String) ((JSONObject) ((JSONObject) result.get("downloads")).get("server")).get("url"), serverJarPath);
                                    }

                                    downloadJarTask.addTaskListener(new TaskAdapter() {
                                        @Override
                                        public void onTaskStarted(ITask task) {
                                            if (shouldStop()) {
                                                setCanceled(true);
                                            }
                                        }
                                    });
                                    downloadJarTask.start();
                                    break;
                                }
                            }

                            if (serverJarPath == null) {
                                throw new RuntimeException("Error downloading Minecraft server JAR for version code " + versionCode);
                            }
                            setOutput(serverJarPath);
                        }

                        @Override
                        public void stop() {
                            System.out.println("STOPPING DOWNLOAD");
                            synchronized (MUTEX) {
                                if (downloadJarTask != null) downloadJarTask.stop();
                            }
                            super.stop();
                        }
                    };
                    multiStepProgressView.addTask(downloadServerJarTask);
                } else {
                    generateServerPropertiesTask.setInput(custom_jar_input.getPath());
                }
                multiStepProgressView.addTask(generateServerPropertiesTask);

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

        download_option_container.getChildren().forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });

        radio_auto_download_jar.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) setOption("download");
        });

        radio_custom_jar.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) setOption("custom");
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
        server_directory_input.setOnValidStateChangeListener(isValid -> create_server_button.setDisable(!form.isValid()));

        custom_jar_input.setOnValidStateChangeListener(isValid -> create_server_button.setDisable(!form.isValid()));

        radio_auto_download_jar.setSelected(true);

        //Calculate height required for Minecraft version input
        final Group group = new Group(getChildrenUnmodifiable());
        final Scene scene = new Scene(group);
        scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
        group.applyCss();
        group.layout();
        getChildren().addAll(group.getChildren());
        loading_versions_indicator.setMinHeight(minecraft_version_input.getHeight());
        loading_versions_indicator.setPrefHeight(minecraft_version_input.getHeight());
        minecraft_version_input.setVisible(false);

        //Download official Minecraft version manifest
        loadMinecraftVersionsList();

        create_server_button.setDisable(true);
    }

    private void loadMinecraftVersionsList() {
        new Thread(() -> {
            Platform.runLater(() -> {
                minecraft_version_input.setVisible(false);
                minecraft_version_input.getItems().clear();
                loading_versions_indicator.setVisible(true);
                loading_label.setText("Loading...");
                loading_label.setTextFill(Color.WHITE);
                progress_indicator.setVisible(true);
            });

            //Simulate long loading TODO: Remove
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                final JSONObject result = sendRequest("https://launchermeta.mojang.com/mc/game/version_manifest.json", 10 * 1000);
                if (result == null) {
                    throw new RuntimeException("Error getting Minecraft version manifest");
                }

                final List<String> versionCodes = new ArrayList<>();
                versions = (JSONArray) result.get("versions");
                if (versions == null) {
                    throw new RuntimeException("Invalid manifest format");
                }

                //Ignore Minecraft versions 1.2.4 and below because they don't have a server JAR available for download.
                final LocalDateTime CUTOFF_RELEASE_DATE = LocalDateTime.parse("2012-03-29T22:00:00+00:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                for (Object object : versions) {
                    final String releaseTimeString = (String) ((JSONObject) object).get("releaseTime");
                    final LocalDateTime localDateTime = LocalDateTime.parse(releaseTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    if (localDateTime.isBefore(CUTOFF_RELEASE_DATE)) {
                        continue;
                    }
                    versionCodes.add((String) ((JSONObject) object).get("id"));
                }

                Platform.runLater(() -> {
                    minecraft_version_input.getItems().setAll(versionCodes);
                    minecraft_version_input.getSelectionModel().selectFirst();
                    loading_versions_indicator.setVisible(false);
                    minecraft_version_input.setVisible(true);
                    create_server_button.setDisable(!form.isValid());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    minecraft_version_input.setVisible(false);
                    loading_versions_indicator.setVisible(true);
                    loading_label.setText("Connection Error");
                    loading_label.setTextFill(Color.RED);
                    progress_indicator.setVisible(false);
                });
            }
        }).start();
    }

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

    private JSONObject sendRequest(final String url, final int timeout) throws IOException, ParseException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.connect();

        //Get response code
        if (connection.getResponseCode() == 200) {
            return Utils.readStreamAsJson(connection.getInputStream());
        } else {
            System.out.println("ERROR RESPONSE CODE: " + connection.getResponseCode());
        }

        return null;
    }
}
