package io.github.konstantinosioan.typesetter.commands;

import java.util.Objects;

import io.github.konstantinosioan.typesetter.building.DocumentBuilder;

/**
 * Shifts the left margin of the following paragraphs
 * by a number of indent units.
 *
 * @param delta the number of indent units to add;
 *        negative values remove indentation
 */
public record IndentCommand(int delta) implements Command {
    /**
     * Creates a command from a signed indent argument, as written after
     * {@code .indent} in the input.
     *
     * @param argument a signed integer such as {@code +2} or {@code -1}
     * @return a command that shifts the indent by the given amount
     * @throws NullPointerException if {@code argument} is null
     * @throws IllegalArgumentException if {@code argument} is not a signed integer
     */
    public static IndentCommand parse(String argument) {
        Objects.requireNonNull(argument, "argument must not be null.");

        if (argument.isEmpty()) {
            throw new IllegalArgumentException(".indent requires an argument such as +2");
        }

        if (!argument.startsWith("+") && !argument.startsWith("-")) {
            throw new IllegalArgumentException(
                ".indent argument must start with + or -: " + argument);
        }

        try {
            return new IndentCommand(Integer.parseInt(argument));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                ".indent argument must be a signed integer: " + argument, e);
        }
    }

    @Override
    public void applyTo(DocumentBuilder builder) {
        builder.changeIndent(delta);
    }
}
