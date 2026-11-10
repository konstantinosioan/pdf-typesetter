package io.github.konstantinosioan.typesetter.model;

/**
 * How the lines of a paragraph are aligned between the margins.
 */
public enum FillMode {
    /** Justified to both margins, except the last line of a paragraph. */
    FILL,
    /** The default: left-aligned text with a ragged right edge. */
    NOFILL
}
