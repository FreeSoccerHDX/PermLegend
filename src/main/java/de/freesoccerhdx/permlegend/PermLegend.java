package de.freesoccerhdx.permlegend;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PermLegend extends JavaPlugin implements Listener {

    private PermissionHandler permissionHandler;
    private MessageConfig messageConfig;
    private SQLDatabase sqlDatabase;
    private SignDisplays signDisplays;

    @Override
    public void onEnable() {

        this.sqlDatabase = new SQLDatabase(this);
        this.permissionHandler = new PermissionHandler(this, sqlDatabase);
        this.messageConfig = new MessageConfig(this);

        this.signDisplays = new SignDisplays(this, messageConfig, permissionHandler, sqlDatabase);

        PermissionCommand permissionCommand = new PermissionCommand(this);

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {

    }

    public PermissionHandler getPermissionHandler() {
        return permissionHandler;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public SQLDatabase getSqlDatabase() {
        return sqlDatabase;
    }

    public SignDisplays getSignDisplays() {
        return signDisplays;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.permissionHandler.updatePlayerPermission(player);
        PermissionGroup permissionGroup = this.permissionHandler.getGroup(player.getUniqueId());
        String prefix = permissionGroup.getPrefix();
        String suffix = permissionGroup.getSuffix();

        event.setJoinMessage(this.messageConfig.getJoinMessage(prefix, player.getName(), suffix));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PermissionGroup permissionGroup = this.permissionHandler.getGroup(player.getUniqueId());
        String prefix = permissionGroup.getPrefix();
        String suffix = permissionGroup.getSuffix();
        String chatColor = permissionGroup.getChatMessageColor();

        event.setCancelled(true);
        String msg = this.messageConfig.getChatMessage(prefix, player.getName(), suffix, chatColor, event.getMessage());
        event.getRecipients().forEach(player1 -> player1.sendMessage(msg));
        Bukkit.getConsoleSender().sendMessage(msg);
    }

}
