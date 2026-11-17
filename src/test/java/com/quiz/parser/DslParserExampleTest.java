package com.quiz.parser;

import com.quiz.model.AnswerType;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверяет, что парсер полностью разбирает листинг из задания.
 * Исходник лежит в {@code shared/examples/basic.dsl}.
 */
class DslParserExampleTest {

    private static final Path EXAMPLE = Paths.get("shared", "examples", "basic.dsl");

    @Test
    @DisplayName("Пример из задания разбирается без ошибок и даёт 3 вопроса")
    void parsesListingFromAssignment() throws IOException {
        String source = new String(Files.readAllBytes(EXAMPLE), StandardCharsets.UTF_8);

        ParseResult result = new DslParser().parse(source);

        assertTrue(result.isSuccess(), () -> result.errorReport());

        Quiz quiz = result.quiz();
        assertEquals("My title", quiz.title());
        assertEquals("Super Test", quiz.heading());
        assertEquals(Quiz.DEFAULT_STYLE, quiz.style());
        assertEquals(3, quiz.size());
    }

    @Test
    @DisplayName("Первый вопрос примера — выбор одного варианта, правильный ответ 4")
    void parsesFirstQuestion() throws IOException {
        Question question = parseExample().questions().get(0);

        assertEquals("2+2=", question.text());
        assertEquals(AnswerType.SINGLE, question.type());
        assertEquals(3, question.options().size());
        assertEquals(1, question.correctOptions().size());
        assertEquals("4", question.correctOptions().get(0).text());
    }

    @Test
    @DisplayName("Второй вопрос примера — множественный выбор с ответами B и C")
    void parsesSecondQuestion() throws IOException {
        Question question = parseExample().questions().get(1);

        assertEquals("Second question", question.text());
        assertEquals(AnswerType.MULTIPLE, question.type());
        assertEquals(3, question.options().size());
        assertEquals(2, question.correctOptions().size());
        assertEquals("B", question.correctOptions().get(0).text());
        assertEquals("C", question.correctOptions().get(1).text());
    }

    @Test
    @DisplayName("Третий вопрос примера — текстовый ответ My answer")
    void parsesThirdQuestion() throws IOException {
        Question question = parseExample().questions().get(2);

        assertEquals("Text question", question.text());
        assertEquals(AnswerType.TEXT, question.type());
        assertEquals("My answer", question.answer());
        assertTrue(question.options().isEmpty());
    }

    private static Quiz parseExample() throws IOException {
        String source = new String(Files.readAllBytes(EXAMPLE), StandardCharsets.UTF_8);
        return new DslParser().parse(source).quiz();
    }
}
