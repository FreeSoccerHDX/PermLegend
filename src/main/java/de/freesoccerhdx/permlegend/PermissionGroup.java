package de.freesoccerhdx.permlegend;

import java.io.File;
import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.file.YamlConfiguration;

public class PermissionGroup {

    private final ArrayList<String> permissions;
    private final String name;
    private String prefix;
    

    private String siblingGroupName = "";
    private String chatMessageColor = "";
    private String suffix = "";


    public PermissionGroup(@Nonnull String name, @Nonnull String prefix, @Nonnull ArrayList<String> permissions) {
        this.name = name;
        this.prefix = prefix;
        this.permissions = permissions;
    }

    public boolean saveGroupToFile(File file) {
        try {
            YamlConfiguration cfg = new YamlConfiguration();

            cfg.set("Name", this.name);
            cfg.set("Prefix", this.prefix);
            cfg.set("SiblingGroupName", this.siblingGroupName);
            cfg.set("Suffix", this.suffix);
            cfg.set("ChatMessageColor", this.chatMessageColor);
            cfg.set("Permissions", this.permissions);

            cfg.save(file);
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public void setSiblingGroupName(@Nullable String siblingGroupName) {
        this.siblingGroupName = siblingGroupName;
    }

    public void setChatMessageColor(@Nullable String chatMessageColor) {
        this.chatMessageColor = chatMessageColor;
    }

    public void setSuffix(@Nullable String suffix) {
        this.suffix = suffix;
    }

    @Nullable
    public String getChatMessageColor() {
        return chatMessageColor;
    }

    @Nullable
    public String getSuffix() {
        return suffix;
    }

    @Nullable
    public String getSiblingGroupName() {
        return siblingGroupName;
    }

    @Nonnull
    public String getPrefix() {
        return prefix;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public ArrayList<String> getPermissions() {
        return permissions;
    }
    
    public void addPermission(@Nonnull String permission) {
        if(this.permissions.contains(permission)) return;
        this.permissions.add(permission);
    }

    public boolean removePermission(String permission) {
        return this.permissions.remove(permission);
    }

    public ArrayList<String> getEffectivePermissions(PermissionHandler permissionHandler) {

        ArrayList<String> perms = new ArrayList<>();
        perms.addAll(this.permissions);

        if(this.getSiblingGroupName() != null) {
            PermissionGroup group = permissionHandler.getGroupByGroupName(this.getSiblingGroupName());
            if(group != null) {
                perms.addAll(group.getEffectivePermissions(permissionHandler));
            }
        }

        return perms;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }



   
}
