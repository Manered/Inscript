package dev.manere.inscript.format;

import dev.manere.inscript.ConfigSection;
import dev.manere.inscript.ErrorContext;
import dev.manere.inscript.Inscript;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FileFormat {
    @NotNull
    @Unmodifiable
    List<ErrorContext> load(final @NotNull InscriptReader reader, final @NotNull Inscript inscript);

    @NotNull
    Optional<ErrorContext> parseNode(final @NotNull Line line, final @NotNull InscriptReader reader, final @NotNull Inscript inscript, final @NotNull ParseNodeContext context, final @NotNull Set<Integer> processedLines);

    @NotNull
    String save(final @NotNull ConfigSection root);

    @NotNull
    @Unmodifiable
    Collection<String> getValidFileExtensions();

    @NotNull
    default InscriptStringWriter newWriter() {
        return InscriptStringWriter.newWriter();
    }

    @NotNull
    default InscriptReader newReader(final @NotNull List<String> lines) {
        return InscriptReader.reader(lines);
    }
}
