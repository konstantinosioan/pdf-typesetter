package io.github.konstantinosioan.typesetter.model;

import java.util.Objects;
import java.util.List;

/**
 * A block of runs that share paragraph-level settings.
 */
public record Paragraph(int indentLevel, FillMode fillMode, List<TextRun> runs) {
    /**
     * @throws NullPointerException if {@code fillMode} or {@code runs} is null
     * @throws IllegalArgumentException if {@code indentLevel} is negative
     */
    public Paragraph {
        Objects.requireNonNull(fillMode, "fillMode must not be null.");
        Objects.requireNonNull(runs, "runs must not be null.");

        if (indentLevel < 0) {
            throw new IllegalArgumentException("indentLevel must not be negative.");
        }

        runs = List.copyOf(runs);
    }
}
