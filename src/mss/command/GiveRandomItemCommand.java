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
    public String execute(String player, ServerShell serverShell, String... parameters) throws Exception {
        for (String string : parameters){
            System.out.println(string);
        }
        if (parameters.length < 2) throw new InvalidParametersException();

        final String targetPlayer = parameters[0];
        final int maxCount = Integer.parseInt(parameters[1]);

        if (targetPlayer.equals("@a")){
            final List<String> players = serverShell.getAllPlayers();
            for (String p : players){
                final String result = give(p, maxCount, serverShell);
                serverShell.execute("say " + result);
            }
            return null;
        }

        return give(targetPlayer, maxCount, serverShell);
    }

    @Override
    public String getCommand() {
        return "mcrandom";
    }

    @Override
    public String getFormat() {
        return "<player> <maxCount>";
    }

    @Override
    public String getDescription() {
        return "Gives <player> up to <maxCount> random items.";
    }

    private String give(final String player, final int maxCount, final ServerShell serverShell) throws IOException {
        final String item = this.ids.get(RANDOM.nextInt(ids.size()));
        final int count = RANDOM.nextInt(maxCount) + 1;
        serverShell.execute("give " + player + " " + item + " " + count);
        return "Gave §2" + count + " §e" + item + "§r to §d" + player + "§r!";
    }
}
