package dev.manere.inscript.format;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InscriptReader {
    private final List<String> lines;

    private InscriptReader(final @NotNull List<String> lines) {
        this.lines = List.copyOf(lines);
    }

    @NotNull
    @CanIgnoreReturnValue
    public static InscriptReader reader(final @NotNull List<String> lines) {
        return new InscriptReader(lines);
    }

    @NotNull
    public String read(final int position) {
        return position < 0 || position >= getLines().size() ? "" : getLines().get(position);
    }

    @NotNull
    @Unmodifiable
    public List<String> readLines(int startPosition, final int endPosition) {
        if (startPosition < 0) startPosition = 0;
        if (startPosition >= getLines().size() || startPosition > endPosition) return Collections.emptyList();

        final int validEndPosition = Math.min(endPosition, getLines().size() - 1);
        final List<String> list = new ArrayList<>();

        for (int position = startPosition; position <= validEndPosition; position++) list.add(getLines().get(position));

        return Collections.unmodifiableList(list);
    }

    @NotNull
    @Unmodifiable
    public List<String> readLines(final int startPosition) {
        return readLines(startPosition, getLines().size() - 1);
    }

    @NotNull
    @Unmodifiable
    public List<String> getLines() {
        return lines;
    }
}