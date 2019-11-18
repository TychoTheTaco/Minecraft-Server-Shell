package mss;

import mss.command.Command;
import mss.command.GiveRandomItemCommand;
import mss.util.StreamReader;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Manager {

    enum State{
        STARTING,
        ONLINE,
        STOPPING,
        OFFLINE
    }

    private State state = State.OFFLINE;

    private final String serverJar;

    private BufferedWriter bufferedWriter;

    private static final String COMMAND_PREFIX = "!";

    private static final Pattern CHAT_MESSAGE_PATTERN = Pattern.compile("<(?<player>.+?)> (?<message>.+)");
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^" + COMMAND_PREFIX + "(?<command>[^ ].+)$");

    private static final Pattern SERVER_STARTED_PATTERN = Pattern.compile("\\[Server thread\\/INFO]: Done \\(.+\\)!");
    private static final Pattern LIST_PLAYERS_PATTERN = Pattern.compile("\\[Server thread\\/INFO]: There are 1 of a max 10 players online: (?<players>.+)");

    public Manager(final String serverJar){
        this.serverJar = serverJar;
        authorizedUsers.add("TychoTheTaco");

        try {
            customCommands.add(new GiveRandomItemCommand(new File("ids.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Set<String> authorizedUsers = new HashSet<>();

    private final Set<Command> customCommands = new HashSet<>();

    private final List<PendingResult> pendingResults = new ArrayList<>();

    public void startServer(final String... options){
        this.state = State.STARTING;

        final List<String> command = new ArrayList<>();
        command.add("java");
        for (String option : options){
            command.add("-" + option);
        }
        command.add("-jar");
        command.add(this.serverJar);
        command.add("nogui");

        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        System.out.println(processBuilder.command());

        try {
            final Process process = processBuilder.start();

            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            final StreamReader errorOutput = new StreamReader(process.getErrorStream());
            errorOutput.start();

            //Read server output
            new Thread(() -> {
                try {
                    final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = bufferedReader.readLine()) != null){
                        System.out.println(line);

                        //Check if server is done starting
                        if (this.state == State.STARTING){
                            final Matcher matcher = SERVER_STARTED_PATTERN.matcher(line);
                            if (matcher.find()){
                                onServerStarted();
                                continue;
                            }
                        }

                        //Check for pending results
                        boolean handled = false;
                        final Iterator<PendingResult> iterator = this.pendingResults.iterator();
                        while (iterator.hasNext()){
                            final PendingResult pendingResult = iterator.next();
                            final Matcher matcher = pendingResult.getPattern().matcher(line);
                            if (matcher.find()){
                                handled |= pendingResult.onResult(matcher);
                                iterator.remove();
                            }
                        }
                        if (handled) continue;

                        //Check if this is a chat message
                        Matcher matcher = CHAT_MESSAGE_PATTERN.matcher(line);
                        if (matcher.find()){
                            final String player = matcher.group("player");
                            final String message = matcher.group("message");

                            System.out.println("PLAYER: " + player);
                            System.out.println("MESSAGE: " + message);

                            //Check if this is a command
                            matcher = COMMAND_PATTERN.matcher(message);
                            if (matcher.find()){
                                final String cmd = matcher.group("command");

                                System.out.println("COMMAND: " + cmd);
                                System.out.println("AUTH: " + authorizedUsers.contains(player));

                                onCommand(player, cmd);
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }).start();

            //Read this program's input stream
            new Thread(() -> {
                try {
                    final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                    String line;
                    while ((line = bufferedReader.readLine()) != null){
                        System.out.println(line);
                        onCommand("server", line);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }).start();

            try {
                process.waitFor();
            }catch (InterruptedException e){}
            System.out.println("Process ended.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onServerStarted(){
        System.out.println("SERVER STARTED!");
        try {
            sendCommand("time set 0");
            sendCommand("weather clear");
        }catch (IOException e){}
    }

    private void onCommand(final String player, final String command) throws IOException {
        //Check if this is a custom command
        for (Command cmd : customCommands){
            if (command.startsWith(cmd.getCommand())){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("COMMAND: " + command);
                            final String result = cmd.execute(Manager.this, command.replace(cmd.getCommand(), "").trim().split(" "));
                            if (result != null) sendCommand("say " + result);
                        }catch (Exception e){
                            System.out.println("ERROR: " + e.getMessage());
                            try {
                                sendCommand("msg " + player + " " + e.getMessage());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }).start();
                return;
            }
        }

        //It was not a custom command, pass it to the server
        sendCommand(command);
    }

    public void sendCommand(final String command) throws IOException{
        this.bufferedWriter.write(command);
        this.bufferedWriter.write("\n");
        this.bufferedWriter.flush();
    }

    public Matcher awaitResult(final String command, final Pattern pattern) throws InterruptedException {
        final Object LOCK = new Object();
        final Container container = new Container();
        pendingResults.add(new PendingResult(command, pattern) {
            @Override
            boolean onResult(Matcher matcher) {
                System.out.println("RESULT FOUND: " + matcher.group());
                container.matcher = matcher;
                synchronized (LOCK){
                    LOCK.notify();
                }
                return false;
            }
        });

        synchronized (LOCK){
            System.out.println("WAITING...");
            try {
                sendCommand(command);
            }catch (IOException e){
                e.printStackTrace();
            }
            LOCK.wait();
        }
        System.out.println("FINISHED!");

        return container.matcher;
    }

    class Container{
        Matcher matcher;
    }

    public List<String> getAllPlayers(){
        final List<String> players = new ArrayList<>();
        try {
            final Matcher matcher = awaitResult("list", LIST_PLAYERS_PATTERN);
            players.addAll(Arrays.asList(matcher.group("players").replace(" ", "").split(",")));
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return players;
    }

    abstract class PendingResult{
        private final String command;
        private final Pattern pattern;

        public PendingResult(String command, Pattern pattern) {
            this.command = command;
            this.pattern = pattern;
        }

        abstract boolean onResult(final Matcher matcher);

        public String getCommand() {
            return command;
        }

        public Pattern getPattern() {
            return pattern;
        }
    }
}
