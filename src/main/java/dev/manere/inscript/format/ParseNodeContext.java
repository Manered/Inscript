package dev.manere.inscript.format;

import dev.manere.inscript.node.SectionNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ParseNodeContext {
    private final int expectedDepth;
    private final SectionNode parent;
    private final Set<String> comments = new LinkedHashSet<>();

    public ParseNodeContext(final int expectedDepth, final @NotNull Set<String> comments, final @NotNull SectionNode parent) {
        this.expectedDepth = expectedDepth;
        this.parent = parent;
        this.comments.addAll(comments);
    }

    public int getExpectedDepth() {
        return expectedDepth;
    }

    @NotNull
    @Unmodifiable
    public Set<String> getComments() {
        return Set.copyOf(comments);
    }

    @NotNull
    public SectionNode getParent() {
        return parent;
    }

    public void setComments(final @NotNull Collection<String> comments) {
        this.comments.clear();
        this.comments.addAll(comments);
    }

    public void addComments(final @NotNull String @NotNull ... comments) {
        this.comments.addAll(List.of(comments));
    }

    public void clearComments() {
        this.comments.clear();
    }
}
