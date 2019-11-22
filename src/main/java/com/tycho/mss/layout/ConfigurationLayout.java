package com.tycho.mss.layout;

import com.tycho.mss.MenuPage;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    private void initialize() {
        //Load the saved configuration
        final Configuration configuration = loadConfiguration();

        if (configuration.getServerJar() != null) server_jar_text_field.setText(configuration.getServerJar().getAbsolutePath());
        launch_options_text_field.setText(configuration.getLaunchOptions());

        server_properties_table_view.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        final Map<String, String> properties = readProperties(new File("server.properties"));

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

        for (String key : properties.keySet()){
            server_properties_table_view.getItems().add(key);
        }
    }

    private Configuration loadConfiguration(){
        final Configuration configuration = new Configuration();

        //Try to find the server jar in the current directory
        final File directory = new File(System.getProperty("user.dir"));
        for (File file : directory.listFiles()){
            if (file.getName().contains("server") && file.getName().contains("jar")){
                //This is probably it
                configuration.setServerJar(file);
                break;
            }
        }

        configuration.setLaunchOptions("-Xms3G -Xmx4G");

        return configuration;
    }

    private Map<String, String> readProperties(final File file){
        final Map<String, String> properties = new HashMap<>();

        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = bufferedReader.readLine()) != null){
                final String[] split = line.split("=");
                if (split.length == 2){
                    properties.put(split[0], split[1]);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return properties;
    }

    public class Configuration{

        private File serverJar;

        private String launchOptions;

        public File getServerJar() {
            return serverJar;
        }

        public void setServerJar(File serverJar) {
            this.serverJar = serverJar;
        }

        public String getLaunchOptions() {
            return launchOptions;
        }

        public void setLaunchOptions(String launchOptions) {
            this.launchOptions = launchOptions;
        }
    }
}
