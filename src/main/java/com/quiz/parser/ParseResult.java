package com.quiz.parser;

import com.quiz.model.Quiz;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Результат разбора: всегда содержит модель (возможно, частичную) и список ошибок.
 */
public record ParseResult(Quiz quiz, List<ParseError> errors) {

    public ParseResult {
        quiz = quiz == null ? Quiz.empty() : quiz;
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static ParseResult success(Quiz quiz) {
        return new ParseResult(quiz, List.of());
    }

    public static ParseResult failure(Quiz quiz, List<ParseError> errors) {
        return new ParseResult(quiz, errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isSuccess() {
        return errors.isEmpty();
    }

    /** Все ошибки в виде строк «Строка N: ... (КОД)», по одной на строку. */
    public String errorReport() {
        return errors.stream()
                .map(ParseError::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
