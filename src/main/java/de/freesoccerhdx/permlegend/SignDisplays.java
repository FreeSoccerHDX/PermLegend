package de.freesoccerhdx.permlegend;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import de.freesoccerhdx.lib.Methods;

public class SignDisplays {

    private final Plugin plugin;
    private final PermissionHandler permissionHandler;

    private ArrayList<SignDisplayData> signPositions = new ArrayList<>();

    private ArrayList<SignDisplayData> spawnedSignPositions = new ArrayList<>();
    private ArrayList<SignDisplayData> unspawnedSignPositions = new ArrayList<>();

    public SignDisplays(Plugin plugin, MessageConfig messageConfig, PermissionHandler permissionHandler) {
        this.plugin = plugin;
        this.permissionHandler = permissionHandler;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

            @Override
            public void run() {
                loadData();
            }

        });

        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {

            @Override
            public void run() {
                tryToSpawnAll();
                ArrayList<SignDisplayData> toRem = new ArrayList<>();
                spawnedSignPositions.forEach(signdisplayData -> {
                    if (!updateSignDisplayData(signdisplayData)) {
                        toRem.add(signdisplayData);
                    }
                });

                toRem.forEach(signdisplayData -> {
                    spawnedSignPositions.remove(signdisplayData);
                    unspawnedSignPositions.add(signdisplayData);
                });
            }

        }, 20 * 5, 20 * 20);
    }

    public void addSign(UUID ownerUUID, Block posBlock) {
        SignDisplayData sdd = new SignDisplayData(ownerUUID, posBlock.getLocation().toVector(), posBlock.getWorld().getName());
        this.signPositions.add(sdd);
        this.unspawnedSignPositions.add(sdd);
        saveData();
    }

    private void saveData() {
        File file = new File(this.plugin.getDataFolder(), "signdisplays.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        try {

            for (int i = 0; i < signPositions.size(); i++) {
                SignDisplayData sDD = signPositions.get(i);
                cfg.set("signs." + i + ".ownerUUID", sDD.ownerUUID().toString());
                cfg.set("signs." + i + ".position", sDD.position().serialize());
                cfg.set("signs." + i + ".world", sDD.world());

            }

            cfg.save(file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void loadData() {
        File file = new File(this.plugin.getDataFolder(), "signdisplays.yml");
        if (!file.exists()) {
            return;
        }
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(file);
            if (cfg.isConfigurationSection("signs")) {
                for (String key : cfg.getConfigurationSection("signs").getKeys(false)) {
                    String path = "signs." + key;

                    UUID ownerUUID = UUID.fromString(cfg.getString(path + ".ownerUUID"));
                    String world = cfg.getString(path + ".world");
                    Vector position = Vector
                            .deserialize(cfg.getConfigurationSection(path + ".position").getValues(false));

                    SignDisplayData sDD = new SignDisplayData(ownerUUID, position, world);
                    signPositions.add(sDD);
                    unspawnedSignPositions.add(sDD);
                }
            }
            

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void tryToSpawnAll() {
        ArrayList<SignDisplayData> toRem = new ArrayList<>();

        for (SignDisplayData signDisplayData : unspawnedSignPositions) {
            if (updateSignDisplayData(signDisplayData)) {
                toRem.add(signDisplayData);
                spawnedSignPositions.add(signDisplayData);
            }
        }
    }

    private boolean updateSignDisplayData(SignDisplayData signDisplayData) {
        String worldname = signDisplayData.world();
        Vector pos = signDisplayData.position();

        World world = Bukkit.getWorld(worldname);
        if (world != null) {
            if (world.isChunkLoaded(((int) pos.getX()) >> 4, ((int) pos.getZ()) >> 4)) {
                Location loc = new Location(world, pos.getX(), pos.getY(), pos.getZ());

                Block block = loc.getBlock();
                if (Tag.ALL_SIGNS.isTagged(block.getType())) {
                    Sign sign = (Sign) block.getState();
                    UUID uuid = signDisplayData.ownerUUID();
                    PlayerPermissionData playerPermissionData = this.permissionHandler.getPlayerPermissionData(uuid);
                    if (playerPermissionData != null) {
                        String groupname = playerPermissionData.getEffectiveGroupName();
                        PermissionGroup permissionGroup = this.permissionHandler.getGroupByGroupName(groupname);
                        if (permissionGroup != null) {
                            SignSide side = sign.getSide(Side.FRONT);
                            side.setLine(0, "");
                            side.setLine(1, Methods.replaceColorCodes(permissionGroup.getPrefix()));
                            side.setLine(2, playerPermissionData.getName());
                            side.setLine(3, "");

                            sign.update();
                            return true;
                        }
                    }

                }
            }
        }
        return false;
    }

    private record SignDisplayData(UUID ownerUUID, Vector position, String world) {

    }

}
