package de.freesoccerhdx.lib.command;

public class StaticArgument extends ArgumentTile {
    private String[] args;

    public StaticArgument(String helpPlaceHolder, String... args) {
        super(helpPlaceHolder);
        this.args = args;
    }

    @Override
    public String[] getValidArguments() {
        return args;
    }
}