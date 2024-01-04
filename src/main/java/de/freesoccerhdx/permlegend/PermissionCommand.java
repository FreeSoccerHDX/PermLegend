package de.freesoccerhdx.permlegend;

import de.freesoccerhdx.lib.command.CommandListener;
import de.freesoccerhdx.lib.command.CustomCommand;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PermissionCommand extends CustomCommand {

    private final PermissionHandler permissionHandler;
    private final MessageConfig messageConfig;

    public PermissionCommand(Plugin plugin, PermissionHandler permissionHandler, MessageConfig messageConfig) {
        super("permission");
        this.permissionHandler = permissionHandler;
        this.messageConfig = messageConfig;

        // /permission info
        addInfoCommand();
        // /permission setGroup <Player> <GroupName>
        // /permission getGroup <Player>
        // /permission setTempGroup <Player> <GroupName> <Time>

        // /permission getGroupInfo <Group>
        // /permission setPrefix <Group> <Prefix>
        // /permission setSuffix <Group> <Suffix>
        // /permission setChatColor <Group> <Color>

        // /permission createGroup <GroupName> <Prefix>
        // /permission listGroups

        // /permission listPermissions <Group> <Page>
        // /permission addPermission <Group> <Permission>
        // /permission removePermission <Group> <Permission>

    }

    private void addInfoCommand() {
        this.apply("info", new CommandListener("Shows the Player his current Permission Information", argMap -> {
            if (argMap.getSender() instanceof Player player) {
                PlayerPermissionData playerPermissionData = this.permissionHandler.getPlayerPermissionData(player.getUniqueId());
                PermissionGroup defaultGroup;
                PermissionGroup tempGroup = null;
                Long tempGroupEnd = null;

                if (playerPermissionData == null) {
                    defaultGroup = this.permissionHandler.getGroup(player.getUniqueId());
                } else {
                    defaultGroup = this.permissionHandler.getGroupByGroupName(playerPermissionData.getGroupName());

                    if (playerPermissionData.hasTempGroup()) {
                        tempGroup = this.permissionHandler.getGroupByGroupName(playerPermissionData.getTempGroupName());
                        tempGroupEnd = playerPermissionData.getTempGroupEnd();
                    }
                }

                if (tempGroup == null) {
                    player.sendMessage(this.messageConfig.getCommandInfoDefaultGroup(defaultGroup.getName()));
                } else {
                    player.sendMessage(this.messageConfig.getCommandInfoDefaultAndTempGroup(defaultGroup.getName(), tempGroup.getName(), tempGroupEnd));
                }

            } else {
                argMap.getSender().sendMessage(this.messageConfig.getCommandNotAPlayer());
            }
        }));
    }

}
