package com.quiz.parser;

import com.quiz.model.Quiz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DslParserMetadataTest {

    private final DslParser parser = new DslParser();

    @Test
    @DisplayName("Разбирает Title, H1 и Style")
    void parsesMetadataDirectives() {
        ParseResult result = parser.parse("""
                Title My title
                H1 Super Test
                Style dark
                """);

        assertTrue(result.isSuccess(), () -> result.errorReport());
        Quiz quiz = result.quiz();
        assertEquals("My title", quiz.title());
        assertEquals("Super Test", quiz.heading());
        assertEquals("dark", quiz.style());
        assertTrue(quiz.isEmpty());
    }

    @Test
    @DisplayName("Без Style подставляется тема default")
    void usesDefaultStyleWhenMissing() {
        Quiz quiz = parser.parse("H1 Super Test").quiz();

        assertEquals(Quiz.DEFAULT_STYLE, quiz.style());
    }

    @Test
    @DisplayName("Понимает CRLF, BOM и пустые строки")
    void handlesCrlfBomAndBlankLines() {
        String source = "\uFEFFTitle My title\r\n\r\nH1 Super Test\r\n";

        ParseResult result = parser.parse(source);

        assertTrue(result.isSuccess(), () -> result.errorReport());
        assertEquals("My title", result.quiz().title());
        assertEquals("Super Test", result.quiz().heading());
    }

    @Test
    @DisplayName("Неизвестная строка попадает в ошибки с номером строки")
    void reportsUnknownDirectiveWithLineNumber() {
        ParseResult result = parser.parse("""
                Title My title

                что-то непонятное
                """);

        assertFalse(result.isSuccess());
        assertEquals(1, result.errors().size());
        ParseError error = result.errors().get(0);
        assertEquals(3, error.line());
        assertEquals(ErrorCode.UNKNOWN_DIRECTIVE, error.code());
    }

    @Test
    @DisplayName("Директива без значения — ошибка EMPTY_DIRECTIVE_VALUE")
    void reportsEmptyDirectiveValue() {
        ParseResult result = parser.parse("H1    ");

        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.EMPTY_DIRECTIVE_VALUE, result.errors().get(0).code());
        assertEquals(1, result.errors().get(0).line());
        assertEquals("", result.quiz().heading());
    }

    @Test
    @DisplayName("Titles не считается директивой Title")
    void requiresSeparatorAfterDirectiveName() {
        ParseResult result = parser.parse("Titles My title");

        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.UNKNOWN_DIRECTIVE, result.errors().get(0).code());
        assertEquals("", result.quiz().title());
    }

    @Test
    @DisplayName("Пустой ввод даёт пустую модель без ошибок")
    void emptyInputProducesEmptyQuiz() {
        ParseResult result = parser.parse("");

        assertTrue(result.isSuccess());
        assertEquals("", result.quiz().title());
        assertEquals(Quiz.DEFAULT_STYLE, result.quiz().style());
        assertTrue(result.quiz().isEmpty());
    }
}
