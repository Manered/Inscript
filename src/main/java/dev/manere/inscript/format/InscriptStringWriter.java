package dev.manere.inscript.format;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;

public class InscriptStringWriter {
    private String content = "";

    private InscriptStringWriter() {}

    @NotNull
    public static InscriptStringWriter newWriter() {
        return new InscriptStringWriter();
    }

    @NotNull
    @CanIgnoreReturnValue
    public InscriptStringWriter write(final @NotNull String text) {
        this.content += text;
        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    public InscriptStringWriter newline() {
        return write("\n");
    }

    @NotNull
    @CanIgnoreReturnValue
    public InscriptStringWriter newLine() {
        return newline();
    }

    @NotNull
    public String build() {
        return content;
    }

    @NotNull
    @Override
    public String toString() {
        return build();
    }
}
