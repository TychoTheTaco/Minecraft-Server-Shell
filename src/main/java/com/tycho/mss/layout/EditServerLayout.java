package com.tycho.mss.layout;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EditServerLayout extends VBox {

    @FXML
    private ValidatedTextField server_name_input;

    @FXML
    private RadioButton auto_download_jar_button;

    @FXML
    private RadioButton custom_jar_button;

    @FXML
    private Pane download_option_container;

    @FXML
    private ComboBox<String> minecraft_version_input;

    @FXML
    private Label loading_label;

    @FXML
    private FileInputLayout custom_jar_input;

    private Node currentNode;

    public EditServerLayout() {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/edit_server_layout.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

        auto_download_jar_button.setSelected(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final JSONObject result = sendRequest("https://launchermeta.mojang.com/mc/game/version_manifest.json");
                System.out.println("RESULT: " + result);

                final List<String> versionCodes = new ArrayList<>();
                final JSONArray versions = (JSONArray) result.get("versions");
                for (Object object : versions) {
                    versionCodes.add((String) ((JSONObject) object).get("id"));
                }

                Platform.runLater(() -> {
                    minecraft_version_input.getItems().addAll(versionCodes);
                    minecraft_version_input.getSelectionModel().selectFirst();
                    loading_label.setVisible(false);
                    loading_label.setManaged(false);
                });
            }
        }).start();
    }

    private void setOption(final String option) {
        final String id;
        if (option.equals("download")) {
            id = "option_download_jar";
        } else {
            id = "option_custom_jar";
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
        System.out.println("REQUEST: " + url);
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            //Get response code
            if (connection.getResponseCode() == 200) {
                return readAsJsonObject(connection.getInputStream());
            } else {
                System.out.println("ERROR RESPONSE CODE: " + connection.getResponseCode());
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JSONObject readAsJsonObject(final InputStream inputStream) throws IOException, ParseException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();
        if (stringBuilder.toString().length() == 0) return new JSONObject();
        return (JSONObject) new JSONParser().parse(stringBuilder.toString());
    }
}
