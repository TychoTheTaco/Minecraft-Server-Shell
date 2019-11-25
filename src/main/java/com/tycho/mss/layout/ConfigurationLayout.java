package com.tycho.mss.layout;

import com.tycho.mss.MenuPage;
import com.tycho.mss.ServerShell;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigurationLayout extends MenuPage {

    @FXML
    private TextField server_jar_text_field;

    @FXML
    private TextField launch_options_text_field;

    @FXML
    private FileInputLayout fileInputLayoutController;

    @FXML
    private TableView<Property<?>> server_properties_table_view;

    @FXML
    private Button save_button;

    private static final File CONFIG_FILE = new File(System.getProperty("user.dir") + File.separator + "mss_config.json");

    private List<Property<?>> properties = new ArrayList<>();

    @FXML
    private void initialize() {
        //Load the saved configuration
        final ServerShell.LaunchConfiguration configuration = loadConfiguration();

        if (configuration.getServerJar() != null) server_jar_text_field.setText(configuration.getServerJar().getPath());
        launch_options_text_field.setText(String.join(" ", configuration.getLaunchOptions()));

        server_properties_table_view.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        save_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveConfiguration(configuration);
            }
        });

        //Key
        final TableColumn<Property<?>, String> keyColumn = new TableColumn<>("Key");
        keyColumn.setSortable(false);
        keyColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getKey()));
        server_properties_table_view.getColumns().add(keyColumn);

        //Key
        final TableColumn<Property<?>, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setSortable(false);
        valueColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(String.valueOf(param.getValue().getValue())));
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
        server_properties_table_view.getColumns().add(valueColumn);
    }

    private ServerShell.LaunchConfiguration loadConfiguration(){
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
    }

    private void saveConfiguration(final ServerShell.LaunchConfiguration launchConfiguration){
        try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(CONFIG_FILE))){
            final JSONObject root = new JSONObject();
            root.put("server_jar", launchConfiguration.getServerJar().getAbsolutePath());
            root.put("launch_options", String.join(" ", launchConfiguration.getLaunchOptions()));
            bufferedWriter.write(root.toString());
           // System.out.println("Saved");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void setServerShell(ServerShell serverShell) {
        super.setServerShell(serverShell);
        this.properties = getProperties();

        server_properties_table_view.getItems().clear();
        for (Property<?> property : this.properties){
            server_properties_table_view.getItems().add(property);
        }
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
