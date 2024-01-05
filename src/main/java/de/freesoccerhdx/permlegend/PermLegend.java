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

    @Override
    public void onEnable() {

        this.permissionHandler = new PermissionHandler(this);
        this.messageConfig = new MessageConfig(this);

        SignDisplays signDisplays = new SignDisplays(this, messageConfig, permissionHandler);

        PermissionCommand permissionCommand = new PermissionCommand(this, permissionHandler, messageConfig, signDisplays);

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {

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
