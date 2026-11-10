package io.github.konstantinosioan.typesetter.model;

import java.util.List;
import java.util.Objects;

/**
 * A block of runs that share paragraph-level settings.
 *
 * @param indentLevel number of indent units (not points) from the left margin; never negative
 * @param fillMode    how the paragraph's lines are aligned between the margins
 * @param runs        the paragraph's text in reading order; stored as an immutable copy
 */
public record Paragraph(int indentLevel, FillMode fillMode, List<TextRun> runs) {
    /**
     * Creates a paragraph, storing an immutable copy of {@code runs}.
     *
     * @param indentLevel number of indent units from the left margin
     * @param fillMode    how the paragraph's lines are aligned
     * @param runs        the paragraph's text in reading order
     * @throws NullPointerException     if {@code fillMode} or {@code runs} is null,
     *                                  or {@code runs} contains null
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
