package io.github.konstantinosioan.typesetter.commands;

import java.util.Objects;

import io.github.konstantinosioan.typesetter.building.DocumentBuilder;
import io.github.konstantinosioan.typesetter.model.FillMode;

/**
 * Switches the fill mode, which controls how the following
 * paragraphs are aligned between the margins.
 *
 * @param mode the fill mode to switch to
 */
public record FillModeCommand(FillMode mode) implements Command {
    /**
     * Creates a command that switches to the given fill mode.
     *
     * @param mode the fill mode to switch to
     * @throws NullPointerException if {@code mode} is null
     */
    public FillModeCommand {
        Objects.requireNonNull(mode, "mode must not be null.");
    }

    @Override
    public void applyTo(DocumentBuilder builder) {
        builder.setFillMode(mode);
    }
}
