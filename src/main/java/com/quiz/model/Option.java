package com.quiz.model;

import java.util.Objects;

/**
 * Вариант ответа на вопрос с выбором.
 *
 * <p>Класс неизменяемый. Аксессоры названы как у record ({@link #text()},
 * {@link #correct()}), чтобы код не зависел от версии Java.
 */
public final class Option {

    private final String text;
    private final boolean correct;

    public Option(String text) {
        this(text, false);
    }

    public Option(String text, boolean correct) {
        this.text = Objects.requireNonNull(text, "text");
        this.correct = correct;
    }

    /** Текст варианта. */
    public String text() {
        return text;
    }

    /** Признак правильного ответа. */
    public boolean correct() {
        return correct;
    }

    public Option asCorrect() {
        return new Option(text, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Option)) {
            return false;
        }
        Option other = (Option) o;
        return correct == other.correct && text.equals(other.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, correct);
    }

    @Override
    public String toString() {
        return "Option{text='" + text + "', correct=" + correct + '}';
    }
}
