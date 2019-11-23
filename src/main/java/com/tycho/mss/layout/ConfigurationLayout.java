package com.tycho.mss.layout;

import com.tycho.mss.MenuPage;
import com.tycho.mss.ServerShell;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationLayout extends MenuPage {

    @FXML
    private TextField server_jar_text_field;

    @FXML
    private TextField launch_options_text_field;

    @FXML
    private TableView<String> server_properties_table_view;

    @FXML
    private Button save_button;

    private static final File CONFIG_FILE = new File(System.getProperty("user.dir") + File.separator + "mss_config.json");

    private Map<String, String> properties = new HashMap<>();

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
        final TableColumn<String, String> keyColumn = new TableColumn<>("Key");
        keyColumn.setSortable(false);
        keyColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<String, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<String, String> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue());
            }
        });
        server_properties_table_view.getColumns().add(keyColumn);

        //Key
        final TableColumn<String, String> valueColumn = new TableColumn<>("Value");
        valueColumn.setSortable(false);
        valueColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<String, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<String, String> param) {
                return new ReadOnlyObjectWrapper<>(properties.get(param.getValue()));
            }
        });
        server_properties_table_view.getColumns().add(valueColumn);
    }

    private ServerShell.LaunchConfiguration loadConfiguration(){
        if (CONFIG_FILE.exists()){
            System.out.println("PATH: " + CONFIG_FILE.getAbsolutePath());
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
        this.properties = serverShell.getProperties();

        server_properties_table_view.getItems().clear();
        for (String key : properties.keySet()){
            server_properties_table_view.getItems().add(key);
        }
    }
}
