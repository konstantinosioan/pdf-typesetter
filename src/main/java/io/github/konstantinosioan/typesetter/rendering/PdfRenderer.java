package io.github.konstantinosioan.typesetter.rendering;

import java.io.IOException;
import java.io.OutputStream;

import io.github.konstantinosioan.typesetter.model.Document;

/**
 * Writes a {@link Document} out as a PDF.
 */
public interface PdfRenderer {
    /**
     * Renders the document as a PDF and writes it to the given stream.
     *
     * @param document the document to render
     * @param out the stream to write the PDF to; closed when rendering finishes
     * @throws NullPointerException if either argument is null
     * @throws IOException if the PDF cannot be written
     */
    void render(Document document, OutputStream out) throws IOException;
}
