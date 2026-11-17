package com.quiz.model;

import java.util.Objects;

/**
 * Вариант ответа на вопрос с выбором.
 *
 * @param text    текст варианта
 * @param correct признак правильного ответа
 */
public record Option(String text, boolean correct) {

    public Option {
        Objects.requireNonNull(text, "text");
    }

    public Option(String text) {
        this(text, false);
    }

    public Option asCorrect() {
        return new Option(text, true);
    }
}
