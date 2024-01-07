package de.freesoccerhdx.permlegend;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.UUID;

import javax.annotation.Nullable;

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

import de.freesoccerhdx.lib.Methods;
import de.freesoccerhdx.permlegend.SignDisplays.SignDisplayData;

public class SignDisplays {

    private final Plugin plugin;
    private final PermissionHandler permissionHandler;
    private final SQLDatabase sqlDatabase;

    private ArrayList<SignDisplayData> signPositions = new ArrayList<>();

    private ArrayList<SignDisplayData> spawnedSignPositions = new ArrayList<>();
    private ArrayList<SignDisplayData> unspawnedSignPositions = new ArrayList<>();

    private Queue<Runnable> tasks = new ArrayDeque<>();

    public SignDisplays(Plugin plugin, MessageConfig messageConfig, PermissionHandler permissionHandler, SQLDatabase sqlDatabase) {
        this.plugin = plugin;
        this.permissionHandler = permissionHandler;
        this.sqlDatabase = sqlDatabase;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

            @Override
            public void run() {
                loadData();
            }

        });

        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {

            @Override
            public void run() {

                long currentTime = System.nanoTime();
                long endTime = currentTime + 1000000; // max 1ms

                while (System.nanoTime() <= endTime) {
                    if (tasks.size() > 0) {
                        tasks.poll().run();
                    } else {
                        for (SignDisplayData signDisplayData : unspawnedSignPositions) {
                            tasks.add(() -> {
                                if (updateSignDisplayData(signDisplayData, null)) {
                                    unspawnedSignPositions.remove(signDisplayData);
                                    spawnedSignPositions.add(signDisplayData);
                                }
                            });
                        }

                        for (SignDisplayData signDisplayData : spawnedSignPositions) {
                            tasks.add(() -> {
                                if (!updateSignDisplayData(signDisplayData, null)) {
                                    unspawnedSignPositions.add(signDisplayData);
                                    spawnedSignPositions.remove(signDisplayData);
                                }
                            });
                        }
                        break;
                    }
                }
            }

        }, 20 * 5, 20);
    }

    public void addSign(UUID ownerUUID, Block posBlock) {
        int[] pos = new int[] { posBlock.getX(), posBlock.getY(), posBlock.getZ() };

        for (SignDisplayData sdd : this.signPositions) {
            int[] toCheck = sdd.getPosition();
            if (toCheck[0] == pos[0] && toCheck[1] == pos[1] && toCheck[2] == pos[2]) {
                sdd.ownerUUID = ownerUUID;
                updateSignDisplayData(sdd, null);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        saveData();
                    }
                });
                this.permissionHandler.addGroupChangeHook(ownerUUID, playerPermData -> {
                    updateSignDisplayData(sdd, playerPermData);
                });
                return;
            }
        }

        SignDisplayData sdd = new SignDisplayData(ownerUUID, pos, posBlock.getWorld().getName());
        this.signPositions.add(sdd);
        this.permissionHandler.addGroupChangeHook(ownerUUID, playerPermData -> {
            updateSignDisplayData(sdd, playerPermData);
        });
        this.unspawnedSignPositions.add(sdd);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                saveData();
            }
        });

    }

    private void saveData() {
        File file = new File(this.plugin.getDataFolder(), "signdisplays.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        try {

            for (int i = 0; i < signPositions.size(); i++) {
                SignDisplayData sDD = signPositions.get(i);
                int[] pos = sDD.getPosition();
                cfg.set("signs." + i + ".ownerUUID", sDD.getOwnerUUID().toString());
                cfg.set("signs." + i + ".x", pos[0]);
                cfg.set("signs." + i + ".y", pos[1]);
                cfg.set("signs." + i + ".z", pos[2]);
                cfg.set("signs." + i + ".world", sDD.getWorld());

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
                    int[] pos = new int[3];
                    pos[0] = cfg.getInt(path + ".x");
                    pos[1] = cfg.getInt(path + ".y");
                    pos[2] = cfg.getInt(path + ".z");

                    SignDisplayData sDD = new SignDisplayData(ownerUUID, pos, world);
                    signPositions.add(sDD);
                    this.permissionHandler.addGroupChangeHook(ownerUUID, playerPermData -> {
                        updateSignDisplayData(sDD, playerPermData);
                    });
                    unspawnedSignPositions.add(sDD);
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private boolean updateSignDisplayData(SignDisplayData signDisplayData,
            @Nullable PlayerPermissionData playerPermissionData) {
        String worldname = signDisplayData.getWorld();
        int[] pos = signDisplayData.getPosition();

        World world = Bukkit.getWorld(worldname);
        if (world != null) {
            if (world.isChunkLoaded(((int) pos[0]) >> 4, ((int) pos[2]) >> 4)) {
                Location loc = new Location(world, pos[0], pos[1], pos[2]);

                Block block = loc.getBlock();
                if (Tag.ALL_SIGNS.isTagged(block.getType())) {
                    Sign sign = (Sign) block.getState();
                    UUID uuid = signDisplayData.getOwnerUUID();
                    if (playerPermissionData == null) {
                        playerPermissionData = this.sqlDatabase.getPlayerPermissionData(uuid);
                    }
                    if (playerPermissionData != null) {
                        if (playerPermissionData.getUuid().equals(signDisplayData.getOwnerUUID())) {
                            String groupname = playerPermissionData.getEffectiveGroupName();
                            PermissionGroup permissionGroup = this.permissionHandler.getGroupByGroupName(groupname);
                            if (permissionGroup != null) {
                                SignSide side = sign.getSide(Side.FRONT);
                                side.setLine(0, "");
                                side.setLine(1, Methods.replaceColorCodes(permissionGroup.getPrefix()));
                                side.setLine(2, playerPermissionData.getPlayerName());
                                if(playerPermissionData.hasTempGroup()) {
                                    side.setLine(3, Methods.secondsToCountdown((playerPermissionData.getTempGroupEnd()-System.currentTimeMillis())/1000));
                                } else {
                                    side.setLine(3, "");
                                }
                                

                                sign.update();
                                return true;
                            }
                        }
                    }

                }
            }
        }
        return false;
    }

    public class SignDisplayData {
        private UUID ownerUUID;
        private int[] position;
        private String world;

        public SignDisplayData(UUID ownerUUID, int[] position, String world) {
            this.ownerUUID = ownerUUID;
            this.position = position;
            this.world = world;
        }

        public UUID getOwnerUUID() {
            return ownerUUID;
        }

        public int[] getPosition() {
            return position;
        }

        public String getWorld() {
            return world;
        }

        public void setOwnerUUID(UUID ownerUUID) {
            this.ownerUUID = ownerUUID;
        }
    }

}
