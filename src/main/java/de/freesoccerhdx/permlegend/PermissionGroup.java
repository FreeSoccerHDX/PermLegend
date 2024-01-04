package de.freesoccerhdx.permlegend;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class PermissionGroup {

    private final ArrayList<String> permissions;
    private final String name;
    private String prefix;
    

    private String siblingGroupName = null;
    private String chatMessageColor = null;
    private String suffix = null;


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
        this.permissions.add(permission);
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
