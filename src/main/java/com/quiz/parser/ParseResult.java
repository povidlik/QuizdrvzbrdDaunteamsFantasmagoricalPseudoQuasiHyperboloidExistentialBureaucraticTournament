package com.quiz.parser;

import com.quiz.model.Quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Результат разбора: всегда содержит модель (возможно, частичную) и список ошибок.
 */
public final class ParseResult {

    private final Quiz quiz;
    private final List<ParseError> errors;

    public ParseResult(Quiz quiz, List<ParseError> errors) {
        this.quiz = quiz == null ? Quiz.empty() : quiz;
        this.errors = errors == null
                ? Collections.<ParseError>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public static ParseResult success(Quiz quiz) {
        return new ParseResult(quiz, Collections.<ParseError>emptyList());
    }

    public static ParseResult failure(Quiz quiz, List<ParseError> errors) {
        return new ParseResult(quiz, errors);
    }

    public Quiz quiz() {
        return quiz;
    }

    public List<ParseError> errors() {
        return errors;
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

    @Override
    public String toString() {
        return "ParseResult{quiz=" + quiz + ", errors=" + errors + '}';
    }
}
