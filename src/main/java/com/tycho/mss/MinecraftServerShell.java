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
        final ServerShell.LaunchConfiguration launchConfiguration = new ServerShell.LaunchConfiguration();
        launchConfiguration.setServerJar(new File(args[0]));
        launchConfiguration.setLaunchOptions(new String[]{"Xms3G", "Xmx4G"});
        serverShell = new ServerShell(launchConfiguration);

        /*serverShell.addEventListener(new ServerShell.EventAdapter(){
            @Override
            public void onServerStarted() {
                try {
                    serverShell.execute("time set 0");
                    serverShell.execute("weather clear");
                    serverShell.execute("difficulty peaceful");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });*/

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

        serverShell.startOnNewThread();

        primaryStage.sizeToScene();
        primaryStage.show();
    }
}
