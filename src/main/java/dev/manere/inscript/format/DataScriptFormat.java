package dev.manere.inscript.format;

import dev.manere.inscript.ConfigSection;
import dev.manere.inscript.InscriptConstants;
import dev.manere.inscript.InscriptException;
import dev.manere.inscript.node.ConfigNode;
import dev.manere.inscript.node.ScalarNode;
import dev.manere.inscript.node.SectionNode;
import dev.manere.inscript.value.InlineValue;
import dev.manere.inscript.value.ValueRegistry;
import dev.manere.inscript.value.impl.StringValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataScriptFormat implements FileFormat {
    @Override
    public void load(final @NotNull BufferedReader reader, final @NotNull ConfigSection root) throws Exception {
        final Set<String> tempComments = new HashSet<>();

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isBlank()) continue;

            final ConfigNode node = parseNode(line, reader, 0, tempComments);
            if (node != null) root.getSection().getChildren().add(node);
        }
    }

    @NotNull
    @Override
    public String save(final @NotNull ConfigSection root) {
        final InscriptStringWriter writer = InscriptStringWriter.newWriter();

        for (final ConfigNode node : root.getChildren()) {
            writeNode(writer, node, 0);
        }

        return writer.build();
    }

    @NotNull
    @Override
    @Unmodifiable
    public Collection<String> getValidFileExtensions() {
        return List.of("ds");
    }

    @Nullable
    @ApiStatus.Internal
    private ConfigNode parseNode(@NotNull String line, final @NotNull BufferedReader reader, final int depth, final @NotNull Set<String> tempComments) throws Exception {
        final String indent = InscriptConstants.INDENT.get().apply(depth);
        if (!line.startsWith(indent)) return null;

        line = line.substring(indent.length());

        if (line.startsWith("//")) {
            tempComments.add(line.substring("//".length()).trim());
            return null;
        }

        final String[] parts = line.split("=", 2);
        final String name = parts[0].trim();

        final String key = name
            .replace("{", "")
            .replace("}", "")
            .trim();

        if (parts.length == 1) {
            if (line.endsWith("{")) {
                final SectionNode section = new SectionNode() {
                    private final Set<ConfigNode> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

                    @NotNull
                    @Override
                    public Set<ConfigNode> getChildren() {
                        return nodes;
                    }

                    @NotNull
                    @Override
                    public String getKey() {
                        return key;
                    }
                };

                section.getComments().addAll(tempComments);
                tempComments.clear();

                String childLine;
                while ((childLine = reader.readLine()) != null && !childLine.trim().equals("}")) {
                    final ConfigNode childNode = parseNode(childLine, reader, depth + 1, tempComments);
                    if (childNode != null) {
                        section.getChildren().add(childNode);
                    }
                }

                return section;
            } else if (line.endsWith("{}")) {
                final SectionNode node = new SectionNode() {
                    private final Set<ConfigNode> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

                    @NotNull
                    @Override
                    public Set<ConfigNode> getChildren() {
                        return nodes;
                    }

                    @NotNull
                    @Override
                    public String getKey() {
                        return key;
                    }
                };

                node.getComments().addAll(tempComments);
                tempComments.clear();

                return node;
            }

            return null;
        }

        final String value = parts[1].trim();

        if (value.equals("[]")) {
            final List<?> list = new ArrayList<>();

            final ScalarNode<?> node = new ScalarNode<>() {
                @Override
                public @NotNull String getKey() {
                    return key;
                }

                @Override
                public @NotNull Object getValue() {
                    return list;
                }
            };

            node.getComments().addAll(tempComments);
            tempComments.clear();

            return node;
        }

        if (value.startsWith("[")) {
            final List<Object> list = new ArrayList<>();
            final StringBuilder listContent = new StringBuilder(value);

            while (!listContent.toString().trim().endsWith("]")) {
                String nextLine = reader.readLine();
                if (nextLine != null) {
                    listContent.append(nextLine.trim());
                }
            }

            final String content = listContent.substring(1, listContent.length() - 1).trim();

            final String[] elements = content.split(",");

            for (String element : elements) {
                element = element.trim();

                if (element.equalsIgnoreCase("Null")) continue;

                InlineValue<?> inlineMatched = new StringValue();

                for (final InlineValue<?> inline : ValueRegistry.REGISTRY.getInlineRegistry().values()) {
                    if (inline.equals(new StringValue())) continue;

                    if (inline.matches(element)) {
                        inlineMatched = inline;
                        break;
                    }
                }

                if (inlineMatched.matches(element)) {
                    list.add(inlineMatched.deserialize(element));
                }
            }

            final ScalarNode<?> node = new ScalarNode<>() {
                @Override
                public @NotNull String getKey() {
                    return key;
                }

                @Override
                public @NotNull Object getValue() {
                    return Collections.synchronizedList(list);
                }
            };

            node.getComments().addAll(tempComments);
            tempComments.clear();

            return node;
        }

        if (value.equalsIgnoreCase("Null")) return null;

        InlineValue<?> inlineMatched = new StringValue();

        for (final InlineValue<?> inline : ValueRegistry.REGISTRY.getInlineRegistry().values()) {
            if (inline.equals(new StringValue())) continue;

            if (inline.matches(value)) {
                inlineMatched = inline;
                break;
            }
        }

        final Object o = inlineMatched.deserialize(value);
        if (o == null) return null;

        final ScalarNode<?> node = new ScalarNode<>() {
            @Override
            public @NotNull String getKey() {
                return key;
            }

            @Override
            public @NotNull Object getValue() {
                return o;
            }
        };

        node.getComments().addAll(tempComments);
        tempComments.clear();

        return node;
    }

    @ApiStatus.Internal
    private void writeNode(final @NotNull InscriptStringWriter writer, final @NotNull ConfigNode node, final int depth) {
        if (depth < 0) throw new InscriptException();

        final String indent = InscriptConstants.INDENT.get().apply(depth);

        final String key = node.getKey();

        if (node instanceof SectionNode section) {
            for (final String comment : section.getComments()) {
                writer.write(indent + "// " + comment);
                writer.newLine();
            }

            if (section.getChildren().isEmpty()) {
                writer.write(indent + key + " {}");
                return;
            }

            writer.write(indent + key + " {\n");

            for (final ConfigNode child : section.getChildren()) {
                writeNode(writer, child, depth + 1);
            }

            writer.write(indent + "}\n");
        } else if (node instanceof ScalarNode<?> scalar) {
            for (final String comment : scalar.getComments()) {
                writer.write(indent + "// " + comment);
                writer.newLine();
            }

            final Object objectValue = scalar.getValue();
            final Class<?> type = objectValue.getClass();

            if (objectValue instanceof List<?> list) {
                if (list.isEmpty()) {
                    writer.write(indent + key + " = []");
                    writer.newLine();
                } else {
                    writer.write(indent + key + " = [\n");

                    for (int i = 0; i < list.size(); i++) {
                        final Object element = list.get(i);

                        final InlineValue<Object> value = ValueRegistry.REGISTRY.<Object>getInline(element.getClass()).orElse(null);

                        if (value == null) {
                            writer.write(indent + InscriptConstants.INDENT.get().apply(1) + element);
                        } else {
                            writer.write(indent + InscriptConstants.INDENT.get().apply(1) + value.serialize(element));
                        }

                        if (i != list.size() - 1) {
                            writer.write(",");
                            writer.newline();
                            continue;
                        }

                        writer.newLine();
                        break;
                    }

                    writer.write(indent + "]\n");
                }
            } else {
                final InlineValue<Object> value = ValueRegistry.REGISTRY.<Object>getInline(type).orElse(null);

                if (value == null) {
                    writer.write(indent + key + " = " + objectValue + "\n");
                    return;
                }

                writer.write(indent + key + " = " + value.serialize(objectValue) + "\n");
            }
        } else {
            throw new InscriptException("Found unsupported ConfigNode: " + node.getClass().getSimpleName());
        }
    }
}
