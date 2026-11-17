package com.quiz.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Один вопрос теста.
 *
 * <p>Для вопросов с выбором заполняется {@link #options()}, для текстового —
 * {@link #answer()}.
 */
public final class Question {

    private final String text;
    private final AnswerType type;
    private final List<Option> options;
    private final String answer;

    /**
     * @param text    текст вопроса
     * @param type    тип ответа
     * @param options варианты ответа (пустой список для {@link AnswerType#TEXT})
     * @param answer  эталонный ответ (только для {@link AnswerType#TEXT}, иначе {@code null})
     */
    public Question(String text, AnswerType type, List<Option> options, String answer) {
        this.text = Objects.requireNonNull(text, "text");
        this.type = Objects.requireNonNull(type, "type");
        this.options = options == null
                ? Collections.<Option>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(options));
        if (type != AnswerType.TEXT && answer != null) {
            throw new IllegalArgumentException("Ответ в виде текста допустим только для типа " + AnswerType.TEXT.code());
        }
        this.answer = answer;
    }

    public static Question singleChoice(String text, List<Option> options) {
        return new Question(text, AnswerType.SINGLE, options, null);
    }

    public static Question multipleChoice(String text, List<Option> options) {
        return new Question(text, AnswerType.MULTIPLE, options, null);
    }

    public static Question textAnswer(String text, String answer) {
        return new Question(text, AnswerType.TEXT, Collections.<Option>emptyList(), answer);
    }

    public String text() {
        return text;
    }

    public AnswerType type() {
        return type;
    }

    public List<Option> options() {
        return options;
    }

    public String answer() {
        return answer;
    }

    public boolean hasOptions() {
        return type == AnswerType.SINGLE || type == AnswerType.MULTIPLE;
    }

    public List<Option> correctOptions() {
        return options.stream()
                .filter(Option::correct)
                .collect(Collectors.collectingAndThen(Collectors.<Option>toList(), Collections::unmodifiableList));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Question)) {
            return false;
        }
        Question other = (Question) o;
        return text.equals(other.text)
                && type == other.type
                && options.equals(other.options)
                && Objects.equals(answer, other.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, type, options, answer);
    }

    @Override
    public String toString() {
        return "Question{text='" + text + "', type=" + type
                + ", options=" + options + ", answer='" + answer + "'}";
    }
}
