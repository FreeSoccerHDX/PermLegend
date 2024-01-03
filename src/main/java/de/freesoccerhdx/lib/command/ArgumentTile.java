package de.freesoccerhdx.lib.command;

public abstract class ArgumentTile {

    private final String helpPlaceHolder;

    public ArgumentTile(String helpPlaceHolder) {
        this.helpPlaceHolder = helpPlaceHolder;
    }

    public abstract String[] getValidArguments();

    public String getHelpPlaceHodler() {
        return helpPlaceHolder;
    }


    @Override
    public int hashCode() {
        return getHelpPlaceHodler().hashCode();
    }

}
