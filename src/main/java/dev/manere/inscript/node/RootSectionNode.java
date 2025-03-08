package dev.manere.inscript.node;

import dev.manere.inscript.InscriptConstants;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public class RootSectionNode extends SectionNode {
    private final Set<ConfigNode> nodes = new LinkedHashSet<>();

    @NotNull
    @Override
    public String getKey() {
        return InscriptConstants.ROOT_SECTION_KEY.getValue();
    }

    @NotNull
    @Override
    public Set<ConfigNode> getChildren() {
        return nodes;
    }
}
