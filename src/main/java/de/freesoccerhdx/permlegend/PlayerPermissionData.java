package de.freesoccerhdx.permlegend;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerPermissionData {

    private final UUID uuid;
    private String playername;
    private String groupName;

    private String tempGroupName = null;
    private long tempGroupEnd = 0;
    private ArrayList<String> additionalPermissions = null;

    public PlayerPermissionData(UUID uuid, String playername, String groupName) {
        this(uuid, playername, groupName, "", 0);
    }

    public PlayerPermissionData(UUID uuid, String playername, String groupName, String tempGroupName, long tempGroupEnd) {
        this.uuid = uuid;
        this.playername = playername;
        this.groupName = groupName;
        this.tempGroupName = tempGroupName;
        this.tempGroupEnd = tempGroupEnd;
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

    public void setPlayerName(String newPlayername) {
        this.playername = newPlayername;
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
    
    public String getPlayerName() {
        return playername;
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
