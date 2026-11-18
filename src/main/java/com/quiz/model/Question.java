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
 * {@link #answer()}. {@link #category()} используется для группировки вопросов
 * в банке.
 */
public final class Question {

    private final String text;
    private final AnswerType type;
    private final List<Option> options;
    private final String answer;
    private final String category;

    public Question(String text, AnswerType type, List<Option> options, String answer) {
        this(text, type, options, answer, "");
    }

    /**
     * @param text     текст вопроса
     * @param type     тип ответа
     * @param options  варианты ответа (пустой список для {@link AnswerType#TEXT})
     * @param answer   эталонный ответ (только для {@link AnswerType#TEXT}, иначе {@code null})
     * @param category категория вопроса (директива {@code Category})
     */
    public Question(String text, AnswerType type, List<Option> options, String answer, String category) {
        this.text = Objects.requireNonNull(text, "text");
        this.type = Objects.requireNonNull(type, "type");
        this.options = options == null
                ? Collections.<Option>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(options));
        if (type != AnswerType.TEXT && answer != null) {
            throw new IllegalArgumentException("Ответ в виде текста допустим только для типа " + AnswerType.TEXT.code());
        }
        this.answer = answer;
        this.category = category == null ? "" : category;
    }

    public static Question singleChoice(String text, List<Option> options) {
        return singleChoice(text, options, "");
    }

    public static Question singleChoice(String text, List<Option> options, String category) {
        return new Question(text, AnswerType.SINGLE, options, null, category);
    }

    public static Question multipleChoice(String text, List<Option> options) {
        return multipleChoice(text, options, "");
    }

    public static Question multipleChoice(String text, List<Option> options, String category) {
        return new Question(text, AnswerType.MULTIPLE, options, null, category);
    }

    public static Question textAnswer(String text, String answer) {
        return textAnswer(text, answer, "");
    }

    public static Question textAnswer(String text, String answer, String category) {
        return new Question(text, AnswerType.TEXT, Collections.<Option>emptyList(), answer, category);
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

    public String category() {
        return category;
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
                && Objects.equals(answer, other.answer)
                && category.equals(other.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, type, options, answer, category);
    }

    @Override
    public String toString() {
        return "Question{text='" + text + "', type=" + type + ", category='" + category
                + "', options=" + options + ", answer='" + answer + "'}";
    }
}
