package io.github.konstantinosioan.typesetter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {
    private final ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
    private final PrintStream err = new PrintStream(errBytes, true, StandardCharsets.UTF_8);

    @TempDir
    private Path dir;

    private int run(String... args) {
        return Main.run(args, err);
    }

    private String errorOutput() {
        return errBytes.toString(StandardCharsets.UTF_8);
    }

    private void assertErrorLine(String expected) {
        assertEquals(expected + System.lineSeparator(), errorOutput());
    }

    private Path input(String content) throws IOException {
        return Files.writeString(dir.resolve("in.txt"), content);
    }

    private void assumePosix() throws IOException {
        assumeTrue(Files.getFileStore(dir).supportsFileAttributeView("posix"));
    }

    @Test
    void writesPdfForValidInput() throws IOException {
        Path input = input("Hello");
        Path output = dir.resolve("out.pdf");

        int status = run(input.toString(), output.toString());

        assertAll(
            () -> assertEquals(0, status),
            () -> assertEquals("%PDF",
                    new String(Files.readAllBytes(output), 0, 4, StandardCharsets.US_ASCII)),
            () -> assertEquals("", errorOutput()));
    }

    @Test
    void emptyInputWritesPdf() throws IOException {
        Path output = dir.resolve("out.pdf");

        int status = run(input("").toString(), output.toString());

        assertAll(
            () -> assertEquals(0, status),
            () -> assertTrue(Files.exists(output)));
    }

    @Test
    void wrongArgumentCountPrintsUsage() {
        int status = run();

        assertAll(
            () -> assertEquals(2, status),
            () -> assertErrorLine("Usage: <input.txt> <output.pdf>"));
    }

    @Test
    void sameInputAndOutputIsRejectedWithoutTouchingInput() throws IOException {
        Path input = input("Hello");

        int status = run(input.toString(), dir.resolve(".").resolve("in.txt").toString());

        assertAll(
            () -> assertEquals(2, status),
            () -> assertErrorLine("Input and output must be different files"),
            () -> assertEquals("Hello", Files.readString(input)));
    }

    @Test
    void outputLinkedToInputIsRejectedWithoutTouchingInput() throws IOException {
        Path input = input("Hello");
        Path link = Files.createSymbolicLink(dir.resolve("link.pdf"), input);

        int status = run(input.toString(), link.toString());

        assertAll(
            () -> assertEquals(2, status),
            () -> assertErrorLine("Input and output must be different files"),
            () -> assertEquals("Hello", Files.readString(input)));
    }

    @Test
    void invalidPathIsRejected() {
        int status = run("in\0.txt", dir.resolve("out.pdf").toString());

        assertAll(
            () -> assertEquals(2, status),
            () -> assertTrue(errorOutput().startsWith("Invalid path: ")));
    }

    @Test
    void parseErrorReportsFileAndLineWithoutWritingOutput() throws IOException {
        Path input = input("Hello\n.underline\n");
        Path output = dir.resolve("out.pdf");

        int status = run(input.toString(), output.toString());

        assertAll(
            () -> assertEquals(1, status),
            () -> assertErrorLine(input + ":2: Unknown command: .underline"),
            () -> assertFalse(Files.exists(output)));
    }

    @Test
    void commandOnFirstLineIsRecognised() throws IOException {
        Path input = input(".underline\n");

        int status = run(input.toString(), dir.resolve("out.pdf").toString());

        assertAll(
            () -> assertEquals(1, status),
            () -> assertErrorLine(input + ":1: Unknown command: .underline"));
    }

    @Test
    void byteOrderMarkIsIgnored() throws IOException {
        Path input = input("\uFEFF.underline\n");

        int status = run(input.toString(), dir.resolve("out.pdf").toString());

        assertAll(
            () -> assertEquals(1, status),
            () -> assertErrorLine(input + ":1: Unknown command: .underline"));
    }

    @Test
    void missingInputIsReported() {
        Path input = dir.resolve("missing.txt");

        int status = run(input.toString(), dir.resolve("out.pdf").toString());

        assertAll(
            () -> assertEquals(1, status),
            () -> assertErrorLine("Cannot read " + input + ": no such file"));
    }

    @Test
    void nonUtf8InputIsReported() throws IOException {
        Path input = Files.write(dir.resolve("in.txt"), new byte[] {(byte) 0xFF, (byte) 0xFE});

        int status = run(input.toString(), dir.resolve("out.pdf").toString());

        assertAll(
            () -> assertEquals(1, status),
            () -> assertErrorLine("Cannot read " + input + ": not valid UTF-8 text"));
    }

    @Test
    void unreadableInputIsReported() throws IOException {
        assumePosix();
        Path input = input("Hello");
        Files.setPosixFilePermissions(input, Set.of());
        assumeFalse(Files.isReadable(input));

        int status = run(input.toString(), dir.resolve("out.pdf").toString());

        assertAll(
            () -> assertEquals(1, status),
            () -> assertErrorLine("Cannot read " + input + ": permission denied"));
    }

    @Test
    void inputThatIsDirectoryIsReported() throws IOException {
        assumePosix();

        int status = run(dir.toString(), dir.resolve("out.pdf").toString());

        assertAll(
            () -> assertEquals(1, status),
            () -> assertErrorLine("Cannot read " + dir + ": Is a directory"));
    }

    @Test
    void outputThatIsDirectoryIsReported() throws IOException {
        assumePosix();

        int status = run(input("Hello").toString(), dir.toString());

        assertAll(
            () -> assertEquals(1, status),
            () -> assertErrorLine("Cannot write " + dir + ": Is a directory"));
    }

    @Test
    void missingOutputFolderIsReported() throws IOException {
        Path output = dir.resolve("missing-folder").resolve("out.pdf");

        int status = run(input("Hello").toString(), output.toString());

        assertAll(
            () -> assertEquals(1, status),
            () -> assertErrorLine("Cannot write " + output + ": no such file"));
    }
}
