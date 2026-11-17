package com.quiz.parser;

import java.util.Objects;

/**
 * Ошибка разбора DSL с привязкой к строке исходного текста.
 *
 * @param line    номер строки, начиная с 1
 * @param code    машинный код ошибки
 * @param message пояснение для пользователя
 */
public record ParseError(int line, ErrorCode code, String message) {

    public ParseError {
        if (line < 1) {
            throw new IllegalArgumentException("Номер строки начинается с 1");
        }
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
    }

    @Override
    public String toString() {
        return "Строка " + line + ": " + message + " (" + code + ")";
    }
}
