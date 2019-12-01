package com.tycho.mss;

import com.sun.jmx.snmp.SnmpNull;
import com.tycho.mss.command.GiveRandomItemCommand;
import com.tycho.mss.command.HelpCommand;
import com.tycho.mss.command.HereCommand;
import com.tycho.mss.layout.MainLayout;
import com.tycho.mss.util.Preferences;
import com.tycho.mss.util.Utils;
import easytasks.ITask;
import easytasks.Task;
import easytasks.TaskAdapter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * STUFF TO DO:
 * - Move all backups to new directory when the backup dir changes.
 * - Create progress bar for moving backups to a new directory
 * - Create backup schedule
 */
public class MinecraftServerShell extends Application{

    public static final String APP_NAME = "Minecraft Server Shell";

    private static ServerShell serverShell;

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

        //Attempt to create a server shell
        createServerShell();

        final MainLayout mainLayout = loader.getController();
        mainLayout.setServerShell(serverShell);

        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public static void restore(final File backup){
        final RestoreBackupTask restoreBackupTask = new RestoreBackupTask(backup, new File("C:\\Users\\Tycho\\Downloads\\out"));
        final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Restoring backup...", new ButtonType("Cancel", ButtonBar.ButtonData.OK_DONE));

        alert.setOnCloseRequest(event -> {
            if (restoreBackupTask.getState() != Task.State.STOPPED) event.consume();
        });

        restoreBackupTask.addTaskListener(new TaskAdapter(){
            @Override
            public void onTaskStopped(ITask task) {
                Platform.runLater(alert::close);
            }
        });

        alert.show();
        restoreBackupTask.startOnNewThread();
    }

    public static void createServerShell(){
        //Validate server jar
        final File serverJar = Preferences.getServerJar();
        if (serverShell == null || serverShell.getServerJar() != serverJar){
            if (serverJar == null || !serverJar.exists()){
                System.out.println("INVALID SERVER JAR");
                return;
            }

            //Create new server with the updated JAR
            serverShell = new ServerShell(serverJar);
        }
    }

    public static void start(){
        if (serverShell == null) createServerShell();
        serverShell.startOnNewThread();
    }

    public static ServerShell getServerShell() {
        return serverShell;
    }
}
