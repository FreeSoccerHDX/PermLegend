package de.freesoccerhdx.lib.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class DynamicArgument extends ArgumentTile {
    private Supplier<String[]> futureArgs;

    public DynamicArgument(String helpPlaceHolder, Supplier<String[]> futureArgs) {
        super(helpPlaceHolder);
        this.futureArgs = futureArgs;
    }

    @Override
    public String[] getValidArguments() {
        return futureArgs.get();
    }


    public static class PlayerNamesArgument extends DynamicArgument {

        public PlayerNamesArgument(String helpPlaceHolder) {
            super(helpPlaceHolder, () -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new));
        }
    }

}