package dev.manere.inscript;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface InscriptConstants {
    @NotNull
    InscriptConstant<Function<Integer, String>> INDENT = new InscriptConstant<>("  "::repeat);

    @NotNull
    InscriptConstant<String> ROOT_SECTION_KEY = new InscriptConstant<>("*");
    
    @NotNull
    InscriptConstant<Supplier<Optional<String>>> VERSION = new InscriptConstant<>(() -> Optional.ofNullable(InscriptConstants.class.getPackage().getImplementationVersion()));

    @NotNull
    InscriptConstant<Consumer<ErrorContext>> ERROR_HANDLER = new InscriptConstant<>(context -> System.err.println(context.buildDefault()));
}
