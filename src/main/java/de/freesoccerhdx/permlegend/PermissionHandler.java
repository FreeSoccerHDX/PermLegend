package de.freesoccerhdx.permlegend;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

public class PermissionHandler {

    private HashMap<String, PermissionGroup> permissionGroups = new HashMap<>();
    private HashMap<UUID, PlayerPermissionData> playerPermissionDatas = new HashMap<>();

    private final Plugin plugin;

    public PermissionHandler(Plugin plugin) {
        this.plugin = plugin;
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

                File playerFolder = new File(plugin.getDataFolder(), "players/");
                if (!playerFolder.exists()) {
                    boolean s = playerFolder.mkdirs();
                    if (!s) {
                        plugin.getLogger().warning("Couldn't create Path: '" + playerFolder.getPath() + "'");
                    }
                }
                if (playerFolder.isDirectory()) {
                    loadPlayerData();
                }
            }
        });
    }

    public void updatePlayerPermission(Player player) {
        Set<PermissionAttachmentInfo> effectivePerms = player.getEffectivePermissions();
        ArrayList<PermissionAttachment> toRemove = new ArrayList<>();
        effectivePerms.forEach(atachInfo -> {
            PermissionAttachment attachment = atachInfo.getAttachment();
            if(attachment != null) {
                if(attachment.getPlugin().equals(this.plugin)) {
                    toRemove.add(attachment);
                }
            }
        });

        toRemove.forEach(permAttachment -> {
            player.removeAttachment(permAttachment);
        });


        Set<String> newPermissions = getEffectivePermissions(player.getUniqueId());

        newPermissions.forEach(permission-> {
            if(permission.startsWith("-")) {
                player.addAttachment(this.plugin, permission.substring(1), false);
            } else {
                player.addAttachment(this.plugin, permission, true);
            }
        });

    }

    public Set<String> getEffectivePermissions(UUID uuid) {
        HashSet<String> effectivePerms = new HashSet<>();

        PlayerPermissionData playerData = this.playerPermissionDatas.get(uuid);

        if(playerData != null && playerData.hasAdditionalPermissions()) {
            effectivePerms.addAll(playerData.getAdditionalPermissions());
        }

        PermissionGroup group = this.getGroup(uuid);
        effectivePerms.addAll(group.getEffectivePermissions(this));


        return effectivePerms;
    }

    private void loadPlayerData() {
        File folder = new File(plugin.getDataFolder(), "players/");
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            loadPlayerData(file);
        }
    }

    private void loadPlayerData(File playerFile) {
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(playerFile);

            String name = cfg.getString("Name");
            String _uuid = cfg.getString("UUID");
            UUID uuid = UUID.fromString(_uuid);

            String groupName = cfg.getString("Group");

            ArrayList<String> permissions = new ArrayList<>();

            if (cfg.isSet("Permissions")) {
                Object obj = cfg.get("Permissions");
                if (obj instanceof ArrayList<?>) {
                    permissions = (ArrayList<String>) obj;
                }
            }

            PlayerPermissionData playerPermissionData = new PlayerPermissionData(uuid, name, groupName);

            playerPermissionData.setAdditionalPermissions(permissions);

            if (cfg.isSet("tempGroup") && cfg.isSet("tempGroupEnd")) {
                String tempGroupName = cfg.getString("groupName");
                Long tempGroupEnd = cfg.getLong("tempGroupEnd");
                if (tempGroupEnd > System.currentTimeMillis()) {
                    playerPermissionData.setTempGroup(tempGroupName, tempGroupEnd);
                }
            }

            this.playerPermissionDatas.put(uuid, playerPermissionData);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public PermissionGroup getGroup(UUID uuid) {
        if(this.playerPermissionDatas.containsKey(uuid)) {
            String groupName = this.playerPermissionDatas.get(uuid).getEffectiveGroupName();
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
                if(siblingGroupName.length() >= 1) {
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

    public PlayerPermissionData getPlayerPermissionData(UUID uniqueId) {
        return this.playerPermissionDatas.get(uniqueId);
    }

    public Set<String> getGroupNames() {
        return this.permissionGroups.keySet();
    }

    public PermissionGroup createNewGroup(@Nonnull String groupname, @Nonnull String prefix, @Nonnull ArrayList<String> initialPermissions) {
        PermissionGroup group = new PermissionGroup(groupname, prefix, initialPermissions);
        this.permissionGroups.put(groupname, group);
        
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {

            @Override
            public void run() {
                group.saveGroupToFile(new File(plugin.getDataFolder(), "groups/"+groupname.toLowerCase(Locale.ENGLISH)+".yml"));
            }
            
        });
        return group;
    }

}
