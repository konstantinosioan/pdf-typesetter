package io.github.konstantinosioan.typesetter.model;

/**
 * The style variant applied to a run of text.
 * The renderer maps each value to a concrete font.
 */
public enum FontStyle {
    /** The default, upright style. */
    REGULAR,
    /** A slanted style, typically used for emphasis. */
    ITALIC,
    /** A heavier style, typically used for strong emphasis. */
    BOLD
}
