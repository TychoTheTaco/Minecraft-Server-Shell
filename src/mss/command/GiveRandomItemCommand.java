package mss.command;

import mss.ServerShell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GiveRandomItemCommand extends Command {

    private static final Random RANDOM = new Random();

    private final List<String> ids = new ArrayList<>();

    public GiveRandomItemCommand(final File idsFile) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new FileReader(idsFile));
        String line;
        while ((line = bufferedReader.readLine()) != null){
            ids.add(line);
        }
        bufferedReader.close();
    }

    @Override
    public String execute(ServerShell serverShell, String... parameters) throws Exception {
        for (String string : parameters){
            System.out.println(string);
        }
        if (parameters.length < 2) throw new RuntimeException("Invalid parameters!");

        final String player = parameters[0];
        final int maxCount = Integer.parseInt(parameters[1]);

        if (player.equals("@a")){
            final List<String> players = serverShell.getAllPlayers();
            for (String p : players){
                final String result = give(p, maxCount, serverShell);
                serverShell.sendCommand("say " + result);
            }
            return null;
        }

        return give(player, maxCount, serverShell);
    }

    @Override
    public String getCommand() {
        return "mcrandom";
    }

    private String give(final String player, final int maxCount, final ServerShell serverShell) throws IOException {
        final String item = this.ids.get(RANDOM.nextInt(ids.size()));
        final int count = RANDOM.nextInt(maxCount) + 1;
        serverShell.sendCommand("give " + player + " " + item + " " + count);
        return "Gave §2" + count + " §e" + item + "§r to §d" + player + "§r!";
    }
}
