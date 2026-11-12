package io.github.konstantinosioan.typesetter.commands;

import static io.github.konstantinosioan.typesetter.model.FillMode.FILL;
import static io.github.konstantinosioan.typesetter.model.FillMode.NOFILL;
import static io.github.konstantinosioan.typesetter.model.FontSize.LARGE;
import static io.github.konstantinosioan.typesetter.model.FontSize.NORMAL;
import static io.github.konstantinosioan.typesetter.model.FontStyle.BOLD;
import static io.github.konstantinosioan.typesetter.model.FontStyle.ITALIC;
import static io.github.konstantinosioan.typesetter.model.FontStyle.REGULAR;

import java.util.Map;
import java.util.Objects;

/**
 * Turns a command name and its argument, as written in the input,
 * into the matching {@link Command}.
 */
public final class CommandRegistry {
    private static final Map<String, Command> NO_ARGUMENT_COMMANDS = Map.of(
            "paragraph", new ParagraphCommand(),
            "fill", new FillModeCommand(FILL),
            "nofill", new FillModeCommand(NOFILL),
            "regular", new FontStyleCommand(REGULAR),
            "italic", new FontStyleCommand(ITALIC),
            "bold", new FontStyleCommand(BOLD),
            "large", new FontSizeCommand(LARGE),
            "normal", new FontSizeCommand(NORMAL));

    /**
     * Returns the command with the given name. Only {@code indent} takes
     * an argument; every other command must be given an empty one.
     *
     * @param name the command name, without the leading dot
     * @param argument the text after the name, or an empty string if there is none
     * @return the matching command
     * @throws NullPointerException if {@code name} or {@code argument} is null
     * @throws IllegalArgumentException if the name is unknown, or the argument
     *         is missing, unexpected or malformed
     */
    public Command create(String name, String argument) {
        Objects.requireNonNull(name, "name must not be null.");
        Objects.requireNonNull(argument, "argument must not be null.");

        if (name.equals("indent")) {
            return IndentCommand.parse(argument);
        }

        Command command = NO_ARGUMENT_COMMANDS.get(name);

        if (command == null) {
            throw new IllegalArgumentException("Unknown command: ." + name);
        }

        if (!argument.isEmpty()) {
            throw new IllegalArgumentException("." + name + " takes no argument");
        }

        return command;
    }
}
