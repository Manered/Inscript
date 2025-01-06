package dev.manere.inscript;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.manere.inscript.format.FileFormat;
import dev.manere.inscript.format.FileFormats;
import dev.manere.inscript.node.RootSectionNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Inscript {
    @Nullable
    private final Path path;

    private final ConfigSection root = new SimpleConfigSection(new RootSectionNode());
    private final FileFormat format;

    private Inscript(final @Nullable Path path, final @NotNull FileFormat format) {
        this.path = path;
        this.format = format;
    }

    @NotNull
    @CanIgnoreReturnValue
    public static Inscript newInscript(final @NotNull Path source) {
        final Path file = source.getFileName();

        for (final FileFormat format : FileFormats.FORMATS) {
            final Collection<String> extensions = format.getValidFileExtensions();
            for (final String extension : extensions) {
                if (file.endsWith("." + extension)) {
                    return new Inscript(source, format);
                }
            }
        }

        throw new InscriptException("Couldn't auto-detect file format by extension for file: " + file);
    }

    @NotNull
    @CanIgnoreReturnValue
    public static Inscript newInscript(final @NotNull FileFormat format, final @NotNull Path source) {
        return new Inscript(source, format);
    }

    @NotNull
    @CanIgnoreReturnValue
    public static Inscript newInscript(final @NotNull FileFormat format, final @NotNull File source) {
        return newInscript(format, source.toPath());
    }

    @NotNull
    @CanIgnoreReturnValue
    public static Inscript newInscript(final @NotNull FileFormat format) {
        return new Inscript(null, format);
    }

    @NotNull
    public ConfigSection getRoot() {
        return root;
    }

    @NotNull
    public Optional<Path> getPath() {
        return Optional.ofNullable(path);
    }

    @NotNull
    public FileFormat getFormat() {
        return format;
    }

    public void saveToDisk() {
        if (getPath().isEmpty()) {
            throw new InscriptException("Attempted to save to disk with a null path");
        }

        final File file = getPath().get().toFile();

        if (!file.exists()) try {
            if (!file.createNewFile()) throw new InscriptException("Failed to save " + file + " to disk, couldn't create file.");
        } catch (final IOException e) {
            throw new InscriptException(e);
        }

        try {
            Files.writeString(getPath().get(), format.save(root));
        } catch (final Exception e) {
            throw new InscriptException(e);
        }
    }

    @NotNull
    public String saveToString() {
        return format.save(root);
    }

    public void loadFromDisk() {
        if (getPath().isEmpty()) throw new InscriptException("Attempted to load from disk with a null path");
        if (!getPath().get().toFile().exists()) return;

        try (final BufferedReader reader = Files.newBufferedReader(getPath().get())) {
            root.reset();
            format.load(reader, root);
        } catch (final Exception e) {
            throw new InscriptException(e);
        }
    }

    public void loadFromString(final @NotNull String configString) {
        try (final BufferedReader reader = new BufferedReader(new StringReader(configString))) {
            root.reset();
            format.load(reader, root);
        } catch (Exception e) {
            throw new InscriptException(e);
        }
    }
}
