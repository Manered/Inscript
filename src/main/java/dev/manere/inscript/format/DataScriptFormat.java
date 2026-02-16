package dev.manere.inscript.format;

import dev.manere.inscript.*;
import dev.manere.inscript.node.ConfigNode;
import dev.manere.inscript.node.ScalarNode;
import dev.manere.inscript.node.SectionNode;
import dev.manere.inscript.value.InlineValue;
import dev.manere.inscript.value.ValueRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class DataScriptFormat implements FileFormat {
    @NotNull
    @Override
    @Unmodifiable
    public List<ErrorContext> load(final @NotNull InscriptReader reader, final @NotNull Inscript inscript) {
        final Set<String> tempComments = new LinkedHashSet<>();
        final List<ErrorContext> errors = new ArrayList<>();
        final Set<Integer> processedLines = new LinkedHashSet<>();

        for (int linePosition = 0; linePosition < reader.getLines().size(); linePosition++) {
            if (processedLines.contains(linePosition)) continue;
            final String line = reader.read(linePosition);
            if (line.isBlank()) {
                processedLines.add(linePosition);
                continue;
            }

            try {
                final Optional<ErrorContext> result = parseNode(new Line(linePosition, line), reader, inscript, new ParseNodeContext(0, tempComments, inscript.getRoot().getSection()), processedLines);
                result.ifPresent(errors::add);
            } catch (final Exception e) {
                ErrorContext.create(new Line(linePosition, line), inscript, "<" + e.getClass().getSimpleName() + "> " + e.getMessage()).handle();
                e.printStackTrace(System.err);
            }
        }

        return errors;
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

    @NotNull
    public Optional<ErrorContext> parseNode(final @NotNull Line line, final @NotNull InscriptReader reader, final @NotNull Inscript inscript, final @NotNull ParseNodeContext context, final @NotNull Set<Integer> processedLines) {
        if (processedLines.contains(line.getPosition())) return Optional.empty();
        processedLines.add(line.getPosition());

        final String indent = InscriptConstants.INDENT.getValue().apply(context.getExpectedDepth());
        final String actualIndent = line.getText().substring(0, line.getText().length() - line.getText().trim().length());

        if (!line.getText().isBlank() && !line.getText().startsWith(indent)) {
            return Optional.of(ErrorContext.create(line, inscript, "Invalid indentation, expected '" + indent + "' but found '" + actualIndent + "'"));
        }

        if (line.getText().trim().startsWith("//")) {
            context.addComments(line.getText().trim().substring("//".length()).trim());
            return Optional.empty();
        }

        String lineText = line.getText().trim();
        String inlineComment = null;
        if (lineText.contains("//")) {
            int commentIndex = lineText.indexOf(" //");
            if (commentIndex != -1) {
                inlineComment = lineText.substring(commentIndex + 3).trim();
                lineText = lineText.substring(0, commentIndex).trim();
                line.setText(lineText);
            }
        }

        if (lineText.equals("...")) {
            return Optional.empty();
        }

        final String[] parts = lineText.split("=");
        final String name = parts[0].trim();

        final String key = name
            .replace("{", "")
            .replace("}", "")
            .trim();

        if (parts.length == 1) {
            if (lineText.endsWith("{")) {
                final SectionNode section = new SectionNode() {
                    private final Set<ConfigNode> nodes = new LinkedHashSet<>();

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

                section.getComments().addAll(context.getComments());
                if (inlineComment != null) section.getInlineComments().add(inlineComment);
                context.clearComments();

                int nestedDepth = 1;
                for (int position = line.getPosition() + 1; position < reader.getLines().size(); position++) {
                    final String childLine = reader.read(position);

                    if (childLine.trim().endsWith("{")) {
                        nestedDepth++;
                    } else if (childLine.trim().equals("}")) {
                        nestedDepth--;
                        if (nestedDepth == 0) {
                            processedLines.add(position);
                            break;
                        }
                    }

                    final ErrorContext error = parseNode(new Line(position, childLine), reader, inscript, new ParseNodeContext(context.getExpectedDepth() + 1, context.getComments(), section), processedLines).orElse(null);
                    if (error != null) return Optional.of(error);
                }

                context.getParent().getChildren().add(section);
                return Optional.empty();
            } else if (lineText.replaceAll(" ", "").endsWith("{}")) {
                final SectionNode section = new SectionNode() {
                    private final Set<ConfigNode> nodes = new LinkedHashSet<>();

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

                section.getComments().addAll(context.getComments());
                if (inlineComment != null) section.getInlineComments().add(inlineComment);
                context.clearComments();

                context.getParent().getChildren().add(section);

                return Optional.empty();
            }
        }

        if (parts.length == 2) {
            final String value = parts[1].trim();

            if (value.replaceAll(" ", "").equals("[]")) {
                final ScalarNode<?> node = new ScalarNode<>() {
                    @Override
                    public @NotNull String getKey() {
                        return key;
                    }

                    @Override
                    public @NotNull Object getValue() {
                        return List.of();
                    }
                };

                node.getComments().addAll(context.getComments());
                if (inlineComment != null) node.getInlineComments().add(inlineComment);
                context.clearComments();

                context.getParent().getChildren().add(node);

                return Optional.empty();
            }

            if (value.startsWith("[")) {
                final List<Object> list = new ArrayList<>();
                final StringBuilder listContent = new StringBuilder(value);

                int newLinePosition = line.getPosition();

                while (!listContent.toString().trim().endsWith("]")) {
                    newLinePosition++;
                    if (newLinePosition >= reader.getLines().size())
                        return Optional.of(ErrorContext.create(line, inscript, "Invalid list"));
                    final String nextLine = reader.read(newLinePosition);
                    listContent.append(nextLine.trim());
                    processedLines.add(newLinePosition);
                }

                final String content = listContent.substring(1, listContent.length() - 1).trim();

                final String[] elements = content.split(",");

                for (String element : elements) {
                    element = element.trim();

                    if (element.equalsIgnoreCase("Null")) continue;

                    InlineValue<?> inlineMatched = ValueRegistry.REGISTRY.getInline(String.class).orElseThrow();

                    for (final InlineValue<?> inline : ValueRegistry.REGISTRY.getInlineRegistry().values()) {
                        if (inline.equals(ValueRegistry.REGISTRY.getInline(String.class).orElseThrow())) continue;

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
                        return List.copyOf(list);
                    }
                };

                node.getComments().addAll(context.getComments());
                if (inlineComment != null) node.getInlineComments().add(inlineComment);
                context.clearComments();

                context.getParent().getChildren().add(node);

                return Optional.empty();
            }

            if (value.isBlank()) return Optional.empty();
            if (value.equalsIgnoreCase("Null")) return Optional.empty();

            InlineValue<?> inlineMatched = ValueRegistry.REGISTRY.getInline(String.class).orElseThrow();

            for (final InlineValue<?> inline : ValueRegistry.REGISTRY.getInlineRegistry().values()) {
                if (inline.equals(ValueRegistry.REGISTRY.getInline(String.class).orElseThrow())) continue;

                if (inline.matches(value)) {
                    inlineMatched = inline;
                    break;
                }
            }

            final Object o = inlineMatched.deserialize(value);
            if (o == null) return Optional.empty();

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


            node.getComments().addAll(context.getComments());
            if (inlineComment != null) node.getInlineComments().add(inlineComment);
            context.clearComments();

            context.getParent().getChildren().add(node);
        }

        return Optional.empty();
    }

    @ApiStatus.Internal
    private void writeNode(final @NotNull InscriptStringWriter writer, final @NotNull ConfigNode node, final int depth) {
        if (depth < 0) throw new InscriptException();

        final String indent = InscriptConstants.INDENT.getValue().apply(depth);

        final String key = node.getKey();

        if (node instanceof SectionNode section) {
            for (final String comment : section.getComments()) {
                writer.write(indent + "// " + comment);
                writer.newLine();
            }

            if (section.getChildren().isEmpty()) {
                writer.write(indent + key + " {}");
                if (!section.getInlineComments().isEmpty()) {
                    writer.write(" // " + String.join(" ", section.getInlineComments()));
                }
                writer.newline();
                return;
            }

            writer.write(indent + key + " {");
            if (!section.getInlineComments().isEmpty()) {
                writer.write(" // " + String.join(" ", section.getInlineComments()));
            }
            writer.write("\n");

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
                    if (!scalar.getInlineComments().isEmpty()) {
                        writer.write(" // " + String.join(" ", scalar.getInlineComments()));
                    }
                    writer.newLine();
                } else {
                    writer.write(indent + key + " = [");
                    if (!scalar.getInlineComments().isEmpty()) {
                        writer.write(" // " + String.join(" ", scalar.getInlineComments()));
                    }
                    writer.newline();

                    for (int i = 0; i < list.size(); i++) {
                        final Object element = list.get(i);

                        final InlineValue<Object> value = ValueRegistry.REGISTRY.getInline(element.getClass()).orElse(null);

                        if (value == null) {
                            writer.write(indent + InscriptConstants.INDENT.getValue().apply(1) + element);
                        } else {
                            writer.write(indent + InscriptConstants.INDENT.getValue().apply(1) + value.serialize(element));
                        }

                        if (i != list.size() - 1) {
                            writer.write(",");
                            writer.newline();
                            continue;
                        }

                        writer.newLine();
                    }

                    writer.write(indent + "]\n");
                }
            } else {
                final InlineValue<Object> value = ValueRegistry.REGISTRY.getInline(type).orElse(null);

                if (value == null) {
                    writer.write(indent + key + " = " + objectValue);
                } else {
                    writer.write(indent + key + " = " + value.serialize(objectValue));
                }

                if (!scalar.getInlineComments().isEmpty()) {
                    writer.write(" // " + String.join(" ", scalar.getInlineComments()));
                }
                writer.write("\n");
            }
        }
    }
}
