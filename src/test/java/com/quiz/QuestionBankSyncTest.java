package com.quiz;

import com.quiz.export.BankExporter;
import com.quiz.parser.DslParser;
import com.quiz.parser.ParseResult;
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
 * Банк вопросов страницы викторины должен совпадать с выгрузкой из DSL,
 * иначе две части проекта снова начнут расходиться.
 */
class QuestionBankSyncTest {

    private final Path content = Paths.get("shared", "content", "web.dsl");
    private final Path bank = Paths.get("question-bank.js");

    @Test
    @DisplayName("shared/content/web.dsl разбирается без ошибок")
    void contentParsesWithoutErrors() throws IOException {
        ParseResult result = new DslParser().parse(read(content));

        assertTrue(result.isSuccess(), () -> content + "\n" + result.errorReport());
        assertEquals(9, result.quiz().size());
        assertEquals(3, result.quiz().categories().size());
    }

    @Test
    @DisplayName("question-bank.js совпадает с выгрузкой из shared/content/web.dsl")
    void questionBankMatchesDsl() throws IOException {
        ParseResult result = new DslParser().parse(read(content));
        assertTrue(result.isSuccess(), () -> result.errorReport());

        String expected = new BankExporter().generate(result.quiz());
        String actual = read(bank);

        assertEquals(normalize(expected), normalize(actual),
                "question-bank.js устарел: пересоберите его командой --bank");
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static String normalize(String text) {
        return text.replace("\r\n", "\n").trim();
    }
}
