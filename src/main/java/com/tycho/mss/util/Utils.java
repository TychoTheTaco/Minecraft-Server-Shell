package com.tycho.mss.util;

import org.json.simple.JSONObject;

public class Utils {

    public static JSONObject createText(final String text, final String color){
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", text);
        jsonObject.put("color", color);
        return jsonObject;
    }

    public static String formatTimeHuman(final long millis, final int precision) {

        if (millis == -1) {
            return "infinite time";
        }

        if (precision < 1) {
            return "";
        }

        final int milliseconds = (int) (millis % 1000);
        final int seconds = (int) ((millis / 1000) % 60);
        final int minutes = (int) ((millis / (1000 * 60)) % 60);
        final int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
        final int days = (int) ((millis / (1000 * 60 * 60 * 24)) % 7);

        final int[] times = new int[]{milliseconds, seconds, minutes, hours, days};
        final String[] parts = new String[]{milliseconds + " ms", seconds + " second", minutes + " minute", hours + " hour", days + " day"};

        final StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (int i = times.length - 1; i >= 0; i--) {
            if (times[i] > 0) {
                stringBuilder.append(parts[i]);
                if (i != 0 && times[i] != 1) {
                    stringBuilder.append('s');
                }
                stringBuilder.append(' ');
                count++;
                if (count == precision) {
                    break;
                }
            } else if (stringBuilder.length() > 0) {
                break;
            }
        }

        if (stringBuilder.length() == 0) {
            stringBuilder.append(parts[0]);
        }

        return stringBuilder.toString().trim();
    }

    public static String formatTimeStopwatch(final long millis, final int precision) {

        if (millis == -1) {
            return "infinite time";
        }

        if (precision < 1) {
            return "";
        }

        final int milliseconds = (int) (millis % 1000);
        final int seconds = (int) ((millis / 1000) % 60);
        final int minutes = (int) ((millis / (1000 * 60)) % 60);
        final int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
        final int days = (int) ((millis / (1000 * 60 * 60 * 24)) % 7);

        final int[] times = new int[]{seconds, minutes, hours, days};

        final StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (int i = times.length - 1; i >= 0; i--) {
            if (times[i] > 0 || i < precision) {
                stringBuilder.append(times[i]);
                stringBuilder.append(':');
                count++;
                if (count == precision) {
                    break;
                }
            } else if (stringBuilder.length() > 0) {
                break;
            }
        }

        return stringBuilder.toString().substring(0, stringBuilder.length() - 1).trim();
    }
}
