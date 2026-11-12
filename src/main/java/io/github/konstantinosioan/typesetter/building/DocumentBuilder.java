package io.github.konstantinosioan.typesetter.building;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.konstantinosioan.typesetter.model.Document;
import io.github.konstantinosioan.typesetter.model.FillMode;
import io.github.konstantinosioan.typesetter.model.FontSize;
import io.github.konstantinosioan.typesetter.model.FontStyle;
import io.github.konstantinosioan.typesetter.model.Paragraph;
import io.github.konstantinosioan.typesetter.model.TextRun;

/**
 * Holds the current formatting state and turns a stream of method calls into
 * a document.
 * It knows nothing about parsing or how the document is rendered.
 */
public final class DocumentBuilder {
    // Quote marks are left out on purpose: a straight " or ' can open or close a quotation,
    // so the character alone does not say which word it belongs to.
    private static final String CLOSING_PUNCTUATION = ",.;:!?)]}";
    private static final String OPENING_PUNCTUATION = "([{";

    private FontStyle currentStyle = FontStyle.REGULAR;
    private FontSize currentSize = FontSize.NORMAL;
    private FillMode currentFillMode = FillMode.NOFILL;
    private int indentLevel;

    private final List<Paragraph> completedParagraphs = new ArrayList<>();
    private final List<TextRun> currentRuns = new ArrayList<>();

    /**
     * Sets the style used for text added from now on.
     *
     * @param style the style to use
     * @throws NullPointerException if {@code style} is null
     */
    public void setFontStyle(FontStyle style) {
        currentStyle = Objects.requireNonNull(style, "style must not be null.");
    }

    /**
     * Sets the size used for text added from now on.
     *
     * @param size the size to use
     * @throws NullPointerException if {@code size} is null
     */
    public void setFontSize(FontSize size) {
        currentSize = Objects.requireNonNull(size, "size must not be null.");
    }

    /**
     * Sets the fill mode. Because fill mode applies to a whole paragraph, an
     * actual change ends the current paragraph; setting the mode it already has
     * does nothing.
     *
     * @param mode the fill mode to use
     * @throws NullPointerException if {@code mode} is null
     */
    public void setFillMode(FillMode mode) {
        Objects.requireNonNull(mode, "mode must not be null.");

        if (mode == currentFillMode) {
            return;
        }

        finishParagraph();
        currentFillMode = mode;
    }

    /**
     * Ends the current paragraph, so text added afterwards starts a new
     * one. Calling this repeatedly never produces empty paragraphs.
     */
    public void startParagraph() {
        finishParagraph();
    }

    /**
     * Changes the indent by {@code delta} units. The indent never goes
     * below zero, and stops at {@link Integer#MAX_VALUE} instead of overflowing.
     * Because indent applies to a whole paragraph, an actual change ends the
     * current paragraph; a change that leaves the indent the same does nothing.
     *
     * @param delta the number of units to indent by; negative to outdent
     */
    public void changeIndent(int delta) {
        long requestedLevel = (long) indentLevel + delta;
        int newLevel = (int) Math.max(0, Math.min(Integer.MAX_VALUE, requestedLevel));

        if (newLevel == indentLevel) {
            return;
        }

        finishParagraph();
        indentLevel = newLevel;
    }

    /**
     * Adds text to the current paragraph in the current style and size.
     * Surrounding whitespace is stripped and blank text is ignored.
     * Text that continues a paragraph is preceded by a single space, unless
     * it starts with closing punctuation, or the previous text ends with
     * opening punctuation.
     *
     * @param text the text to add
     * @throws NullPointerException if {@code text} is null
     */
    public void addText(String text) {
        Objects.requireNonNull(text, "text must not be null.");
        String stripped = text.strip();

        if (stripped.isEmpty()) {
            return;
        }

        String runText = needsSpaceBefore(stripped) ? " " + stripped : stripped;
        currentRuns.add(new TextRun(runText, currentStyle, currentSize));
    }

    /**
     * Finishes the current paragraph and returns the document built so far.
     * The builder is intended to be used once; it should not be used after
     * this method is called.
     *
     * @return an immutable document containing every paragraph added
     */
    public Document build() {
        finishParagraph();
        return new Document(completedParagraphs);
    }

    /**
     * Returns whether a space should separate the given text from the text already
     * in the paragraph.
     */
    private boolean needsSpaceBefore(String text) {
        if (currentRuns.isEmpty()) {
            return false;
        }

        String previousText = currentRuns.get(currentRuns.size() - 1).text();
        return !endsWithOpeningPunctuation(previousText) && !startsWithClosingPunctuation(text);
    }

    /**
     * Returns whether the text starts with punctuation that attaches to the preceding
     * word, and so should not have a space before it.
     */
    private static boolean startsWithClosingPunctuation(String text) {
        return CLOSING_PUNCTUATION.indexOf(text.charAt(0)) != -1;
    }

    /**
     * Returns whether the text ends with punctuation that attaches to the following
     * word, and so should not have a space after it.
     */
    private static boolean endsWithOpeningPunctuation(String text) {
        return OPENING_PUNCTUATION.indexOf(text.charAt(text.length() - 1)) != -1;
    }

    /**
     * Closes the paragraph being built, if it has any text, using the
     * current paragraph-level settings.
     */
    private void finishParagraph() {
        if (currentRuns.isEmpty()) {
            return;
        }

        Paragraph paragraph = new Paragraph(indentLevel, currentFillMode, currentRuns);
        completedParagraphs.add(paragraph);
        currentRuns.clear();
    }
}
