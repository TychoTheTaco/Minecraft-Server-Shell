package com.tycho.mss.layout;

import com.tycho.mss.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.file.Path;

public class ConfigurationLayout implements Page, StatusHost, ServerShellConnection {

    @FXML
    private TextField server_name_text_field;

    @FXML
    private FileInputLayout server_jar_input;

    @FXML
    private TextField launch_options_text_field;

    @FXML
    private Button delete_server_button;

    @FXML
    private Button server_properties_button;

   /* @FXML
    private TableView<Property<?>> server_properties_table_view;*/

    @FXML
    private Button save_button;

    //private List<Property<?>> properties = new ArrayList<>();

    private ServerConfiguration configuration;

    private final StatusContainer statusContainer = new StatusContainer();

    @FXML
    private void initialize() {
        server_name_text_field.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                setDirty(isDirty());
            }
        });

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

        server_properties_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final ServerPropertiesLayout serverPropertiesLayout = new ServerPropertiesLayout(serverShell);
                final Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Server Properties");

                final Scene scene = new Scene(serverPropertiesLayout);
                scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
                stage.setScene(scene);
                stage.showAndWait();
            }
        });

        //Delete server button
        delete_server_button.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Are you sure you want to delete this server? All data including the Minecraft world, player data, and server configuration data will be permanently deleted! Any backups that were created will remain.", ButtonType.YES, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                ServerManager.delete(serverShell.getServerConfiguration());
                MinecraftServerManager.setPage("server_list");
            }
        });

        save_button.setOnAction(event -> {
            save_button.setDisable(true);

            configuration.setName(server_name_text_field.getText().trim());
            configuration.setJar(server_jar_input.getPath());
            configuration.setLaunchOptions(launch_options_text_field.getText().trim());
            ServerManager.save();

            //TODO: Save to server configuration
            //Preferences.setServerJar(server_jar_input.getPath());
            //MinecraftServerManager.refresh();
            //Preferences.setLaunchOptions(launch_options_text_field.getText());

            statusContainer.setStatus(StatusContainer.Status.OK);
            //initialConfiguration = getConfiguration();
            //setDirty(false);
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
        if (configuration == null) return false;
        final Path path = server_jar_input.getPath();
        return !(path != null && configuration.getJar().equals(server_jar_input.getPath()) && configuration.getLaunchOptions().equals(launch_options_text_field.getText()) && configuration.getName().equals(server_name_text_field.getText().trim()));
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

    private void setConfiguration(final ServerConfiguration configuration) {
        this.configuration = configuration;
        this.server_name_text_field.setText(configuration.getName());
        server_jar_input.setPath(configuration.getJar());
        launch_options_text_field.setText(configuration.getLaunchOptions());
    }

    @Override
    public void onPageSelected() {
        setConfiguration(configuration);
        statusContainer.setStatus(server_jar_input.isValid() ? StatusContainer.Status.OK : StatusContainer.Status.ERROR);
    }

    @Override
    public void onPageHidden() {

    }

    private ServerShell serverShell;

    @Override
    public void attach(ServerShell serverShell) {
        this.serverShell = serverShell;

        setConfiguration(serverShell.getServerConfiguration());
        statusContainer.setStatus(server_jar_input.isValid() ? StatusContainer.Status.OK : StatusContainer.Status.ERROR);
    }

    @Override
    public void detach(ServerShell serverShell) {
        this.serverShell = null;
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

}
