package com.quiz.parser;

/**
 * Коды ошибок разбора DSL. Полный перечень — в {@code shared/SPEC.md}.
 */
public enum ErrorCode {

    UNKNOWN_DIRECTIVE("неизвестная строка"),
    EMPTY_DIRECTIVE_VALUE("у директивы нет значения"),
    EMPTY_QUESTION("пустой текст вопроса"),
    EXPECTED_ANSWER_TYPE("не указан тип ответа r/c/t"),
    OPTION_OUTSIDE_OPTIONS("вариант ответа вне блока вариантов"),
    ANSWER_OUTSIDE_QUESTION("ответ вне вопроса с текстовым вводом"),
    EMPTY_OPTION("пустой вариант ответа"),
    NO_OPTIONS("у вопроса нет вариантов ответа"),
    NO_CORRECT_OPTION("нет правильного варианта"),
    TOO_MANY_CORRECT_OPTIONS("больше одного правильного варианта"),
    MISSING_TEXT_ANSWER("нет эталонного текстового ответа"),
    BLOCK_NOT_CLOSED("блок вариантов не закрыт маркером e");

    private final String description;

    ErrorCode(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
