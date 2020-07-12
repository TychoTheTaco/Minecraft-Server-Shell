package com.tycho.mss;

import com.tycho.mss.layout.MainLayout;
import com.tycho.mss.layout.ServerListLayout;
import com.tycho.mss.util.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * STUFF TO DO:
 * - Create progress bar for moving backups to a new directory
 * - Create backup schedule
 * - Help command should show only authorized commands
 *      > different modes: show all, show authorized, show all but different color for auth/non auth
 * - Guide command should ask the target player if they want to be tracked
 * - Make backups show server version. Can be found inside server.jar as version.json
 * - Restoring backups via command should ask first
 */
public class MinecraftServerManager extends Application{

    public static final String APP_NAME = "Minecraft Server Manager";

    /**
     * Directory where application files are stored (such as user preferences).
     */
    public static final Path PRIVATE_DIR = Paths.get(System.getProperty("user.dir")).resolve(".mss");

    private static MainLayout mainLayoutController;
    private static ServerListLayout serverListLayoutController;

    /**
     * This StackPane will be the root of the scene.
     */
    private static final StackPane stackPane = new StackPane();

    private static Node serverListLayout;
    private static Node mainLayout;
    private static Node currentPage;

    public static void main(String... args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(APP_NAME);

        //Improved text anti-aliasing
        System.setProperty("prism.lcdtext", "false");

        //Load user preferences
        Preferences.load();
        ServerManager.init();

        stackPane.setPrefWidth(900);
        stackPane.setPrefHeight(500);

        //Load server list layout
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/server_list_layout.fxml"));
        serverListLayout = loader.load();
        serverListLayoutController = loader.getController();

        //Set up scene
        final Scene scene = new Scene(stackPane);
        scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
        primaryStage.setScene(scene);

        setPage("server_list");

        //Show the main stage
        primaryStage.sizeToScene();
        primaryStage.show();
        primaryStage.setOnHidden(event -> {
            ServerManager.stopAll();
            mainLayoutController.onHidden();
        });
    }

    public static void setServer(final ServerConfiguration configuration){
        if (mainLayout == null){
            try {
                final FXMLLoader loader = new FXMLLoader(MinecraftServerManager.class.getResource("/layout/main_layout.fxml"));
                mainLayout = loader.load();
                mainLayoutController = loader.getController();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        setPage("main");
        mainLayoutController.setServerShell(ServerManager.getOrCreate(configuration));
    }

    public static void setPage(final String id){
        Node requestedPage;
        switch (id){
            case "server_list":
                requestedPage = serverListLayout;
                break;

            default:
                requestedPage = mainLayout;
        }

        if (currentPage != requestedPage){
            stackPane.getChildren().remove(currentPage);
            stackPane.getChildren().add(requestedPage);
            currentPage = requestedPage;
        }
    }
}
