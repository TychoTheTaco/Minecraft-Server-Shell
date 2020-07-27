package com.tycho.mss.command;

import com.tycho.mss.Context;
import com.tycho.mss.util.Utils;
import easytasks.ITask;
import easytasks.Task;
import easytasks.TaskAdapter;

import java.io.IOException;

public class BattleRoyaleCommand extends Command{

    private BattleRoyaleTimerTask battleRoyaleTimerTask;

    public BattleRoyaleCommand(){
        super("br");
    }

    @Override
    public void execute(String player, Context context, String... parameters) throws Exception {
        if (parameters.length < 1){
            throw new InvalidParametersException();
        }

        if ("prepare".equals(parameters[0])){

        }else if ("start".equals(parameters[0])){
            if (parameters.length < 5){
                throw new InvalidParametersException();
            }

            if (battleRoyaleTimerTask != null){
                throw new RuntimeException("Battle royale already in progress!");
            }

            //Put everyone on a team and disable PVP
            context.execute("team add Everyone");
            context.execute("team modify Everyone friendlyFire false");
            context.execute("team join Everyone @a");

            //set world border
            final int worldBorderSize = Integer.parseInt(parameters[1]);
            context.execute("execute at " + player + " run worldborder center ~ ~");
            context.execute("worldborder set " + worldBorderSize);

            //Clear inventory
            context.execute("clear @a");

            //Spread players
            final int minSpreadDistance = Integer.parseInt(parameters[1]) / 2;
            context.execute("execute at " + player + " run spreadplayers ~ ~ " + minSpreadDistance + " " + parameters[1] + " false @a");

            //Set daytime
            context.execute("time set day");
            context.execute("weather clear");

            //Start timer
            context.execute("scoreboard objectives add TimeRemaining dummy");
            context.execute("scoreboard objectives setdisplay list TimeRemaining");
            final long minutesRemaining = Integer.parseInt(parameters[3]);
            context.execute("scoreboard players set @a TimeRemaining " + minutesRemaining);
            battleRoyaleTimerTask = new BattleRoyaleTimerTask(context, Long.parseLong(parameters[3]) * 60 * 1000, Integer.parseInt(parameters[2]), Long.parseLong(parameters[4]) * 60 * 1000);
            battleRoyaleTimerTask.addTaskListener(new TaskAdapter(){
                @Override
                public void onTaskStopped(ITask task) {

                }
            });
            battleRoyaleTimerTask.startOnNewThread();
        }else if ("stop".equals(parameters[0])){
            if (battleRoyaleTimerTask != null){
                battleRoyaleTimerTask.stop();
                battleRoyaleTimerTask = null;
            }
        }
    }

    @Override
    public String getFormat() {
        return "prepare | start <start_size> <end_size> <prepare_time> <close_time> | stop";
    }

    @Override
    public String getDescription() {
        return "Starts a battle royale!";
    }

    private class BattleRoyaleTimerTask extends Task{

        private final Context context;
        private final long prepareTime;
        private final long closeTime;
        private final int endSize;

        private final long[] NOTIFICATION_TIMES;

        private final long GLOW_DELAY = 2 * 1000;

        public BattleRoyaleTimerTask(final Context context, final long prepareTime, final int endSize, final long closeTime){
            this.context = context;
            this.prepareTime = prepareTime;
            this.closeTime = closeTime;
            this.endSize = endSize;

            NOTIFICATION_TIMES = new long[]{
                    prepareTime / 2,
                    5 * 60 * 1000,
                    1 * 60 * 1000
            };
        }

        @Override
        protected void run() {
            int notificationIndex = 0;

            while (isRunning()){
                final long elapsedTime = System.currentTimeMillis() - getStartTime();
                if (elapsedTime >= prepareTime){
                    break;
                }

                try {
                    context.execute("scoreboard players remove @a TimeRemaining 1");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final long timeRemaining = prepareTime - elapsedTime;
                if (notificationIndex < NOTIFICATION_TIMES.length && timeRemaining <= NOTIFICATION_TIMES[notificationIndex]){
                    notificationIndex++;
                    final int minutesRemaining = (int) (timeRemaining / 1000 / 60);
                    context.tellraw("@a", Utils.createText(minutesRemaining + " minutes until border closes!", "red"));
                }

                try {
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                context.execute("scoreboard objectives remove TimeRemaining");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (shouldStop()){
                return;
            }

            final long combatStartTime = System.currentTimeMillis();
            try {
                context.execute("worldborder set " + endSize + " " + (closeTime / 1000));
            } catch (IOException e) {
                e.printStackTrace();
            }
            context.tellraw("@a", Utils.createText("Border closing! PVP is now enabled!", "red"));

            while (isRunning()){
                final long elapsedTime = System.currentTimeMillis() - combatStartTime;
                if (elapsedTime >= closeTime){
                    break;
                }

                try {
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (shouldStop()){
                return;
            }

            final long borderClosedTime = System.currentTimeMillis();
            while (isRunning()){
                final long elapsedTime = System.currentTimeMillis() - borderClosedTime;
                if (elapsedTime >= GLOW_DELAY){
                    break;
                }

                try {
                    Thread.sleep(1000 * 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Apply glow effect
            try {
                context.execute("effect give @a minecraft:glowing " + (60 * 60));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
