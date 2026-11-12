package io.github.konstantinosioan.typesetter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.CharacterCodingException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.konstantinosioan.typesetter.commands.CommandRegistry;
import io.github.konstantinosioan.typesetter.model.Document;
import io.github.konstantinosioan.typesetter.parsing.ParseException;
import io.github.konstantinosioan.typesetter.parsing.Parser;
import io.github.konstantinosioan.typesetter.rendering.ITextPdfRenderer;

/**
 * Command-line entry point: reads a text file of formatting commands and writes it out as a PDF.
 */
public final class Main {
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_USAGE = 2;
    private static final String USAGE = "Usage: <input.txt> <output.pdf>";
    private static final String BYTE_ORDER_MARK = "\uFEFF";

    private Main() {}

    /**
     * Converts the input file to a PDF, exiting with a non-zero status on failure.
     *
     * @param args the input file path and the output PDF path
     */
    public static void main(String[] args) {
        int status = run(args, System.err);

        if (status != EXIT_SUCCESS) {
            System.exit(status);
        }
    }

    /**
     * Converts the input file to a PDF. The input is fully parsed before the output file is
     * opened, so invalid input never creates or overwrites the PDF.
     *
     * @param args the input file path and the output PDF path
     * @param err where error and usage messages are printed
     * @return 0 on success, 1 if the input cannot be read, parsed or rendered, or 2 if the
     *         arguments are wrong: not exactly two, an invalid path, or the same file twice
     */
    static int run(String[] args, PrintStream err) {
        if (args.length != 2) {
            err.println(USAGE);
            return EXIT_USAGE;
        }

        Path input;
        Path output;
        try {
            input = Path.of(args[0]);
            output = Path.of(args[1]);
        } catch (InvalidPathException e) {
            err.println("Invalid path: " + e.getReason());
            return EXIT_USAGE;
        }

        List<String> lines;
        try {
            lines = stripByteOrderMark(Files.readAllLines(input));
        } catch (IOException e) {
            err.println("Cannot read " + input + ": " + reason(e));
            return EXIT_FAILURE;
        }

        if (isSameFile(input, output)) {
            err.println("Input and output must be different files");
            return EXIT_USAGE;
        }

        Document document;
        try {
            document = new Parser(new CommandRegistry()).parse(lines);
        } catch (ParseException e) {
            err.println(input + ":" + e.getLineNumber() + ": " + e.getMessage());
            return EXIT_FAILURE;
        }

        try (OutputStream out = Files.newOutputStream(output)) {
            new ITextPdfRenderer().render(document, out);
        } catch (IOException e) {
            err.println("Cannot write " + output + ": " + reason(e));
            return EXIT_FAILURE;
        }

        return EXIT_SUCCESS;
    }

    /**
     * Removes the invisible byte order mark some editors put at the start of a UTF-8 file,
     * which would otherwise stop a command on the first line being recognised.
     */
    private static List<String> stripByteOrderMark(List<String> lines) {
        if (lines.isEmpty() || !lines.get(0).startsWith(BYTE_ORDER_MARK)) {
            return lines;
        }

        List<String> stripped = new ArrayList<>(lines);
        stripped.set(0, lines.get(0).substring(BYTE_ORDER_MARK.length()));
        return stripped;
    }

    /**
     * Returns whether both paths lead to the same file, including through a symbolic link or a
     * case-insensitive file system. An output that does not exist yet is never the same file.
     */
    private static boolean isSameFile(Path input, Path output) {
        try {
            return Files.isSameFile(input, output);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns a short, readable description of a file error, since the messages of some
     * exceptions are only the file's path.
     */
    private static String reason(IOException e) {
        if (e instanceof NoSuchFileException) {
            return "no such file";
        }
        if (e instanceof AccessDeniedException) {
            return "permission denied";
        }
        if (e instanceof CharacterCodingException) {
            return "not valid UTF-8 text";
        }
        if (e instanceof FileSystemException fileError && fileError.getReason() != null) {
            return fileError.getReason();
        }
        return Objects.requireNonNullElse(e.getMessage(), e.getClass().getSimpleName());
    }
}
