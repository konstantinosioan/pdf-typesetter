package io.github.konstantinosioan.typesetter.rendering;

import static io.github.konstantinosioan.typesetter.model.FillMode.FILL;
import static io.github.konstantinosioan.typesetter.model.FillMode.NOFILL;
import static io.github.konstantinosioan.typesetter.model.FontSize.LARGE;
import static io.github.konstantinosioan.typesetter.model.FontSize.NORMAL;
import static io.github.konstantinosioan.typesetter.model.FontStyle.BOLD;
import static io.github.konstantinosioan.typesetter.model.FontStyle.ITALIC;
import static io.github.konstantinosioan.typesetter.model.FontStyle.REGULAR;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import org.junit.jupiter.api.Test;

import io.github.konstantinosioan.typesetter.model.Document;
import io.github.konstantinosioan.typesetter.model.FillMode;
import io.github.konstantinosioan.typesetter.model.FontSize;
import io.github.konstantinosioan.typesetter.model.FontStyle;
import io.github.konstantinosioan.typesetter.model.Paragraph;
import io.github.konstantinosioan.typesetter.model.TextRun;

class ITextPdfRendererTest {
    /** The width of "WWWW" in 12pt Helvetica. */
    private static final float INDENT_UNIT = 45.312f;
    /** iText's default page margin. */
    private static final float LEFT_MARGIN = 36f;
    /** An A4 page's width (595pt) minus both margins. */
    private static final float TEXT_WIDTH = 523f;
    private static final float TOLERANCE = 0.01f;
    private static final String SENTENCE = "These words are repeated so that the paragraph wraps"
            + " over several lines, which shows how each line is aligned between the margins. ";
    private static final String LONG_TEXT = SENTENCE + SENTENCE + "It ends with a short last line.";

    private final PdfRenderer renderer = new ITextPdfRenderer();

    /** One line of text on a page: where it starts and ends, and its largest font size. */
    private record Line(float start, float end, float fontSize) {
    }

    private static Paragraph paragraph(
            int indentLevel, FillMode fillMode, String text, FontStyle style, FontSize size) {
        return new Paragraph(indentLevel, fillMode, List.of(new TextRun(text, style, size)));
    }

    private static Paragraph paragraph(String text) {
        return paragraph(0, NOFILL, text, REGULAR, NORMAL);
    }

