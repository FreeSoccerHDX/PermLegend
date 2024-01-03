package de.freesoccerhdx.lib.command;

import de.freesoccerhdx.lib.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class CustomCommand extends Arg implements TabCompleter, CommandExecutor {

    private boolean handleMultiArg(CommandSender sender, String cmd, int argindex, String[] args, Arg originArg, ArgumentMap map) {
        HashMap<MultiArgument, Arg> multiArgTiles = originArg.multiArgTiles;
        String currentArgu = args[argindex];

        for (Map.Entry<MultiArgument, Arg> entry : multiArgTiles.entrySet()) {
            MultiArgument multiArgument = entry.getKey();
            Arg arg = entry.getValue();

            String start = multiArgument.getStart();
            String end = multiArgument.getEnd();


            ArrayList<String> usedArgs = new ArrayList<>();
            if (currentArgu.startsWith(start)) {
                for (int i = argindex; i < args.length; i++) {
                    String arg0 = args[i];
                    usedArgs.add(arg0);
                    if (arg0.endsWith(end)) {

                        if ((end.isEmpty() || i == (args.length - 1)) && arg.commandListener != null) {
                            if (end.isEmpty()) {
                                for (int x = i + 1; x < args.length; x++) {
                                    usedArgs.add(args[x]);
                                }
                            }

                            map.setArgument(multiArgument.getHelpPlaceHodler(), String.join(" ", usedArgs));
                            arg.commandListener.onCommand(map);
                            return true;
                        } else {
                            map.setArgument(multiArgument.getHelpPlaceHodler(), String.join(" ", usedArgs));
                            if (args.length > (i + 1)) {
                                String newArg0 = args[i + 1];
                                HashMap<String, Pair<ArgumentTile, Arg>> argMap = arg.createArgsMap(newArg0);

                                if (argMap.containsKey(newArg0)) {
                                    return handleInput(sender, cmd, i + 1, args, argMap, arg, map);
                                }

                            }
                            if (arg.onFailMessage != null) {
                                sender.sendMessage(arg.onFailMessage);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }


    private boolean handleInput(CommandSender sender, String cmd, int argindex, String[] args, HashMap<String, Pair<ArgumentTile, Arg>> argTiles, Arg originArg, ArgumentMap map) {
        String currentArg = args[argindex];

        if (argTiles.containsKey(currentArg)) {
            Pair<ArgumentTile, Arg> tileArgPair = argTiles.get(currentArg);
            Arg tile = tileArgPair.getRight();
            ArgumentTile arg = tileArgPair.getLeft();
            map.setArgument(arg.getHelpPlaceHodler(), currentArg);

            if (tile.commandListener != null && argindex == (args.length - 1)) {
                tile.commandListener.onCommand(map);
                return true;
            } else {
                if (args.length > (argindex + 1)) {
                    String newArg = args[argindex + 1];
                    HashMap<String, Pair<ArgumentTile, Arg>> argMap = tile.createArgsMap(newArg);

                    if (argMap.containsKey(newArg)) {
                        boolean success = handleInput(sender, cmd, argindex + 1, args, argMap, tile, map);
                        if (!success && tile.onFailMessage != null) {
                            sender.sendMessage(tile.onFailMessage);
                            return true;
                        }
                        return success;
                    } else {
                        if (!tile.multiArgTiles.isEmpty()) {
                            return handleMultiArg(sender, cmd, argindex + 1, args, tile, map);
                        }
                    }

                }
                if (tile.onFailMessage != null) {
                    sender.sendMessage(tile.onFailMessage);
                    return true;
                }
            }
        } else if ((args.length >= (argindex + 1)) && !originArg.multiArgTiles.isEmpty()) {
            return handleMultiArg(sender, cmd, argindex, args, originArg, map);
        }

        return false;
    }

    private PluginCommand command;

    public CustomCommand(String commandName) {
        this.command = Bukkit.getPluginCommand(commandName);
        this.command.setTabCompleter(this);
        this.command.setExecutor(this);

        this.commandListener = new CommandListener("Shows this Command-Help", argMap-> {
            this.getCommandHelp("").forEach(argMap.getSender()::sendMessage);
        });
    }

    public PluginCommand getCommand() {
        return command;
    }

    @Override
    public ArrayList<String> getCommandHelp(String commandPrefix) {
        ArrayList<String> list = new ArrayList<>();

        String cmdName = command.getName();
        list.add("§6§lHelp for §e§l/" + cmdName + "§6§l:");
        if (this.commandListener != null) {
            list.add("§6/" + cmdName + " §8- §7" + this.commandListener.getDescription());
        }
        this.argTiles.forEach((tile, arg) -> {
            list.addAll(arg.getCommandHelp("§6/" + cmdName + " " + tile.getHelpPlaceHodler() + " "));
        });

        return list;
    }

    @Override
    public ArrayList<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        ArrayList<String> compl = new ArrayList<>();

        HashMap<String, Arg> argMap = new HashMap<>();
        CustomCommand.this.argTiles.forEach((tile, arg0) -> {
            for (String s : tile.getValidArguments()) {
                argMap.put(s, arg0);
            }
        });

        if (args.length > 0) {
            Arg lastArg = CustomCommand.this;
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                HashMap<ArgumentTile, Arg> argTiles = lastArg.argTiles;

                if (i == args.length - 1) {
                    if (arg.isEmpty()) {
                        lastArg.argTiles.forEach((tile, arg0) -> {
                            compl.addAll(Arrays.asList(tile.getValidArguments()));
                        });
                    } else {
                        for (ArgumentTile tile : argTiles.keySet()) {
                            for (String s : tile.getValidArguments()) {
                                if (s.startsWith(arg)) {
                                    compl.add(s);
                                }
                            }
                            if (tile instanceof TypeArgument typeArg) {
                                if (typeArg.checkArgument(arg)) {
                                    compl.add(arg);
                                }
                            }
                        }
                    }

                } else {
                    boolean valid = false;
                    for (ArgumentTile tile : argTiles.keySet()) {
                        if (Arrays.asList(tile.getValidArguments()).contains(arg)) {
                            lastArg = argTiles.get(tile);
                            valid = true;
                            break;
                        } else if (tile instanceof TypeArgument typeArg) {
                            if (typeArg.checkArgument(arg)) {
                                lastArg = argTiles.get(tile);
                                valid = true;
                                break;
                            }
                        } else if (tile instanceof MultiArgument multiArg) {
                            if (arg.startsWith(multiArg.getStart())) {
                                for (int x = i; x < args.length; x++) {
                                    String arg0 = args[x];
                                    if (arg0.endsWith(multiArg.getEnd())) {
                                        lastArg = argTiles.get(tile);
                                        valid = true;
                                        i = x;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (!valid) {
                        return new ArrayList<>();
                    }

                }

            }
        }

        return compl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        ArgumentMap map = new ArgumentMap(sender, label);
        if (CustomCommand.this.commandListener != null && args.length == 0) {
            CustomCommand.this.commandListener.onCommand(map);
        } else {
            if (args.length > 0) {
                HashMap<String, Pair<ArgumentTile, Arg>> argMap = createArgsMap(args[0]);

                boolean success = handleInput(sender, label, 0, args, argMap, CustomCommand.this, map);
                if (!success && CustomCommand.this.onFailMessage != null) {
                    sender.sendMessage(CustomCommand.this.onFailMessage);
                } else if (!success) {
                    CustomCommand.this.getCommandHelp("").forEach(sender::sendMessage);
                }

            } else if (CustomCommand.this.onFailMessage != null) {
                sender.sendMessage(CustomCommand.this.onFailMessage);
            }
        }


        return false;
    }
}
