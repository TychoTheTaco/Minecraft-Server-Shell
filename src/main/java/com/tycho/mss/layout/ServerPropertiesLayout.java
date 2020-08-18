package com.tycho.mss.layout;

import com.tycho.mss.ServerShell;
import com.tycho.mss.property.BooleanProperty;
import com.tycho.mss.property.LongProperty;
import com.tycho.mss.property.Property;
import com.tycho.mss.property.StringProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerPropertiesLayout extends VBox {

    private ServerShell serverShell;

    @FXML
    private TableView<Property<?>> properties_table;

    @FXML
    private TextField search_text_field;

    private final List<Property<?>> properties = new ArrayList<>();

    public ServerPropertiesLayout(final ServerShell serverShell){
        this.serverShell = serverShell;

        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/server_properties_layout.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        search_text_field.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                filter(newValue);
            }
        });

        final TableColumn<Property<?>, String> keyColumn = new TableColumn<>("Key");
        keyColumn.setSortable(false);
        keyColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getKey()));
        properties_table.getColumns().add(keyColumn);

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
        valueColumn.prefWidthProperty().bind(properties_table.widthProperty().subtract(keyColumn.widthProperty()).subtract(11));
        properties_table.getColumns().add(valueColumn);

        loadProperties();
        properties_table.getItems().addAll(properties);
    }

    private void filter(final String query){
        if (query == null || query.isEmpty()){
            properties_table.getItems().setAll(properties);
        }else{
            properties_table.getItems().setAll(properties.stream().filter(property -> property.getKey().startsWith(query)).collect(Collectors.toList()));
        }
    }

    private void loadProperties() {
        final Map<String, String> propertyMap = serverShell.getProperties();
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
                    properties.add(new LongProperty(key, Long.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Long value) {
                            return 0 <= value;
                        }
                    });
                    break;

                case "function-permission-level":
                case "op-permission-level":
                    properties.add(new LongProperty(key, Long.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Long value) {
                            return 1 <= value && value <= 4;
                        }
                    });
                    break;

                case "max-world-size":
                    properties.add(new LongProperty(key, Long.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Long value) {
                            return 1 <= value && value <= 29999984;
                        }
                    });
                    break;

                case "network-compression-threshold":
                    properties.add(new LongProperty(key, Long.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Long value) {
                            return -1 <= value;
                        }
                    });
                    break;

                case "query.port":
                case "rcon.port":
                case "server-port":
                    properties.add(new LongProperty(key, Long.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Long value) {
                            return 1 <= value && value <= 65534;
                        }
                    });
                    break;

                case "view-distance":
                    properties.add(new LongProperty(key, Long.valueOf(propertyMap.get(key))) {
                        @Override
                        public boolean isValidValue(Long value) {
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
    }

    /*@FXML
    private void initialize() {
        //Command Name
        final TableColumn<Command, String> commandNameColumn = new TableColumn<>("Command");
        commandNameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getCommand()));
        commandNameColumn.setPrefWidth(150);
        custom_commands_table_view.getColumns().add(commandNameColumn);

        //Description
        final TableColumn<Command, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setSortable(false);
        descriptionColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getDescription()));
        descriptionColumn.prefWidthProperty().bind(custom_commands_table_view.widthProperty().subtract(commandNameColumn.widthProperty()).subtract(2));
        custom_commands_table_view.getColumns().add(descriptionColumn);
    }*/
}
