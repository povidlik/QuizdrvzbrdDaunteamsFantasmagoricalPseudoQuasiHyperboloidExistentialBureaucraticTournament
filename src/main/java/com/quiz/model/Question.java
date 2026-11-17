package com.quiz.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Один вопрос теста.
 *
 * <p>Для вопросов с выбором заполняется {@link #options()}, для текстового —
 * {@link #answer()}.
 *
 * @param text    текст вопроса
 * @param type    тип ответа
 * @param options варианты ответа (пустой список для {@link AnswerType#TEXT})
 * @param answer  эталонный ответ (только для {@link AnswerType#TEXT}, иначе {@code null})
 */
public record Question(String text, AnswerType type, List<Option> options, String answer) {

    public Question {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(type, "type");
        options = options == null ? List.of() : List.copyOf(options);
        if (type != AnswerType.TEXT && answer != null) {
            throw new IllegalArgumentException("Ответ в виде текста допустим только для типа " + AnswerType.TEXT.code());
        }
    }

    public static Question singleChoice(String text, List<Option> options) {
        return new Question(text, AnswerType.SINGLE, options, null);
    }

    public static Question multipleChoice(String text, List<Option> options) {
        return new Question(text, AnswerType.MULTIPLE, options, null);
    }

    public static Question textAnswer(String text, String answer) {
        return new Question(text, AnswerType.TEXT, List.of(), answer);
    }

    public boolean hasOptions() {
        return type == AnswerType.SINGLE || type == AnswerType.MULTIPLE;
    }

    public List<Option> correctOptions() {
        return options.stream()
                .filter(Option::correct)
                .collect(Collectors.toUnmodifiableList());
    }
}
