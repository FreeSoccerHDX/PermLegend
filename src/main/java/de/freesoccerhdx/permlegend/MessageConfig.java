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

    private String commandListGroups = "&aThere are currently following Groups:\n&7{groupnames}";
    private String commandGroupAlreadyExists = "&cThe Group &6{group} &calready exists.";
    private String commandGroupNotExisting = "&cThe Group &6{group} &cdoes not exist.";
    private String commandGroupCreated = "&aThe Group &6{group} &awas successfully created.";
    private String groupUpdated = "&aYou updated the Permissions-Group &b'{group}'";

    private String commandGroupInfo = "&aGroup Information: "
        + "\n&eName: &f'{group}&f'"
        + "\n&ePrefix: &f'{prefix}&f'"
        + "\n&eSuffix: &f'{suffix}&f'"
        + "\n&eChatColor: &f'{chatcolor}ChAtCoLoR&f'"
        + "\n&eSibling: &f'{sibling}&f'"
        + "\n&ePermissions: &f'{permissionsize}&f'"
        ;

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

    public String getCommandListGroups(String groups) {
        return Methods.replaceColorCodes(commandListGroups.replace("{groupnames}", groups));
    }

    public String getCommandGroupAlreadyExists(String group) {
        return Methods.replaceColorCodes(commandGroupAlreadyExists.replace("{group}", group));
    }

    public String getCommandGroupCreated(String group) {
        return Methods.replaceColorCodes(commandGroupCreated.replace("{group}", group));
    }

    public String getCommandGroupNotExisting(String group) {
        return Methods.replaceColorCodes(commandGroupNotExisting.replace("{group}", group));
    }

    public String getCommandGroupInfo(String group, String prefix, String suffix, String chatColor, String sibling, int permSize) {
        return Methods.replaceColorCodes(commandGroupInfo
            .replace("{group}", group)
            .replace("{prefix}", prefix)
            .replace("{suffix}", suffix)
            .replace("{chatcolor}", chatColor)
            .replace("{sibling}", sibling)
            .replace("{permissionsize}", ""+permSize)
            );
    }

    public String getCommandGroupUpdated(String group) {
        return Methods.replaceColorCodes(groupUpdated.replace("{group}", group));
    }
}
