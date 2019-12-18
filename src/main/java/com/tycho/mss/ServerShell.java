package com.tycho.mss;

import com.tycho.mss.command.*;
import com.tycho.mss.util.Preferences;
import com.tycho.mss.util.StreamReader;
import com.tycho.mss.util.Utils;
import easytasks.ITask;
import easytasks.TaskAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final File serverJar;

    /**
     * Lock used to synchronize state.
     */
    private final Object STATE_LOCK = new Object();

    /**
     * The set of custom commands.
     */
    private final List<Command> customCommands = new ArrayList<>();

    /**
     * List of players currently connected to the server.
     */
    private final List<Player> players = new ArrayList<>();

    /**
     * Authentication messages arrive before the player actually connects to the server. This list keeps track of those players who have been authenticated, but not connected yet.
     */
    private final Map<String, UUID> pendingAuthenticatedUsers = new HashMap<>();

    /**
     * This map keeps track of which users have permission to use which commands.
     */
    private final Map<String, PermissionGroup> permissions = new HashMap<>();

    /**
     * After calling {@link #awaitResult(String, Pattern)}, a pending result will be added to this list to keep track of its state.
     */
    private final List<PendingResult> pendingResults = new ArrayList<>();

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

    public ServerShell(final File serverJar) {
        this.serverJar = serverJar;

        final PermissionGroup pleb = new PermissionGroup("pleb", HereCommand.class, HelpCommand.class, LocationCommand.class, GuideCommand.class);
        final PermissionGroup admin = new PermissionGroup("admin", GiveRandomItemCommand.class, BackupCommand.class, PermissionCommand.class);
        admin.commands.addAll(pleb.commands);

        this.permissions.put("TychoTheTaco", admin);
        this.permissions.put("Metroscorpio", pleb);
        this.permissions.put("Assassin_Actual7", pleb);

        //Add custom commands
        addCustomCommand(new HereCommand());
        addCustomCommand(new HelpCommand());
        addCustomCommand(new LocationCommand());
        addCustomCommand(new GuideCommand());
        addCustomCommand(new BackupCommand());
        addCustomCommand(new PermissionCommand());
        try {
            addCustomCommand(new GiveRandomItemCommand(new File("ids.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getServerJar() {
        return serverJar;
    }

    public File getDirectory() {
        return this.serverJar.getAbsoluteFile().getParentFile();
    }

    public static class PermissionGroup {

        private final String name;

        private Set<Class<? extends Command>> commands = new HashSet<>();

        public PermissionGroup(final String name, Class<? extends Command>... classes) {
            this.name = name;
            this.commands.addAll(Arrays.asList(classes));
        }

        public String getName() {
            return name;
        }

        public Set<Class<? extends Command>> getCommands() {
            return commands;
        }
    }

    public boolean isAuthorized(final String player, final Command command) {
        return this.permissions.containsKey(player) && this.permissions.get(player).commands.contains(command.getClass());
    }

    public void authorize(final String player, final Command command){
        final PermissionGroup permissionGroup = this.permissions.get(player);
        if (permissionGroup == null){
            this.permissions.put(player, new PermissionGroup("auth", command.getClass()));
        }else{
            this.permissions.get(player).commands.add(command.getClass());
        }
    }

    public void deauthorize(final String player, final Command command){
        if (!this.permissions.containsKey(player)) return;

        this.permissions.get(player).commands.remove(command.getClass());
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public Thread startOnNewThread() {
        if (this.state != State.OFFLINE) {
            throw new RuntimeException("Server is already started!");
        }
        final Thread thread = new Thread(this::start);
        thread.start();
        return thread;
    }

    public void start() {
        this.onServerStarting();

        final List<String> command = new ArrayList<>();
        command.add("java");
        for (String option : Preferences.getLaunchOptions()) {
            if (option.length() > 0) command.add(option);
        }
        command.add("-jar");
        command.add(this.serverJar.getAbsolutePath());
        command.add("nogui");

        final ProcessBuilder processBuilder = new ProcessBuilder(command).directory(this.serverJar.getParentFile());
        System.out.println(processBuilder.command());

        try {
            final Process process = processBuilder.start();

            notifyOnServerIOready();

            this.serverInputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            final StreamReader errorOutput = new StreamReader(process.getErrorStream());
            errorOutput.start();

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
    }

    public void stop() {
        try {
            execute("stop");
        } catch (IOException e) {
            e.printStackTrace();
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
                    System.out.println(line);
                    notifyOnOutput(line);

                    //Check for pending results
                    boolean handled = false;
                    synchronized (pendingResults){
                        final Iterator<PendingResult> iterator = pendingResults.iterator();
                        while (iterator.hasNext()) {
                            final PendingResult pendingResult = iterator.next();
                            final Matcher matcher = pendingResult.getPattern().matcher(line);
                            if (matcher.find()) {
                                handled |= pendingResult.onResult(matcher);
                                iterator.remove();
                            }
                        }
                    }

                    if (handled) continue;

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

                            if (cmd.equals("crash")) throw new RuntimeException("Crashed by user");

                            //Not a valid command, show an error message
                            if (!isValidCommand){
                                final JSONObject root = Utils.createText("Unknown command: ", "red");
                                final JSONArray extras = new JSONArray();
                                extras.add(Utils.createText(input, "white"));
                                root.put("extra", extras);
                                tellraw(player, root);
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
                            PlayerDatabaseManager.get(player);
                            players.add(player);
                            notifyOnPlayerConnected(player);

                            //Send welcome message
                            final JSONObject root = Utils.createText("Welcome to the server " + player.getUsername() + "! Type \"!help\" for a list of commands.", "aqua");
                            tellraw("@a", root);
                        }

                        //Check if a player disconnected
                        matcher = PLAYER_DISCONNECTED_PATTERN.matcher(line);
                        if (matcher.find()) {
                            final String username = matcher.group("player");
                            final String reason = matcher.group("reason");
                            final Player player = getPlayer(username);
                            PlayerDatabaseManager.save(player);
                            notifyOnPlayerDisconnected(player);
                            players.remove(player);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                tellraw("@a", Utils.createText("Shell crashed!", "red"));
                run();
            }
            System.out.println("STOPPED INTERCEPTOR");
        }
    }

    public void addCustomCommand(final Command command) {
        this.customCommands.add(command);
    }

    public void tellraw(final String player, final JSONObject jsonObject) {
        try {
            this.execute("tellraw " + player + " " + jsonObject.toString());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void onCommand(final String player, final Command command, final String... parameters) throws IOException {
        //Make sure the player is authorized to use this command
        if (!isAuthorized(player, command)) {
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

    /**
     * Execute the specified command in the Minecraft server process.
     * @param command The command to execute.
     * @throws IOException
     */
    public void execute(final String command) throws IOException {
        this.serverInputWriter.write(command);
        this.serverInputWriter.write("\n");
        this.serverInputWriter.flush();
    }

    /**
     * Restore the world from the specified backup. This method will automatically stop and restart the server if it was already running when this method was called.
     * @param backup The path to a ZIP file containing a backup of the server.
     */
    public void restore(final Path backup){
        //Remember the initial state to restore later
        final State initialState = this.state;

        //Make sure the server is offline first
        final Object LOCK = new Object();
        if (this.state != State.OFFLINE){
            tellraw("@a", Utils.createText("Going offline to restore backup...", "white"));
            try{
                Thread.sleep(3000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            addEventListener(new EventAdapter(){
                @Override
                public void onServerStopped() {
                    synchronized (LOCK){
                        System.out.println("Notify!");
                        LOCK.notifyAll();
                    }
                    removeEventListener(this);
                }
            });
            stop();
        }

        synchronized (LOCK){
            try {
                synchronized (STATE_LOCK){
                    if (this.state != State.OFFLINE){
                        System.out.println("Waiting...");
                        LOCK.wait();
                    }
                }

                final RestoreBackupTask restoreBackupTask = new RestoreBackupTask(backup.toFile(), Paths.get(getDirectory().getAbsolutePath()));
                restoreBackupTask.addTaskListener(new TaskAdapter(){
                    @Override
                    public void onTaskStarted(ITask task) {
                        System.out.println("Restoring backup...");
                    }

                    @Override
                    public void onTaskStopped(ITask task) {
                        //Restore initial state
                        switch (initialState){
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String[] clean(final String parameters){
        final String[] split = parameters.split(" +");
        final List<String> strings = new ArrayList<>();
        for (String string : split){
            if (string.length() > 0){
                strings.add(string);
            }
        }
        final String[] array = new String[strings.size()];
        for (int i = 0; i < array.length; i++){
            array[i] = strings.get(i);
        }
        return array;
    }

    public Matcher awaitResult(final String command, final Pattern pattern) throws InterruptedException {
        final Object LOCK = new Object();
        final Container container = new Container();

        //Add pending result
        synchronized (pendingResults){
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
        }

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

    static class Container {
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

    private void onServerStarting() {
        synchronized (STATE_LOCK) {
            this.state = State.STARTING;
            notifyOnServerStarting();
        }
    }

    private void onServerStarted() {
        synchronized (STATE_LOCK) {
            this.state = State.ONLINE;
            this.startTime = System.currentTimeMillis();
            notifyOnServerStarted();
        }
    }

    private void onServerStopping() {
        synchronized (STATE_LOCK) {
            this.state = State.STOPPING;
            notifyOnServerStopping();
        }
    }

    private void onServerStopped() {
        synchronized (STATE_LOCK) {
            this.players.clear();
            System.out.println("SERVER PROCESS STOPPED");
            this.state = State.OFFLINE;
            notifyOnServerStopped();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Event listener
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public interface EventListener {
        void onServerStarting();

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
