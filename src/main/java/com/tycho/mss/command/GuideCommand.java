package com.tycho.mss.command;

import com.tycho.mss.Context;
import com.tycho.mss.util.Utils;
import easytasks.Task;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuideCommand extends Command {

    private static final Pattern POSITION_PATTERN = Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}] \\[Server thread\\/INFO]: (?<player>.+) has the following entity data: \\[(?<x>-?\\d+\\.\\d+)d, (?<y>-?\\d+\\.\\d+)d, (?<z>-?\\d+\\.\\d+)d]");

    private final Map<String, GuideTask> tasks = new HashMap<>();

    private static final int PARTICLE_COUNT = 7;

    private static final double VERTICAL_OFFSET = 1.2;

    private static final double DEACTIVATE_RANGE = 5;

    public GuideCommand() {
        super("guide");
    }

    @Override
    public void execute(String player, Context context, String... parameters) throws Exception {
        if (parameters.length < 1){
            throw new InvalidParametersException();
        }

        //Stop
        if ("stop".equals(parameters[0])){
            if (this.tasks.get(player) != null) this.tasks.get(player).stop();
            this.tasks.put(player, null);
            return;
        }

        //If there is only 1 parameter it might be a player or the index of a saved location
        if (parameters.length == 1){
            try {
                final int index = Integer.parseInt(parameters[0]);
                final SavedLocation savedLocation = context.getPlayer(player).getSavedLocations().get(index);
                final int dx = savedLocation.getX();
                final int dy = savedLocation.getY();
                final int dz = savedLocation.getZ();

                if (tasks.get(player) != null) tasks.get(player).stop();
                final GuideTask guideTask = new GuideTask(player, dx + " " + dy + " " + dz, context);
                this.tasks.put(player, guideTask);
                guideTask.startOnNewThread();
            }catch (NumberFormatException e){
                if (tasks.get(player) != null) tasks.get(player).stop();
                final GuideTask guideTask = new GuideTask(player, parameters[0], context);
                this.tasks.put(player, guideTask);
                guideTask.startOnNewThread();
            }
        }else if (parameters.length == 3){
            try {
                final int dx = Integer.parseInt(parameters[0]);
                final int dy = Integer.parseInt(parameters[1]);
                final int dz = Integer.parseInt(parameters[2]);

                if (tasks.get(player) != null) tasks.get(player).stop();
                final GuideTask guideTask = new GuideTask(player, dx + " " + dy + " " + dz, context);
                this.tasks.put(player, guideTask);
                guideTask.startOnNewThread();
            }catch (NumberFormatException e){
                throw new InvalidParametersException();
            }
        }else{
            throw new InvalidParametersException();
        }
    }

    @Override
    public String getFormat() {
        return "<player> | <position> | <save_location_index> | stop";
    }

    @Override
    public String getDescription() {
        return "Guides you to another player or location.";
    }

    private static class GuideTask extends Task {

        private final String player;

        private final String target;

        private final Context context;

        public GuideTask(String player, String target, Context context) {
            this.player = player;
            this.target = target;
            this.context = context;
        }

        @Override
        protected void run() {
            while (isRunning()){
                try {
                    //Get player position
                    Matcher matcher = context.awaitResult("data get entity " + player + " Pos", POSITION_PATTERN);
                    final double x = Double.parseDouble(matcher.group("x"));
                    final double y = Double.parseDouble(matcher.group("y"));
                    final double z = Double.parseDouble(matcher.group("z"));

                    //Determine destination
                    final int dx;
                    final int dy;
                    final int dz;
                    final String[] split = target.split(" ");
                    if (split.length == 3){
                        dx = Integer.parseInt(split[0]);
                        dy = Integer.parseInt(split[1]);
                        dz = Integer.parseInt(split[2]);
                    }else{
                        //Get player position
                        matcher = context.awaitResult("data get entity " + target + " Pos", POSITION_PATTERN);
                        dx = Integer.parseInt(matcher.group("x"));
                        dy = Integer.parseInt(matcher.group("y"));
                        dz = Integer.parseInt(matcher.group("z"));
                    }

                    //Calculate direction
                    final double distance = Math.sqrt(Math.pow(dx - x, 2) + Math.pow(dy - y, 2) + Math.pow(dz - z, 2));
                    final double direction = Math.atan2(dz - z, dx - x);
                    final double angle = Math.asin((dy - y) / distance);

                    if (distance <= DEACTIVATE_RANGE){
                        stop();
                        final JSONObject root = Utils.createText("Destination reached!", "green");
                        context.tellraw(player, root);
                    }

                    //Spawn particles
                    for (int i = 1; i < Math.min((int) distance, PARTICLE_COUNT); i++){
                        context.execute("execute positioned " + x + " " + y + " " + z + " run particle composter"
                                + " ~" + String.format("%.2f", i * Math.cos(direction))
                                + " ~" + String.format("%.2f", i * Math.tan(angle) + VERTICAL_OFFSET)
                                + " ~" + String.format("%.2f", i * Math.sin(direction))
                                + " 0 0 0 0 1 normal");
                        try {
                            Thread.sleep(100);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }catch (InterruptedException | IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
