package de.freesoccerhdx.lib.command;

import de.freesoccerhdx.lib.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Arg {

    protected HashMap<ArgumentTile, Arg> argTiles = new HashMap<>();
    protected HashMap<MultiArgument, Arg> multiArgTiles = new HashMap<>();
    protected CommandListener commandListener = null;
    protected boolean showCommandHelp = true;
    protected String onFailMessage = null;

    public List<String> getCommandHelp(String commandPrefix) {
        ArrayList<String> list = new ArrayList<>();
        if (this.commandListener != null) {
            if (showCommandHelp) {
                list.add(commandPrefix + "§8- §7" + this.commandListener.getDescription());
            }
        }

        this.argTiles.forEach((tile, arg) -> {
            String newcom = commandPrefix + tile.getHelpPlaceHodler() + " ";
            list.addAll(arg.getCommandHelp(newcom));
        });
        return list;
    }

    public Arg setListener(CommandListener commandListener, boolean showCommandHelp) {
        this.showCommandHelp = showCommandHelp;
        return setListener(commandListener);
    }

    public Arg setListener(CommandListener commandListener) {
        this.commandListener = commandListener;
        return this;
    }

    public Arg fail(String onFailMessage) {
        this.onFailMessage = onFailMessage;
        return this;
    }

    /**
     * Method to create easy commands with multiple arguments
     *
     * @param tile A Chain of Arguments that are required to be executed
     * @return The last ArgumentTile in the chain
     */
    public Arg multiApply(ArgumentTile... tile) {

        Arg last = this;

        for(ArgumentTile at : tile) {
            Arg arg = new Arg();
            last.apply(at, arg);
            last = arg;
        }

        /*
        Arg last = this;

        for (ArgumentTile t : tile) {
            Arg nextArg = new Arg();
            last.apply(t, nextArg);
            last = nextArg;
        }

        return last;*/
        return last;
    }


    public Arg apply(String item, CommandListener listener) {
        return apply(item, new Arg().setListener(listener));
    }

    public Arg apply(ArgumentTile tile, CommandListener listener) {
        //this.argTiles.put(tile, new Arg().setListener(listener));
        //return this;
        return apply(tile, new Arg().setListener(listener));
    }

    public Arg apply(ArgumentTile tile, Arg argument) {
        if (tile instanceof MultiArgument ma) {
            multiArgTiles.put(ma, argument);
        }
        argTiles.put(tile, argument);

        return this;
    }

    public Arg apply(String arg, Arg commandArgument) {
        argTiles.put(new StaticArgument(arg, arg), commandArgument);
        return this;
    }

    public HashMap<ArgumentTile, Arg> getArgTiles() {
        return argTiles;
    }

    protected HashMap<String, Pair<ArgumentTile, Arg>> createArgsMap(String currentArg) {
        HashMap<String, Pair<ArgumentTile, Arg>> argMap = new HashMap<>();
        this.argTiles.forEach((tile, arg) -> {
            if (tile instanceof TypeArgument typeArg) {
                if (typeArg.checkArgument(currentArg)) {
                    argMap.put(currentArg, new Pair<>(tile, arg));
                }
            } else {
                if (!(tile instanceof MultiArgument)) {
                    for (String s : tile.getValidArguments()) {
                        argMap.put(s, new Pair<>(tile, arg));
                    }
                }
            }
        });
        return argMap;
    }

}