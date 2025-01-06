package dev.manere.inscript.format;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface FileFormats {
    @NotNull
    FileFormat DATASCRIPT = new DataScriptFormat();

    @NotNull
    FileFormat YAML = new YAMLFormat();

    @NotNull
    Collection<FileFormat> FORMATS = new HashSet<>(Set.of(
        DATASCRIPT,
        YAML
    ));
}
