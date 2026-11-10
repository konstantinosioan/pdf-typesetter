package io.github.konstantinosioan.typesetter.building;

import static io.github.konstantinosioan.typesetter.model.FillMode.FILL;
import static io.github.konstantinosioan.typesetter.model.FillMode.NOFILL;
import static io.github.konstantinosioan.typesetter.model.FontSize.LARGE;
import static io.github.konstantinosioan.typesetter.model.FontSize.NORMAL;
import static io.github.konstantinosioan.typesetter.model.FontStyle.BOLD;
import static io.github.konstantinosioan.typesetter.model.FontStyle.REGULAR;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.konstantinosioan.typesetter.model.Document;
import io.github.konstantinosioan.typesetter.model.Paragraph;
import io.github.konstantinosioan.typesetter.model.TextRun;

class DocumentBuilderTest {
    private final DocumentBuilder builder = new DocumentBuilder();

    private static TextRun regular(String text) {
        return new TextRun(text, REGULAR, NORMAL);
    }

    private static Paragraph defaultParagraph(TextRun... runs) {
        return new Paragraph(0, NOFILL, List.of(runs));
    }

    @Test
    void emptyBuilderBuildsEmptyDocument() {
        assertEquals(List.of(), builder.build().paragraphs());
    }

    @Test
    void textWithoutParagraphCommandFormsImplicitParagraph() {
        builder.addText("Hello");
        assertEquals(List.of(defaultParagraph(regular("Hello"))), builder.build().paragraphs());
    }

    @Test
    void consecutiveTextIsJoinedWithSingleSpace() {
        builder.addText("Hello");
        builder.addText("world");

        assertEquals(
            List.of(defaultParagraph(regular("Hello"), regular(" world"))),
            builder.build().paragraphs());
    }

    @ParameterizedTest
    @ValueSource(strings = {",", ".", ";", ":", "!", "?", ")", "]", "}"})
    void noSpaceBeforeClosingPunctuation(String punctuation) {
        builder.addText("Hello");
        builder.addText(punctuation + " again");

        assertEquals(
            List.of(defaultParagraph(regular("Hello"), regular(punctuation + " again"))),
            builder.build().paragraphs());
    }

    @ParameterizedTest
    @ValueSource(strings = {"(", "[", "{"})
    void noSpaceAfterOpeningPunctuation(String punctuation) {
        builder.addText("Hello");
        builder.addText(punctuation);
        builder.addText("world");

        assertEquals(
            List.of(defaultParagraph(
                regular("Hello"), regular(" " + punctuation), regular("world"))),
            builder.build().paragraphs());
    }

    @Test
    void surroundingWhitespaceIsStripped() {
        builder.addText("   Hello   ");
        assertEquals(List.of(defaultParagraph(regular("Hello"))), builder.build().paragraphs());
    }

    @Test
    void blankTextIsIgnored() {
        builder.addText("Hello");
        builder.addText("   ");
        builder.addText("");
        builder.addText("world");

        assertEquals(
            List.of(defaultParagraph(regular("Hello"), regular(" world"))),
            builder.build().paragraphs());
    }

    @Test
    void runsUseStyleAndSizeCurrentWhenAdded() {
        builder.addText("Hello");
        builder.setFontStyle(BOLD);
        builder.setFontSize(LARGE);
        builder.addText("world");
        builder.setFontStyle(REGULAR);
        builder.setFontSize(NORMAL);
        builder.addText("again");

        assertEquals(
            List.of(defaultParagraph(
                regular("Hello"), new TextRun(" world", BOLD, LARGE), regular(" again"))),
            builder.build().paragraphs());
    }

    @Test
    void startParagraphSeparatesParagraphs() {
        builder.addText("Hello");
        builder.startParagraph();
        builder.addText("world");

        assertEquals(
            List.of(defaultParagraph(regular("Hello")), defaultParagraph(regular("world"))),
            builder.build().paragraphs());
    }

    @Test
    void repeatedStartParagraphCreatesNoEmptyParagraphs() {
        builder.startParagraph();
        builder.startParagraph();
        builder.addText("Hello");
        builder.startParagraph();
        builder.startParagraph();
        builder.addText("world");
        builder.startParagraph();

        assertEquals(
            List.of(defaultParagraph(regular("Hello")), defaultParagraph(regular("world"))),
            builder.build().paragraphs());
    }

    @Test
    void changingFillModeEndsParagraph() {
        builder.addText("Hello");
        builder.setFillMode(FILL);
        builder.addText("world");

        assertEquals(
            List.of(
                new Paragraph(0, NOFILL, List.of(regular("Hello"))),
                new Paragraph(0, FILL, List.of(regular("world")))),
            builder.build().paragraphs());
    }

    @Test
    void settingCurrentFillModeKeepsParagraph() {
        builder.addText("Hello");
        builder.setFillMode(NOFILL);
        builder.addText("world");

        assertEquals(
            List.of(defaultParagraph(regular("Hello"), regular(" world"))),
            builder.build().paragraphs());
    }

    @Test
    void fillModeSetBeforeTextAppliesToThatText() {
        builder.startParagraph();
        builder.setFillMode(FILL);
        builder.addText("Hello");

        assertEquals(
            List.of(new Paragraph(0, FILL, List.of(regular("Hello")))),
            builder.build().paragraphs());
    }

    @Test
    void changingIndentEndsParagraph() {
        builder.addText("Hello");
        builder.changeIndent(2);
        builder.addText("world");

        assertEquals(
            List.of(
                new Paragraph(0, NOFILL, List.of(regular("Hello"))),
                new Paragraph(2, NOFILL, List.of(regular("world")))),
            builder.build().paragraphs());
    }

    @Test
    void indentChangesAccumulate() {
        builder.changeIndent(2);
        builder.changeIndent(1);
        builder.addText("Hello");

        assertEquals(
            List.of(new Paragraph(3, NOFILL, List.of(regular("Hello")))),
            builder.build().paragraphs());
    }

    @Test
    void indentNeverGoesBelowZero() {
        builder.changeIndent(1);
        builder.changeIndent(-5);
        builder.addText("Hello");

        assertEquals(
            List.of(new Paragraph(0, NOFILL, List.of(regular("Hello")))),
            builder.build().paragraphs());
    }

    @Test
    void indentChangeWithNoEffectKeepsParagraph() {
        builder.addText("Hello");
        builder.changeIndent(0);
        builder.changeIndent(-1);
        builder.addText("world");

        assertEquals(
            List.of(defaultParagraph(regular("Hello"), regular(" world"))),
            builder.build().paragraphs());
    }

    @Test
    void nullArgumentsAreRejected() {
        assertAll(
            () -> assertThrows(NullPointerException.class, () -> builder.setFontStyle(null)),
            () -> assertThrows(NullPointerException.class, () -> builder.setFontSize(null)),
            () -> assertThrows(NullPointerException.class, () -> builder.setFillMode(null)),
            () -> assertThrows(NullPointerException.class, () -> builder.addText(null)));
    }

    @Test
    void builtDocumentCannotBeModified() {
        builder.addText("Hello");
        Document document = builder.build();

        assertAll(
            () -> assertThrows(UnsupportedOperationException.class,
                () -> document.paragraphs().add(defaultParagraph(regular("world")))),
            () -> assertThrows(UnsupportedOperationException.class,
                () -> document.paragraphs().get(0).runs().add(regular("world"))));
    }
}
