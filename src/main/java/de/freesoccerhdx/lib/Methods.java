package de.freesoccerhdx.lib;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.ChatColor;

public class Methods {

    public static String replaceColorCodes(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String secondsToCountdown(long seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (days != 0 || hours > 0) {
            sb.append(hours < 10 ? "0"+hours : hours).append("h ");
        }
        if(days != 0 || hours != 0 || minutes > 0) {
            sb.append(minutes < 10 ? "0"+minutes : minutes).append("m ");
        }
        sb.append(seconds < 10 ? "0"+seconds : seconds).append("s");

        return sb.toString();
    }

    public static String timestampToFullDate(long timestamp) {
        return timestampToDateFormat(timestamp, "dd.MM.yyyy HH:mm:ss");
    }

    public static String timestampToDateFormat(long timestamp, String format) {
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static long calculateTimeFromInput(String input) {
        if(input.length() == 1) {
            return Long.parseLong(input);
        }
        long time = 0;
        String[] split = input.toLowerCase().split(" ");
        for (String s : split) {
            if (s.endsWith("d")) {
                time += Long.parseLong(s.replace("d", "")) * 24 * 60 * 60;
            } else if (s.endsWith("h")) {
                time += Long.parseLong(s.replace("h", "")) * 60 * 60;
            } else if (s.endsWith("m")) {
                time += Long.parseLong(s.replace("m", "")) * 60;
            } else if (s.endsWith("s")) {
                time += Long.parseLong(s.replace("s", ""));
            } else {
                time += Long.parseLong(s);
            }
        }
        return time;
    }


}
