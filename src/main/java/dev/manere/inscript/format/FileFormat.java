package dev.manere.inscript.format;

import dev.manere.inscript.ConfigSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.BufferedReader;
import java.util.Collection;

public interface FileFormat {
    void load(final @NotNull BufferedReader reader, final @NotNull ConfigSection root) throws Exception;

    @NotNull
    String save(final @NotNull ConfigSection root);

    @NotNull
    @Unmodifiable
    Collection<String> getValidFileExtensions();
}
