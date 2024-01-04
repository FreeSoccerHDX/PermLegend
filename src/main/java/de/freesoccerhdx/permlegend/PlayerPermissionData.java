package de.freesoccerhdx.permlegend;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerPermissionData {

    private final UUID uuid;
    private String name;
    private String groupName;

    private String tempGroupName = null;
    private Long tempGroupEnd = null;
    private ArrayList<String> additionalPermissions = null;

    public PlayerPermissionData(UUID uuid, String name, String groupName) {
        this.uuid = uuid;
        this.name = name;
        this.groupName = groupName;
    }

    public boolean saveToPlayerFile(File file) {
        try {
            YamlConfiguration cfg = new YamlConfiguration();

            cfg.set("Name", this.name);
            cfg.set("UUID", this.uuid.toString());
            cfg.set("Group", this.groupName);

            if(tempGroupName != null) {
                cfg.set("tempGroup", this.tempGroupName);
                cfg.set("tempGroupEnd", this.tempGroupEnd);
            } else {
                cfg.set("tempGroup", "");
                cfg.set("tempGroupEnd", -1);
            }

            if(this.additionalPermissions != null) {
                cfg.set("Permissions", this.additionalPermissions);
            } else {
                cfg.set("Permissions", new String[0]);
            }

            cfg.save(file);
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public String getEffectiveGroupName() {
        if(hasTempGroup()) {
            return this.getTempGroupName();
        }
        return this.getGroupName();
    }

    public void setTempGroup(String tempGroupName, long tempGroupEnd) {
        this.tempGroupName = tempGroupName;
        this.tempGroupEnd = tempGroupEnd;
    }

    public long getTempGroupEnd() {
        return this.tempGroupEnd;
    }

    public String getTempGroupName() {
        return this.tempGroupName;
    }

    public boolean hasTempGroup() {
        return tempGroupName != null && this.tempGroupEnd > System.currentTimeMillis();
    }

    public void setName(String newName) {
        this.name = newName;
    }
    
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String newGroupName) {
        this.groupName = newGroupName;
    }
    

    public UUID getUuid() {
        return uuid;
    }
    
    public String getName() {
        return name;
    }

    public boolean hasAdditionalPermissions() {
        return this.additionalPermissions != null && !this.additionalPermissions.isEmpty();
    }
    
    public ArrayList<String> getAdditionalPermissions() {
        return additionalPermissions;
    }

    public void additionalPermission(String permission) {
        this.additionalPermissions.add(permission);
    }

    public void setAdditionalPermissions(ArrayList<String> permissions) {
        this.additionalPermissions = permissions;
    }
}
