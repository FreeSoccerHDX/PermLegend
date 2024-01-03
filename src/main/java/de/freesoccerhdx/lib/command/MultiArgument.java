package de.freesoccerhdx.lib.command;

public final class MultiArgument extends ArgumentTile {

    private final String start;
    private final String end;

    public MultiArgument(String helpPlaceHolder, String start, String end) {
        super(helpPlaceHolder);
        this.start = start;
        this.end = end;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    @Override
    public String[] getValidArguments() {
        return new String[]{getHelpPlaceHodler()};
    }

}
