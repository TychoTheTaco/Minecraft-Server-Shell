package com.tycho.mss;

import com.tycho.mss.command.GiveRandomItemCommand;
import com.tycho.mss.command.HelpCommand;
import com.tycho.mss.command.HereCommand;
import com.tycho.mss.layout.MainLayout;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MinecraftServerShell extends Application{

    public static final String APP_NAME = "Minecraft Server Shell";

    private static ServerShell serverShell;

    public static void main(String... args){
        serverShell = new ServerShell(new File(args[0]));
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(APP_NAME);

        //Add custom commands
        try {
            serverShell.addCustomCommand(new GiveRandomItemCommand(new File("ids.txt")));
            serverShell.addCustomCommand(new HereCommand());
            serverShell.addCustomCommand(new HelpCommand());
        } catch (IOException e) {
            e.printStackTrace();
        }

        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/main_layout.fxml"));
        final Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/styles/dark.css").toExternalForm());
        primaryStage.setScene(scene);

        final MainLayout mainLayout = loader.getController();
        mainLayout.setServerShell(serverShell);

        new Thread(() -> serverShell.startServer("Xms3G", "Xmx4G")).start();

        primaryStage.sizeToScene();
        primaryStage.show();
    }
}