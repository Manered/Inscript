package dev.manere.inscript.node;

import org.jetbrains.annotations.NotNull;

public abstract class InscriptNode {
    @NotNull
    public abstract String getKey();

    @NotNull
    @Override
    public String toString() {
        return getKey();
    }
}