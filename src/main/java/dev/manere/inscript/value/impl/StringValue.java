package dev.manere.inscript.value.impl;

import dev.manere.inscript.value.InlineValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringValue implements InlineValue<String> {
    @Override
    public boolean matches(final @NotNull String text) {
        return true;
    }

    @Override
    public @Nullable String deserialize(final @NotNull String text) {
        if (text.startsWith("'") && text.endsWith("'")) return text.substring(1, text.length() - 1);
        if (text.startsWith("\"") && text.endsWith("\"")) return text.substring(1, text.length() - 1);

        return text;
    }

    @Override
    public @Nullable String serialize(final @NotNull String s) {
        return "'" + s + "'";
    }
}
