package de.freesoccerhdx.lib.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Optional;

public class ArgumentMap {

    private final CommandSender sender;
    private final String cmdname;
    private HashMap<String, String> argMap = new HashMap<>();

    public ArgumentMap(CommandSender sender, String cmdname) {
        this.sender = sender;
        this.cmdname = cmdname;
    }

    protected void setArgument(String placeholder, String value) {
        argMap.put(placeholder, value);
    }

    public String getArgument(String placeholder) {
        return argMap.get(placeholder);
    }

    public Optional<Integer> getArgumentAsInt(String placeholder) {
        String obj = getArgument(placeholder);
        if (obj != null) {
            try {
                return Optional.of(Integer.parseInt(obj));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public Optional<Double> getArgumentAsDouble(String placeholder) {
        String obj = getArgument(placeholder);
        if (obj != null) {
            try {
                return Optional.of(Double.parseDouble(obj));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public Player getArgumentAsPlayer(String placeholder) {
        String obj = getArgument(placeholder);
        if (obj != null) {
            return (Player) sender.getServer().getPlayer(obj);
        }
        return null;
    }


    public CommandSender getSender() {
        return sender;
    }

    public String getCommandName() {
        return cmdname;
    }
}
