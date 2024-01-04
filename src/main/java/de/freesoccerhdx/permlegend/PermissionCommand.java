package de.freesoccerhdx.permlegend;

import de.freesoccerhdx.lib.command.Arg;
import de.freesoccerhdx.lib.command.CommandListener;
import de.freesoccerhdx.lib.command.CustomCommand;
import de.freesoccerhdx.lib.command.MultiArgument;
import de.freesoccerhdx.lib.command.TypeArgument;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PermissionCommand extends CustomCommand {

    private final PermissionHandler permissionHandler;
    private final MessageConfig messageConfig;

    private final TypeArgument playerNameOrUUIDProvider;
    private final TypeArgument groupNameProvider;

    public PermissionCommand(Plugin plugin, PermissionHandler permissionHandler, MessageConfig messageConfig) {
        super("permission");
        this.permissionHandler = permissionHandler;
        this.messageConfig = messageConfig;

        this.playerNameOrUUIDProvider = new TypeArgument("<Player>") {

            @Override
            public boolean checkArgument(String arg) {
                if(!arg.contains("<") && !arg.contains(">")) {
                    //allow playernames between 2 and 16 or UUID's of length 36
                    return (arg.length() <= 16 && arg.length() >= 2) || (arg.length() == 36);
                }
                return false;
            }

            @Override
            public String[] getValidArguments() {
                return new String[] { "Playername/UUID" };
            }
        };

        this.groupNameProvider = new TypeArgument("<Group>") {

            @Override
            public boolean checkArgument(String arg) {
                return !arg.contains("<") && !arg.contains(">");
            }

            @Override
            public String[] getValidArguments() {
                return new String[] { "Default" };
            }
        };

        // /permission info
        addInfoCommand();
        // /permission listGroups
        addListGroupsCommand();
        // /permission createGroup <Group> "Prefix"
        addCreateCommand();

        // /permission getGroupInfo <Group>
        addGetGroupInfoCommand();
        // /permission setPrefix <Group> "Prefix"
        // /permission setSuffix <Group> "Suffix"
        // /permission setChatColor <Group> <Color>
        // /permission listPermissions <Group> <Page>
        // /permission addPermission <Group> <Permission>
        // /permission removePermission <Group> <Permission>

        // /permission getGroup <Player>
        // /permission setGroup <Player> <Group>
        // /permission setTempGroup <Player> <Group> <Time>


    }

    private void addInfoCommand() {
        this.apply("info", new CommandListener("Shows the Player his current Permission-Group information", argMap -> {
            if (argMap.getSender() instanceof Player player) {
                PlayerPermissionData playerPermissionData = this.permissionHandler
                        .getPlayerPermissionData(player.getUniqueId());
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
                    player.sendMessage(this.messageConfig.getCommandInfoDefaultAndTempGroup(defaultGroup.getName(),
                            tempGroup.getName(), tempGroupEnd));
                }

            } else {
                argMap.getSender().sendMessage(this.messageConfig.getCommandNotAPlayer());
            }
        }));
    }

    private void addListGroupsCommand() {
        this.apply("listGroups", new CommandListener("Lists all existing Groups", argMap -> {
            argMap.getSender().sendMessage(this.messageConfig.getCommandListGroups(String.join(", ", this.permissionHandler.getGroupNames())));
        }));
    }

    private void addCreateCommand() {
        this.apply("createGroup", 
                new Arg().apply(groupNameProvider, 
                new Arg().apply(new MultiArgument("\"Prefix\"", "", ""),
                    new CommandListener("Creates a new Permission-Group", argMap -> {
                        CommandSender cs = argMap.getSender();
                        String groupname = argMap.getArgument("<Group>");
                        String prefix = argMap.getArgument("\"Prefix\"");

                        if(this.permissionHandler.getGroupByGroupName(groupname) == null) {
                            PermissionGroup group = this.permissionHandler.createNewGroup(groupname, prefix, new ArrayList<>());
                            cs.sendMessage(this.messageConfig.getCommandGroupCreated(group.getName()));
                        } else {
                            cs.sendMessage(this.messageConfig.getCommandGroupAlreadyExists(groupname));
                        }

                    }))));
    }

    private void addGetGroupInfoCommand() {
        this.apply("getGroupInfo", new Arg().apply(groupNameProvider, new CommandListener("Shows some information about the Group", argMap -> {
            CommandSender cs = argMap.getSender();
            String groupname = argMap.getArgument("<Group>");
            PermissionGroup group = this.permissionHandler.getGroupByGroupName(groupname);

            if (group != null) {
                String name = group.getName();
                String prefix = group.getPrefix();
                String suffix = group.getSuffix();
                String chatColor = group.getChatMessageColor();
                String sibling = group.getSiblingGroupName();
                ArrayList<String> permissions = group.getPermissions();

                cs.sendMessage(this.messageConfig.getCommandGroupInfo(name, prefix, suffix, chatColor, sibling, permissions.size()));

            } else {
                cs.sendMessage(this.messageConfig.getCommandGroupNotExisting(groupname));
            }
        })));
    }

}
