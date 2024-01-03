package de.freesoccerhdx.permlegend;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class PermissionHandler {

    private HashMap<String, PermissionGroup> permissionGroups = new HashMap<>();

    private final Plugin plugin;
    public PermissionHandler(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                File folder = new File(plugin.getDataFolder(), "groups/");
                if(!folder.exists()) {
                    boolean s = folder.mkdirs();
                    if(s) {
                        createDefaultGroup();
                    } else {
                        plugin.getLogger().warning("Couldn't create Path: '" + folder.getPath() + "'");
                    }
                }
                if(folder.isDirectory()) {
                    loadGroups();
                }
            }
        });
    }

    public PermissionGroup getGroup(UUID uuid) {
        return permissionGroups.get("Default");
    }

    private void loadGroups() {
        File folder = new File(plugin.getDataFolder(), "groups/");
        if(!folder.exists() || !folder.isDirectory()) {
            return;
        }
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if(files == null) {
            return;
        }
        for(File file : files) {
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

            if(obj instanceof ArrayList<?>) {
                permissionsList = (ArrayList<String>) obj;
            } else {
                permissionsList = new ArrayList<>();
            }

            PermissionGroup group = new PermissionGroup(name, prefix, permissionsList);

            if(cfg.isSet("Suffix")) {
                group.setSuffix(cfg.getString("Suffix"));
            }
            if(cfg.isSet("ChatMessageColor")) {
                group.setChatMessageColor(cfg.getString("ChatMessageColor"));
            }
            if(cfg.isSet("SiblingGroupName")) {
                group.setSiblingGroupName(cfg.getString("SiblingGroupName"));
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
            cfg.set("Permissions", new String[]{"default"});
            cfg.save(file);


        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

}
