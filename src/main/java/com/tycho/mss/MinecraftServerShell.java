package com.tycho.mss;

import com.tycho.mss.layout.MainLayout;
import com.tycho.mss.util.Preferences;
import easytasks.ITask;
import easytasks.Task;
import easytasks.TaskAdapter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
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
public class MinecraftServerShell extends Application{

    public static final String APP_NAME = "Minecraft Server Shell";

    public static final Path PRIVATE_DIR = Paths.get(System.getProperty("user.dir")).resolve(".mss");

    private static ServerShell serverShell;

    private static MainLayout mainLayoutController;

    public static void main(String... args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(APP_NAME);

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

        primaryStage.sizeToScene();
        primaryStage.show();
        primaryStage.setOnHidden(event -> {
            if (serverShell != null && serverShell.getState() != ServerShell.State.OFFLINE) serverShell.stop();
            mainLayoutController.onHidden();
        });
    }

    /*public static void restore(final File backup){
        if (serverShell == null || serverShell.getState() == ServerShell.State.OFFLINE){
            //restore now
        }else{
            //stop server first
        }
        final RestoreBackupTask restoreBackupTask = new RestoreBackupTask(backup, Preferences.getServerJar().getParentFile().toPath());
        final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Restoring backup...", new ButtonType("Cancel", ButtonBar.ButtonData.OK_DONE));

        restoreBackupTask.addTaskListener(new TaskAdapter(){
            @Override
            public void onTaskStopped(ITask task) {
                Platform.runLater(alert::close);
            }
        });
        alert.setOnCloseRequest(event -> {
            if (restoreBackupTask.getState() != Task.State.STOPPED) event.consume();
        });

        alert.show();
        restoreBackupTask.startOnNewThread();
    }*/

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
        final File serverJar = Preferences.getServerJar();
        if (serverShell == null || serverShell.getServerJar() != serverJar){
            if (serverJar == null || !serverJar.exists()){
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
