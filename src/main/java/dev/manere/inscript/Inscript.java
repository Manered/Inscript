package dev.manere.inscript;

import dev.manere.inscript.node.InscriptNode;
import dev.manere.inscript.node.RootSectionNode;
import dev.manere.inscript.node.ScalarNode;
import dev.manere.inscript.node.SectionNode;
import dev.manere.inscript.value.InlineValue;
import dev.manere.inscript.value.ValueRegistry;
import dev.manere.inscript.value.impl.StringValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Inscript {
    @Nullable
    private final Path path;
    private final InscriptEditor editor = new SimpleInscriptEditor(new RootSectionNode());

    private Inscript(final @Nullable Path path) {
        this.path = path;
    }

    @NotNull
    public static Inscript newInscript(final @Nullable Path path) {
        return new Inscript(path);
    }

    @NotNull
    public static Inscript newInscript(final @NotNull File file) {
        return newInscript(file.toPath());
    }

    @NotNull
    public static Inscript emptyInscript() {
        return new Inscript(null);
    }

    @NotNull
    public InscriptEditor getEditor() {
        return editor;
    }

    @NotNull
    public Optional<Path> getPath() {
        return Optional.ofNullable(path);
    }

    public void saveToDisk() {
        if (getPath().isEmpty()) {
            throw new InscriptError("Attempted to save to disk with a null path");
        }

        final File file = getPath().get().toFile();

        if (!file.exists()) try {
            if (!file.createNewFile()) throw new InscriptError("Failed to save " + file + " to disk, couldn't create file.");
        } catch (final IOException e) {
            throw new InscriptError(e);
        }

        try (final BufferedWriter writer = Files.newBufferedWriter(getPath().get())) {
            for (final InscriptNode node : editor.getChildren()) writeNode(writer, node, 0);
        } catch (final Exception e) {
            throw new InscriptError(e);
        }
    }

    @NotNull
    public String saveToString() {
        final StringWriter stringWriter = new StringWriter();

        try (final BufferedWriter writer = new BufferedWriter(stringWriter)) {
            for (final InscriptNode node : editor.getChildren()) writeNode(writer, node, 0);
        } catch (final Exception e) {
            throw new InscriptError(e);
        }

        return stringWriter.toString();
    }

    private void writeNode(final @NotNull BufferedWriter writer, final @NotNull InscriptNode node, final int depth) throws Exception {
        if (depth < 0) throw new InscriptError("Argument `depth` in call Inscript#writeNode(..., final int depth) is `" + depth + "`, which is under 0");

        final String indent = InscriptConstants.INDENT.get().apply(depth);

        final String key = node.getKey();

        if (node instanceof SectionNode section) {
            for (final String comment : section.getComments()) {
                writer.write(InscriptConstants.COMMENT_START.get() + " " + comment);
                writer.newLine();
            }

            if (section.getChildren().isEmpty()) {
                writer.write(indent + key + " " + InscriptConstants.SECTION_START.get() + InscriptConstants.SECTION_END.get());
                return;
            }

            writer.write(indent + key + " " + InscriptConstants.SECTION_START.get() + "\n");

            for (final InscriptNode child : section.getChildren()) {
                writeNode(writer, child, depth + 1);
            }

            writer.write(indent + InscriptConstants.SECTION_END.get() + "\n");
        } else if (node instanceof ScalarNode<?> scalar) {
            for (final String comment : scalar.getComments()) {
                writer.write(InscriptConstants.COMMENT_START.get() + " " + comment);
                writer.newLine();
            }

            final Object objectValue = scalar.getValue();
            final Class<?> type = objectValue.getClass();

            if (objectValue instanceof List<?> list) {
                if (list.isEmpty()) {
                    writer.write(indent + key + " = " + InscriptConstants.LIST_START.get() + InscriptConstants.LIST_END.get());
                } else {
                    writer.write(indent + key + " = " + InscriptConstants.LIST_START.get() + "\n");

                    for (int i = 0; i < list.size(); i++) {
                        final Object element = list.get(i);

                        final InlineValue<Object> value = ValueRegistry.REGISTRY.<Object>getInline(element.getClass()).orElse(null);

                        if (value == null) {
                            writer.write(indent + InscriptConstants.INDENT.get().apply(1) + element);
                        } else {
                            writer.write(indent + InscriptConstants.INDENT.get().apply(1) + value.serialize(element));
                        }

                        if (i != list.size() - 1) {
                            writer.append(",");
                            writer.newLine();
                            continue;
                        }

                        writer.newLine();
                        break;
                    }

                    writer.write(indent + InscriptConstants.LIST_END.get() + "\n");
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
            throw new InscriptError("Found unsupported InscriptNode: " + node.getClass().getSimpleName());
        }
    }

    public void loadFromDisk() {
        if (getPath().isEmpty()) throw new InscriptError("Attempted to load from disk with a null path");
        if (!getPath().get().toFile().exists()) return;

        try (final BufferedReader reader = Files.newBufferedReader(getPath().get())) {
            getEditor().reset();

            final Set<String> tempComments = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                final InscriptNode node = parseNode(line, reader, 0, tempComments);
                if (node != null) getEditor().getSection().getChildren().add(node);
            }
        } catch (final Exception e) {
            throw new InscriptError(e);
        }
    }

    public void loadFromString(final @NotNull String configString) {
        try (final BufferedReader reader = new BufferedReader(new StringReader(configString))) {
            getEditor().reset();

            final Set<String> tempComments = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                final InscriptNode node = parseNode(line, reader, 0, tempComments);
                if (node != null) getEditor().getSection().getChildren().add(node);
            }
        } catch (final IOException e) {
            throw new InscriptError(e);
        }
    }

    @Nullable
    @ApiStatus.Internal
    private InscriptNode parseNode(@NotNull String line, final @NotNull BufferedReader reader, final int depth, final @NotNull Set<String> tempComments) throws IOException {
        final String indent = InscriptConstants.INDENT.get().apply(depth);
        if (!line.startsWith(indent)) return null;

        line = line.substring(indent.length());

        if (line.startsWith(InscriptConstants.COMMENT_START.get())) {
            tempComments.add(line.substring(InscriptConstants.COMMENT_START.get().length()).trim());
            return null;
        }

        final String[] parts = line.split("=", 2);
        final String name = parts[0].trim();

        final String key = name
            .replace(InscriptConstants.SECTION_START.get(), "")
            .replace(InscriptConstants.SECTION_END.get(), "")
            .trim();

        if (parts.length == 1) {
            if (line.endsWith(InscriptConstants.SECTION_START.get())) {
                final SectionNode section = new SectionNode() {
                    private final Set<InscriptNode> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

                    @NotNull
                    @Override
                    public Set<InscriptNode> getChildren() {
                        return nodes;
                    }

                    @NotNull
                    @Override
                    public String getKey() {
                        return key;
                    }
                };

                String childLine;
                while ((childLine = reader.readLine()) != null && !childLine.trim().equals(InscriptConstants.SECTION_END.get())) {
                    final InscriptNode childNode = parseNode(childLine, reader, depth + 1, new HashSet<>());
                    if (childNode != null) {
                        section.getChildren().add(childNode);
                    }
                }

                System.out.println(1);
                section.getComments().addAll(tempComments);
                tempComments.clear();

                return section;
            } else if (line.endsWith(InscriptConstants.SECTION_START.get() + InscriptConstants.SECTION_END.get())) {
                final SectionNode node = new SectionNode() {
                    private final Set<InscriptNode> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());

                    @NotNull
                    @Override
                    public Set<InscriptNode> getChildren() {
                        return nodes;
                    }

                    @NotNull
                    @Override
                    public String getKey() {
                        return key;
                    }
                };

                System.out.println(2);
                node.getComments().addAll(tempComments);
                tempComments.clear();

                return node;
            }

            return null;
        }

        final String value = parts[1].trim();

        if (value.startsWith(InscriptConstants.LIST_START.get())) {
            final List<Object> list = new ArrayList<>();
            final StringBuilder listContent = new StringBuilder(value);

            while (!listContent.toString().trim().endsWith(InscriptConstants.LIST_END.get())) {
                String nextLine = reader.readLine();
                if (nextLine != null) {
                    listContent.append(nextLine.trim());
                }
            }

            final String content = listContent.substring(1, listContent.length() - 1).trim();

            final String[] elements = content.split(",");

            for (String element : elements) {
                element = element.trim();

                InlineValue<?> inlineMatched = new StringValue();

                for (final InlineValue<?> inline : ValueRegistry.REGISTRY.getInlineRegistry().values()) {
                    if (inline.equals(new StringValue())) continue;

                    if (inline.matches(value)) {
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


            System.out.println(3);
            node.getComments().addAll(tempComments);
            tempComments.clear();

            return node;
        }

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

        System.out.println(4);

        node.getComments().addAll(tempComments);
        tempComments.clear();

        return node;
    }
}
