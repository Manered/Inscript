package dev.manere.inscript.value;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface InlineValue<T> {
    boolean matches(final @NotNull String text);

    @Nullable
    T deserialize(final @NotNull String text);

    @Nullable
    String serialize(final @NotNull T t);

    @NotNull
    static <T> InlineValue<T> create(
        final @NotNull Function<@NotNull String, @NotNull Boolean> matches,
        final @NotNull Function<@NotNull String, @Nullable T> deserialize,
        final @NotNull Function<@NotNull T, @Nullable String> serialize
    ) {
        return new InlineValue<>() {
            @Override
            public boolean matches(@NotNull String text) {
                return matches.apply(text);
            }

            @Override
            public @Nullable T deserialize(@NotNull String text) {
                return deserialize.apply(text);
            }

            @Override
            public @Nullable String serialize(@NotNull T t) {
                return serialize.apply(t);
            }
        };
    }

    @NotNull
    static <T> InlineValue.Builder<T> builder() {
        return new InlineValue.Builder<>();
    }

    class Builder<T> {
        private Function<@NotNull String, @NotNull Boolean> matches;
        private Function<@NotNull String, @Nullable T> deserialize;
        private Function<@NotNull T, @Nullable String> serialize;

        public Function<@NotNull String, @NotNull Boolean> matches() {
            return matches;
        }

        @NotNull
        @CanIgnoreReturnValue
        public Builder<T> matches(final @NotNull Function<@NotNull String, @NotNull Boolean> matches) {
            this.matches = matches;
            return this;
        }

        @NotNull
        public Function<@NotNull String,T> deserialize() {
            return deserialize;
        }

        @NotNull
        @CanIgnoreReturnValue
        public Builder<T> deserialize(final @NotNull Function<@NotNull String,T> deserialize) {
            this.deserialize = deserialize;
            return this;
        }

        @NotNull
        public Function<T, @Nullable String> serialize() {
            return serialize;
        }

        @NotNull
        @CanIgnoreReturnValue
        public Builder<T> serialize(final @NotNull Function<T, @Nullable String> serialize) {
            this.serialize = serialize;
            return this;
        }

        @NotNull
        public InlineValue<T> build() {
            return create(matches, deserialize, serialize);
        }
    }
}
