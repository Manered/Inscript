package dev.manere.inscript;

import dev.manere.inscript.format.Line;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class ErrorContext {
    private final Inscript inscript;
    private final int position;
    private final String line;
    private final String error;

    public ErrorContext(final @NotNull Inscript inscript, final int position, final @NotNull String line, final @NotNull String error) {
        this.inscript = inscript;
        this.position = position;
        this.line = line;
        this.error = error;
    }

    @NotNull
    public static ErrorContext create(final @NotNull Line line, final @NotNull Inscript inscript, final @NotNull String error) {
        return new ErrorContext(inscript, line.getPosition() + 1, line.getText(), error);
    }

    public int getPosition() {
        return position;
    }

    @NotNull
    public String getLine() {
        return line;
    }

    @NotNull
    public String getError() {
        return error;
    }

    @NotNull
    public String buildDefault() {
        if (inscript.getPath().isPresent()) {
            final Path path = inscript.getPath().get();
            final String name = path.getFileName().toString();

            return String.join("\n", List.of(
                "Failed to parse " + name + ":" + position,
                " " + error + ": " + line
            ));
        } else {
            return String.join("\n", List.of(
                "Failed to parse line " + position,
                " " + error + ": " + line
            ));
        }
    }

    public void handle() {
        InscriptConstants.ERROR_HANDLER.getValue().accept(this);
    }
}
