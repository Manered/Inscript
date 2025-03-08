package dev.manere.inscript.format;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class Line {
    private final int position;
    private String text;

    public Line(final int position, final @NotNull String text) {
        this.position = position;
        this.text = text;
    }

    public int getPosition() {
        return position;
    }

    @NotNull
    public String getText() {
        return text;
    }

    public void editText(final @NotNull Function<String, String> editor) {
        text = editor.apply(text);
    }

    @NotNull
    @Override
    public String toString() {
        return "Line " + (getPosition() + 1) + ": \n" + getText();
    }
}
