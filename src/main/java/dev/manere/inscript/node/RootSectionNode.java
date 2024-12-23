package dev.manere.inscript.node;

import dev.manere.inscript.InscriptConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RootSectionNode extends SectionNode {
    private final Set<InscriptNode> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @NotNull
    @Override
    public String getKey() {
        return InscriptConstants.ROOT_SECTION_KEY.get();
    }

    @NotNull
    @Override
    public Set<InscriptNode> getChildren() {
        return nodes;
    }
}
