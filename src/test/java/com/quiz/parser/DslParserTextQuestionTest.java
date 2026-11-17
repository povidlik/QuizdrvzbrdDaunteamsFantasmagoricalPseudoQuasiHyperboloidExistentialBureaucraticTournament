package com.quiz.parser;

import com.quiz.model.AnswerType;
import com.quiz.model.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DslParserTextQuestionTest {

    private final DslParser parser = new DslParser();

    @Test
    @DisplayName("Разбирает вопрос с текстовым ответом")
    void parsesTextQuestion() {
        ParseResult result = parser.parse(lines(
                "? Text question t",
                "_ My answer"));

        assertTrue(result.isSuccess(), () -> result.errorReport());
        assertEquals(1, result.quiz().size());

        Question question = result.quiz().questions().get(0);
        assertEquals("Text question", question.text());
        assertEquals(AnswerType.TEXT, question.type());
        assertEquals("My answer", question.answer());
        assertTrue(question.options().isEmpty());
    }

    @Test
    @DisplayName("Пробелы вокруг ответа обрезаются")
    void trimsAnswer() {
        ParseResult result = parser.parse(lines(
                "? Question t",
                "_    ответ с пробелами   "));

        assertTrue(result.isSuccess(), () -> result.errorReport());
        assertEquals("ответ с пробелами", result.quiz().questions().get(0).answer());
    }

    @Test
    @DisplayName("Ответ может быть внутри общего теста вместе с вопросами выбора")
    void parsesMixedQuestions() {
        ParseResult result = parser.parse(lines(
                "? First r",
                "- a +",
                "- b e",
                "? Text t",
                "_ answer",
                "? Third c",
                "- c +",
                "- d +e"));

        assertTrue(result.isSuccess(), () -> result.errorReport());
        assertEquals(3, result.quiz().size());
        assertEquals(AnswerType.SINGLE, result.quiz().questions().get(0).type());
        assertEquals(AnswerType.TEXT, result.quiz().questions().get(1).type());
        assertEquals(AnswerType.MULTIPLE, result.quiz().questions().get(2).type());
    }

    @Test
    @DisplayName("Вопрос t без строки _ — ошибка MISSING_TEXT_ANSWER")
    void reportsMissingAnswerAtEndOfInput() {
        ParseResult result = parser.parse("? Question t");

        assertEquals(ErrorCode.MISSING_TEXT_ANSWER, result.errors().get(0).code());
        assertEquals(1, result.errors().get(0).line());
        assertTrue(result.quiz().isEmpty());
    }

    @Test
    @DisplayName("Пустая строка _ — ошибка MISSING_TEXT_ANSWER")
    void reportsEmptyAnswer() {
        ParseResult result = parser.parse(lines(
                "? Question t",
                "_"));

        assertEquals(ErrorCode.MISSING_TEXT_ANSWER, result.errors().get(0).code());
        assertEquals(2, result.errors().get(0).line());
        assertTrue(result.quiz().isEmpty());
    }

    @Test
    @DisplayName("Незакрытый вопрос t обнаруживается перед следующим вопросом")
    void reportsMissingAnswerWhenNextQuestionStarts() {
        ParseResult result = parser.parse(lines(
                "? Broken t",
                "? Working t",
                "_ answer"));

        assertEquals(1, result.errors().size());
        assertEquals(ErrorCode.MISSING_TEXT_ANSWER, result.errors().get(0).code());
        assertEquals(1, result.errors().get(0).line());
        assertEquals(1, result.quiz().size());
        assertEquals("Working", result.quiz().questions().get(0).text());
    }

    @Test
    @DisplayName("Строка _ без вопроса — ошибка ANSWER_OUTSIDE_QUESTION")
    void reportsAnswerOutsideQuestion() {
        ParseResult result = parser.parse("_ answer");

        assertEquals(ErrorCode.ANSWER_OUTSIDE_QUESTION, result.errors().get(0).code());
        assertEquals(1, result.errors().get(0).line());
    }

    @Test
    @DisplayName("Вариант ответа внутри вопроса t — ошибка OPTION_OUTSIDE_OPTIONS")
    void reportsOptionInsideTextQuestion() {
        ParseResult result = parser.parse(lines(
                "? Question t",
                "- a +",
                "_ answer"));

        assertEquals(ErrorCode.OPTION_OUTSIDE_OPTIONS, result.errors().get(0).code());
        assertEquals(2, result.errors().get(0).line());
        assertEquals(1, result.quiz().size());
    }

    private static String lines(String... lines) {
        return String.join("\n", lines);
    }
}
