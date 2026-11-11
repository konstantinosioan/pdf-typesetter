package io.github.konstantinosioan.typesetter.parsing;

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

import io.github.konstantinosioan.typesetter.commands.CommandRegistry;
import io.github.konstantinosioan.typesetter.model.Paragraph;
import io.github.konstantinosioan.typesetter.model.TextRun;

class ParserTest {
    private final Parser parser = new Parser(new CommandRegistry());

    private static TextRun regular(String text) {
        return new TextRun(text, REGULAR, NORMAL);
    }

    private static TextRun bold(String text) {
        return new TextRun(text, BOLD, NORMAL);
    }

    private static Paragraph defaultParagraph(TextRun... runs) {
        return new Paragraph(0, NOFILL, List.of(runs));
    }

    @Test
    void emptyInputGivesEmptyDocument() throws ParseException {
        assertEquals(List.of(), parser.parse(List.of()).paragraphs());
    }

    @Test
    void textLinesFormOneParagraph() throws ParseException {
        assertEquals(
            List.of(defaultParagraph(regular("Hello"), regular(" world"))),
            parser.parse(List.of("Hello", "world")).paragraphs());
    }

    @Test
    void commandsAreApplied() throws ParseException {
        List<String> lines =
            List.of(".bold", "Hello", ".paragraph", ".indent +2", ".fill", ".large", "world");

        assertEquals(
            List.of(
                new Paragraph(0, NOFILL, List.of(bold("Hello"))),
                new Paragraph(2, FILL, List.of(new TextRun("world", BOLD, LARGE)))),
            parser.parse(lines).paragraphs());
    }

    @Test
    void commandWithSurroundingWhitespaceIsAccepted() throws ParseException {
        List<String> lines = List.of(".bold   ", ".indent   +2", "Hello");

        assertEquals(
            List.of(new Paragraph(2, NOFILL, List.of(bold("Hello")))),
            parser.parse(lines).paragraphs());
    }

    @Test
    void indentedDotLineIsText() throws ParseException {
        assertEquals(
            List.of(defaultParagraph(regular(".bold"))),
            parser.parse(List.of("  .bold")).paragraphs());
    }

    @Test
    void escapedLineIsText() throws ParseException {
        assertEquals(
            List.of(defaultParagraph(regular(".bold"))),
            parser.parse(List.of("\\&.bold")).paragraphs());
    }

    @Test
    void escapedFullStopAttachesToPreviousText() throws ParseException {
        List<String> lines = List.of(".bold", "Hello", ".regular", "\\&.");

        assertEquals(
            List.of(defaultParagraph(bold("Hello"), regular("."))),
            parser.parse(lines).paragraphs());
    }

    @Test
    void eachParseStartsFromDefaults() throws ParseException {
        parser.parse(List.of(".bold", ".indent +2", "Hello"));

        assertEquals(
            List.of(defaultParagraph(regular("world"))),
            parser.parse(List.of("world")).paragraphs());
    }

    @Test
    void unknownCommandReportsLineNumber() {
        ParseException e = assertThrows(ParseException.class,
            () -> parser.parse(List.of("Hello", ".underline")));

        assertAll(
            () -> assertEquals(2, e.getLineNumber()),
            () -> assertEquals("Unknown command: .underline", e.getMessage()));
    }

    @Test
    void malformedCommandReportsLineNumber() {
        ParseException e = assertThrows(ParseException.class,
            () -> parser.parse(List.of(".indent 2")));

        assertEquals(1, e.getLineNumber());
    }

    @Test
    void nullArgumentsAreRejected() {
        assertAll(
            () -> assertThrows(NullPointerException.class, () -> new Parser(null)),
            () -> assertThrows(NullPointerException.class, () -> parser.parse(null)));
    }
}
