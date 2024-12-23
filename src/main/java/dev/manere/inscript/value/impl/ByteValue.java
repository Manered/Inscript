package dev.manere.inscript.value.impl;

import dev.manere.inscript.value.InlineValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ByteValue implements InlineValue<Byte> {
    @Override
    public boolean matches(final @NotNull String text) {
        if (!text.endsWith("B")) return false;

        final String other = text.substring(0, text.length() - 1);
        try {
            Byte.valueOf(other);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    @Override
    public @Nullable Byte deserialize(final @NotNull String text) {
        return Byte.valueOf(text.substring(0, text.length() - 1));
    }

    @Override
    public @Nullable String serialize(final @NotNull Byte aByte) {
        return aByte + "B";
    }
}
