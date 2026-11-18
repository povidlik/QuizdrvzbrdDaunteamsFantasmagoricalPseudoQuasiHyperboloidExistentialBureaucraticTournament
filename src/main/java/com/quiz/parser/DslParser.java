package com.quiz.parser;

import com.quiz.model.AnswerType;
import com.quiz.model.Option;
import com.quiz.model.Question;
import com.quiz.model.Quiz;

import java.util.ArrayList;
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
    private static final String CATEGORY = "Category";

    private static final String QUESTION_PREFIX = "?";
    private static final String OPTION_PREFIX = "-";
    private static final String ANSWER_PREFIX = "_";
    private static final String BLOCK_END = "e";

    private static final int SNIPPET_LIMIT = 40;

    public ParseResult parse(String source) {
        List<ParseError> errors = new ArrayList<>();
        List<Question> questions = new ArrayList<>();

        String title = "";
        String heading = "";
        String style = Quiz.DEFAULT_STYLE;
        String category = "";

        QuestionBuilder current = null;

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

            value = directiveValue(text, CATEGORY);
            if (value != null) {
                if (value.isEmpty()) {
                    errors.add(new ParseError(line.number(), ErrorCode.EMPTY_DIRECTIVE_VALUE,
                            "у директивы Category нет значения"));
                } else {
                    category = value;
                }
                continue;
            }

            if (text.startsWith(QUESTION_PREFIX)) {
                if (current != null) {
                    errors.add(unanswered(current));
                }
                current = startQuestion(line.number(), text, category, errors);
                continue;
            }

            if (current != null && current.type == AnswerType.TEXT) {
                if (text.startsWith(ANSWER_PREFIX)) {
                    String answer = text.substring(ANSWER_PREFIX.length()).trim();
                    if (answer.isEmpty()) {
                        errors.add(new ParseError(line.number(), ErrorCode.MISSING_TEXT_ANSWER,
                                "после _ нет эталонного ответа"));
                    } else {
                        questions.add(Question.textAnswer(current.text, answer, current.category));
                    }
                    current = null;
                } else if (text.startsWith(OPTION_PREFIX)) {
                    errors.add(new ParseError(line.number(), ErrorCode.OPTION_OUTSIDE_OPTIONS,
                            "вариант ответа вне вопроса с выбором"));
                } else {
                    errors.add(unknownLine(line.number(), text));
                }
                continue;
            }

            if (text.equals(BLOCK_END)) {
                if (current == null) {
                    errors.add(unknownLine(line.number(), text));
                } else {
                    closeBlock(current, questions, errors);
                    current = null;
                }
                continue;
            }

            if (text.startsWith(OPTION_PREFIX)) {
                if (current == null) {
                    errors.add(new ParseError(line.number(), ErrorCode.OPTION_OUTSIDE_OPTIONS,
                            "вариант ответа вне блока вариантов"));
                } else if (parseOption(line.number(), text, current, errors)) {
                    closeBlock(current, questions, errors);
                    current = null;
                }
                continue;
            }

            if (text.startsWith(ANSWER_PREFIX)) {
                errors.add(new ParseError(line.number(), ErrorCode.ANSWER_OUTSIDE_QUESTION,
                        "строка _ вне вопроса типа t"));
                continue;
            }

            errors.add(unknownLine(line.number(), text));
        }

        if (current != null) {
            errors.add(unanswered(current));
        }

        Quiz quiz = new Quiz(title, heading, style, questions);
        return new ParseResult(quiz, errors);
    }

    private static QuestionBuilder startQuestion(int lineNumber, String text, String category, List<ParseError> errors) {
        String body = text.substring(QUESTION_PREFIX.length()).trim();
        if (body.isEmpty()) {
            errors.add(new ParseError(lineNumber, ErrorCode.EMPTY_QUESTION, "после ? нет текста вопроса"));
            return null;
        }

        int space = body.lastIndexOf(' ');
        if (space <= 0) {
            errors.add(new ParseError(lineNumber, ErrorCode.EMPTY_QUESTION, "после ? нет текста вопроса"));
            return null;
        }

        String code = body.substring(space + 1);
        AnswerType type;
        try {
            type = AnswerType.fromCode(code);
        } catch (IllegalArgumentException e) {
            errors.add(new ParseError(lineNumber, ErrorCode.EXPECTED_ANSWER_TYPE,
                    "неизвестный тип ответа: " + code));
            return null;
        }

        return new QuestionBuilder(lineNumber, body.substring(0, space).trim(), type, category);
    }

    /**
     * Разбирает строку варианта ответа и сообщает, закрывает ли она блок.
     */
    private static boolean parseOption(int lineNumber, String text, QuestionBuilder builder, List<ParseError> errors) {
        String body = text.substring(OPTION_PREFIX.length()).trim();
        boolean closed = false;
        boolean correct = false;

        if (body.equals(BLOCK_END)) {
            closed = true;
            body = "";
        } else if (body.endsWith(" " + BLOCK_END)) {
            closed = true;
            body = body.substring(0, body.length() - 2).trim();
        } else if (body.endsWith("+" + BLOCK_END)) {
            closed = true;
            correct = true;
            body = body.substring(0, body.length() - 2).trim();
        }

        if (body.endsWith(" +")) {
            correct = true;
            body = body.substring(0, body.length() - 2).trim();
        } else if (body.equals("+")) {
            correct = true;
            body = "";
        }

        if (body.isEmpty()) {
            if (!closed) {
                errors.add(new ParseError(lineNumber, ErrorCode.EMPTY_OPTION, "пустой вариант ответа"));
            }
            return closed;
        }

        builder.options.add(new Option(body, correct));
        return closed;
    }

    private static void closeBlock(QuestionBuilder builder, List<Question> questions, List<ParseError> errors) {
        if (builder.options.isEmpty()) {
            errors.add(new ParseError(builder.line, ErrorCode.NO_OPTIONS, "у вопроса нет вариантов ответа"));
            return;
        }

        int correctCount = 0;
        for (Option option : builder.options) {
            if (option.correct()) {
                correctCount++;
            }
        }

        if (correctCount == 0) {
            errors.add(new ParseError(builder.line, ErrorCode.NO_CORRECT_OPTION,
                    "у вопроса нет правильного варианта"));
            return;
        }

        if (builder.type == AnswerType.SINGLE && correctCount > 1) {
            errors.add(new ParseError(builder.line, ErrorCode.TOO_MANY_CORRECT_OPTIONS,
                    "у вопроса с одним ответом " + correctCount + " правильных варианта"));
            return;
        }

        questions.add(new Question(builder.text, builder.type, builder.options, null, builder.category));
    }

    private static ParseError unanswered(QuestionBuilder builder) {
        if (builder.type == AnswerType.TEXT) {
            return new ParseError(builder.line, ErrorCode.MISSING_TEXT_ANSWER,
                    "у вопроса типа t нет строки с ответом _");
        }
        return new ParseError(builder.line, ErrorCode.BLOCK_NOT_CLOSED, "блок вариантов не закрыт маркером e");
    }

    private static ParseError unknownLine(int lineNumber, String text) {
        return new ParseError(lineNumber, ErrorCode.UNKNOWN_DIRECTIVE,
                "не удалось разобрать строку: " + snippet(text));
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

    /** Накопитель данных о ещё не закрытом вопросе. */
    private static final class QuestionBuilder {

        private final int line;
        private final String text;
        private final AnswerType type;
        private final String category;
        private final List<Option> options = new ArrayList<>();

        private QuestionBuilder(int line, String text, AnswerType type, String category) {
            this.line = line;
            this.text = text;
            this.type = type;
            this.category = category;
        }
    }
}
