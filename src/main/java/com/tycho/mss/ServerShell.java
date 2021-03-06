package com.tycho.mss;

import com.tycho.mss.command.*;
import com.tycho.mss.module.backup.RestoreBackupTask;
import com.tycho.mss.module.permission.Role;
import com.tycho.mss.util.Utils;
import easytasks.ITask;
import easytasks.TaskAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is a custom shell that wraps around the standard Minecraft server. It can read chat messages and execute custom commands.
 */
public class ServerShell implements Context{

    /**
     * Possible states for the Minecraft server.
     */
    public enum State {
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
    private static final String SERVER_LOG_PREFIX = "^\\[\\d{2}:\\d{2}:\\d{2}] \\[Server thread\\/INFO]: ";
    private static final Pattern CHAT_MESSAGE_PATTERN = Pattern.compile(SERVER_LOG_PREFIX + "<(?<player>.+?)> (?<message>.+)$");
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^" + COMMAND_PREFIX + "(?<input>(?<command>[^ !].*?)(?: (?<parameters>[^ ].*))?)$");
    private static final Pattern SERVER_STARTED_PATTERN = Pattern.compile("\\[Server thread\\/INFO]: Done \\(.+\\)!");
    private static final Pattern SERVER_STOPPING_PATTERN = Pattern.compile(SERVER_LOG_PREFIX + "Stopping the server$");
    private static final Pattern PLAYER_CONNECTED_PATTERN = Pattern.compile(SERVER_LOG_PREFIX + "(?<player>[^ ]+)\\[(?<ip>.+)] logged in with entity id (?<entity>\\d+) at \\((?<x>.+), (?<y>.+), (?<z>.+)\\)$");
    private static final Pattern PLAYER_DISCONNECTED_PATTERN = Pattern.compile(SERVER_LOG_PREFIX + "(?<player>[^ ]+) lost connection: (?<reason>.+)$");
    private static final Pattern PLAYER_AUTHENTICATED_PATTERN = Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}] \\[User Authenticator #\\d+\\/INFO]: UUID of player (?<player>.+) is (?<id>.+)$");

    /**
     * The vanilla Minecraft server JAR file used to run the server.
     */
    private final Path serverJar;

    /**
     * Mutex used to synchronize state.
     */
    private final Object STATE_MUTEX = new Object();

    /**
     * List of custom commands.
     */
    private final List<Command> customCommands = new ArrayList<>();

    /**
     * List of players currently connected to the server.
     */
    private final List<Player> players = new ArrayList<>();

    private final PlayerDatabase playerDatabase;

    /**
     * Authentication messages arrive before the player actually connects to the server. This list keeps track of those players who have been authenticated, but not connected yet.
     */
    private final Map<String, UUID> pendingAuthenticatedUsers = new HashMap<>();

    /**
     * After calling {@link #awaitMatch(String, Pattern)}, a pending result will be added to this list to keep track of its state.
     */
    private final List<PendingPatternMatch> pendingPatternMatches = new ArrayList<>();

    /**
     * The current state of the Minecraft server.
     */
    private State state = State.OFFLINE;

    /**
     * This {@link BufferedWriter} is used to write inputs to the Minecraft server.
     */
    private BufferedWriter serverInputWriter;

    /**
     * The time the server started.
     */
    private long startTime;

    private final ServerConfiguration serverConfiguration;

    public ServerShell(final ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
        this.serverJar = serverConfiguration.getJar();

        playerDatabase = new PlayerDatabase(getDirectory());

        //Add custom commands
        addCustomCommand(new HereCommand());
        addCustomCommand(new HelpCommand());
        addCustomCommand(new LocationCommand());
        addCustomCommand(new GuideCommand());
        addCustomCommand(new BackupCommand());
        addCustomCommand(new PermissionCommand());
        addCustomCommand(new BattleRoyaleCommand());
        addCustomCommand(new OpCommand());
        try {
            addCustomCommand(new GiveRandomItemCommand());
        } catch (IOException e) {
            System.err.println("Failed to load command: " + GiveRandomItemCommand.class.getSimpleName());
            e.printStackTrace();
        }
    }

    public Path getDirectory() {
        return this.serverJar.getParent();
    }

    public Map<String, String> getProperties() {
        final Map<String, String> properties = new HashMap<>();
        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(getDirectory() + File.separator + "server.properties"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final String[] split = line.split("=");
                if (split.length == 2) {
                    properties.put(split[0], split[1]);
                }
            }
        } catch (FileNotFoundException e){
           //Ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public Thread startOnNewThread() {
        if (this.state != State.OFFLINE) {
            throw new RuntimeException("Server is already started!");
        }
        verifyConfiguration();
        final Thread thread = new Thread(this::start);
        thread.start();
        return thread;
    }

    private void verifyConfiguration(){
        //Make sure server JAR exists
        if (!Files.exists(serverJar)){
            throw new RuntimeException("Server JAR does not exist!");
        }
    }

    public void start() {
        notifyOnOutput("[Minecraft Server Manager] Starting server...");
        this.onServerStarting();

        verifyConfiguration();

        final List<String> command = new ArrayList<>();
        command.add("java");
        for (String option : serverConfiguration.getLaunchOptions().split(" ")) {
            if (option.length() > 0) command.add(option);
        }
        command.add("-jar");
        command.add(this.serverJar.toString());
        command.add("nogui");

        final ProcessBuilder processBuilder = new ProcessBuilder(command).directory(this.serverJar.getParent().toFile());
        System.out.println(processBuilder.command());

        try {
            final Process process = processBuilder.start();

            notifyOnServerIOready();

            this.serverInputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            //final StreamReader errorOutput = new StreamReader(process.getErrorStream());
            //errorOutput.start();

            //Read error output
            new Thread(() -> {
                try {
                    final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }).start();

            //Read server output
            final InputStreamInterceptor inputStreamInterceptor = new InputStreamInterceptor(process);
            final Thread interceptor = new Thread(inputStreamInterceptor);
            interceptor.start();

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.onServerStopped();
        notifyOnOutput("[Minecraft Server Manager] Server stopped.");
    }

    public void stop() {
        if (this.state != State.OFFLINE){
            try {
                execute("stop");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class InputStreamInterceptor implements Runnable {

        private final Process process;

        public InputStreamInterceptor(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            try {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    notifyOnOutput(line);

                    //Check for pending results
                    synchronized (pendingPatternMatches) {
                        final List<PendingPatternMatch> pendingCancel = new ArrayList<>();

                        final Iterator<PendingPatternMatch> iterator = pendingPatternMatches.iterator();
                        while (iterator.hasNext()) {
                            final PendingPatternMatch pendingPatternMatch = iterator.next();

                            //Verify all required players are online
                            for (String username : pendingPatternMatch.getRequiredPlayers()){
                                if (getPlayer(username) == null){
                                    pendingCancel.add(pendingPatternMatch);
                                    //pendingPatternMatch.cancel();
                                    iterator.remove();
                                    break;
                                }
                            }

                            final Matcher matcher = pendingPatternMatch.getPattern().matcher(line);
                            if (matcher.find()) {
                                pendingPatternMatch.setResult(matcher);
                                iterator.remove();
                            }
                        }

                        for (PendingPatternMatch pendingPatternMatch : pendingCancel){
                            pendingPatternMatch.cancel();
                        }
                    }

                    //Check if a player was authenticated
                    Matcher matcher = PLAYER_AUTHENTICATED_PATTERN.matcher(line);
                    if (matcher.find()) {
                        final String player = matcher.group("player");
                        final UUID id = UUID.fromString(matcher.group("id"));
                        pendingAuthenticatedUsers.put(player, id);
                        continue;
                    }

                    //Check if this is a chat message
                    matcher = CHAT_MESSAGE_PATTERN.matcher(line);
                    if (matcher.find()) {
                        final String player = matcher.group("player");
                        final String message = matcher.group("message");

                        //Check if this is a command
                        matcher = COMMAND_PATTERN.matcher(message);
                        if (matcher.find()) {
                            final String input = matcher.group("input");
                            final String cmd = matcher.group("command");

                            //Check which command was entered
                            boolean isValidCommand = false;
                            for (Command command : getCustomCommands()) {
                                matcher = command.getPattern().matcher(input);
                                if (matcher.find()) {
                                    final String parameters = matcher.group("parameters");
                                    onCommand(player, command, clean(parameters));
                                    isValidCommand = true;
                                    break;
                                }
                            }

                            //Not a valid command, show an error message
                            if (!isValidCommand) {
                                tellraw(player, Utils.createText("Unknown command: ", "red", cmd, "white"));
                            }
                        }
                    } else {
                        //Check if server is done starting
                        if (state == State.STARTING) {
                            matcher = SERVER_STARTED_PATTERN.matcher(line);
                            if (matcher.find()) {
                                onServerStarted();
                                continue;
                            }
                        }

                        //Check if server is stopping
                        if (state == State.ONLINE) {
                            matcher = SERVER_STOPPING_PATTERN.matcher(line);
                            if (matcher.find()) {
                                onServerStopping();
                                continue;
                            }
                        }

                        //Check if a player connected
                        matcher = PLAYER_CONNECTED_PATTERN.matcher(line);
                        if (matcher.find()) {
                            final String username = matcher.group("player");
                            final String ipAddress = matcher.group("ip");

                            //Make sure user is authenticated
                            final UUID id = pendingAuthenticatedUsers.get(username);
                            assert id != null;
                            pendingAuthenticatedUsers.remove(username);

                            final Player player = new Player(id, username, ipAddress);
                            playerDatabase.load(player);
                            players.add(player);
                            notifyOnPlayerConnected(player);

                            //Auto assign roles
                            for (Role role : serverConfiguration.getPermissionsManager().getRoles()){
                                if (role.isAutoAssign()){
                                    serverConfiguration.getPermissionsManager().assign(player.getUsername(), role);
                                }
                            }

                            //Send welcome message
                            tellraw("@a", Utils.createText("Welcome to the server ", "aqua", player.getUsername(), Colors.PLAYER_COLOR, "! Type ", "aqua", "!help", Colors.COMMAND_COLOR, " for a list of commands.", "aqua"));
                        }

                        //Check if a player disconnected
                        matcher = PLAYER_DISCONNECTED_PATTERN.matcher(line);
                        if (matcher.find()) {
                            final String username = matcher.group("player");
                            final String reason = matcher.group("reason");
                            final Player player = getPlayer(username);
                            playerDatabase.save(player);
                            notifyOnPlayerDisconnected(player);
                            players.remove(player);

                            //Remove pending matches that require this player
                            synchronized (pendingPatternMatches){
                                final List<PendingPatternMatch> pendingCancel = pendingPatternMatches.stream().filter(new Predicate<PendingPatternMatch>() {
                                    @Override
                                    public boolean test(PendingPatternMatch pendingPatternMatch) {
                                        return pendingPatternMatch.getRequiredPlayers().contains(player.getUsername());
                                    }
                                }).collect(Collectors.toList());

                                for (PendingPatternMatch pendingPatternMatch : pendingCancel){
                                    pendingPatternMatch.cancel();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("SHELL CRASHED");
                e.printStackTrace();
                tellraw("@a", Utils.createText("Shell crashed!", "red"));
                run();
            }
        }
    }

    public void addCustomCommand(final Command command) {
        this.customCommands.add(command);
    }

    /*********************************************************************************************************************************
     * Context
     ********************************************************************************************************************************/

    @Override
    public void execute(String command) throws IOException {
        this.serverInputWriter.write(command);
        this.serverInputWriter.write("\n");
        this.serverInputWriter.flush();
    }

    @Override
    public void tellraw(String player, JSONObject jsonObject) {
        try {
            this.execute("tellraw " + player + " " + jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PendingPatternMatch awaitMatch(final String command, final Pattern pattern){
        final PendingPatternMatch pendingPatternMatch = new PendingPatternMatch(pattern){
            @Override
            protected void onCancel() {
                synchronized (pendingPatternMatches){
                    pendingPatternMatches.remove(this);
                }
            }
        };

        synchronized (pendingPatternMatches) {
            pendingPatternMatches.add(pendingPatternMatch);
        }

        try {
            execute(command);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pendingPatternMatch;
    }

    @Override
    public void restore(Path backup) {
        //Remember the initial state to restore later
        final State initialState = this.state;

        //Make sure the server is offline first
        if (this.state != State.OFFLINE) {
            tellraw("@a", Utils.createText("Going offline to restore backup...", "dark_aqua"));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stop();
        }

        try {
            synchronized (STATE_MUTEX) {
                while (this.state != State.OFFLINE) {
                    System.out.println("Waiting...");
                    STATE_MUTEX.wait();
                }

                final RestoreBackupTask restoreBackupTask = new RestoreBackupTask(backup, getDirectory());
                restoreBackupTask.addTaskListener(new TaskAdapter() {
                    @Override
                    public void onTaskStarted(ITask task) {
                        System.out.println("Restoring backup...");
                    }

                    @Override
                    public void onTaskStopped(ITask task) {
                        //Restore initial state
                        switch (initialState) {
                            case STARTING:
                            case ONLINE:
                                System.out.println("Restore finished! Starting server...");
                                startOnNewThread();
                                break;

                            case STOPPING:
                            case OFFLINE:
                                System.out.println("Restore finished!");
                                break;
                        }
                    }
                });
                restoreBackupTask.start();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public PlayerDatabase getPlayerDatabaseManager() {
        return playerDatabase;
    }

    @Override
    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    /*********************************************************************************************************************************
     * Misc.
     ********************************************************************************************************************************/

    public PendingPatternMatch listen(final Pattern pattern){
        final PendingPatternMatch pendingPatternMatch = new PendingPatternMatch(pattern){
            @Override
            protected void onCancel() {
                synchronized (pendingPatternMatches){
                    pendingPatternMatches.remove(this);
                }
            }
        };

        synchronized (pendingPatternMatches) {
            pendingPatternMatches.add(pendingPatternMatch);
        }

        return pendingPatternMatch;
    }

    private void onCommand(final String player, final Command command, final String... parameters) {
        //Make sure the player is authorized to use this command
        if (!serverConfiguration.getPermissionsManager().isAuthorized(player, command)) {
            final JSONObject root = Utils.createText("Unauthorized: ", "red");
            final JSONArray extra = new JSONArray();
            extra.add(Utils.createText("You are not authorized to use this command!", "gray"));
            root.put("extra", extra);
            this.tellraw(player, root);
            return;
        }

        //Execute the command
        new Thread(() -> {
            try {
                command.execute(player, ServerShell.this, parameters);
            } catch (Command.InvalidParametersException e) {
                tellraw(player, e.getJson());
            } catch (Exception e) {
                e.printStackTrace();
                this.tellraw(player, Utils.createText("Error executing command: " + e.toString(), "red"));
            }
        }).start();
    }

    private String[] clean(final String parameters) {
        if (parameters == null) return new String[]{};
        final String[] split = parameters.split(" +");
        final List<String> strings = new ArrayList<>();
        for (String string : split) {
            if (string.length() > 0) {
                strings.add(string);
            }
        }
        final String[] array = new String[strings.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = strings.get(i);
        }
        return array;
    }

    public static abstract class PendingPatternMatch {
        private final Pattern pattern;

        private Matcher result = null;

        private final List<String> requiredPlayers = new ArrayList<>();

        public PendingPatternMatch(Pattern pattern) {
            this.pattern = pattern;
        }

        boolean isResultSet = false;
        public synchronized void setResult(Matcher result) {
            if (!isResultSet){
                this.isResultSet = true;
                this.result = result;
                notifyAll();
            }else{
                throw new RuntimeException("Result was already set!");
            }
        }

        public PendingPatternMatch requiresPlayersOnline(final String... players){
            requiredPlayers.addAll(Arrays.asList(players));
            return this;
        }

        public List<String> getRequiredPlayers() {
            return requiredPlayers;
        }

        private boolean isCanceled = false;

        public Matcher getResult() {
            return result;
        }

        public synchronized void cancel(){
            isCanceled = true;
            onCancel();
            notifyAll();
        }

        protected abstract void onCancel();

        public synchronized Matcher waitFor() throws InterruptedException {
            if (isCanceled){
                return null;
            }
            if (!isResultSet){
                wait();
            }
            return result;
        }

        public Pattern getPattern() {
            return pattern;
        }
    }

    public List<Command> getCustomCommands() {
        return customCommands;
    }

    public Player getPlayer(final String username) {
        for (Player player : this.players) {
            if (player.getUsername().equals(username)) return player;
        }
        return null;
    }

    public State getState() {
        return state;
    }

    public long getUptime() {
        if (this.state == State.ONLINE) {
            return System.currentTimeMillis() - this.startTime;
        }
        return 0;
    }

    //This flag is used to detect a failed start. If the server stops and this flag is still false that meanst the server didnt start correctly.
    private boolean didServerStart = false;

    private void onServerStarting() {
        synchronized (STATE_MUTEX) {
            this.state = State.STARTING;
            notifyOnServerStarting();
        }
    }

    private void onServerStarted() {
        synchronized (STATE_MUTEX) {
            didServerStart = true;
            this.state = State.ONLINE;
            this.startTime = System.currentTimeMillis();
            notifyOnServerStarted();
        }
    }

    private void onServerStopping() {
        synchronized (STATE_MUTEX) {
            this.state = State.STOPPING;
            notifyOnServerStopping();
        }
    }

    private void onServerStopped() {
        synchronized (STATE_MUTEX) {
            this.players.clear();
            System.out.println("SERVER PROCESS STOPPED");
            this.state = State.OFFLINE;
            STATE_MUTEX.notifyAll();
            notifyOnServerStopped();

            //Check for failed start
            if (!didServerStart){
                for (EventListener listener : eventListeners){
                    listener.onFailedStart();
                }
                didServerStart = false;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Event listener
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public interface EventListener {
        void onServerStarting();

        void onFailedStart();

        void onServerIoReady();

        void onServerStarted();

        void onServerStopping();

        void onServerStopped();

        void onPlayerConnected(final Player player);

        void onPlayerDisconnected(final Player player);

        void onOutput(final String message);
    }

    public static class EventAdapter implements EventListener {
        @Override
        public void onServerStarting() {

        }

        @Override
        public void onFailedStart() {

        }

        @Override
        public void onServerIoReady() {

        }

        @Override
        public void onServerStarted() {

        }

        @Override
        public void onServerStopping() {

        }

        @Override
        public void onServerStopped() {

        }

        @Override
        public void onPlayerConnected(Player player) {

        }

        @Override
        public void onPlayerDisconnected(Player player) {

        }

        @Override
        public void onOutput(String message) {

        }
    }

    private final CopyOnWriteArrayList<EventListener> eventListeners = new CopyOnWriteArrayList<>();

    public void addEventListener(final EventListener listener) {
        this.eventListeners.add(listener);
    }

    public void removeEventListener(final EventListener listener) {
        this.eventListeners.remove(listener);
    }

    private void notifyOnServerStarting() {
        for (EventListener listener : this.eventListeners) {
            listener.onServerStarting();
        }
    }

    private void notifyOnServerIOready() {
        for (EventListener listener : this.eventListeners) {
            listener.onServerIoReady();
        }
    }

    private void notifyOnOutput(final String message) {
        for (EventListener listener : this.eventListeners) {
            listener.onOutput(message);
        }
    }

    private void notifyOnServerStarted() {
        for (EventListener listener : this.eventListeners) {
            listener.onServerStarted();
        }
    }

    private void notifyOnServerStopping() {
        for (EventListener listener : this.eventListeners) {
            listener.onServerStopping();
        }
    }

    private void notifyOnServerStopped() {
        for (EventListener listener : this.eventListeners) {
            listener.onServerStopped();
        }
    }

    private void notifyOnPlayerConnected(final Player player) {
        for (EventListener listener : this.eventListeners) {
            listener.onPlayerConnected(player);
        }
    }

    private void notifyOnPlayerDisconnected(final Player player) {
        for (EventListener listener : this.eventListeners) {
            listener.onPlayerDisconnected(player);
        }
    }
}
