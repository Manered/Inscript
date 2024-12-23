package dev.manere.inscript.value;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.manere.inscript.InscriptEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface InscriptValue<T> {
    @Nullable
    T deserialize(final @NotNull InscriptEditor section);

    void serialize(final @NotNull T t, final @NotNull InscriptEditor section);

    @NotNull
    static <T> InscriptValue<T> create(
        final @NotNull Function<@NotNull InscriptEditor, @Nullable T> deserialize,
        final @NotNull BiConsumer<@NotNull T, @NotNull InscriptEditor> serialize
    ) {
        return new InscriptValue<>() {
            @Override
            public @Nullable T deserialize(@NotNull InscriptEditor section) {
                return deserialize.apply(section);
            }

            @Override
            public void serialize(@NotNull T t, @NotNull InscriptEditor section) {
                serialize.accept(t, section);
            }
        };
    }


    @NotNull
    static <T> InscriptValue.Builder<T> builder() {
        return new InscriptValue.Builder<>();
    }

    class Builder<T> {
        private Function<@NotNull InscriptEditor, @Nullable T> deserialize;
        private BiConsumer<@NotNull T, @NotNull InscriptEditor> serialize;

        @NotNull
        public Function<@NotNull InscriptEditor,T> deserialize() {
            return deserialize;
        }

        @NotNull
        @CanIgnoreReturnValue
        public Builder<T> deserialize(final @NotNull Function<@NotNull InscriptEditor,T> deserialize) {
            this.deserialize = deserialize;
            return this;
        }

        @NotNull
        public BiConsumer<T, @NotNull InscriptEditor> serialize() {
            return serialize;
        }

        @NotNull
        @CanIgnoreReturnValue
        public Builder<T> serialize(final @NotNull BiConsumer<T, @NotNull InscriptEditor> serialize) {
            this.serialize = serialize;
            return this;
        }

        @NotNull
        public InscriptValue<T> build() {
            return create(deserialize, serialize);
        }
    }
}
