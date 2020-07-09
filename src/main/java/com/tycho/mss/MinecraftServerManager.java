package com.tycho.mss;

import com.tycho.mss.layout.MainLayout;
import com.tycho.mss.util.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Files;
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

    private static ServerShell serverShell;

    private static MainLayout mainLayoutController;

    public static void main(String... args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(APP_NAME);

        //Load user preferences
        Preferences.load();

        //Load main layout
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/main_layout.fxml"));
        final Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
        primaryStage.setScene(scene);
        mainLayoutController = loader.getController();

        //Attempt to create a server shell
        serverShell = createServerShell();
        mainLayoutController.setServerShell(serverShell);

        //Show the main stage
        primaryStage.sizeToScene();
        primaryStage.show();
        primaryStage.setOnHidden(event -> {
            if (serverShell != null && serverShell.getState() != ServerShell.State.OFFLINE) serverShell.stop();
            mainLayoutController.onHidden();
        });
    }

    public static void refresh(){
        if (serverShell == null || serverShell.getState() == ServerShell.State.OFFLINE){
            serverShell = createServerShell();
            mainLayoutController.setServerShell(serverShell);
        }else{
            System.out.println("Server must be offline to apply changes!");
        }
    }

    private static ServerShell createServerShell(){
        //Validate server jar
        final Path serverJar = Preferences.getServerJar();
        if (serverShell == null || serverShell.getServerJar() != serverJar){
            if (serverJar == null || !Files.exists(serverJar)){
                System.out.println("INVALID SERVER JAR");
                return null;
            }

            //Create new server with the updated JAR
            return new ServerShell(serverJar);
        }
        return null;
    }

    public static void start(){
        createServerShell();
        serverShell.startOnNewThread();
    }

    public static ServerShell getServerShell() {
        return serverShell;
    }
}
