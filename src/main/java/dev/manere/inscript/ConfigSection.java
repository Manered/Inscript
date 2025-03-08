package dev.manere.inscript;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.manere.inscript.node.ConfigNode;
import dev.manere.inscript.node.RootSectionNode;
import dev.manere.inscript.node.ScalarNode;
import dev.manere.inscript.node.SectionNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Consumer;

public interface ConfigSection {
    @NotNull
    @Unmodifiable
    default Set<ConfigNode> getChildren() {
        return new LinkedHashSet<>(getSection().getChildren());
    }

    @NotNull
    @CanIgnoreReturnValue
    default ConfigSection copy(final @NotNull ConfigSection other) {
        getSection().getChildren().addAll(other.getChildren());
        return this;
    }

    @NotNull
    @Unmodifiable
    default Set<String> getKeys() {
        final Set<String> keys = new LinkedHashSet<>();

        for (final ConfigNode node : getChildren()) {
            keys.add(node.getKey());
        }

        return keys;
    }

    default boolean isRoot() {
        return getSection() instanceof RootSectionNode;
    }

    @NotNull
    default Optional<ConfigNode> getNode(final @NotNull String key) {
        for (final ConfigNode node : getChildren()) if (node.getKey().equals(key)) return Optional.of(node);
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
    Optional<ConfigSection> getSection(final @NotNull String key);

    @NotNull
    ConfigSection createSection(final @NotNull String key);

    @NotNull
    @CanIgnoreReturnValue
    default ConfigSection section(final @NotNull String key, final @NotNull Consumer<ConfigSection> handler) {
        handler.accept(getSection(key).orElse(createSection(key)));
        return this;
    }

    @NotNull
    <T> Optional<T> get(final @NotNull String key, final @NotNull Class<? extends T> ignoredType);

    @NotNull
    <T> List<T> getList(final @NotNull String key, final @NotNull Class<? extends T> ignoredType);

    @NotNull
    @CanIgnoreReturnValue
    <T> ConfigSection set(final @NotNull String key, final @Nullable T value);

    default boolean has(final @NotNull String key) {
        return contains(key);
    }

    default boolean contains(final @NotNull String key) {
        return getNode(key).isPresent();
    }

    @NotNull
    @CanIgnoreReturnValue
    default ConfigSection unset(final @NotNull String key) {
        getNode(key).ifPresent(node -> getSection().getChildren().remove(node));
        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    default ConfigSection reset() {
        getSection().getChildren().clear();
        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    default ConfigSection forEachSection(final @NotNull Consumer<ConfigSection> sectionConsumer) {
        for (final ConfigNode node : getSection().getChildren()) {
            getSection(node.getKey()).ifPresent(sectionConsumer);
        }

        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    default ConfigSection forEachScalar(final @NotNull Consumer<ScalarNode<?>> scalarConsumer) {
        for (final ConfigNode node : getSection().getChildren()) {
            if (node instanceof ScalarNode<?> scalar) scalarConsumer.accept(scalar);
        }

        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    default ConfigSection forEach(final @NotNull Consumer<ScalarNode<?>> scalarConsumer, final @NotNull Consumer<ConfigSection> sectionConsumer) {
        for (final ConfigNode node : getSection().getChildren()) {
            if (node instanceof ScalarNode<?> scalar) {
                scalarConsumer.accept(scalar);
            } else if (node instanceof SectionNode section) {
                sectionConsumer.accept(new SimpleConfigSection(section));
            }
        }

        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    default ConfigSection comment(final @NotNull String key, final @NotNull Collection<? extends String> comments) {
        getNode(key).ifPresent(node -> {
            node.getComments().clear();
            node.getComments().addAll(comments);
        });

        return this;
    }

    @NotNull
    default Collection<String> getComments(final @NotNull String key) {
        final ConfigNode node = getNode(key).orElse(null);
        if (node == null) return Set.of();

        return Set.copyOf(node.getComments());
    }

    @NotNull
    @CanIgnoreReturnValue
    default ConfigSection comment(final @NotNull String key, final @NotNull String @NotNull ... comments) {
        return comment(key, Arrays.asList(comments));
    }

    @NotNull
    default String getKey() {
        return getSection().getKey();
    }
}
