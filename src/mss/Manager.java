package mss;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Manager {

    private final String serverJar;

    private BufferedWriter bufferedWriter;

    private static final String COMMAND_PREFIX = "!";

    private static final Pattern CHAT_MESSAGE_PATTERN = Pattern.compile("<(?<player>.+?)> (?<message>.+)");
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^" + COMMAND_PREFIX + "(?<command>[^ ].+)$");

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

    public void startServer(final String... options){
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
            //final StreamReader stdOutput = new StreamReader(process.getInputStream());
            final StreamReader errorOutput = new StreamReader(process.getErrorStream());
            //stdOutput.start();
            errorOutput.start();

            //Read server output
            new Thread(() -> {
                try {
                    final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = bufferedReader.readLine()) != null){
                        System.out.println(line);

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

    private void onCommand(final String player, final String command) throws IOException {
        //Check if this is a custom command
        for (Command cmd : customCommands){
            if (command.startsWith(cmd.getCommand())){
                try {
                    System.out.println("COMMAND: " + command);
                    final String result = cmd.execute(this, command.replace(cmd.getCommand(), "").trim().split(" "));
                    if (result != null) sendCommand("say " + result);
                }catch (Exception e){
                    System.out.println("ERROR: " + e.getMessage());
                    sendCommand("msg " + player + " " + e.getMessage());
                }
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

    public List<String> getAllPlayers(){
        final List<String> players = new ArrayList<>();

        return players;
    }
}
