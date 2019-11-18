package mss;

import mss.command.GiveRandomItemCommand;
import mss.command.HereCommand;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String... args){
        final ServerShell serverShell = new ServerShell(new File(args[0]));

        //Add custom commands
        try {
            serverShell.addCustomCommand(new GiveRandomItemCommand(new File("ids.txt")));
            serverShell.addCustomCommand(new HereCommand());
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverShell.startServer("Xms3G", "Xmx4G");
    }
}
