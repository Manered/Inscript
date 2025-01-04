package dev.manere.inscript.node;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class ScalarNode<V> extends InscriptNode {
    @NotNull
    public abstract V getValue();

    @NotNull
    public <T> Optional<T> getValueAs(final @NotNull Class<T> type) {
        try {
            return (Optional<T>) type.cast(getValue());
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    @NotNull
    @Override
    public String toString() {
        return getKey() + "[value=" + getValue() + "]";
    }
}
