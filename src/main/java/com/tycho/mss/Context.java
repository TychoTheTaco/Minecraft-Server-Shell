package com.tycho.mss;

import com.tycho.mss.command.Command;
import com.tycho.mss.module.permission.PermissionsManager;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Context {

    /**
     * Execute the specified command in the Minecraft server process.
     *
     * @param command The command to execute.
     * @throws IOException
     */
    void execute(final String command) throws IOException;

    /**
     * Execute the 'tellraw' command with the specified JSON data. The output will only be visible for the specified player.
     * @param player The target player.
     * @param jsonObject A JSON object representing the text to display.
     */
    void tellraw(final String player, final JSONObject jsonObject);

    /**
     * Execute the specified command and wait for a result that matches the specified REGEX pattern. This method will block until a result is returned.
     * @param command The command to execute.
     * @param pattern A pattern defining the desired result.
     * @return
     * @throws InterruptedException
     */
    Matcher awaitResult(final String command, final Pattern pattern) throws InterruptedException;

    /**
     * Restore the world from the specified backup. This method will automatically stop and restart the server if it was already running when this method was called.
     *
     * @param backup The path to a ZIP file containing a backup of the server.
     */
    void restore(final Path backup);

    /**
     * Get a list of players currently online.
     * @return
     */
    List<Player> getPlayers();

    /**
     * Get a player using their username.
     * @param username
     * @return
     */
    Player getPlayer(final String username);

    List<Command> getCustomCommands();

    PermissionsManager getPermissionsManager();

    PlayerDatabaseManager getPlayerDatabaseManager();

    Path getServerJar();
}
