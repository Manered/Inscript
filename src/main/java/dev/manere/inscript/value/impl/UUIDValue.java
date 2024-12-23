package dev.manere.inscript.value.impl;

import dev.manere.inscript.value.InlineValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UUIDValue implements InlineValue<UUID> {
    @Override
    public boolean matches(final @NotNull String text) {
        if (!text.startsWith("uuid(") && !text.endsWith(")")) {
            return false;
        }

        try {
            UUID.fromString(text.substring(5, text.length() - 1));
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable UUID deserialize(final @NotNull String text) {
        try {
            return UUID.fromString(text.substring(5, text.length() - 1));
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public @Nullable String serialize(final @NotNull UUID uuid) {
        return "uuid(" + uuid + ")";
    }
}
