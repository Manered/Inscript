package dev.manere.inscript.value;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.manere.inscript.ConfigSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface InscriptValue<T> {
    @Nullable
    T deserialize(final @NotNull ConfigSection section);

    void serialize(final @NotNull T t, final @NotNull ConfigSection section);

    @NotNull
    static <T> InscriptValue<T> create(
        final @NotNull Function<@NotNull ConfigSection, @Nullable T> deserialize,
        final @NotNull BiConsumer<@NotNull T, @NotNull ConfigSection> serialize
    ) {
        return new InscriptValue<>() {
            @Override
            public @Nullable T deserialize(@NotNull ConfigSection section) {
                return deserialize.apply(section);
            }

            @Override
            public void serialize(@NotNull T t, @NotNull ConfigSection section) {
                serialize.accept(t, section);
            }
        };
    }


    @NotNull
    static <T> InscriptValue.Builder<T> builder() {
        return new InscriptValue.Builder<>();
    }

    class Builder<T> {
        private Function<@NotNull ConfigSection, @Nullable T> deserialize;
        private BiConsumer<@NotNull T, @NotNull ConfigSection> serialize;

        @NotNull
        public Function<@NotNull ConfigSection,T> deserialize() {
            return deserialize;
        }

        @NotNull
        @CanIgnoreReturnValue
        public Builder<T> deserialize(final @NotNull Function<@NotNull ConfigSection, T> deserialize) {
            this.deserialize = deserialize;
            return this;
        }

        @NotNull
        public BiConsumer<T, @NotNull ConfigSection> serialize() {
            return serialize;
        }

        @NotNull
        @CanIgnoreReturnValue
        public Builder<T> serialize(final @NotNull BiConsumer<T, @NotNull ConfigSection> serialize) {
            this.serialize = serialize;
            return this;
        }

        @NotNull
        public InscriptValue<T> build() {
            return create(deserialize, serialize);
        }
    }
}
