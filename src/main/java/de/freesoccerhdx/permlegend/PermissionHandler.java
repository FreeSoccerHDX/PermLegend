package de.freesoccerhdx.permlegend;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

public class PermissionHandler {

    private HashMap<String, PermissionGroup> permissionGroups = new HashMap<>();
    // private HashMap<UUID, PlayerPermissionData> playerPermissionDatas = new
    // HashMap<>();
    private HashMap<UUID, List<Consumer<PlayerPermissionData>>> groupChangeHook = new HashMap<>();

    private final Plugin plugin;
    private final SQLDatabase sqlDatabase;

    private ArrayList<Player> toCheckTemp = new ArrayList<>();
    private Queue<Runnable> tasks = new ArrayDeque<>();

    public PermissionHandler(Plugin plugin, SQLDatabase sqlDatabase) {
        this.plugin = plugin;
        this.sqlDatabase = sqlDatabase;

        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {

            @Override
            public void run() {

                long currentTime = System.nanoTime();
                long endTime = currentTime + 1000000; // max 1ms

                while (System.nanoTime() <= endTime) {
                    if (tasks.size() > 0) {
                        tasks.poll().run();
                    } else {
                        for (Player player : toCheckTemp) {
                            tasks.add(() -> {
                                if (player.isValid()) {
                                    PlayerPermissionData playerPermData = sqlDatabase
                                            .getPlayerPermissionData(player.getUniqueId());
                                    if (!playerPermData.hasTempGroup()) {
                                        toCheckTemp.remove(player);
                                        updatePlayerPermission(player);
                                        checkHook(player.getUniqueId(), playerPermData);
                                    }

                                } else {
                                    toCheckTemp.remove(player);
                                }
                            });
                        }
                        break;
                    }
                }
            }

        }, 20 * 2, 5);

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerPermission(player);
                }
            }

        }, 20 * 1);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                File folder = new File(plugin.getDataFolder(), "groups/");
                if (!folder.exists()) {
                    boolean s = folder.mkdirs();
                    if (s) {
                        createDefaultGroup();
                    } else {
                        plugin.getLogger().warning("Couldn't create Path: '" + folder.getPath() + "'");
                    }
                }
                if (folder.isDirectory()) {
                    loadGroups();
                }
            }
        });
    }

    public void addGroupChangeHook(UUID targetUUID, Consumer<PlayerPermissionData> playerPermissionDataHook) {
        groupChangeHook.putIfAbsent(targetUUID, new ArrayList<>());

        groupChangeHook.get(targetUUID).add(playerPermissionDataHook);
    }

    private void checkPlayerExist(Player player) {
        PlayerPermissionData ppD = this.sqlDatabase.getPlayerPermissionData(player.getUniqueId());
        boolean save = false;
        if (ppD == null) {
            ppD = setGroup(player.getUniqueId(), "Default");
            save = true;
        }
        if (!ppD.getPlayerName().equals(player.getName())) {
            ppD.setPlayerName(player.getName());
            save = true;
        }

        if (save) {
            final PlayerPermissionData toSave = ppD;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                sqlDatabase.savePlayerData(toSave);
            });
        }

    }

    public void updatePlayerPermission(Player player) {
        checkPlayerExist(player);

        Set<PermissionAttachmentInfo> effectivePerms = player.getEffectivePermissions();
        ArrayList<PermissionAttachment> toRemove = new ArrayList<>();
        effectivePerms.forEach(atachInfo -> {

            PermissionAttachment attachment = atachInfo.getAttachment();
            if (attachment != null) {
                if (attachment.getPlugin().equals(this.plugin)) {
                    toRemove.add(attachment);
                }
            }
        });

        toRemove.forEach(permAttachment -> {
            permAttachment.remove();
        });

        Set<String> newPermissions = getEffectivePermissions(player.getUniqueId());

        if (newPermissions.contains("*")) {
            Bukkit.getPluginManager().getPermissions().forEach(perm -> {
                player.addAttachment(plugin, perm.getName(), true);
            });
        }
        if (newPermissions.contains("-*")) {
            Bukkit.getPluginManager().getPermissions().forEach(perm -> {
                player.addAttachment(plugin, perm.getName(), false);
            });
        }

        newPermissions.forEach(permission -> {
            if (permission.startsWith("-")) {
                player.addAttachment(this.plugin, permission.substring(1), false);
            } else {
                player.addAttachment(this.plugin, permission, true);
            }
        });
        player.updateCommands();

        PlayerPermissionData playerPermData = this.sqlDatabase.getPlayerPermissionData(player.getUniqueId());
        if (playerPermData != null) {
            if (playerPermData.hasTempGroup()) {
                this.toCheckTemp.add(player);
            }
        }
    }

    public Set<String> getEffectivePermissions(UUID uuid) {
        HashSet<String> effectivePerms = new HashSet<>();

        PlayerPermissionData playerData = this.sqlDatabase.getPlayerPermissionData(uuid);

        if (playerData != null && playerData.hasAdditionalPermissions()) {
            effectivePerms.addAll(playerData.getAdditionalPermissions());
        }

        PermissionGroup group = this.getGroup(uuid);
        effectivePerms.addAll(group.getEffectivePermissions(this));

        return effectivePerms;
    }

    public PermissionGroup getGroup(UUID uuid) {
        PlayerPermissionData playerPermissionData = this.sqlDatabase.getPlayerPermissionData(uuid);
        if (playerPermissionData != null) {
            String groupName = playerPermissionData.getEffectiveGroupName();
            return permissionGroups.get(groupName);
        }
        return permissionGroups.get("Default");
    }

    private void loadGroups() {
        File folder = new File(plugin.getDataFolder(), "groups/");
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            loadGroup(file);
        }
        if (this.getGroupByGroupName("Default") == null) {
            createDefaultGroup();
            File file = new File(this.plugin.getDataFolder(), "groups/default.yml");
            loadGroup(file);
        }
    }

    private void loadGroup(File groupFile) {
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(groupFile);
            String name = cfg.getString("Name");
            String prefix = cfg.getString("Prefix");
            Object obj = cfg.get("Permissions");

            ArrayList<String> permissionsList;

            if (obj instanceof ArrayList<?>) {
                permissionsList = (ArrayList<String>) obj;
            } else {
                permissionsList = new ArrayList<>();
            }

            PermissionGroup group = new PermissionGroup(name, prefix, permissionsList);

            if (cfg.isSet("Suffix")) {
                group.setSuffix(cfg.getString("Suffix"));
            }
            if (cfg.isSet("ChatMessageColor")) {
                group.setChatMessageColor(cfg.getString("ChatMessageColor"));
            }
            if (cfg.isSet("SiblingGroupName")) {
                String siblingGroupName = cfg.getString("SiblingGroupName");
                if (siblingGroupName.length() >= 1) {
                    group.setSiblingGroupName(siblingGroupName);
                }
            }

            this.permissionGroups.put(name, group);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void createDefaultGroup() {
        File file = new File(this.plugin.getDataFolder(), "groups/default.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.set("Name", "Default");
            cfg.set("Prefix", "&7[Default]");
            cfg.set("SiblingGroupName", "");
            cfg.set("Suffix", "");
            cfg.set("ChatMessageColor", "");
            cfg.set("Permissions", new String[] { "default" });
            cfg.save(file);

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public PermissionGroup getGroupByGroupName(String groupName) {
        return this.permissionGroups.get(groupName);
    }

    public Set<String> getGroupNames() {
        return this.permissionGroups.keySet();
    }

    public PermissionGroup createNewGroup(@Nonnull String groupname, @Nonnull String prefix,
            @Nonnull ArrayList<String> initialPermissions) {
        PermissionGroup group = new PermissionGroup(groupname, prefix, initialPermissions);
        this.permissionGroups.put(groupname, group);

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {

            @Override
            public void run() {
                group.saveGroupToFile(
                        new File(plugin.getDataFolder(), "groups/" + groupname.toLowerCase(Locale.ENGLISH) + ".yml"));
            }

        });
        return group;
    }

    private void checkHook(UUID uuid, PlayerPermissionData playerPermissionData) {
        if (this.groupChangeHook.containsKey(uuid)) {
            for (Consumer<PlayerPermissionData> list : this.groupChangeHook.get(uuid)) {
                list.accept(playerPermissionData);
            }
        }
    }

    public PlayerPermissionData setGroup(UUID uuid, String group) {
        PlayerPermissionData playerData = this.sqlDatabase.getPlayerPermissionData(uuid);
        Player player = Bukkit.getPlayer(uuid);

        if (playerData == null) {
            playerData = new PlayerPermissionData(uuid, player == null ? "" : player.getName(), group, "", 0);
        }
        playerData.setGroupName(group);

        final PlayerPermissionData toSave = playerData;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            sqlDatabase.savePlayerData(toSave);
        });

        if (player != null) {
            this.updatePlayerPermission(player);
        }

        checkHook(uuid, playerData);

        return playerData;
    }

    public PlayerPermissionData removeTempGroup(UUID uuid) {
        return this.setTempGroup(uuid, "", 0);
    }

    public PlayerPermissionData setTempGroup(UUID uuid, String group, long timestampEnd) {
        PlayerPermissionData playerData = this.sqlDatabase.getPlayerPermissionData(uuid);
        Player player = Bukkit.getPlayer(uuid);

        if (playerData == null) {
            playerData = new PlayerPermissionData(uuid, player == null ? "" : player.getName(), "Default", "", 0);
        }
        playerData.setTempGroup(group, timestampEnd);

        final PlayerPermissionData toSave = playerData;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            sqlDatabase.savePlayerData(toSave);
        });

        if (player != null) {
            this.updatePlayerPermission(player);
        }

        checkHook(uuid, playerData);

        return playerData;
    }

}
