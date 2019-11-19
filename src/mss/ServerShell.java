package mss;

import mss.command.Command;
import mss.command.GiveRandomItemCommand;
import mss.util.StreamReader;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a custom shell that wraps around the standard Minecraft server. It can intercept player messages and execute custom commands.
 */
public class ServerShell {

    /**
     * Possible states for the Minecraft server.
     */
    private enum State{
        /**
         * The server is starting but has not started yet. Players cannot connect until the server becomes online.
         */
        STARTING,

        /**
         * The server is online and accepting new player connections.
         */
        ONLINE,

        /**
         * The server is stopping.
         */
        STOPPING,

        /**
         * The server is offline.
         */
        OFFLINE
    }

    /**
     * If this prefix is found at the beginning of any player message, it will be interpreted as a command.
     */
    private static final String COMMAND_PREFIX = "!";

    //REGEX Patterns
    private static final Pattern CHAT_MESSAGE_PATTERN = Pattern.compile("<(?<player>.+?)> (?<message>.+)");
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^" + COMMAND_PREFIX + "(?<command>[^ ].+)$");
    private static final Pattern SERVER_STARTED_PATTERN = Pattern.compile("\\[Server thread\\/INFO]: Done \\(.+\\)!");
    private static final Pattern LIST_PLAYERS_PATTERN = Pattern.compile("\\[Server thread\\/INFO]: There are 1 of a max 10 players online: (?<players>.+)");

    /**
     * The vanilla Minecraft server.jar file.
     */
    private final File serverJar;

    /**
     * The current state of the Minecraft server.
     */
    private State state = State.OFFLINE;

    /**
     * This {@link BufferedWriter} is used to write inputs to the Minecraft server.
     */
    private BufferedWriter serverInputWriter;

    /**
     * The set of custom commands.
     */
    private final Set<Command> customCommands = new HashSet<>();

    public ServerShell(final File serverJar){
        this.serverJar = serverJar;
        authorizedUsers.add("TychoTheTaco");
    }

    private final Set<String> authorizedUsers = new HashSet<>();

    private final List<PendingResult> pendingResults = new ArrayList<>();

    /**
     * Start the Minecraft server with the specified launch options.
     * @param options An optional list of Java launch options.
     */
    public void startServer(final String... options){
        this.state = State.STARTING;

        final List<String> command = new ArrayList<>();
        command.add("java");
        for (String option : options){
            command.add("-" + option);
        }
        command.add("-jar");
        command.add(this.serverJar.getAbsolutePath());
        command.add("nogui");

        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        System.out.println(processBuilder.command());

        try {
            final Process process = processBuilder.start();

            this.serverInputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
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

                            //Check if this is a command
                            matcher = COMMAND_PATTERN.matcher(message);
                            if (matcher.find()){
                                final String cmd = matcher.group("command");
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
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("Process ended.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCustomCommand(final Command command){
        this.customCommands.add(command);
    }

    public void msg(final String player, final String message) throws IOException {
        this.execute("msg " + player + " " + message);
    }

    private void onServerStarted(){
        System.out.println("SERVER STARTED!");
        try {
            execute("time set 0");
            execute("weather clear");
            execute("difficulty peaceful");
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
                            final String result = cmd.execute(player, ServerShell.this, command.replace(cmd.getCommand(), "").trim().split(" "));
                            if (result != null) execute("say " + result);
                        }catch (Command.InvalidParametersException e){
                            try {
                                execute("msg " + player + " " + e.getMessage());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            try {
                                execute("msg " + player + " " + e.getMessage());
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
        execute(command);
    }

    public void execute(final String command) throws IOException{
        this.serverInputWriter.write(command);
        this.serverInputWriter.write("\n");
        this.serverInputWriter.flush();
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
                execute(command);
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

    public Set<Command> getCustomCommands() {
        return customCommands;
    }
}
