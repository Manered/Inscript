package dev.manere.inscript;

import dev.manere.inscript.node.ConfigNode;
import dev.manere.inscript.node.ScalarNode;
import dev.manere.inscript.node.SectionNode;
import dev.manere.inscript.value.InscriptValue;
import dev.manere.inscript.value.ValueRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record SimpleConfigSection(@NotNull SectionNode sectionNode) implements ConfigSection {
    @Override
    public @NotNull SectionNode getSection() {
        return sectionNode;
    }

    @Override
    public @NotNull Optional<ConfigSection> getSection(final @NotNull String key) {
        if (key.contains(".")) {
            String[] parts = key.split("\\.");
            ConfigSection current = this;
            for (String part : parts) {
                Optional<ConfigSection> next = current.getSection(part);
                if (next.isEmpty()) return Optional.empty();
                current = next.get();
            }
            return Optional.of(current);
        }

        final ConfigNode node = getNode(key).orElse(null);
        return !(node instanceof SectionNode childSection) ? Optional.empty() : Optional.of(new SimpleConfigSection(childSection));
    }

    @Override
    public @NotNull ConfigSection createSection(final @NotNull String key) {
        if (key.contains(".")) {
            String[] parts = key.split("\\.");
            ConfigSection current = this;
            for (String part : parts) {
                current = current.getSection(part).orElse(current.createSection(part));
            }
            return current;
        }

        if (key.equalsIgnoreCase(InscriptConstants.ROOT_SECTION_KEY.getValue())) throw new IllegalArgumentException("Illegal attempt to create a root section.");

        final Optional<ConfigSection> sectionFound = getSection(key);
        if (sectionFound.isPresent()) return sectionFound.get();

        final SectionNode created = new SectionNode() {
            private final Set<ConfigNode> nodes = new LinkedHashSet<>();

            @NotNull
            public Set<ConfigNode> getChildren() {
                return nodes;
            }

            @NotNull
            public String getKey() {
                return key;
            }
        };

        getSection().getChildren().add(created);
        return new SimpleConfigSection(created);
    }

    @Override
    public @NotNull <T> Optional<T> get(final @NotNull String key, final @NotNull Class<? extends T> ignoredType) {
        if (key.contains(".")) {
            String[] parts = key.split("\\.");
            ConfigSection current = this;
            for (int i = 0; i < parts.length - 1; i++) {
                Optional<ConfigSection> next = current.getSection(parts[i]);
                if (next.isEmpty()) return Optional.empty();
                current = next.get();
            }
            return current.get(parts[parts.length - 1], ignoredType);
        }

        final ConfigNode node = getNode(key).orElse(null);
        if (node == null) return Optional.empty();

        if (node instanceof ScalarNode<?> scalar) {
            final Object objectValue = scalar.getValue();

            if (!ignoredType.isInstance(objectValue)) return Optional.empty();

            try {
                return Optional.of((T) objectValue);
            } catch (final Exception e) {
                return Optional.empty();
            }
        } else {
            try {
                final Optional<InscriptValue<T>> found = ValueRegistry.REGISTRY.getInscript(ignoredType);
                return found.map(t -> t.deserialize(new SimpleConfigSection((SectionNode) node)));
            } catch (final Exception e) {
                return Optional.empty();
            }
        }
    }

    @Override
    public @NotNull <T> List<T> getList(final @NotNull String key, final @NotNull Class<? extends T> ignoredType) {
        if (key.contains(".")) {
            String[] parts = key.split("\\.");
            ConfigSection current = this;
            for (int i = 0; i < parts.length - 1; i++) {
                Optional<ConfigSection> next = current.getSection(parts[i]);
                if (next.isEmpty()) return Collections.synchronizedList(new ArrayList<>());
                current = next.get();
            }
            return current.getList(parts[parts.length - 1], ignoredType);
        }

        final ConfigNode node = getNode(key).orElse(null);
        if (node == null) return Collections.synchronizedList(new ArrayList<>());
        if (!(node instanceof ScalarNode<?> scalar)) return Collections.synchronizedList(new ArrayList<>());

        try {
            return (List<T>) scalar.getValue();
        } catch (final Exception e) {
            return Collections.synchronizedList(new ArrayList<>());
        }
    }

    @Override
    public @NotNull <T> ConfigSection set(final @NotNull String key, final @Nullable T value) {
        if (key.contains(".")) {
            String[] parts = key.split("\\.");
            ConfigSection current = this;
            for (int i = 0; i < parts.length - 1; i++) {
                current = current.getSection(parts[i]).orElse(current.createSection(parts[i]));
            }
            current.set(parts[parts.length - 1], value);
            return this;
        }

        unset(key);
        if (value == null) return this;

        final Optional<InscriptValue<Object>> inscriptValue = ValueRegistry.REGISTRY.getInscript(value.getClass());
        if (inscriptValue.isPresent()) {
            inscriptValue.get().serialize(value, getSection(key).orElse(createSection(key)));
            return this;
        }

        sectionNode.getChildren().add(new ScalarNode<>() {
            @Override
            public @NotNull String getKey() {
                return key;
            }

            @Override
            public @NotNull T getValue() {
                return value;
            }
        });

        return this;
    }
}
