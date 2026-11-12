package io.github.konstantinosioan.typesetter.rendering;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import io.github.konstantinosioan.typesetter.model.Document;
import io.github.konstantinosioan.typesetter.model.FillMode;
import io.github.konstantinosioan.typesetter.model.FontSize;
import io.github.konstantinosioan.typesetter.model.FontStyle;
import io.github.konstantinosioan.typesetter.model.Paragraph;
import io.github.konstantinosioan.typesetter.model.TextRun;

/**
 * Renders documents with iText, using the standard Helvetica fonts.
 * One indent unit is the width of {@code WWWW} in the normal font,
 * and indentation is capped at half the text width so text always fits.
 *
 * <p>PDF viewers must provide the standard fonts, or substitutes with the same letter widths,
 * so nothing is embedded and files stay small. The fonts only cover Western European
 * characters, and iText leaves any other character out of the PDF without reporting an error.
 */
public final class ITextPdfRenderer implements PdfRenderer {
    // 12 pt is a common size for body text; 18 pt is one and a half times that, for headings.
    private static final float NORMAL_FONT_SIZE = 12f;
    private static final float LARGE_FONT_SIZE = 18f;
    // About 16 mm at the normal size.
    private static final String INDENT_UNIT_TEXT = "WWWW";
    // iText silently drops a paragraph whose left margin leaves no room for its text, so the
    // indent needs a cap below the full width. Half still leaves room for several words a line.
    private static final float MAX_INDENT_FRACTION = 0.5f;

    @Override
    public void render(Document document, OutputStream out) throws IOException {
        Objects.requireNonNull(document, "document must not be null.");
        Objects.requireNonNull(out, "out must not be null.");

        try (var pdf = new com.itextpdf.layout.Document(new PdfDocument(new PdfWriter(out)))) {
            Fonts fonts = Fonts.create();
            float indentUnit = fonts.regular().getWidth(INDENT_UNIT_TEXT, NORMAL_FONT_SIZE);
            PageSize pageSize = pdf.getPdfDocument().getDefaultPageSize();
            float maxIndent = pdf.getPageEffectiveArea(pageSize).getWidth() * MAX_INDENT_FRACTION;

            for (Paragraph paragraph : document.paragraphs()) {
                float indent = Math.min(paragraph.indentLevel() * indentUnit, maxIndent);
                pdf.add(toPdfParagraph(paragraph, fonts).setMarginLeft(indent));
            }
        } catch (PdfException e) {
            // iText reports a failed write as an unchecked PdfException that wraps the
            // IOException, so unwrap it to throw the checked exception this method declares.
            if (e.getCause() instanceof IOException io) {
                throw io;
            }
            throw e;
        }
    }

    /**
     * Converts a paragraph to its iText equivalent, without its indent.
     */
    private static com.itextpdf.layout.element.Paragraph toPdfParagraph(
            Paragraph paragraph, Fonts fonts) {
        var pdfParagraph = new com.itextpdf.layout.element.Paragraph()
                .setTextAlignment(alignmentOf(paragraph.fillMode()));

        for (TextRun run : paragraph.runs()) {
            pdfParagraph.add(toText(run, fonts));
        }

        return pdfParagraph;
    }

    /**
     * Converts a run to an iText text element in the run's font and size.
     */
    private static Text toText(TextRun run, Fonts fonts) {
        return new Text(run.text())
                .setFont(fonts.forStyle(run.style()))
                .setFontSize(sizeOf(run.size()));
    }

    /**
     * Returns the iText alignment for a fill mode.
     */
    private static TextAlignment alignmentOf(FillMode mode) {
        return switch (mode) {
            case FILL -> TextAlignment.JUSTIFIED;
            case NOFILL -> TextAlignment.LEFT;
        };
    }

    /**
     * Returns the font size in points for a size value.
     */
    private static float sizeOf(FontSize size) {
        return switch (size) {
            case NORMAL -> NORMAL_FONT_SIZE;
            case LARGE -> LARGE_FONT_SIZE;
        };
    }

    /**
     * The fonts for one rendering. A {@link PdfFont} belongs to a single PDF,
     * so a new set is created for each document. Bold uses Helvetica Bold, whose letters are
     * heavier and wider, instead of iText's simulated bold, which only outlines the regular ones.
     */
    private record Fonts(PdfFont regular, PdfFont italic, PdfFont bold) {
        /**
         * Creates the regular, italic and bold Helvetica fonts.
         */
        static Fonts create() throws IOException {
            return new Fonts(
                    PdfFontFactory.createFont(StandardFonts.HELVETICA),
                    PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE),
                    PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        }

        /**
         * Returns the font for a style.
         */
        PdfFont forStyle(FontStyle style) {
            return switch (style) {
                case REGULAR -> regular;
                case ITALIC -> italic;
                case BOLD -> bold;
            };
        }
    }
}
