package io.github.konstantinosioan.typesetter.commands;

import java.util.Objects;

import io.github.konstantinosioan.typesetter.building.DocumentBuilder;
import io.github.konstantinosioan.typesetter.model.FontSize;

/**
 * Switches the font size used for the following text.
 *
 * @param size the font size to switch to
 */
public record FontSizeCommand(FontSize size) implements Command {
    /**
     * Creates a command that switches to the given font size.
     *
     * @param size the font size to switch to
     * @throws NullPointerException if {@code size} is null
     */
    public FontSizeCommand {
        Objects.requireNonNull(size, "size must not be null.");
    }

    @Override
    public void applyTo(DocumentBuilder builder) {
        builder.setFontSize(size);
    }
}
