package dev.manere.inscript.value.impl;

import dev.manere.inscript.value.InlineValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShortValue implements InlineValue<Short> {
    @Override
    public boolean matches(final @NotNull String text) {
        if (!text.endsWith("S")) return false;

        final String other = text.replaceAll("S", "");
        try {
            Short.valueOf(other);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    @Override
    public @Nullable Short deserialize(final @NotNull String text) {
        return Short.valueOf(text.replaceAll("S", ""));
    }

    @Override
    public @Nullable String serialize(final @NotNull Short aShort) {
        return aShort + "S";
    }
}
