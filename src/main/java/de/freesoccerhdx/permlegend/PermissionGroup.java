package de.freesoccerhdx.permlegend;

import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;

public class PermissionGroup {

    private final ArrayList<String> permissions;
    private final String prefix;
    private final String name;


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

    public void setSiblingGroupName(@Nonnull String siblingGroupName) {
        this.siblingGroupName = siblingGroupName;
    }

    public void setChatMessageColor(@Nonnull String chatMessageColor) {
        this.chatMessageColor = chatMessageColor;
    }

    public void setSuffix(@Nonnull String suffix) {
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
}
