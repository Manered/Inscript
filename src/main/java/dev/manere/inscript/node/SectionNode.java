package dev.manere.inscript.node;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class SectionNode extends ConfigNode {
    @NotNull
    public abstract Set<ConfigNode> getChildren();

    @NotNull
    @Override
    public String toString() {
        return getKey() + "[comments = " + getComments() + ", children = " + getChildren() + "]";
    }
}
