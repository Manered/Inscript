package dev.manere.inscript.node;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class ScalarNode<V> extends ConfigNode {
    @NotNull
    public static <V> ScalarNode<V> scalar(final @NotNull String key, final @NotNull V value) {
        return new ScalarNode<>() {
            @Override
            public @NotNull V getValue() {
                return value;
            }

            @Override
            public @NotNull String getKey() {
                return key;
            }
        };
    }

    @NotNull
    public abstract V getValue();

    @NotNull
    public <T> Optional<T> getValueAs(final @NotNull Class<T> type) {
        try {
            return Optional.of(type.cast(getValue()));
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    @NotNull
    @Override
    public String toString() {
        return "<" + getKey() + " = " + getValue() + ">";
    }
}
