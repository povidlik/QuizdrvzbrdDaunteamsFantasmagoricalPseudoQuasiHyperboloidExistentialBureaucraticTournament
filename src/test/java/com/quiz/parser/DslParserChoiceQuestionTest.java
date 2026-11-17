package com.quiz.parser;

import com.quiz.model.AnswerType;
import com.quiz.model.Option;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DslParserChoiceQuestionTest {

    private final DslParser parser = new DslParser();

    @Test
    @DisplayName("Разбирает вопрос с одним правильным вариантом")
    void parsesSingleChoiceQuestion() {
        ParseResult result = parser.parse(lines(
                "? 2+2= r",
                "- 4 +",
                "- 5",
                "- 6 e"));

        assertTrue(result.isSuccess(), () -> result.errorReport());
        Quiz quiz = result.quiz();
        assertEquals(1, quiz.size());

        Question question = quiz.questions().get(0);
        assertEquals("2+2=", question.text());
        assertEquals(AnswerType.SINGLE, question.type());
        assertEquals(3, question.options().size());
        assertEquals(Collections.singletonList(new Option("4", true)), question.correctOptions());
    }

    @Test
    @DisplayName("Разбирает вопрос с несколькими правильными вариантами")
    void parsesMultipleChoiceQuestion() {
        ParseResult result = parser.parse(lines(
                "? Second question c",
                "- A",
                "- B +",
                "- C +e"));

        assertTrue(result.isSuccess(), () -> result.errorReport());
        Question question = result.quiz().questions().get(0);
        assertEquals(AnswerType.MULTIPLE, question.type());
        assertEquals(Arrays.asList(new Option("B", true), new Option("C", true)), question.correctOptions());
    }

    @Test
    @DisplayName("Несколько вопросов подряд не смешивают варианты")
    void parsesSeveralQuestions() {
        ParseResult result = parser.parse(lines(
                "? First r",
                "- a +",
                "- b e",
                "",
                "? Second c",
                "- c +",
                "- d +e"));

        assertTrue(result.isSuccess(), () -> result.errorReport());
        assertEquals(2, result.quiz().size());
        assertEquals(2, result.quiz().questions().get(0).options().size());
        assertEquals(2, result.quiz().questions().get(1).options().size());
    }

    @Test
    @DisplayName("Метаданные и вопросы разбираются вместе")
    void parsesMetadataAndQuestions() {
        ParseResult result = parser.parse(lines(
                "Title My title",
                "H1 Super Test",
                "Style dark",
                "? 2+2= r",
                "- 4 +",
                "- 5 e"));

        assertTrue(result.isSuccess(), () -> result.errorReport());
        assertEquals("My title", result.quiz().title());
        assertEquals("Super Test", result.quiz().heading());
        assertEquals("dark", result.quiz().style());
        assertEquals(1, result.quiz().size());
    }

    @Test
    @DisplayName("Без правильного варианта вопрос не попадает в модель")
    void reportsNoCorrectOption() {
        ParseResult result = parser.parse(lines(
                "? Question r",
                "- a",
                "- b e"));

        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.NO_CORRECT_OPTION, result.errors().get(0).code());
        assertEquals(1, result.errors().get(0).line());
        assertTrue(result.quiz().isEmpty());
    }

    @Test
    @DisplayName("Два правильных варианта у вопроса r — ошибка")
    void reportsTooManyCorrectOptions() {
        ParseResult result = parser.parse(lines(
                "? Question r",
                "- a +",
                "- b +e"));

        assertEquals(ErrorCode.TOO_MANY_CORRECT_OPTIONS, result.errors().get(0).code());
        assertTrue(result.quiz().isEmpty());
    }

    @Test
    @DisplayName("Блок без маркера e — ошибка BLOCK_NOT_CLOSED")
    void reportsBlockNotClosedAtEndOfInput() {
        ParseResult result = parser.parse(lines(
                "? Question r",
                "- a +"));

        assertEquals(ErrorCode.BLOCK_NOT_CLOSED, result.errors().get(0).code());
        assertEquals(1, result.errors().get(0).line());
        assertTrue(result.quiz().isEmpty());
    }

    @Test
    @DisplayName("Незакрытый блок обнаруживается перед следующим вопросом")
    void reportsBlockNotClosedWhenNextQuestionStarts() {
        ParseResult result = parser.parse(lines(
                "? Broken r",
                "- a +",
                "? Working r",
                "- b +e"));

        assertEquals(1, result.errors().size());
        assertEquals(ErrorCode.BLOCK_NOT_CLOSED, result.errors().get(0).code());
        assertEquals(1, result.errors().get(0).line());
        assertEquals(1, result.quiz().size());
        assertEquals("Working", result.quiz().questions().get(0).text());
    }

    @Test
    @DisplayName("Вопрос без вариантов — ошибка NO_OPTIONS")
    void reportsNoOptions() {
        ParseResult result = parser.parse(lines(
                "? Question r",
                "e"));

        assertEquals(ErrorCode.NO_OPTIONS, result.errors().get(0).code());
        assertTrue(result.quiz().isEmpty());
    }

    @Test
    @DisplayName("Пустой вариант ответа — ошибка EMPTY_OPTION")
    void reportsEmptyOption() {
        ParseResult result = parser.parse(lines(
                "? Question r",
                "-",
                "- a +e"));

        assertEquals(1, result.errors().size());
        assertEquals(ErrorCode.EMPTY_OPTION, result.errors().get(0).code());
        assertEquals(2, result.errors().get(0).line());
        assertEquals(1, result.quiz().size());
        assertEquals(1, result.quiz().questions().get(0).options().size());
    }

    @Test
    @DisplayName("Вариант без вопроса — ошибка OPTION_OUTSIDE_OPTIONS")
    void reportsOptionOutsideQuestion() {
        ParseResult result = parser.parse("- a +");

        assertEquals(ErrorCode.OPTION_OUTSIDE_OPTIONS, result.errors().get(0).code());
        assertEquals(1, result.errors().get(0).line());
    }

    @Test
    @DisplayName("? без текста — ошибка EMPTY_QUESTION, неизвестный тип — EXPECTED_ANSWER_TYPE")
    void reportsInvalidQuestionLine() {
        ParseResult emptyQuestion = parser.parse("?");
        assertEquals(ErrorCode.EMPTY_QUESTION, emptyQuestion.errors().get(0).code());

        ParseResult badType = parser.parse("? Question x");
        assertEquals(ErrorCode.EXPECTED_ANSWER_TYPE, badType.errors().get(0).code());
    }

    private static String lines(String... lines) {
        return String.join("\n", lines);
    }
}
