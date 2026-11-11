package io.github.konstantinosioan.typesetter.commands;

import io.github.konstantinosioan.typesetter.building.DocumentBuilder;

/**
 * Ends the current paragraph, so that the following text
 * starts a new one.
 */
public record ParagraphCommand() implements Command {
    @Override
    public void applyTo(DocumentBuilder builder) {
        builder.startParagraph();
    }
}
