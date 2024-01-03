package de.freesoccerhdx.lib;

import org.bukkit.ChatColor;

public class Methods {

    public static String replaceColorCodes(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}
