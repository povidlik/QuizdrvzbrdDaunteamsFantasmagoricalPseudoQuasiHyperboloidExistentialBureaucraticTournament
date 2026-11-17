package com.quiz.parser;

import com.quiz.model.Question;
import com.quiz.model.Quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Разбирает текст в формате DSL и возвращает модель теста.
 *
 * <p>Правила языка описаны в {@code shared/SPEC.md}. Парсер не бросает исключений
 * на некорректном вводе: он собирает ошибки с номерами строк и возвращает
 * частично заполненную модель.
 */
public final class DslParser {

    private static final String TITLE = "Title";
    private static final String HEADING = "H1";
    private static final String STYLE = "Style";

    private static final int SNIPPET_LIMIT = 40;

    public ParseResult parse(String source) {
        List<ParseError> errors = new ArrayList<>();

        String title = "";
        String heading = "";
        String style = Quiz.DEFAULT_STYLE;

        for (Line line : splitLines(source)) {
            String text = line.text();
            if (text.isEmpty()) {
                continue;
            }

            String value = directiveValue(text, TITLE);
            if (value != null) {
                if (value.isEmpty()) {
                    errors.add(new ParseError(line.number(), ErrorCode.EMPTY_DIRECTIVE_VALUE,
                            "у директивы Title нет значения"));
                } else {
                    title = value;
                }
                continue;
            }

            value = directiveValue(text, HEADING);
            if (value != null) {
                if (value.isEmpty()) {
                    errors.add(new ParseError(line.number(), ErrorCode.EMPTY_DIRECTIVE_VALUE,
                            "у директивы H1 нет значения"));
                } else {
                    heading = value;
                }
                continue;
            }

            value = directiveValue(text, STYLE);
            if (value != null) {
                if (value.isEmpty()) {
                    errors.add(new ParseError(line.number(), ErrorCode.EMPTY_DIRECTIVE_VALUE,
                            "у директивы Style нет значения"));
                } else {
                    style = value;
                }
                continue;
            }

            errors.add(new ParseError(line.number(), ErrorCode.UNKNOWN_DIRECTIVE,
                    "не удалось разобрать строку: " + snippet(text)));
        }

        Quiz quiz = new Quiz(title, heading, style, Collections.<Question>emptyList());
        return new ParseResult(quiz, errors);
    }

    /**
     * Возвращает значение директивы или {@code null}, если строка этой директивой не является.
     * После имени директивы обязателен пробел или конец строки, поэтому {@code Titles} не
     * считается директивой {@code Title}.
     */
    private static String directiveValue(String line, String directive) {
        if (!line.startsWith(directive)) {
            return null;
        }
        String rest = line.substring(directive.length());
        if (!rest.isEmpty() && !Character.isWhitespace(rest.charAt(0))) {
            return null;
        }
        return rest.trim();
    }

    private static List<Line> splitLines(String source) {
        List<Line> lines = new ArrayList<>();
        if (source == null) {
            return lines;
        }

        String text = source.replace("\r\n", "\n").replace('\r', '\n');
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
            text = text.substring(1);
        }

        String[] raw = text.split("\n", -1);
        for (int i = 0; i < raw.length; i++) {
            lines.add(new Line(i + 1, raw[i].trim()));
        }
        return lines;
    }

    private static String snippet(String text) {
        return text.length() <= SNIPPET_LIMIT ? text : text.substring(0, SNIPPET_LIMIT) + "…";
    }

    /** Строка исходного текста с номером. */
    private static final class Line {

        private final int number;
        private final String text;

        private Line(int number, String text) {
            this.number = number;
            this.text = text;
        }

        int number() {
            return number;
        }

        String text() {
            return text;
        }
    }
}
