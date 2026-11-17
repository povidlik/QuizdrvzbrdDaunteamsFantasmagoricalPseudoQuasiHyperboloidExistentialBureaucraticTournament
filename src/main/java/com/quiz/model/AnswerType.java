package com.quiz.model;

/**
 * Тип ответа на вопрос. Код соответствует символу в DSL.
 */
public enum AnswerType {

    /** Один правильный вариант (радиокнопки). */
    SINGLE("r"),

    /** Несколько правильных вариантов (чекбоксы). */
    MULTIPLE("c"),

    /** Текстовый ввод. */
    TEXT("t");

    private final String code;

    AnswerType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    /**
     * @throws IllegalArgumentException если код неизвестен
     */
    public static AnswerType fromCode(String code) {
        for (AnswerType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип ответа: " + code);
    }
}
