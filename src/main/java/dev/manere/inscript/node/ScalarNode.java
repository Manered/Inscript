package dev.manere.inscript.node;

import org.jetbrains.annotations.NotNull;

public abstract class ScalarNode<V> extends InscriptNode {
    @NotNull
    public abstract V getValue();

    @NotNull
    @Override
    public String toString() {
        return getKey() + "[value=" + getValue() + "]";
    }
}
