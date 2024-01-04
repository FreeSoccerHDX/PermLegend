package de.freesoccerhdx.permlegend;

import de.freesoccerhdx.lib.Methods;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class MessageConfig {

    private String joinmessage = "&e{prefix}{playername}{suffix} &ejoined the game.";
    private String chatmessage = "{prefix}{playername}{suffix}: {chatcolor}{message}";
    private String commandNotAPlayer = "&cThis Command is only for Players";

    private String commandInfoDefaultGroup = "&aYour Permission-Group is &b'{group}'";
    private String commandInfoDefaultAndTempGroup = "&aYour Permission-Group is currently &b'{tempgroup}'.\n&7You will lose this Group get the Group &b'{group}' &7in {time}.";

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
        // TODO load Messages from Config / or create if not existing
    }

    public String getJoinMessage(String prefix, String name, String suffix) {
        return Methods.replaceColorCodes(
                joinmessage.replace("{prefix}", prefix).replace("{playername}", name).replace("{suffix}", suffix));
    }

    public String getChatMessage(String prefix, String name, String suffix, String chatColor, String message) {
        return Methods.replaceColorCodes(chatmessage.replace("{prefix}", prefix).replace("{playername}", name)
                .replace("{suffix}", suffix).replace("{chatcolor}", chatColor).replace("{message}", message));
    }

    public String getCommandNotAPlayer() {
        return Methods.replaceColorCodes(commandNotAPlayer);
    }

    public String getCommandInfoDefaultGroup(String defaultGroup) {
        return Methods.replaceColorCodes(commandInfoDefaultGroup.replace("{group}", defaultGroup));
    }

    public String getCommandInfoDefaultAndTempGroup(String defaultGroup, String tempGroup, Long tempGroupEnd) {
        return Methods.replaceColorCodes(commandInfoDefaultAndTempGroup
                .replace("{group}", defaultGroup)
                .replace("{tempgroup}", tempGroup)
                .replace("{time}", Methods.secondsToCountdown(tempGroupEnd/1000)));
    }
}
