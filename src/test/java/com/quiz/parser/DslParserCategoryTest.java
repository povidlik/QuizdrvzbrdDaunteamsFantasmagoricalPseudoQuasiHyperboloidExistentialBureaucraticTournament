package com.quiz.parser;

import com.quiz.model.Question;
import com.quiz.model.Quiz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DslParserCategoryTest {

    private final DslParser parser = new DslParser();

    @Test
    @DisplayName("Category задаёт категорию следующим за ней вопросам")
    void assignsCategoryToFollowingQuestions() {
        ParseResult result = parser.parse(
                "H1 Test\n"
                        + "Category HTML\n"
                        + "? Первый r\n"
                        + "- a +e\n"
                        + "? Второй t\n"
                        + "_ ответ\n"
                        + "Category CSS\n"
                        + "? Третий c\n"
                        + "- a +\n"
                        + "- b +e\n");

        assertTrue(result.isSuccess(), () -> result.errorReport());
        List<Question> questions = result.quiz().questions();
        assertEquals("HTML", questions.get(0).category());
        assertEquals("HTML", questions.get(1).category());
        assertEquals("CSS", questions.get(2).category());
    }

    @Test
    @DisplayName("Вопрос до первой Category остаётся без категории")
    void questionBeforeCategoryHasEmptyCategory() {
        ParseResult result = parser.parse(
                "? Первый r\n"
                        + "- a +e\n"
                        + "Category CSS\n"
                        + "? Второй r\n"
                        + "- a +e\n");

        assertTrue(result.isSuccess(), () -> result.errorReport());
        assertEquals("", result.quiz().questions().get(0).category());
        assertEquals("CSS", result.quiz().questions().get(1).category());
    }

    @Test
    @DisplayName("Пустая Category — ошибка с номером строки")
    void reportsEmptyCategoryValue() {
        ParseResult result = parser.parse(
                "H1 Test\n"
                        + "Category\n"
                        + "? Q r\n"
                        + "- a +e\n");

        assertFalse(result.isSuccess());
        assertEquals(1, result.errors().size());
        assertEquals(2, result.errors().get(0).line());
        assertEquals(ErrorCode.EMPTY_DIRECTIVE_VALUE, result.errors().get(0).code());
    }

    @Test
    @DisplayName("Quiz раскладывает вопросы по категориям в порядке появления")
    void quizGroupsQuestionsByCategory() {
        ParseResult result = parser.parse(
                "Category HTML\n"
                        + "? Первый r\n"
                        + "- a +e\n"
                        + "Category CSS\n"
                        + "? Второй r\n"
                        + "- a +e\n"
                        + "Category HTML\n"
                        + "? Третий r\n"
                        + "- a +e\n");

        assertTrue(result.isSuccess(), () -> result.errorReport());
        Map<String, List<Question>> categories = result.quiz().categories();

        assertEquals(Arrays.asList("HTML", "CSS"), new ArrayList<>(categories.keySet()));
        assertEquals(2, categories.get("HTML").size());
        assertEquals(1, categories.get("CSS").size());
        assertEquals("Третий", categories.get("HTML").get(1).text());
    }

    @Test
    @DisplayName("Вопросы без категории попадают в «Общие»")
    void questionsWithoutCategoryUseDefaultGroup() {
        ParseResult result = parser.parse(
                "? Первый r\n"
                        + "- a +e\n"
                        + "? Второй r\n"
                        + "- a +e\n");

        assertTrue(result.isSuccess(), () -> result.errorReport());
        Map<String, List<Question>> categories = result.quiz().categories();

        assertEquals(1, categories.size());
        assertEquals(2, categories.get(Quiz.DEFAULT_CATEGORY).size());
    }

    @Test
    @DisplayName("Category без пробела после имени — не директива")
    void requiresSeparatorAfterDirectiveName() {
        ParseResult result = parser.parse("CategoryHTML\n");

        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.UNKNOWN_DIRECTIVE, result.errors().get(0).code());
    }
}
