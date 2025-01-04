package dev.manere.inscript;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

// We don't care if VERSION or FILE_EXTENSION is not used.
@SuppressWarnings("unused")
public interface InscriptConstants {
    @NotNull
    InscriptConstant<String> LIST_START = new InscriptConstant<>("[");

    @NotNull
    InscriptConstant<String> LIST_END = new InscriptConstant<>("]");

    @NotNull
    InscriptConstant<String> SECTION_START = new InscriptConstant<>("{");

    @NotNull
    InscriptConstant<String> SECTION_END = new InscriptConstant<>("}");

    @NotNull
    InscriptConstant<Function<Integer, String>> INDENT = new InscriptConstant<>("    "::repeat);

    @NotNull
    InscriptConstant<String> ROOT_SECTION_KEY = new InscriptConstant<>("*");

    @NotNull
    InscriptConstant<String> COMMENT_START = new InscriptConstant<>("//");

    @NotNull
    InscriptConstant<Supplier<Optional<String>>> VERSION = new InscriptConstant<>(() -> Optional.ofNullable(InscriptConstants.class.getPackage().getImplementationVersion()));

    @NotNull
    InscriptConstant<String> FILE_EXTENSION = new InscriptConstant<>(".is");
}
