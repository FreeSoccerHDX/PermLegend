package de.freesoccerhdx.permlegend;

import de.freesoccerhdx.lib.command.Arg;
import de.freesoccerhdx.lib.command.CommandListener;
import de.freesoccerhdx.lib.command.CustomCommand;
import de.freesoccerhdx.lib.command.MultiArgument;
import de.freesoccerhdx.lib.command.TypeArgument;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PermissionCommand extends CustomCommand {

    private final PermissionHandler permissionHandler;
    private final MessageConfig messageConfig;

    private final TypeArgument playerNameOrUUIDProvider;
    private final TypeArgument groupNameProvider;

    private final Plugin plugin;

    public PermissionCommand(Plugin plugin, PermissionHandler permissionHandler, MessageConfig messageConfig) {
        super("permission");
        this.plugin = plugin;
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
        addSetPrefixCommand();
        // /permission setSuffix <Group> "Suffix"
        addSetSuffixCommand();
        // /permission setChatColor <Group> <Color>
        addSetChatColorCommand();
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

    private void addSetPrefixCommand() {
        this.apply("setPrefix", new Arg().apply(groupNameProvider, new Arg().apply(new MultiArgument("\"Prefix\"", "\"", "\""), new CommandListener("Sets the prefix of a Group", argMap -> {
            CommandSender cs = argMap.getSender();
            String groupname = argMap.getArgument("<Group>");
            String prefix = argMap.getArgument("\"Prefix\"");
            PermissionGroup group = this.permissionHandler.getGroupByGroupName(groupname);

            if (group != null) {
                group.setPrefix(prefix);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                    @Override
                    public void run() {
                        group.saveGroupToFile(new File(plugin.getDataFolder(), "groups/"+group.getName().toLowerCase(Locale.ENGLISH)+".yml"));
                    }
                    
                });
                cs.sendMessage(this.messageConfig.getCommandGroupUpdated(group.getName()));
            } else {
                cs.sendMessage(this.messageConfig.getCommandGroupNotExisting(groupname));
            }
        }))));
    }

    private void addSetSuffixCommand() {
        this.apply("setSuffix", new Arg().apply(groupNameProvider, new Arg().apply(new MultiArgument("\"Suffix\"", "\"", "\""), new CommandListener("Sets the suffix of a Group", argMap -> {
            CommandSender cs = argMap.getSender();
            String groupname = argMap.getArgument("<Group>");
            String suffix = argMap.getArgument("\"Suffix\"");
            PermissionGroup group = this.permissionHandler.getGroupByGroupName(groupname);

            if (group != null) {
                group.setSuffix(suffix);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                    @Override
                    public void run() {
                        group.saveGroupToFile(new File(plugin.getDataFolder(), "groups/"+group.getName().toLowerCase(Locale.ENGLISH)+".yml"));
                    }
                    
                });
                cs.sendMessage(this.messageConfig.getCommandGroupUpdated(group.getName()));
            } else {
                cs.sendMessage(this.messageConfig.getCommandGroupNotExisting(groupname));
            }
        }))));
    }

    private void addSetChatColorCommand() {
        TypeArgument typeArgument = new TypeArgument("<Color>") {

            @Override
            public boolean checkArgument(String arg) {
                return true;
            }

            @Override
            public String[] getValidArguments() {
                return new String[] {"&0","&1","&2","&3","&4","&5","&6","&7","&8","&9","&a","&b","&c","&d","&e","&f"};
            }
            
        };
        this.apply("setChatColor", new Arg().apply(groupNameProvider, new Arg().apply(typeArgument, new CommandListener("Sets the chatcolor of a Group", argMap -> {
            CommandSender cs = argMap.getSender();
            String groupname = argMap.getArgument("<Group>");
            String chatcolor = argMap.getArgument("<Color>");
            PermissionGroup group = this.permissionHandler.getGroupByGroupName(groupname);

            if (group != null) {
                group.setChatMessageColor(chatcolor);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                    @Override
                    public void run() {
                        group.saveGroupToFile(new File(plugin.getDataFolder(), "groups/"+group.getName().toLowerCase(Locale.ENGLISH)+".yml"));
                    }
                    
                });
                cs.sendMessage(this.messageConfig.getCommandGroupUpdated(group.getName()));
            } else {
                cs.sendMessage(this.messageConfig.getCommandGroupNotExisting(groupname));
            }
        }))));
    }

}
