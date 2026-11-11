package io.github.konstantinosioan.typesetter.commands;

import java.util.Objects;

import io.github.konstantinosioan.typesetter.building.DocumentBuilder;
import io.github.konstantinosioan.typesetter.model.FontStyle;

/**
 * Switches the font style used for the following text.
 *
 * @param style the font style to switch to
 */
public record FontStyleCommand(FontStyle style) implements Command {
    /**
     * Creates a command that switches to the given font style.
     *
     * @param style the font style to switch to
     * @throws NullPointerException if {@code style} is null
     */
    public FontStyleCommand {
        Objects.requireNonNull(style, "style must not be null.");
    }

    @Override
    public void applyTo(DocumentBuilder builder) {
        builder.setFontStyle(style);
    }
}
