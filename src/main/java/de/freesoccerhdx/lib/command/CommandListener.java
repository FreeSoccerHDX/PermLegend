package de.freesoccerhdx.lib.command;

import java.util.function.Consumer;

public class CommandListener {

    private String description;
    private Consumer<ArgumentMap> argumentMapConsumer;

    public CommandListener(String description, Consumer<ArgumentMap> argumentMapConsumer) {
        this.description = description;
        this.argumentMapConsumer = argumentMapConsumer;
    }

    public void onCommand(ArgumentMap argumentMap) {
        if (argumentMapConsumer != null) {
            argumentMapConsumer.accept(argumentMap);
        }
    }

    public final String getDescription() {
        return description;
    }
}