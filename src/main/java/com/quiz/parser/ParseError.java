package com.quiz.parser;

import java.util.Objects;

/**
 * Ошибка разбора DSL с привязкой к строке исходного текста.
 */
public final class ParseError {

    private final int line;
    private final ErrorCode code;
    private final String message;

    /**
     * @param line    номер строки, начиная с 1
     * @param code    машинный код ошибки
     * @param message пояснение для пользователя
     */
    public ParseError(int line, ErrorCode code, String message) {
        if (line < 1) {
            throw new IllegalArgumentException("Номер строки начинается с 1");
        }
        this.line = line;
        this.code = Objects.requireNonNull(code, "code");
        this.message = Objects.requireNonNull(message, "message");
    }

    public int line() {
        return line;
    }

    public ErrorCode code() {
        return code;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return "Строка " + line + ": " + message + " (" + code + ")";
    }
}
