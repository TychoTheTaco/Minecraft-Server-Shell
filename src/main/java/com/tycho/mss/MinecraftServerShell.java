package com.tycho.mss;

import com.tycho.mss.command.GiveRandomItemCommand;
import com.tycho.mss.command.HelpCommand;
import com.tycho.mss.command.HereCommand;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MinecraftServerShell extends Application{

    public static final String APP_NAME = "Minecraft Server Shell";

    public static void main(String... args){
        launch(args);

        final ServerShell serverShell = new ServerShell(new File(args[0]));

        //Add custom commands
        try {
            serverShell.addCustomCommand(new GiveRandomItemCommand(new File("ids.txt")));
            serverShell.addCustomCommand(new HereCommand());
            serverShell.addCustomCommand(new HelpCommand());
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverShell.startServer("Xms3G", "Xmx4G");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/layout/main_layout.fxml"));
        primaryStage.setTitle(APP_NAME);
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }
}
