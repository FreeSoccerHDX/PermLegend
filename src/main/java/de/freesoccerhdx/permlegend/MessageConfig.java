package de.freesoccerhdx.permlegend;

import de.freesoccerhdx.lib.Methods;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class MessageConfig {


    private String joinmessage = "&e{prefix}{playername}{suffix} &ejoined the game.";
    private String chatmessage = "{prefix}{playername}{suffix}: {chatcolor}{message}";
    private final Plugin plugin;
    public MessageConfig(Plugin plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                loadConfig();
            }
        });
    }

    private void loadConfig() {

    }


    public String getJoinMessage(String prefix, String name, String suffix) {
        return Methods.replaceColorCodes(joinmessage.replace("{prefix}", prefix).replace("{playername}", name).replace("{suffix}", suffix));
    }


    public String getChatMessage(String prefix, String name, String suffix, String chatColor, String message) {
        return Methods.replaceColorCodes(chatmessage.replace("{prefix}", prefix).replace("{playername}", name).replace("{suffix}", suffix).replace("{chatcolor}", chatColor).replace("{message}",message));
    }
}
