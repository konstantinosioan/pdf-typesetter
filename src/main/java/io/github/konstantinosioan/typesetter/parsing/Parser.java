package io.github.konstantinosioan.typesetter.parsing;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import io.github.konstantinosioan.typesetter.building.DocumentBuilder;
import io.github.konstantinosioan.typesetter.commands.Command;
import io.github.konstantinosioan.typesetter.commands.CommandRegistry;
import io.github.konstantinosioan.typesetter.model.Document;

/**
 * Reads lines of input and turns them into a {@link Document}.
 * A line starting with a dot is a command; any other line is text.
 * A line starting with {@code \&} is always text; the {@code \&} is removed,
 * so text can begin with a dot.
 */
public final class Parser {
    private static final String COMMAND_PREFIX = ".";
    private static final String TEXT_ESCAPE = "\\&";
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final CommandRegistry registry;

    /**
     * Creates a parser that looks up commands in the given registry.
     *
     * @param registry the registry used to look up commands
     * @throws NullPointerException if {@code registry} is null
     */
    public Parser(CommandRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null.");
    }

    /**
     * Parses the given lines into a document.
     *
     * @param lines the input, one element per line
     * @return the parsed document
     * @throws NullPointerException if {@code lines} is null or contains null
     * @throws ParseException if a line contains an unknown or malformed command
     */
    public Document parse(List<String> lines) throws ParseException {
        Objects.requireNonNull(lines, "lines must not be null.");
        DocumentBuilder builder = new DocumentBuilder();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.startsWith(COMMAND_PREFIX)) {
                applyCommand(line, i + 1, builder);
            } else if (line.startsWith(TEXT_ESCAPE)) {
                builder.addText(line.substring(TEXT_ESCAPE.length()));
            } else {
                builder.addText(line);
            }
        }

        return builder.build();
    }

    /**
     * Splits a command line into its name and argument, then applies the matching
     * command to the builder, reporting any problem against the given line number.
     */
    private void applyCommand(String line, int lineNumber, DocumentBuilder builder)
            throws ParseException {
        String[] parts = WHITESPACE.split(line.substring(COMMAND_PREFIX.length()).strip(), 2);
        String name = parts[0];
        String argument = parts.length > 1 ? parts[1] : "";
        Command command;

        try {
            command = registry.create(name, argument);
        } catch (IllegalArgumentException e) {
            throw new ParseException(lineNumber, e.getMessage(), e);
        }

        command.applyTo(builder);
    }
}
