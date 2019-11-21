package com.tycho.mss;

import com.tycho.mss.command.Command;
import com.tycho.mss.util.StreamReader;
import com.tycho.mss.util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a custom shell that wraps around the standard Minecraft server. It can intercept player messages and execute custom commands.
 */
public class ServerShell {

    /**
     * Possible states for the Minecraft server.
     */
    private enum State {
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
    private static final Pattern CHAT_MESSAGE_PATTERN = Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}] \\[Server thread\\/INFO]: <(?<player>.+?)> (?<message>.+)$");
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^" + COMMAND_PREFIX + "(?<command>[^ ].*?)(?: (?<parameters>[^ ].*))?$");
    private static final Pattern SERVER_STARTED_PATTERN = Pattern.compile("\\[Server thread\\/INFO]: Done \\(.+\\)!");
    private static final Pattern PLAYER_CONNECTED_PATTERN = Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}] \\[Server thread\\/INFO]: (?<player>[^ ]+)\\[(?<ip>.+)] logged in with entity id (?<entity>\\d+) at \\((?<x>.+), (?<y>.+), (?<z>.+)\\)$");
    private static final Pattern PLAYER_DISCONNECTED_PATTERN = Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}] \\[Server thread\\/INFO]: (?<player>.+) lost connection: (?<reason>.+)$");

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
    private final List<Command> customCommands = new ArrayList<>();

    /**
     * List of players currently connected to the server.
     */
    private final List<Player> players = new ArrayList<>();

    public ServerShell(final File serverJar) {
        this.serverJar = serverJar;
        this.authorize("TychoTheTaco", "help");
        this.authorize("TychoTheTaco", "here");
        this.authorize("TychoTheTaco", "mcrandom");
    }

    private final Map<String, Set<String>> permissions = new HashMap<>();

    private final List<PendingResult> pendingResults = new ArrayList<>();

    public void authorize(final String player, final String command) {
        Set<String> commands = permissions.get(player);
        if (commands == null) {
            commands = new HashSet<>();

            if (command.equals("@a")) {
                for (Command customCommand : customCommands) {
                    commands.add(customCommand.getCommand());
                }
            } else {
                commands.add(command);
            }

            this.permissions.put(player, commands);
        } else {
            if (command.equals("@a")) {
                for (Command customCommand : customCommands) {
                    commands.add(customCommand.getCommand());
                }
            } else {
                commands.add(command);
            }
        }
    }

    public void deauthorize(final String player, final String command) {

    }

    public boolean isAuthorized(final String player, final String command) {
        return this.permissions.containsKey(player) && this.permissions.get(player).contains(command);
    }

    private OutputStream inputStream;
    private InputStream outputStream;

    public OutputStream getInputStream() {
        return inputStream;
    }

    public InputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Start the Minecraft server with the specified launch options.
     *
     * @param options An optional list of Java launch options.
     */
    public void startServer(final String... options) {
        this.state = State.STARTING;
        notifyOnServerStarting();

        final List<String> command = new ArrayList<>();
        command.add("java");
        for (String option : options) {
            command.add("-" + option);
        }
        command.add("-jar");
        command.add(this.serverJar.getAbsolutePath());
        command.add("nogui");

        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        System.out.println(processBuilder.command());

        try {
            final Process process = processBuilder.start();

            this.inputStream = process.getOutputStream();
            this.outputStream = process.getInputStream();
            notifyOnServerIOready();

            this.serverInputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            final StreamReader errorOutput = new StreamReader(process.getErrorStream());
            errorOutput.start();

            //Read server output
            new Thread(() -> {
                try {
                    final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        notifyOnOutput(line);

                        //Check for pending results
                        boolean handled = false;
                        final Iterator<PendingResult> iterator = this.pendingResults.iterator();
                        while (iterator.hasNext()) {
                            final PendingResult pendingResult = iterator.next();
                            final Matcher matcher = pendingResult.getPattern().matcher(line);
                            if (matcher.find()) {
                                handled |= pendingResult.onResult(matcher);
                                iterator.remove();
                            }
                        }
                        if (handled) continue;

                        //Check if this is a chat message
                        Matcher matcher = CHAT_MESSAGE_PATTERN.matcher(line);
                        if (matcher.find()) {
                            final String player = matcher.group("player");
                            final String message = matcher.group("message");

                            //Check if this is a command
                            matcher = COMMAND_PATTERN.matcher(message);
                            if (matcher.find()) {
                                final String cmd = matcher.group("command");
                                final String parameters = matcher.group("parameters") == null ? "" : matcher.group("parameters");
                                onCommand(player, cmd, parameters.split(" "));
                            }
                        }else{
                            //Check if server is done starting
                            if (this.state == State.STARTING) {
                                matcher = SERVER_STARTED_PATTERN.matcher(line);
                                if (matcher.find()) {
                                    onServerStarted();
                                    continue;
                                }
                            }

                            //Check if a player connected
                            matcher = PLAYER_CONNECTED_PATTERN.matcher(line);
                            if (matcher.find()) {
                                final String username = matcher.group("player");
                                final String ipAddress = matcher.group("ip");
                                final Player player = new Player(username, ipAddress);
                                this.players.add(player);
                                this.notifyOnPlayerConnected(player);

                                //Send welcome message
                                final JSONObject root = Utils.createText("Welcome to the server " + player.getUsername() + "! Type \"!help\" for a list of commands.", "aqua");
                                this.tellraw("@a", root);
                            }

                            //Check if a player disconnected
                            matcher = PLAYER_DISCONNECTED_PATTERN.matcher(line);
                            if (matcher.find()) {
                                final String username = matcher.group("player");
                                final String reason = matcher.group("reason");
                                final Player player = getPlayer(username);
                                notifyOnPlayerDisconnected(player);
                                this.players.remove(player);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            //Read this program's input stream
            new Thread(() -> {
                try {
                    final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        onCommand("server", line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            notifyOnServerStopped();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCustomCommand(final Command command) {
        this.customCommands.add(command);
    }

    public void tellraw(final String player, final JSONObject jsonObject) throws IOException {
        this.execute("tellraw " + player + " " + jsonObject.toString());
    }

    private void onServerStarted() {
        System.out.println("SERVER STARTED!");
        this.state = State.ONLINE;
        try {
            execute("time set 0");
            execute("weather clear");
            execute("difficulty peaceful");
        } catch (IOException e) {
        }
    }

    private void onCommand(final String player, final String command, final String... parameters) throws IOException {
        //Check if this is a custom command
        for (Command cmd : customCommands) {
            if (command.equals(cmd.getCommand())) {

                //Make sure the player is authorized to use this command
                if (!isAuthorized(player, command.split(" ")[0])) {
                    final JSONObject root = Utils.createText("Unauthorized: ", "red");
                    final JSONArray extra = new JSONArray();
                    extra.add(Utils.createText("You are not authorized to use this command!", "gray"));
                    root.put("extra", extra);
                    this.tellraw(player, root);
                    return;
                }

                new Thread(() -> {
                    try {
                        System.out.println("COMMAND: " + command);
                        System.out.println("PRAMS: " + parameters.length);
                        cmd.execute(player, ServerShell.this, parameters);
                    } catch (Command.InvalidParametersException e) {
                        try {
                            tellraw(player, e.getJson());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            this.tellraw(player, Utils.createText("Error executing command: " + e.toString(), "red"));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
                return;
            }
        }

        //It was not a custom command, pass it to the server
        execute(command);
    }

    public void execute(final String command) throws IOException {
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
                synchronized (LOCK) {
                    LOCK.notify();
                }
                return false;
            }
        });

        synchronized (LOCK) {
            System.out.println("WAITING...");
            try {
                execute(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
            LOCK.wait();
        }
        System.out.println("FINISHED!");

        return container.matcher;
    }

    class Container {
        Matcher matcher;
    }

    public List<Player> getPlayers() {
        return players;
    }

    abstract class PendingResult {
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

    public List<Command> getCustomCommands() {
        return customCommands;
    }

    public Player getPlayer(final String username){
        for (Player player : this.players){
            if (player.getUsername().equals(username)) return player;
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Event listener
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public interface EventListener{
        void onServerStarting();
        void onServerIOready();
        void onServerStarted();
        void onServerStopped();
        void onPlayerConnected(final Player player);
        void onPlayerDisconnected(final Player player);
        void onOutput(final String message);
    }

    private final CopyOnWriteArrayList<EventListener> eventListeners = new CopyOnWriteArrayList<>();

    public void addEventListener(final EventListener listener){
        this.eventListeners.add(listener);
    }

    public void removeEventListener(final EventListener listener){
        this.eventListeners.remove(listener);
    }

    private void notifyOnServerStarting(){
        for (EventListener listener : this.eventListeners){
            listener.onServerStarting();
        }
    }

    private void notifyOnServerIOready(){
        for (EventListener listener : this.eventListeners){
            listener.onServerIOready();
        }
    }

    private void notifyOnOutput(final String message){
        for (EventListener listener : this.eventListeners){
            listener.onOutput(message);
        }
    }

    private void notifyOnServerStarted(){
        for (EventListener listener : this.eventListeners){
            listener.onServerStarted();
        }
    }

    private void notifyOnServerStopped(){
        for (EventListener listener : this.eventListeners){
            listener.onServerStopped();
        }
    }

    private void notifyOnPlayerConnected(final Player player){
        for (EventListener listener : this.eventListeners){
            listener.onPlayerConnected(player);
        }
    }

    private void notifyOnPlayerDisconnected(final Player player){
        for (EventListener listener : this.eventListeners){
            listener.onPlayerDisconnected(player);
        }
    }
}
