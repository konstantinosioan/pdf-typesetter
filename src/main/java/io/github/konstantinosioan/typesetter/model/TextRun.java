package io.github.konstantinosioan.typesetter.model;

import java.util.Objects;

/**
 * A piece of text that has the same formatting all the way through. Text is stored verbatim;
 * spacing between runs is the responsibility of the DocumentBuilder.
 */
public record TextRun(String text, FontStyle style, FontSize size) {
    /**
     * @throws NullPointerException if any component is null
     */
    public TextRun {
        Objects.requireNonNull(text, "text must not be null.");
        Objects.requireNonNull(style, "style must not be null.");
        Objects.requireNonNull(size, "size must not be null.");
    }
}
