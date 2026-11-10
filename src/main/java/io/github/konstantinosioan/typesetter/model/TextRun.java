package io.github.konstantinosioan.typesetter.model;

import java.util.Objects;

/**
 * A piece of text that has the same formatting all the way through. Text is stored verbatim,
 * so any spacing between runs must already be part of the text.
 *
 * @param text  the text, exactly as it should appear; may be empty
 * @param style the style variant the text is drawn in
 * @param size  the size the text is drawn at
 */
public record TextRun(String text, FontStyle style, FontSize size) {
    /**
     * Creates a run of text.
     *
     * @param text  the text, exactly as it should appear
     * @param style the style variant the text is drawn in
     * @param size  the size the text is drawn at
     * @throws NullPointerException if any argument is null
     */
    public TextRun {
        Objects.requireNonNull(text, "text must not be null.");
        Objects.requireNonNull(style, "style must not be null.");
        Objects.requireNonNull(size, "size must not be null.");
    }
}
