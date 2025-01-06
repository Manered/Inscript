package dev.manere.inscript.node;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public abstract class ConfigNode {
    private final Set<String> comments = new HashSet<>();

    @NotNull
    public abstract String getKey();

    @NotNull
    @Override
    public String toString() {
        return getKey() + "[comments = " + getComments() + "]";
    }

    @NotNull
    public Set<String> getComments() {
        return comments;
    }
}