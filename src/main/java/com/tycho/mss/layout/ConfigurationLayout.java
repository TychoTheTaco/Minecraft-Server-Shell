package com.tycho.mss.layout;

import com.tycho.mss.MenuPage;
import com.tycho.mss.MinecraftServerShell;
import com.tycho.mss.ServerShell;
import com.tycho.mss.util.Preferences;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationLayout extends MenuPage {

    @FXML
    private FileInputLayout serverJarInputController;

    @FXML
    private TextField launch_options_text_field;

   /* @FXML
    private TableView<Property<?>> server_properties_table_view;*/

    @FXML
    private Button save_button;

    @FXML
    private Button revert_button;

    //private List<Property<?>> properties = new ArrayList<>();

    private JSONObject initialConfiguration;

    @FXML
    private void initialize() {
        //Server JAR
        serverJarInputController.setValidator(File::exists);
        serverJarInputController.addExtensionFilter(new FileChooser.ExtensionFilter("Server JAR file", "*.jar"));

        //Load the saved configuration
        setDefaults();
        initialConfiguration = getConfiguration();

        serverJarInputController.setOnTextChanged(() -> {
            setDirty(!initialConfiguration.get("server_jar").toString().equals(serverJarInputController.getFile().getAbsolutePath()));
        });

        launch_options_text_field.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                setDirty(!initialConfiguration.get("launch_options").toString().equals(newValue));
            }
        });

        //server_properties_table_view.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        revert_button.setOnAction(event -> {
            setDefaults();
        });

        save_button.setOnAction(event -> {
            if (serverJarInputController.isValid()){
                Preferences.setServerJar(serverJarInputController.getFile());
                MinecraftServerShell.refresh();
                setStatus(Status.OK);
            }else{
                setStatus(Status.ERROR);
            }
            Preferences.setLaunchOptions(launch_options_text_field.getText());
            Preferences.save();
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

        setStatus(serverJarInputController.isValid() ? Status.OK : Status.ERROR);
    }

    private void setDirty(final boolean dirty){
        if (dirty){
            revert_button.setVisible(true);
            revert_button.setManaged(true);
        }else{
            revert_button.setVisible(false);
            revert_button.setManaged(false);
        }
    }

    private void setDefaults(){
        serverJarInputController.setFile(Preferences.getServerJar());
        launch_options_text_field.setText(String.join(" ", Preferences.getLaunchOptions()));
    }

    private JSONObject getConfiguration(){
        final JSONObject root = new JSONObject();
        root.put("server_jar", serverJarInputController.getFile());
        root.put("launch_options", launch_options_text_field.getText().trim());
        return root;
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

    @Override
    public void setServerShell(ServerShell serverShell) {
        super.setServerShell(serverShell);
        //this.properties = getProperties();

        /*server_properties_table_view.getItems().clear();
        for (Property<?> property : this.properties){
            server_properties_table_view.getItems().add(property);
        }*/
    }

    private List<Property<?>> getProperties(){
        final List<Property<?>> properties = new ArrayList<>();

        final Map<String, String> propertyMap = getServerShell().getProperties();
        for (String key : propertyMap.keySet()){
            switch (key){
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
                    properties.add(new IntegerProperty(key, Integer.valueOf(propertyMap.get(key))){
                        @Override
                        public boolean isValidValue(Integer value) {
                            return 0 <= value;
                        }
                    });
                    break;

                case "function-permission-level":
                case "op-permission-level":
                    properties.add(new IntegerProperty(key, Integer.valueOf(propertyMap.get(key))){
                        @Override
                        public boolean isValidValue(Integer value) {
                            return 1 <= value && value <= 4;
                        }
                    });
                    break;

                case "max-world-size":
                    properties.add(new IntegerProperty(key, Integer.valueOf(propertyMap.get(key))){
                        @Override
                        public boolean isValidValue(Integer value) {
                            return 1 <= value && value <= 29999984;
                        }
                    });
                    break;

                case "network-compression-threshold":
                    properties.add(new IntegerProperty(key, Integer.valueOf(propertyMap.get(key))){
                        @Override
                        public boolean isValidValue(Integer value) {
                            return -1 <= value;
                        }
                    });
                    break;

                case "query.port":
                case "rcon.port":
                case "server-port":
                    properties.add(new IntegerProperty(key, Integer.valueOf(propertyMap.get(key))){
                        @Override
                        public boolean isValidValue(Integer value) {
                            return 1 <= value && value <= 65534;
                        }
                    });
                    break;

                case "view-distance":
                    properties.add(new IntegerProperty(key, Integer.valueOf(propertyMap.get(key))){
                        @Override
                        public boolean isValidValue(Integer value) {
                            return 3 <= value && value <= 32;
                        }
                    });
                    break;

                //////////////////////////////////////////////// Long Integers ////////////////////////////////////////////////

                case "max-tick-time":
                    properties.add(new LongProperty(key, Long.valueOf(propertyMap.get(key))){
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
                    properties.add(new StringProperty(key, propertyMap.get(key)){
                        @Override
                        public boolean isValidValue(String value) {
                            return true;
                        }
                    });
                    break;

                case "difficulty":
                    properties.add(new StringProperty(key, propertyMap.get(key)){
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
                    properties.add(new StringProperty(key, propertyMap.get(key)){
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
                    properties.add(new StringProperty(key, propertyMap.get(key)){
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
    }

    private abstract static class Property<T>{

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

        public boolean isValidValue(final T value){
            return true;
        }
    }

    private static class BooleanProperty extends Property<Boolean>{

        public BooleanProperty(String key, Boolean value) {
            super(key, value);
        }
    }

    private static class IntegerProperty extends Property<Integer>{

        public IntegerProperty(String key, Integer value) {
            super(key, value);
        }
    }

    private static class LongProperty extends Property<Long>{

        public LongProperty(String key, Long value) {
            super(key, value);
        }
    }

    private static class StringProperty extends Property<String>{

        public StringProperty(String key, String value) {
            super(key, value);
        }

        @Override
        public boolean isValidValue(String value) {
            for (String string : getValidValues()){
                if (string.equals(value)) return true;
            }
            return false;
        }

        protected String[] getValidValues(){
            return new String[1];
        }
    }
}
