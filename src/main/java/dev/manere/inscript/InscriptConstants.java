package dev.manere.inscript;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

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
}
