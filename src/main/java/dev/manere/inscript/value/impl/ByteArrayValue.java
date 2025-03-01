package dev.manere.inscript.value.impl;

import dev.manere.inscript.value.InlineValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;

public class ByteArrayValue implements InlineValue<byte[]> {
    @Override
    public boolean matches(final @NotNull String text) {
        return text.startsWith("base64(") && text.endsWith(")");
    }

    @Override
    public byte @Nullable [] deserialize(final @NotNull String text) {
        if (!text.startsWith("base64(") && !text.endsWith(")")) return null;
        final String content = text.substring(7, text.length() - 1);

        try {
            return Base64.getDecoder().decode(content);
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public @Nullable String serialize(final byte @NotNull [] bytes) {
        return "base64(" + Base64.getEncoder().encodeToString(bytes) + ")";
    }
}