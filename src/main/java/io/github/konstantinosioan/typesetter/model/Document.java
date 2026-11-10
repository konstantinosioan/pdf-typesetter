package io.github.konstantinosioan.typesetter.model;

import java.util.List;
import java.util.Objects;

/**
 * An ordered list of paragraphs, independent of any output format.
 *
 * @param paragraphs the paragraphs in reading order; stored as an immutable copy
 */
public record Document(List<Paragraph> paragraphs) {
    /**
     * Creates a document, storing an immutable copy of {@code paragraphs}.
     *
     * @param paragraphs the paragraphs in reading order
     * @throws NullPointerException if {@code paragraphs} is null or contains null
     */
    public Document {
        Objects.requireNonNull(paragraphs, "paragraphs must not be null.");
        paragraphs = List.copyOf(paragraphs);
    }
}
