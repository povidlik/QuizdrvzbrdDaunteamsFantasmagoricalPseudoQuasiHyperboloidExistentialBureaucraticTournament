package com.quiz.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Разобранный тест: метаданные и список вопросов в порядке следования в DSL.
 */
public final class Quiz {

    public static final String DEFAULT_STYLE = "default";

    /** Категория для вопросов, у которых директива {@code Category} не задана. */
    public static final String DEFAULT_CATEGORY = "Общие";

    private final String title;
    private final String heading;
    private final String style;
    private final List<Question> questions;

    public Quiz(String title, String heading, String style, List<Question> questions) {
        this.title = title == null ? "" : title;
        this.heading = heading == null ? "" : heading;
        this.style = style == null || style.trim().isEmpty() ? DEFAULT_STYLE : style;
        this.questions = questions == null
                ? Collections.<Question>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(questions));
    }

    public static Quiz empty() {
        return new Quiz("", "", DEFAULT_STYLE, Collections.<Question>emptyList());
    }

    /** Текст вкладки браузера (директива {@code Title}). */
    public String title() {
        return title;
    }

    /** Заголовок теста (директива {@code H1}). */
    public String heading() {
        return heading;
    }

    /** Имя CSS-темы (директива {@code Style}). */
    public String style() {
        return style;
    }

    public List<Question> questions() {
        return questions;
    }

    /**
     * Вопросы, разложенные по категориям в порядке первого появления.
     * Вопросы без категории попадают в {@link #DEFAULT_CATEGORY}.
     */
    public Map<String, List<Question>> categories() {
        Map<String, List<Question>> grouped = new LinkedHashMap<>();
        for (Question question : questions) {
            String category = question.category().isEmpty() ? DEFAULT_CATEGORY : question.category();
            List<Question> bucket = grouped.get(category);
            if (bucket == null) {
                bucket = new ArrayList<>();
                grouped.put(category, bucket);
            }
            bucket.add(question);
        }
        for (Map.Entry<String, List<Question>> entry : grouped.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(grouped);
    }

    public boolean isEmpty() {
        return questions.isEmpty();
    }

    public int size() {
        return questions.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Quiz)) {
            return false;
        }
        Quiz other = (Quiz) o;
        return title.equals(other.title)
                && heading.equals(other.heading)
                && style.equals(other.style)
                && questions.equals(other.questions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, heading, style, questions);
    }

    @Override
    public String toString() {
        return "Quiz{title='" + title + "', heading='" + heading + "', style='" + style
                + "', questions=" + questions + '}';
    }
}
