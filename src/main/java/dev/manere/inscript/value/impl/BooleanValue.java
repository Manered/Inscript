package dev.manere.inscript.value.impl;

import dev.manere.inscript.value.InlineValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BooleanValue implements InlineValue<Boolean> {
    @Override
    public boolean matches(final @NotNull String text) {
        return text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false");
    }

    @Override
    public @Nullable Boolean deserialize(final @NotNull String text) {
        if (text.equalsIgnoreCase("true")) return true;
        if (text.equalsIgnoreCase("false")) return false;
        return null;
    }

    @Override
    public @Nullable String serialize(final @NotNull Boolean b) {
        return b ? "True" : "False";
    }
}
