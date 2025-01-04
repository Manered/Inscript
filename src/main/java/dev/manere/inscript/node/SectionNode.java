package dev.manere.inscript.node;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public abstract class SectionNode extends InscriptNode {
    @NotNull
    public abstract Set<InscriptNode> getChildren();

    @NotNull
    @Override
    public String toString() {
        return getKey() + "[comments = " + getComments() + ", children = " + getChildren() + "]";
    }
}
