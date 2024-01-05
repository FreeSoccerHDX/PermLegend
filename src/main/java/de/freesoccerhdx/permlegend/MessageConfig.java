package de.freesoccerhdx.permlegend;

import de.freesoccerhdx.lib.Methods;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class MessageConfig {

    private String joinmessage = "&e{prefix}{playername}{suffix} &ejoined the game.";
    private String chatmessage = "{prefix}{playername}{suffix}: {chatcolor}{message}";
    private String commandNotAPlayer = "&cThis Command is only for Players";

    private String commandInfoDefaultGroup = "&aYour Permission-Group is &b'{group}'";
    private String commandInfoOtherDefaultGroup = "&aThe Permission-Group of {playername} is &b'{group}'";
    private String commandInfoDefaultAndTempGroup = "&aYour Permission-Group is currently &b'{tempgroup}'.\n&7You will lose this Group and get the Group &b'{group}' &7in {time}.";
    private String commandInfoOtherDefaultAndTempGroup = "&aThe Permission-Group of {playername }is currently &b'{tempgroup}'.\n&7You will lose this Group and get the Group &b'{group}' &7in {time}.";

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
            + "\n&ePermissions: &f'{permissionsize}&f'";

    private String listPageNotExisting = "&cThe Page your looking for does not exist.";
    private String listPermHeader = "&aShowing Permissions for Group &b{group}&7({permissionsize})\n&aPage {page} of {maxpage}";
    private String listPermInfo = "&b{id}. &a{permission}";

    private String commandHasPerm = "&aThe Player has this Permission.";
    private String commandHasNotPerm = "&cThe Player doesn't have this Permission.";
    private String commandSetGroupChanged = "&aThe Group of the Player has changed.";
    private String commandSetTempGroupFailed = "&cSomething went wrong! Did you use the correct input ?";

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
        File cfgFile = new File(this.plugin.getDataFolder(), "messages.yml");

        if(cfgFile.exists()) {
            YamlConfiguration cfg = new YamlConfiguration();
            try {
                cfg.load(cfgFile);
                joinmessage = cfg.getString("join-msg");
                chatmessage = cfg.getString("chat-msg");
                commandNotAPlayer = cfg.getString("cmd.not-a-player");
                commandInfoDefaultGroup = cfg.getString("cmd.info-default");
                commandInfoOtherDefaultGroup = cfg.getString("cmd.info-other-default");
                commandInfoDefaultAndTempGroup = cfg.getString("cmd.info-default-and-temp");
                commandInfoOtherDefaultAndTempGroup = cfg.getString("cmd.info-other-default-and-temp");
                commandListGroups = cfg.getString("cmd.list-groups");
                commandGroupAlreadyExists = cfg.getString("cmd.group-already-exist");
                commandGroupNotExisting = cfg.getString("cmd-group-not-existing");
                commandGroupCreated = cfg.getString("cmd.group-created");
                groupUpdated = cfg.getString("cmd.group-updated");
                commandGroupInfo = cfg.getString("cmd.group-info");
                listPageNotExisting = cfg.getString("cmd.list-page-not-exist");
                listPermHeader = cfg.getString("cmd.list-header");
                listPermInfo = cfg.getString("cmd.list-entry-info");
                commandHasPerm = cfg.getString("cmd.has-perm");
                commandHasNotPerm = cfg.getString("cmd.has-not-perm");
                commandSetGroupChanged = cfg.getString("cmd.set-group-changed");
                commandSetTempGroupFailed = cfg.getString("cmd.set-temp-group");

            } catch(Exception exception) {
                exception.printStackTrace();
            }
        } else {
            YamlConfiguration cfg = new YamlConfiguration();
            
            cfg.set("join-msg", joinmessage);
            cfg.set("chat-msg", chatmessage);
            cfg.set("cmd.not-a-player", commandNotAPlayer);
            cfg.set("cmd.info-default", commandInfoDefaultGroup);
            cfg.set("cmd.info-other-default", commandInfoOtherDefaultGroup);
            cfg.set("cmd.info-default-and-temp", commandInfoDefaultAndTempGroup);
            cfg.set("cmd.info-other-default-and-temp", commandInfoOtherDefaultAndTempGroup);
            cfg.set("cmd.list-groups", commandListGroups);
            cfg.set("cmd.group-already-exist", commandGroupAlreadyExists);
            cfg.set("cmd-group-not-existing", commandGroupNotExisting);
            cfg.set("cmd.group-created", commandGroupCreated);
            cfg.set("cmd.group-updated", groupUpdated);
            cfg.set("cmd.group-info", commandGroupInfo);
            cfg.set("cmd.list-page-not-exist", listPageNotExisting);
            cfg.set("cmd.list-header", listPermHeader);
            cfg.set("cmd.list-entry-info", listPermInfo);
            cfg.set("cmd.has-perm", commandHasPerm);
            cfg.set("cmd.has-not-perm", commandHasNotPerm);
            cfg.set("cmd.set-group-changed", commandSetGroupChanged);
            cfg.set("cmd.set-temp-group", commandSetTempGroupFailed);

            try {
                cfg.save(cfgFile);
            } catch(Exception exception) {
                exception.printStackTrace();
            }

        }
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

    public String getCommandInfoOtherDefaultGroup(String playername, String group) {
        return Methods.replaceColorCodes(commandInfoOtherDefaultGroup.replace("{group}", group).replace("{playername}", playername));
    }

    public String getCommandInfoDefaultAndTempGroup(String defaultGroup, String tempGroup, Long tempGroupEnd) {
        return Methods.replaceColorCodes(commandInfoDefaultAndTempGroup
                .replace("{group}", defaultGroup)
                .replace("{tempgroup}", tempGroup)
                .replace("{time}", Methods.secondsToCountdown(tempGroupEnd / 1000)));
    }

    public String getCommandInfoOtherDefaultAndTempGroup(String playername, String defaultGroup, String tempGroup, Long tempGroupEnd) {
        return Methods.replaceColorCodes(commandInfoOtherDefaultAndTempGroup
                .replace("{playername}", playername)
                .replace("{group}", defaultGroup)
                .replace("{tempgroup}", tempGroup)
                .replace("{time}", Methods.secondsToCountdown(tempGroupEnd / 1000)));
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

    public String getCommandGroupInfo(String group, String prefix, String suffix, String chatColor, String sibling,
            int permSize) {
        return Methods.replaceColorCodes(commandGroupInfo
                .replace("{group}", group)
                .replace("{prefix}", prefix)
                .replace("{suffix}", suffix)
                .replace("{chatcolor}", chatColor)
                .replace("{sibling}", sibling)
                .replace("{permissionsize}", "" + permSize));
    }

    public String getCommandGroupUpdated(String group) {
        return Methods.replaceColorCodes(groupUpdated.replace("{group}", group));
    }

    public String getCommandListPermissionPageNotExist() {
        return Methods.replaceColorCodes(listPageNotExisting);
    }

    public String getCommandListPermissionHeader(String group, int permSize, int page, int maxPage) {
        return Methods.replaceColorCodes(listPermHeader
                .replace("{group}", group)
                .replace("{permissionsize}", "" + permSize)
                .replace("{page}", "" + page)
                .replace("{maxpage}", "" + maxPage));
    }

    
    public String getCommandListPermissionInfo(int id, String permission) {
        return Methods.replaceColorCodes(listPermInfo).replace("{id}", ""+id).replace("{permission}", permission);
    }

    private String playerNotFoundByNameOrUUID = "&cCouldn't find Player by UUID or Name with following input:\n&c{input}";
    public String getCommandPlayerNotFoundByNameOrUUID(String playerNameUUID) {
        return Methods.replaceColorCodes(playerNotFoundByNameOrUUID.replace("{input}", playerNameUUID));
    }

    public String getCommandHasPerm() {
        return Methods.replaceColorCodes(commandHasPerm);
    }

    public String getCommandHasNotPerm() {
        return Methods.replaceColorCodes(commandHasNotPerm);
    }

    public String getCommandSetGroupChanged() {
        return Methods.replaceColorCodes(commandSetGroupChanged);
    }

    public String getCommandSetTempGroupFailed() {
        return Methods.replaceColorCodes(commandSetTempGroupFailed);
    }


}
