package dev.manere.inscript.node;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class ConfigNode {
    private final Set<String> comments = new LinkedHashSet<>();
    private final Set<String> inlineComments = new LinkedHashSet<>();

    @NotNull
    public abstract String getKey();

    @NotNull
    @Override
    public String toString() {
        return "<" + getKey() + ">";
    }

    @NotNull
    public Set<String> getComments() {
        return comments;
    }

    @NotNull
    public Set<String> getInlineComments() {
        return inlineComments;
    }
}
