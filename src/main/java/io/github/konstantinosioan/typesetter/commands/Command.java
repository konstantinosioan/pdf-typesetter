package io.github.konstantinosioan.typesetter.commands;

import io.github.konstantinosioan.typesetter.building.DocumentBuilder;

/**
 * A single formatting instruction from the input that changes
 * the state of a {@link DocumentBuilder}.
 */
public sealed interface Command permits FillModeCommand, FontSizeCommand,
        FontStyleCommand, IndentCommand, ParagraphCommand {
    /**
     * Applies this command to the given builder, changing its
     * formatting state or paragraph structure.
     *
     * @param builder the builder to update
     * @throws NullPointerException if {@code builder} is null
     */
    void applyTo(DocumentBuilder builder);
}
