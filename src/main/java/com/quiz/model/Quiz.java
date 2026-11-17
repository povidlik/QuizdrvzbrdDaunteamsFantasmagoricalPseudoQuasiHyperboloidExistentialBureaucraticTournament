package com.quiz.model;

import java.util.List;
import java.util.Objects;

/**
 * Разобранный тест.
 *
 * @param title     текст вкладки браузера (директива {@code Title})
 * @param heading   заголовок теста (директива {@code H1})
 * @param style     имя CSS-темы (директива {@code Style})
 * @param questions вопросы теста в порядке следования в DSL
 */
public record Quiz(String title, String heading, String style, List<Question> questions) {

    public static final String DEFAULT_STYLE = "default";

    public Quiz {
        title = title == null ? "" : title;
        heading = heading == null ? "" : heading;
        style = style == null || style.isBlank() ? DEFAULT_STYLE : style;
        questions = questions == null ? List.of() : List.copyOf(questions);
    }

    public static Quiz empty() {
        return new Quiz("", "", DEFAULT_STYLE, List.of());
    }

    public boolean isEmpty() {
        return questions.isEmpty();
    }

    public int size() {
        return questions.size();
    }
}