    private byte[] render(Paragraph... paragraphs) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderer.render(new Document(List.of(paragraphs)), out);
        return out.toByteArray();
    }

    private static PdfDocument read(byte[] pdf) throws IOException {
        return new PdfDocument(new PdfReader(new ByteArrayInputStream(pdf)));
    }

    private static String text(byte[] pdf) throws IOException {
        try (PdfDocument document = read(pdf)) {
            StringBuilder text = new StringBuilder();
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                text.append(PdfTextExtractor.getTextFromPage(document.getPage(page)));
            }
            return text.toString();
        }
    }

    private static Set<String> fontNames(byte[] pdf) throws IOException {
        try (PdfDocument document = read(pdf)) {
            Set<String> names = new TreeSet<>();
            PdfDictionary fonts = document.getPage(1).getResources().getResource(PdfName.Font);
            for (PdfName key : fonts.keySet()) {
                names.add(fonts.getAsDictionary(key).getAsName(PdfName.BaseFont).getValue());
            }
            return names;
        }
    }

    /**
     * Returns the lines of text on the first page, top to bottom. Whitespace-only pieces are
     * skipped, so a line ends where its last visible character ends.
     */
    private static List<Line> lines(byte[] pdf) throws IOException {
        try (PdfDocument document = read(pdf)) {
            List<Line> lines = new ArrayList<>();
            float[] currentY = {Float.NaN};
            new PdfCanvasProcessor(new IEventListener() {
                @Override
                public void eventOccurred(IEventData data, EventType type) {
                    var info = (TextRenderInfo) data;
                    if (info.getText().isBlank()) {
                        return;
                    }
                    float y = info.getBaseline().getStartPoint().get(1);
                    float start = info.getBaseline().getStartPoint().get(0);
                    float end = info.getBaseline().getEndPoint().get(0);
                    if (y != currentY[0]) {
                        lines.add(new Line(start, end, info.getFontSize()));
                        currentY[0] = y;
                    } else {
                        Line last = lines.remove(lines.size() - 1);
                        lines.add(new Line(last.start(), Math.max(last.end(), end),
                                Math.max(last.fontSize(), info.getFontSize())));
                    }
                }

                @Override
                public Set<EventType> getSupportedEvents() {
                    return Collections.singleton(EventType.RENDER_TEXT);
                }
            }).processPageContent(document.getPage(1));
            return lines;
        }
    }

    @Test
    void longDocumentSpansAtLeastThreePages() throws IOException {
        Paragraph[] paragraphs = new Paragraph[60];
        for (int i = 0; i < paragraphs.length; i++) {
            paragraphs[i] = paragraph(LONG_TEXT);
        }

        try (PdfDocument document = read(render(paragraphs))) {
            assertTrue(document.getNumberOfPages() >= 3);
        }
    }

    @Test
    void emptyDocumentRendersBlankPage() throws IOException {
        try (PdfDocument document = read(render())) {
            assertEquals(1, document.getNumberOfPages());
        }
    }

    @Test
    void allTextIsRendered() throws IOException {
        String text = text(render(paragraph("Hello"), paragraph("world")));

        assertAll(
            () -> assertTrue(text.contains("Hello")),
            () -> assertTrue(text.contains("world")));
    }

    @Test
    void eachStyleUsesItsOwnFont() throws IOException {
        byte[] pdf = render(
            paragraph(0, NOFILL, "Hello", REGULAR, NORMAL),
            paragraph(0, NOFILL, "Hello", ITALIC, NORMAL),
            paragraph(0, NOFILL, "Hello", BOLD, NORMAL));

        assertEquals(
            Set.of("Helvetica", "Helvetica-Bold", "Helvetica-Oblique"),
            fontNames(pdf));
    }

    @Test
    void eachSizeUsesItsOwnFontSize() throws IOException {
        List<Line> lines = lines(render(
            paragraph(0, NOFILL, "Hello", REGULAR, NORMAL),
            paragraph(0, NOFILL, "Hello", REGULAR, LARGE)));

        assertAll(
            () -> assertEquals(12f, lines.get(0).fontSize(), TOLERANCE),
            () -> assertEquals(18f, lines.get(1).fontSize(), TOLERANCE));
    }

    @Test
    void fillModeJustifiesEveryLineButTheLast() throws IOException {
        List<Line> lines = lines(render(paragraph(0, FILL, LONG_TEXT, REGULAR, NORMAL)));
        float rightMargin = LEFT_MARGIN + TEXT_WIDTH;

        assertAll(
            () -> assertEquals(4, lines.size()),
            () -> assertEquals(rightMargin, lines.get(0).end(), TOLERANCE),
            () -> assertEquals(rightMargin, lines.get(1).end(), TOLERANCE),
            () -> assertEquals(rightMargin, lines.get(2).end(), TOLERANCE),
            () -> assertTrue(lines.get(3).end() < rightMargin - 100));
    }

    @Test
    void noFillModeLeavesRaggedRightEdge() throws IOException {
        List<Line> lines = lines(render(paragraph(0, NOFILL, LONG_TEXT, REGULAR, NORMAL)));
        float rightMargin = LEFT_MARGIN + TEXT_WIDTH;

        assertAll(
            () -> assertEquals(4, lines.size()),
            () -> assertTrue(lines.get(0).end() < rightMargin - 1),
            () -> assertTrue(lines.get(1).end() < rightMargin - 1),
            () -> assertTrue(lines.get(2).end() < rightMargin - 1));
    }

    @Test
    void indentShiftsTextByIndentUnits() throws IOException {
        List<Line> lines = lines(render(
            paragraph(0, NOFILL, "Hello", REGULAR, NORMAL),
            paragraph(2, NOFILL, "Hello", REGULAR, NORMAL)));

        assertAll(
            () -> assertEquals(LEFT_MARGIN, lines.get(0).start(), TOLERANCE),
            () -> assertEquals(LEFT_MARGIN + 2 * INDENT_UNIT, lines.get(1).start(), TOLERANCE));
    }

    @Test
    void deepIndentIsCappedSoTextStillAppears() throws IOException {
        byte[] pdf = render(paragraph(1_000, NOFILL, "Hello", REGULAR, NORMAL));

        assertAll(
            () -> assertTrue(text(pdf).contains("Hello")),
            () -> assertEquals(
                LEFT_MARGIN + TEXT_WIDTH / 2, lines(pdf).get(0).start(), TOLERANCE));
    }

    @Test
    void renderingTwiceProducesTwoValidDocuments() throws IOException {
        assertAll(
            () -> assertTrue(text(render(paragraph("Hello"))).contains("Hello")),
            () -> assertTrue(text(render(paragraph("world"))).contains("world")));
    }

    @Test
    void outputStreamIsClosed() throws IOException {
        boolean[] closed = {false};
        OutputStream out = new ByteArrayOutputStream() {
            @Override
            public void close() {
                closed[0] = true;
            }
        };

        renderer.render(new Document(List.of(paragraph("Hello"))), out);

        assertTrue(closed[0]);
    }

    @Test
    void writeFailureIsReportedAsIOException() {
        IOException failure = new IOException("disk full");
        OutputStream failing = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw failure;
            }
        };

        IOException e = assertThrows(IOException.class,
            () -> renderer.render(new Document(List.of(paragraph("Hello"))), failing));

        assertSame(failure, e);
    }

    @Test
    void nullArgumentsAreRejected() {
        Document document = new Document(List.of());

        assertAll(
            () -> assertThrows(NullPointerException.class,
                () -> renderer.render(null, new ByteArrayOutputStream())),
            () -> assertThrows(NullPointerException.class, () -> renderer.render(document, null)));
    }
}
