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
    default Set<InscriptNode> getChildren() {
        return Set.copyOf(getSection().getChildren());
    }

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

    @NotNull
    @CanIgnoreReturnValue
    default InscriptEditor forEachSection(final @NotNull Consumer<InscriptEditor> sectionConsumer) {
        for (final InscriptNode node : getSection().getChildren()) {
            getSection(node.getKey()).ifPresent(sectionConsumer);
        }

        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    default InscriptEditor forEachScalar(final @NotNull Consumer<ScalarNode<?>> scalarConsumer) {
        for (final InscriptNode node : getSection().getChildren()) {
            if (node instanceof ScalarNode<?> scalar) scalarConsumer.accept(scalar);
        }

        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    default InscriptEditor forEach(final @NotNull Consumer<ScalarNode<?>> scalarConsumer, final @NotNull Consumer<InscriptEditor> sectionConsumer) {
        for (final InscriptNode node : getSection().getChildren()) {
            if (node instanceof ScalarNode<?> scalar) {
                scalarConsumer.accept(scalar);
            } else if (node instanceof SectionNode section) {
                sectionConsumer.accept(new SimpleInscriptEditor(section));
            }
        }

        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    default InscriptEditor setComments(final @NotNull String key, final @NotNull Collection<? extends String> comments) {
        getNode(key).ifPresent(node -> {
            node.getComments().clear();
            node.getComments().addAll(comments);
        });

        return this;
    }

    @NotNull
    default Collection<String> getComments(final @NotNull String key) {
        final InscriptNode node = getNode(key).orElse(null);
        if (node == null) return Set.of();

        return Set.copyOf(node.getComments());
    }

    @NotNull
    @CanIgnoreReturnValue
    default InscriptEditor setComments(final @NotNull String key, final @NotNull String @NotNull ... comments) {
        return setComments(key, Arrays.asList(comments));
    }
}
