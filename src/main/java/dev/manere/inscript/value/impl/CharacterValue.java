package dev.manere.inscript.value.impl;

import dev.manere.inscript.value.InlineValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CharacterValue implements InlineValue<Character> {
    @Override
    public boolean matches(final @NotNull String text) {
        return text.length() == 4 && (text.startsWith("'") || text.endsWith("'C"));
    }

    @Override
    public @Nullable Character deserialize(final @NotNull String text) {
        return text.substring(1).replaceAll("'C", "").charAt(0);
    }

    @Override
    public @Nullable String serialize(final @NotNull Character character) {
        return "'" + character + "'C";
    }
}
