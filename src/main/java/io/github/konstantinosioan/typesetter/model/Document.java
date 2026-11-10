package io.github.konstantinosioan.typesetter.model;

import java.util.List;
import java.util.Objects;

/**
 * An ordered list of paragraphs, independent of any output format.
 */
public record Document(List<Paragraph> paragraphs) {
    /**
     * @throws NullPointerException if {@code paragraphs} is null
     */
    public Document {
        Objects.requireNonNull(paragraphs, "paragraphs must not be null.");
        paragraphs = List.copyOf(paragraphs);
    }
}
