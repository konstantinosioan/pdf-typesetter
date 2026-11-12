/**
 * A command-line typesetter that turns a text file of formatting commands into a PDF.
 *
 * <p>{@link io.github.konstantinosioan.typesetter.Main} reads the input file and passes its
 * lines to the {@code parsing} package. The parser gets each command from the {@code commands}
 * package and applies it to a {@code building} document builder, which produces an immutable
 * {@code model} document.
 * The {@code rendering} package then writes that document as a PDF.
 */
package io.github.konstantinosioan.typesetter;
