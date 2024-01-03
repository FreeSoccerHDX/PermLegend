package de.freesoccerhdx.permlegend;

import de.freesoccerhdx.lib.command.CustomCommand;
import org.bukkit.plugin.Plugin;

public class PermissionCommand extends CustomCommand {
    public PermissionCommand(Plugin plugin, PermissionHandler permissionHandler, MessageConfig messageConfig) {
        super("permission");


        /*
            /permission setGroup <Player> <GroupName>
            /permission getGroup <Player>
            /permission info
            /permission setTempGroup <Player> <GroupName> <Time>
            /permission setPrefix <Group> <Prefix>
            /permission getPrefix <Group>
            /permission createGroup <GroupName> <Prefix>
            /permission listGroups
            /permission listPermissions <Group> <Page>
            /permission addPermission <Group> <Permission>
            /permission removePermission <Group> <Permission>



         */
    }
}
