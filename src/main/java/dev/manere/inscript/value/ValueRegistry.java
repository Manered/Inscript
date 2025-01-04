package dev.manere.inscript.value;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.manere.inscript.value.impl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ValueRegistry {
    public static final ValueRegistry REGISTRY;

    static {
        REGISTRY = new ValueRegistry()
            .register(byte[].class, new ByteArrayValue())
            .register(Boolean.class, new BooleanValue())
            .register(Byte.class, new ByteValue())
            .register(Short.class, new ShortValue())
            .register(Integer.class, new IntegerValue())
            .register(Double.class, new DoubleValue())
            .register(Float.class, new FloatValue())
            .register(Long.class, new LongValue())
            .register(UUID.class, new UUIDValue())
            .register(Character.class, new CharacterValue());

        REGISTRY.register(String.class, new StringValue());
    }

    private ValueRegistry() {}

    private final Map<Class<?>, InlineValue<?>> inlineRegistry = new LinkedHashMap<>();
    private final Map<Class<?>, InscriptValue<?>> inscriptRegistry = new LinkedHashMap<>();

    @NotNull
    public <T> Optional<InlineValue<T>> getInline(final @NotNull Class<? extends T> ignoredKey) {
        final InlineValue<?> raw = inlineRegistry.get(ignoredKey);
        if (raw == null) return Optional.empty();

        try {
            return Optional.of((InlineValue<T>) raw);
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    @NotNull
    public <T> Optional<InscriptValue<T>> getInscript(final @NotNull Class<? extends T> ignoredKey) {
        final InscriptValue<?> raw = inscriptRegistry.get(ignoredKey);
        if (raw == null) return Optional.empty();

        try {
            return Optional.of((InscriptValue<T>) raw);
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    @NotNull
    @CanIgnoreReturnValue
    public <T> ValueRegistry register(final @NotNull Class<? super T> key, final @NotNull InlineValue<? super T> value) {
        inlineRegistry.put(key, value);
        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    public <T> ValueRegistry register(final @NotNull Class<? super T> key, final @NotNull InscriptValue<? super T> value) {
        inscriptRegistry.put(key, value);
        return this;
    }

    @NotNull
    @Unmodifiable
    public Map<Class<?>, InlineValue<?>> getInlineRegistry() {
        return new LinkedHashMap<>(inlineRegistry);
    }

    @NotNull
    @Unmodifiable
    public Map<Class<?>, InscriptValue<?>> getInscriptRegistry() {
        return new LinkedHashMap<>(inscriptRegistry);
    }
}
