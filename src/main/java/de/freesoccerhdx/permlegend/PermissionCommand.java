package de.freesoccerhdx.permlegend;

import de.freesoccerhdx.lib.Methods;
import de.freesoccerhdx.lib.command.Arg;
import de.freesoccerhdx.lib.command.CommandListener;
import de.freesoccerhdx.lib.command.CustomCommand;
import de.freesoccerhdx.lib.command.MultiArgument;
import de.freesoccerhdx.lib.command.TypeArgument;
import de.freesoccerhdx.lib.command.TypeArgument.IntArgument;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PermissionCommand extends CustomCommand {

    private final TypeArgument playerNameOrUUIDProvider;
    private final TypeArgument groupNameProvider;
    private final TypeArgument permissionsProvider;

    private final PermLegend plugin;
    

    public PermissionCommand(PermLegend plugin) {
        super("permission");
        this.plugin = plugin;

        this.playerNameOrUUIDProvider = new TypeArgument("<Player>") {

            @Override
            public boolean checkArgument(String arg) {
                if (!arg.contains("<") && !arg.contains(">")) {
                    // allow playernames between 2 and 16 or UUID's of length 36
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
                return plugin.getPermissionHandler().getGroupNames().toArray(new String[0]);
            }
        };

        this.permissionsProvider = new TypeArgument("<Permission>") {

            @Override
            public boolean checkArgument(String arg) {
                return true;
            }

            @Override
            public String[] getValidArguments() {
                return new String[] { "perm.perm" };
            }
        };

        // /permission info <Player>
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
        // /permission addPermission <Group> <Permission>
        addAddPermissionCommand();
        // /permission removePermission <Group> <Permission>
        addRemovePermissionCommand();
        // /permission listPermissions <Group> <Page>
        addListPermissionCommand();

        // /permission hasPermission <Player> <Permission>
        addHasPermissionCommand();
        // /permission setGroup <Player> <Group>
        addSetGroupCommand();
        // /permission setTempGroup <Player> <Group> <Time>
        addSetTempGroupCommand();

        // /permission setSignDisplay <Player>
        addSetSignDisplayCommand();
    }

    private void addSetSignDisplayCommand() {
        apply("setSignDisplay", new Arg().apply(playerNameOrUUIDProvider, new CommandListener(
                "Sets the Sign your looking at to a SignDisplay that shows the Playername and his Group", argMap -> {
                    if (argMap.getSender() instanceof Player player) {
                        String playerNameOrUUID = argMap.getArgument("<Player>");
                        UUID uuid = getUUIDFromInput(player, playerNameOrUUID);
                        if(uuid == null) return;

                        PlayerPermissionData playerPermissionData = plugin.getSqlDatabase().getPlayerPermissionData(uuid);

                        if(playerPermissionData != null) {
                            Block block = player.getTargetBlockExact(8);
                            if(block != null) {
                                if(Tag.ALL_SIGNS.isTagged(block.getType())) {
                                    this.plugin.getSignDisplays().addSign(playerPermissionData.getUuid(), block);
                                    player.sendMessage(this.plugin.getMessageConfig().getCommandSetSignDisplaySuccess());
                                } else {
                                    player.sendMessage(plugin.getMessageConfig().getCommandSetSignDisplayNotASign());
                                }
                            } else {
                                player.sendMessage(plugin.getMessageConfig().getCommandSetSignDisplayNotASign());
                            }
                        } else {
                            playerNotFound(player, playerNameOrUUID);
                        }


                    } else {
                        argMap.getSender().sendMessage(plugin.getMessageConfig().getCommandNotAPlayer());
                    }
                })));
    }

    private UUID getUUIDFromInput(CommandSender cs, String input) {
        UUID uuid = null;
        if (input.length() <= 16) {
            // PlayerName
            try {
                Player onlinePlayer = Bukkit.getPlayer(input);
                if (onlinePlayer != null) {
                    uuid = onlinePlayer.getUniqueId();
                } else {
                    playerNotFound(cs, input);
                }
            } catch (Exception exception) {
                playerNotFound(cs, input);
            }
        } else {
            // UUID
            try {
                uuid = UUID.fromString(input);
            } catch (Exception exception) {
                exception.printStackTrace();
                playerNotFound(cs, input);
            }
        }
        return uuid;
    }

    private void addSetTempGroupCommand() {
        MultiArgument timeInputArg = new MultiArgument("<Time>", "", "");

        apply("setTempGroup",
                new Arg().apply(playerNameOrUUIDProvider,
                        new Arg().apply(groupNameProvider, new Arg().apply(timeInputArg,
                                new CommandListener("Sets a Group temporary to the Player", argMap -> {
                                    CommandSender cs = argMap.getSender();
                                    String group = argMap.getArgument("<Group>");
                                    String playerNameOrUUID = argMap.getArgument("<Player>");
                                    String timeInput = argMap.getArgument("<Time>");

                                    UUID uuid = getUUIDFromInput(cs, playerNameOrUUID);

                                    if (uuid != null) {
                                        if (plugin.getPermissionHandler().getGroupByGroupName(group) != null) {
                                            try {
                                                long seconds = Methods.calculateTimeFromInput(timeInput);
                                                long end = System.currentTimeMillis() + seconds * 1000;
                                                plugin.getPermissionHandler().setTempGroup(uuid, group, end);

                                                cs.sendMessage(plugin.getMessageConfig().getCommandSetGroupChanged());
                                            } catch (Exception exception) {
                                                cs.sendMessage(plugin.getMessageConfig().getCommandSetTempGroupFailed());
                                            }

                                        } else {
                                            cs.sendMessage(plugin.getMessageConfig().getCommandGroupNotExisting(group));
                                        }
                                    }

                                })))));
    }

    private void addSetGroupCommand() {
        apply("setGroup", new Arg().apply(playerNameOrUUIDProvider, new Arg().apply(groupNameProvider,
                new CommandListener("Sets a Group to the Player", argMap -> {
                    CommandSender cs = argMap.getSender();
                    String group = argMap.getArgument("<Group>");
                    String playerNameOrUUID = argMap.getArgument("<Player>");
                    UUID uuid = null;
                    if (playerNameOrUUID.length() <= 16) {
                        // PlayerName
                        try {
                            Player onlinePlayer = Bukkit.getPlayer(playerNameOrUUID);
                            if (onlinePlayer != null) {
                                uuid = onlinePlayer.getUniqueId();
                            } else {
                                playerNotFound(cs, playerNameOrUUID);
                            }
                        } catch (Exception exception) {
                            playerNotFound(cs, playerNameOrUUID);
                        }
                    } else {
                        // UUID
                        try {
                            uuid = UUID.fromString(playerNameOrUUID);
                        } catch (Exception exception) {
                            playerNotFound(cs, playerNameOrUUID);
                        }
                    }

                    if (uuid != null) {
                        if (plugin.getPermissionHandler().getGroupByGroupName(group) != null) {
                            plugin.getPermissionHandler().setGroup(uuid, group);

                            cs.sendMessage(plugin.getMessageConfig().getCommandSetGroupChanged());
                        } else {
                            cs.sendMessage(plugin.getMessageConfig().getCommandGroupNotExisting(group));
                        }
                    }

                }))));
    }

    private void sendInfo(CommandSender sender, UUID target, boolean isOther) {
        PlayerPermissionData playerPermissionData = plugin.getSqlDatabase().getPlayerPermissionData(target);
        PermissionGroup defaultGroup;
        PermissionGroup tempGroup = null;
        Long tempGroupEnd = null;

        if (playerPermissionData == null) {
            defaultGroup = plugin.getPermissionHandler().getGroup(target);
        } else {
            defaultGroup = plugin.getPermissionHandler().getGroupByGroupName(playerPermissionData.getGroupName());

            if (playerPermissionData.hasTempGroup()) {
                tempGroup = plugin.getPermissionHandler().getGroupByGroupName(playerPermissionData.getTempGroupName());
                tempGroupEnd = playerPermissionData.getTempGroupEnd();
            }
        }

        String playername = target.toString();
        if (playerPermissionData != null) {
            playername = playerPermissionData.getPlayerName();
        }

        if (tempGroup == null) {
            if (isOther) {
                sender.sendMessage(
                        plugin.getMessageConfig().getCommandInfoOtherDefaultGroup(playername, defaultGroup.getName()));
            } else {
                sender.sendMessage(plugin.getMessageConfig().getCommandInfoDefaultGroup(defaultGroup.getName()));
            }
        } else {
            if (isOther) {
                sender.sendMessage(plugin.getMessageConfig().getCommandInfoOtherDefaultAndTempGroup(playername,
                        defaultGroup.getName(), tempGroup.getName(), tempGroupEnd));
            } else {
                sender.sendMessage(plugin.getMessageConfig().getCommandInfoDefaultAndTempGroup(defaultGroup.getName(),
                        tempGroup.getName(), tempGroupEnd));
            }
        }
    }

    private void addInfoCommand() {
        CommandListener commandListener = new CommandListener(
                "Shows the current Permission-Group information for a Player", argMap -> {

                    String playerNameOrUUID = argMap.getArgument("<Player>");
                    if (playerNameOrUUID == null) {
                        if (argMap.getSender() instanceof Player player) {
                            sendInfo(player, player.getUniqueId(), false);
                        } else {
                            argMap.getSender().sendMessage(plugin.getMessageConfig().getCommandNotAPlayer());
                        }
                    } else {
                        CommandSender cs = argMap.getSender();
                        UUID uuid = null;
                        if (playerNameOrUUID.length() <= 16) {
                            // PlayerName
                            try {
                                Player onlinePlayer = Bukkit.getPlayer(playerNameOrUUID);
                                if (onlinePlayer != null) {
                                    uuid = onlinePlayer.getUniqueId();
                                } else {
                                    playerNotFound(cs, playerNameOrUUID);
                                }
                            } catch (Exception exception) {
                                playerNotFound(cs, playerNameOrUUID);
                            }
                        } else {
                            // UUID
                            try {
                                uuid = UUID.fromString(playerNameOrUUID);

                            } catch (Exception exception) {
                                playerNotFound(cs, playerNameOrUUID);
                            }
                        }

                        if (uuid != null) {
                            sendInfo(cs, uuid, true);
                        }
                    }

                });
        this.apply("info",
                new Arg().setListener(commandListener).apply(this.playerNameOrUUIDProvider, commandListener));
    }

    private void addListGroupsCommand() {
        this.apply("listGroups", new CommandListener("Lists all existing Groups", argMap -> {
            argMap.getSender().sendMessage(
                    plugin.getMessageConfig().getCommandListGroups(String.join(", ", plugin.getPermissionHandler().getGroupNames())));
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

                                    if (plugin.getPermissionHandler().getGroupByGroupName(groupname) == null) {
                                        PermissionGroup group = plugin.getPermissionHandler().createNewGroup(groupname,
                                                prefix.substring(1, prefix.length() - 1), new ArrayList<>());
                                        cs.sendMessage(plugin.getMessageConfig().getCommandGroupCreated(group.getName()));
                                    } else {
                                        cs.sendMessage(plugin.getMessageConfig().getCommandGroupAlreadyExists(groupname));
                                    }

                                }))));
    }

    private void addGetGroupInfoCommand() {
        this.apply("getGroupInfo", new Arg().apply(groupNameProvider,
                new CommandListener("Shows some information about the Group", argMap -> {
                    CommandSender cs = argMap.getSender();
                    String groupname = argMap.getArgument("<Group>");
                    PermissionGroup group = plugin.getPermissionHandler().getGroupByGroupName(groupname);

                    if (group != null) {
                        String name = group.getName();
                        String prefix = group.getPrefix();
                        String suffix = group.getSuffix();
                        String chatColor = group.getChatMessageColor();
                        String sibling = group.getSiblingGroupName();
                        ArrayList<String> permissions = group.getPermissions();

                        cs.sendMessage(plugin.getMessageConfig().getCommandGroupInfo(name, prefix, suffix, chatColor, sibling,
                                permissions.size()));

                    } else {
                        cs.sendMessage(plugin.getMessageConfig().getCommandGroupNotExisting(groupname));
                    }
                })));
    }

    private void addSetPrefixCommand() {
        this.apply("setPrefix",
                new Arg().apply(groupNameProvider, new Arg().apply(new MultiArgument("\"Prefix\"", "\"", "\""),
                        new CommandListener("Sets the prefix of a Group", argMap -> {
                            CommandSender cs = argMap.getSender();
                            String groupname = argMap.getArgument("<Group>");
                            String prefix = argMap.getArgument("\"Prefix\"");
                            PermissionGroup group = plugin.getPermissionHandler().getGroupByGroupName(groupname);

                            if (group != null) {
                                group.setPrefix(prefix.substring(1, prefix.length() - 1));
                                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                                    @Override
                                    public void run() {
                                        group.saveGroupToFile(new File(plugin.getDataFolder(),
                                                "groups/" + group.getName().toLowerCase(Locale.ENGLISH) + ".yml"));
                                    }

                                });
                                cs.sendMessage(plugin.getMessageConfig().getCommandGroupUpdated(group.getName()));
                            } else {
                                cs.sendMessage(plugin.getMessageConfig().getCommandGroupNotExisting(groupname));
                            }
                        }))));
    }

    private void addSetSuffixCommand() {
        this.apply("setSuffix",
                new Arg().apply(groupNameProvider, new Arg().apply(new MultiArgument("\"Suffix\"", "\"", "\""),
                        new CommandListener("Sets the suffix of a Group", argMap -> {
                            CommandSender cs = argMap.getSender();
                            String groupname = argMap.getArgument("<Group>");
                            String suffix = argMap.getArgument("\"Suffix\"");
                            PermissionGroup group = plugin.getPermissionHandler().getGroupByGroupName(groupname);

                            if (group != null) {
                                group.setSuffix(suffix.substring(1, suffix.length() - 1));
                                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                                    @Override
                                    public void run() {
                                        group.saveGroupToFile(new File(plugin.getDataFolder(),
                                                "groups/" + group.getName().toLowerCase(Locale.ENGLISH) + ".yml"));
                                    }

                                });
                                cs.sendMessage(plugin.getMessageConfig().getCommandGroupUpdated(group.getName()));
                            } else {
                                cs.sendMessage(plugin.getMessageConfig().getCommandGroupNotExisting(groupname));
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
                return new String[] { "&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9", "&a", "&b", "&c",
                        "&d", "&e", "&f" };
            }

        };
        this.apply("setChatColor", new Arg().apply(groupNameProvider,
                new Arg().apply(typeArgument, new CommandListener("Sets the chatcolor of a Group", argMap -> {
                    CommandSender cs = argMap.getSender();
                    String groupname = argMap.getArgument("<Group>");
                    String chatcolor = argMap.getArgument("<Color>");
                    PermissionGroup group = plugin.getPermissionHandler().getGroupByGroupName(groupname);

                    if (group != null) {
                        group.setChatMessageColor(chatcolor);
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                            @Override
                            public void run() {
                                group.saveGroupToFile(new File(plugin.getDataFolder(),
                                        "groups/" + group.getName().toLowerCase(Locale.ENGLISH) + ".yml"));
                            }

                        });
                        cs.sendMessage(plugin.getMessageConfig().getCommandGroupUpdated(group.getName()));
                    } else {
                        cs.sendMessage(plugin.getMessageConfig().getCommandGroupNotExisting(groupname));
                    }
                }))));
    }

    private void addAddPermissionCommand() {
        this.apply("addPermission", new Arg().apply(groupNameProvider, new Arg().apply(this.permissionsProvider,
                new CommandListener("Adds a Permission to a Group", argMap -> {
                    CommandSender cs = argMap.getSender();
                    String groupname = argMap.getArgument("<Group>");
                    String permission = argMap.getArgument("<Permission>");
                    PermissionGroup group = plugin.getPermissionHandler().getGroupByGroupName(groupname);

                    if (group != null) {
                        group.addPermission(permission);
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                            @Override
                            public void run() {
                                group.saveGroupToFile(new File(plugin.getDataFolder(),
                                        "groups/" + group.getName().toLowerCase(Locale.ENGLISH) + ".yml"));
                            }

                        });
                        cs.sendMessage(plugin.getMessageConfig().getCommandGroupUpdated(group.getName()));
                    } else {
                        cs.sendMessage(plugin.getMessageConfig().getCommandGroupNotExisting(groupname));
                    }
                }))));
    }

    private void addRemovePermissionCommand() {
        this.apply("removePermission", new Arg().apply(groupNameProvider, new Arg().apply(this.permissionsProvider,
                new CommandListener("Removes a Permission from a Group", argMap -> {
                    CommandSender cs = argMap.getSender();
                    String groupname = argMap.getArgument("<Group>");
                    String permission = argMap.getArgument("<Permission>");
                    PermissionGroup group = plugin.getPermissionHandler().getGroupByGroupName(groupname);

                    if (group != null) {
                        group.removePermission(permission);
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                            @Override
                            public void run() {
                                group.saveGroupToFile(new File(plugin.getDataFolder(),
                                        "groups/" + group.getName().toLowerCase(Locale.ENGLISH) + ".yml"));
                            }

                        });
                        cs.sendMessage(plugin.getMessageConfig().getCommandGroupUpdated(group.getName()));
                    } else {
                        cs.sendMessage(plugin.getMessageConfig().getCommandGroupNotExisting(groupname));
                    }
                }))));
    }

    private void addListPermissionCommand() {
        IntArgument pageArgument = new IntArgument("<Page>");
        CommandListener cmdListener = new CommandListener("Shows all the Permissions of a Group", argMap -> {
            CommandSender cs = argMap.getSender();
            String groupname = argMap.getArgument("<Group>");
            int page = argMap.getArgumentAsInt("<Page>").orElseGet(() -> 1);

            PermissionGroup group = plugin.getPermissionHandler().getGroupByGroupName(groupname);

            if (group != null) {
                ArrayList<String> permissions = group.getPermissions();
                int startIndex = (page - 1) * 10;
                int endIndex = Math.min((page) * 10, permissions.size());
                int maxPage = permissions.size() / 10 + 1;

                if (startIndex > endIndex) {
                    cs.sendMessage(plugin.getMessageConfig().getCommandListPermissionPageNotExist());
                } else {
                    List<String> subPerms = permissions.subList(startIndex, endIndex);
                    cs.sendMessage(plugin.getMessageConfig().getCommandListPermissionHeader(group.getName(),
                            permissions.size(), page, maxPage));
                    int c = startIndex + 1;
                    for (String s : subPerms) {
                        cs.sendMessage(plugin.getMessageConfig().getCommandListPermissionInfo(c, s));
                        c++;
                    }
                }

            } else {
                cs.sendMessage(plugin.getMessageConfig().getCommandGroupNotExisting(groupname));
            }

        });

        this.apply("listPermissions", new Arg().apply(groupNameProvider,
                new Arg().setListener(cmdListener).apply(pageArgument, cmdListener)));
    }

    private void addHasPermissionCommand() {
        this.apply("hasPermission",
                new Arg().apply(this.playerNameOrUUIDProvider, new Arg().apply(this.permissionsProvider,
                        new CommandListener("Checks if the Player has the specific Permission", argMap -> {
                            CommandSender cs = argMap.getSender();
                            String player = argMap.getArgument("<Player>");
                            String permission = argMap.getArgument("<Permission>");

                            if (player.length() <= 16) {
                                // PlayerName
                                try {
                                    Player onlinePlayer = Bukkit.getPlayer(player);
                                    if (onlinePlayer != null) {
                                        boolean hasPerm = onlinePlayer.hasPermission(permission);
                                        if (hasPerm) {
                                            cs.sendMessage(plugin.getMessageConfig().getCommandHasPerm());
                                        } else {
                                            cs.sendMessage(plugin.getMessageConfig().getCommandHasNotPerm());
                                        }
                                    } else {
                                        playerNotFound(cs, player);
                                    }
                                } catch (Exception exception) {
                                    playerNotFound(cs, player);
                                }
                            } else {
                                // UUID
                                try {
                                    UUID uuid = UUID.fromString(player);
                                    Set<String> effectivePerms = plugin.getPermissionHandler().getEffectivePermissions(uuid);

                                    if (effectivePerms.contains(permission)) {
                                        cs.sendMessage(plugin.getMessageConfig().getCommandHasPerm());
                                    } else {
                                        cs.sendMessage(plugin.getMessageConfig().getCommandHasNotPerm());
                                    }

                                } catch (Exception exception) {
                                    playerNotFound(cs, player);
                                }
                            }

                        }))));
    }

    private void playerNotFound(CommandSender sender, String playerNameUUID) {
        sender.sendMessage(plugin.getMessageConfig().getCommandPlayerNotFoundByNameOrUUID(playerNameUUID));
    }

}
