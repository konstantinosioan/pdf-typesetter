package io.github.konstantinosioan.typesetter.commands;

import static io.github.konstantinosioan.typesetter.model.FillMode.FILL;
import static io.github.konstantinosioan.typesetter.model.FillMode.NOFILL;
import static io.github.konstantinosioan.typesetter.model.FontSize.LARGE;
import static io.github.konstantinosioan.typesetter.model.FontSize.NORMAL;
import static io.github.konstantinosioan.typesetter.model.FontStyle.BOLD;
import static io.github.konstantinosioan.typesetter.model.FontStyle.ITALIC;
import static io.github.konstantinosioan.typesetter.model.FontStyle.REGULAR;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class CommandRegistryTest {
    private final CommandRegistry registry = new CommandRegistry();

    private static Stream<Arguments> noArgumentCommands() {
        return Stream.of(
            Arguments.of("paragraph", new ParagraphCommand()),
            Arguments.of("fill", new FillModeCommand(FILL)),
            Arguments.of("nofill", new FillModeCommand(NOFILL)),
            Arguments.of("regular", new FontStyleCommand(REGULAR)),
            Arguments.of("italic", new FontStyleCommand(ITALIC)),
            Arguments.of("bold", new FontStyleCommand(BOLD)),
            Arguments.of("large", new FontSizeCommand(LARGE)),
            Arguments.of("normal", new FontSizeCommand(NORMAL)));
    }

    @ParameterizedTest
    @MethodSource("noArgumentCommands")
    void createsNoArgumentCommands(String name, Command expected) {
        assertEquals(expected, registry.create(name, ""));
    }

    @Test
    void createsIndentCommandFromSignedArgument() {
        assertAll(
            () -> assertEquals(new IndentCommand(2), registry.create("indent", "+2")),
            () -> assertEquals(new IndentCommand(-1), registry.create("indent", "-1")),
            () -> assertEquals(new IndentCommand(0), registry.create("indent", "+0")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "2", "+", "-", "+x", "+ 2", "+-2", "+2.5"})
    void rejectsInvalidIndentArgument(String argument) {
        assertThrows(IllegalArgumentException.class, () -> registry.create("indent", argument));
    }

    @Test
    void rejectsUnknownCommand() {
        assertThrows(IllegalArgumentException.class, () -> registry.create("underline", ""));
    }

    @Test
    void rejectsArgumentForNoArgumentCommand() {
        assertThrows(IllegalArgumentException.class, () -> registry.create("bold", "+2"));
    }

    @Test
    void nullArgumentsAreRejected() {
        assertAll(
            () -> assertThrows(NullPointerException.class, () -> registry.create(null, "")),
            () -> assertThrows(NullPointerException.class, () -> registry.create("bold", null)));
    }
}
