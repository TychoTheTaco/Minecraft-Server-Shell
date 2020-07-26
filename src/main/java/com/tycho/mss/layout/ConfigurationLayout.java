package com.tycho.mss.layout;

import com.tycho.mss.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.json.simple.JSONObject;

import java.nio.file.Path;

public class ConfigurationLayout implements Page, StatusHost, ServerShellConnection {

    @FXML
    private FileInputLayout server_jar_input;

    @FXML
    private TextField launch_options_text_field;

    @FXML
    private Button delete_server_button;

   /* @FXML
    private TableView<Property<?>> server_properties_table_view;*/

    @FXML
    private Button save_button;

    //private List<Property<?>> properties = new ArrayList<>();

    private JSONObject initialConfiguration;

    private final StatusContainer statusContainer = new StatusContainer();
    private final ServerShellContainer serverShellContainer = new ServerShellContainer(){
        @Override
        public void setServerShell(ServerShell serverShell) {
            super.setServerShell(serverShell);
            setDefaults();
            statusContainer.setStatus(server_jar_input.isValid() ? StatusContainer.Status.OK : StatusContainer.Status.ERROR);
        }
    };

    @FXML
    private void initialize() {
        //Server JAR
        server_jar_input.setOnValidStateChangeListener(new ValidatedTextField.OnValidStateChangeListener() {
            @Override
            public void onValidStateChange(boolean isValid) {
                if (isValid && isDirty()) {
                    save_button.setDisable(false);
                } else {
                    save_button.setDisable(true);
                }
            }
        });
        server_jar_input.setOnPathChangedListener((path) -> {
            setDirty(isDirty());
        });

        //Launch options
        launch_options_text_field.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                setDirty(isDirty());
            }
        });

        //server_properties_table_view.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        //Delete server button
        delete_server_button.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Are you sure you want to delete this server? All data including the Minecraft world, player data, and server configuration data will be permanently deleted! Any backups that were created will remain.", ButtonType.YES, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES){
                ServerManager.delete(getServerShellContainer().getServerShell().getServerConfiguration());
                MinecraftServerManager.setPage("server_list");
            }
        });

        save_button.setOnAction(event -> {
            save_button.setDisable(true);

            //TODO: Save to server configuration
            //Preferences.setServerJar(server_jar_input.getPath());
            //MinecraftServerManager.refresh();
            //Preferences.setLaunchOptions(launch_options_text_field.getText());

            statusContainer.setStatus(StatusContainer.Status.OK);
            initialConfiguration = getConfiguration();
            setDirty(false);
        });

        //Key
       /* final TableColumn<Property<?>, String> keyColumn = new TableColumn<>("Key");
        keyColumn.setSortable(false);
        keyColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getKey()));
        server_properties_table_view.getColumns().add(keyColumn);

        //Key
        final TableColumn<Property<?>, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setSortable(false);
        valueColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(String.valueOf(param.getValue().getValue())));*/
        /*valueColumn.setCellFactory(new Callback<TableColumn<Property<?>, String>, TableCell<Property<?>, String>>() {
            @Override
            public TableCell<Property<?>, String> call(TableColumn<Property<?>, String> param) {
                return new TableCell<Property<?>, String>(){
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                    }
                };
            }
        });*/
        //server_properties_table_view.getColumns().add(valueColumn);
    }

    private boolean isDirty() {
        if (initialConfiguration == null) return false;
        final Path path = server_jar_input.getPath();
        return !(path != null && initialConfiguration.get("jar").equals(server_jar_input.getPath()) && initialConfiguration.get("launch_options").toString().equals(launch_options_text_field.getText()));
    }

    private void setDirty(final boolean dirty) {
        if (dirty) {
            //revert_button.setVisible(true);
            //revert_button.setManaged(true);
            if (server_jar_input.isValid()) save_button.setDisable(false);
        } else {
            save_button.setDisable(true);
            //revert_button.setVisible(false);
            //revert_button.setManaged(false);
        }
    }

    private void setDefaults() {
        server_jar_input.setPath(serverShellContainer.getServerShell().getServerConfiguration().getJar());
        launch_options_text_field.setText(serverShellContainer.getServerShell().getServerConfiguration().getLaunchOptions());
    }

    private JSONObject getConfiguration() {
        final JSONObject root = new JSONObject();
        root.put("server_jar", server_jar_input.getPath() == null ? "" : server_jar_input.getPath().toString());
        root.put("launch_options", launch_options_text_field.getText().trim());
        return root;
    }

    @Override
    public void onPageSelected() {
        //Load the saved configuration
        initialConfiguration = getServerShellContainer().getServerShell().getServerConfiguration().toJson();
        setDefaults();
        statusContainer.setStatus(server_jar_input.isValid() ? StatusContainer.Status.OK : StatusContainer.Status.ERROR);
    }

    @Override
    public void onPageHidden() {

    }

    @Override
    public ServerShellContainer getServerShellContainer() {
        return serverShellContainer;
    }

    @Override
    public StatusContainer getStatusManager() {
        return statusContainer;
    }

    /*private ServerShell.LaunchConfiguration loadConfiguration(){
        if (CONFIG_FILE.exists()){
            try {
                final String string = new String(Files.readAllBytes(Paths.get(CONFIG_FILE.getAbsolutePath())));
                final JSONObject root = (JSONObject) new JSONParser().parse(string);

                final ServerShell.LaunchConfiguration launchConfiguration = new ServerShell.LaunchConfiguration();
                launchConfiguration.setServerJar(new File((String) root.get("server_jar")));
                launchConfiguration.setLaunchOptions(((String) root.get("launch_options")).split(" "));
                return launchConfiguration;
            }catch (IOException | ParseException e){
                e.printStackTrace();
            }

        }else{
            final ServerShell.LaunchConfiguration launchConfiguration = new ServerShell.LaunchConfiguration();

            //Try to find the server jar in the current directory
            final File directory = new File(System.getProperty("user.dir"));
            for (File file : directory.listFiles()){
                if (file.getName().contains("server") && file.getName().contains("jar")){
                    //This is probably it
                    launchConfiguration.setServerJar(file);
                    break;
                }
            }

            launchConfiguration.setLaunchOptions("-Xms3G -Xmx4G".split(" "));

            return launchConfiguration;
        }

        return null;
    }*/

    /*private List<Property<?>> getProperties() {
        final List<Property<?>> properties = new ArrayList<>();

        final Map<String, String> propertyMap = getServerShell().getProperties();
        for (String key : propertyMap.keySet()) {
            switch (key) {
                case "allow-flight":
                case "allow-nether":
                case "enable-command-block":
                case "enable-query":
                case "enable-rcon":
                case "force-gamemode":
                case "generate-structures":
                case "hardcore":
                case "online-mode":
                case "prevent-proxy-connections":
                case "pvp":
                case "snooper-enabled":
                case "spawn-animals":
                case "spawn-monsters":
                case "spawn-npcs":
                case "use-native-transport":
                case "white-list":
                case "enforce-whitelist":
                    properties.add(new BooleanProperty(key, Boolean.valueOf(propertyMap.get(key))));
                    break;

                //////////////////////////////////////////////// Integers ////////////////////////////////////////////////

                case "max-build-height":
                case "max-players":
                case "player-idle-timeout":
                case "spawn-protection":
                    properties.add(new IntegerProperty(key, Integer.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Integer value) {
                            return 0 <= value;
                        }
                    });
                    break;

                case "function-permission-level":
                case "op-permission-level":
                    properties.add(new IntegerProperty(key, Integer.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Integer value) {
                            return 1 <= value && value <= 4;
                        }
                    });
                    break;

                case "max-world-size":
                    properties.add(new IntegerProperty(key, Integer.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Integer value) {
                            return 1 <= value && value <= 29999984;
                        }
                    });
                    break;

                case "network-compression-threshold":
                    properties.add(new IntegerProperty(key, Integer.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Integer value) {
                            return -1 <= value;
                        }
                    });
                    break;

                case "query.port":
                case "rcon.port":
                case "server-port":
                    properties.add(new IntegerProperty(key, Integer.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Integer value) {
                            return 1 <= value && value <= 65534;
                        }
                    });
                    break;

                case "view-distance":
                    properties.add(new IntegerProperty(key, Integer.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Integer value) {
                            return 3 <= value && value <= 32;
                        }
                    });
                    break;

                //////////////////////////////////////////////// Long Integers ////////////////////////////////////////////////

                case "max-tick-time":
                    properties.add(new LongProperty(key, Long.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Long value) {
                            // TODO: Allow -1 after 14w32a
                            if (value == -1) return true;
                            return 0 <= value && value <= Long.MAX_VALUE - 1;
                        }
                    });
                    break;

                //////////////////////////////////////////////// Strings ////////////////////////////////////////////////

                case "generator-settings":
                case "level-name":
                case "level-seed":
                case "motd":
                case "rcon.password":
                case "resource-pack":
                case "resource-pack-sha1":
                case "server-ip":
                    properties.add(new StringProperty(key, propertyMap.get(key)) {
                        @Override
                        public boolean isValidValue(String value) {
                            return true;
                        }
                    });
                    break;

                case "difficulty":
                    properties.add(new StringProperty(key, propertyMap.get(key)) {
                        @Override
                        protected String[] getValidValues() {
                            return new String[]{
                                    "peaceful",
                                    "easy",
                                    "normal",
                                    "hard"
                            };
                        }
                    });
                    break;

                case "gamemode":
                    properties.add(new StringProperty(key, propertyMap.get(key)) {
                        @Override
                        protected String[] getValidValues() {
                            return new String[]{
                                    "survival",
                                    "creative",
                                    "adventure",
                                    "spectator"
                            };
                        }
                    });
                    break;

                case "level-type":
                    properties.add(new StringProperty(key, propertyMap.get(key)) {
                        @Override
                        protected String[] getValidValues() {
                            return new String[]{
                                    "default",
                                    "flat",
                                    "largebiomes",
                                    "amplified",
                                    "buffet"
                            };
                        }
                    });
                    break;
            }
        }

        return properties;
    }*/

    private abstract static class Property<T> {

        private final String key;

        private T value;

        public Property(final String key, final T value) {
            this.key = key;
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public boolean isValidValue(final T value) {
            return true;
        }
    }

    private static class BooleanProperty extends Property<Boolean> {

        public BooleanProperty(String key, Boolean value) {
            super(key, value);
        }
    }

    private static class IntegerProperty extends Property<Integer> {

        public IntegerProperty(String key, Integer value) {
            super(key, value);
        }
    }

    private static class LongProperty extends Property<Long> {

        public LongProperty(String key, Long value) {
            super(key, value);
        }
    }

    private static class StringProperty extends Property<String> {

        public StringProperty(String key, String value) {
            super(key, value);
        }

        @Override
        public boolean isValidValue(String value) {
            for (String string : getValidValues()) {
                if (string.equals(value)) return true;
            }
            return false;
        }

        protected String[] getValidValues() {
            return new String[1];
        }
    }
}
