package dev.manere.inscript.node;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class SectionNode extends ConfigNode {
    @NotNull
    public static SectionNode section(final @NotNull String key) {
        return new SectionNode() {
            @Override
            public @NotNull Set<ConfigNode> getChildren() {
                return new LinkedHashSet<>();
            }

            @Override
            public @NotNull String getKey() {
                return key;
            }
        };
    }

    @NotNull
    public abstract Set<ConfigNode> getChildren();

    public boolean isRoot() {
        return this instanceof RootSectionNode;
    }

    @NotNull
    @Override
    public String toString() {
        return "<" + getKey() + ": " + getChildren() + ">";
    }
}
