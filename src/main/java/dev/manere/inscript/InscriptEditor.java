package dev.manere.inscript;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.manere.inscript.node.InscriptNode;
import dev.manere.inscript.node.RootSectionNode;
import dev.manere.inscript.node.ScalarNode;
import dev.manere.inscript.node.SectionNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Consumer;

public interface InscriptEditor {
    @NotNull
    @Unmodifiable
    Set<InscriptNode> getChildren();

    @NotNull
    @Unmodifiable
    default Set<String> getKeys() {
        final Set<String> keys = new HashSet<>();

        for (final InscriptNode node : getChildren()) {
            keys.add(node.getKey());
        }

        return keys;
    }

    default boolean isRoot() {
        return getSection() instanceof RootSectionNode;
    }

    @NotNull
    default Optional<InscriptNode> getNode(final @NotNull String key) {
        for (final InscriptNode node : getChildren()) if (node.getKey().equals(key)) return Optional.of(node);
        return Optional.empty();
    }

    default boolean isSection(final @NotNull String key) {
        return getNode(key).orElse(null) instanceof SectionNode;
    }

    default boolean isScalar(final @NotNull String key) {
        return getNode(key).orElse(null) instanceof ScalarNode<?>;
    }

    @NotNull
    SectionNode getSection();

    @NotNull
    Optional<InscriptEditor> getSection(final @NotNull String key);

    @NotNull
    InscriptEditor createSection(final @NotNull String key);

    @NotNull
    @CanIgnoreReturnValue
    default InscriptEditor section(final @NotNull String key, final @NotNull Consumer<InscriptEditor> handler) {
        handler.accept(getSection(key).orElse(createSection(key)));
        return this;
    }

    @NotNull
    <T> Optional<T> get(final @NotNull String key, final @NotNull Class<? extends T> ignoredType);

    @NotNull
    <T> List<T> getList(final @NotNull String key, final @NotNull Class<? extends T> ignoredType);

    @NotNull
    @CanIgnoreReturnValue
    <T> InscriptEditor set(final @NotNull String key, final @Nullable T value);

    default boolean has(final @NotNull String key) {
        return contains(key);
    }

    default boolean contains(final @NotNull String key) {
        return getNode(key).isPresent();
    }

    @NotNull
    @CanIgnoreReturnValue
    default InscriptEditor unset(final @NotNull String key) {
        getNode(key).ifPresent(node -> getSection().getChildren().remove(node));
        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    default InscriptEditor reset() {
        getSection().getChildren().clear();
        return this;
    }
}
