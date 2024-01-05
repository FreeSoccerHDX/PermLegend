package de.freesoccerhdx.permlegend;

import java.util.HashMap;

import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import de.freesoccerhdx.lib.Pair;

public class SignDisplays {

    private final Plugin plugin;

    private HashMap<Pair<String, Vector>, SignDisplayData> signPositions = new HashMap<>();

    public SignDisplays(Plugin plugin, MessageConfig messageConfig, PermissionHandler permissionHandler) {
        this.plugin = plugin;
    }



    private class SignDisplayData {

    }
    
}
