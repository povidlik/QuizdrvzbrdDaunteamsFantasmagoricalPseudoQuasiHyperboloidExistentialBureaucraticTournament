package com.quiz.cli;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @TempDir
    Path dir;

    @Test
    @DisplayName("Превращает DSL-файл в HTML-файл")
    void convertsDslToHtml() throws Exception {
        Path dsl = dir.resolve("test.dsl");
        Path html = dir.resolve("test.html");
        Files.write(dsl, "Title T\nH1 H\n? Q r\n- a +e\n".getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code = Main.run(new String[]{dsl.toString(), html.toString()},
                stream(out), stream(new ByteArrayOutputStream()));

        assertEquals(Main.OK, code);
        assertTrue(Files.exists(html));

        String generated = new String(Files.readAllBytes(html), StandardCharsets.UTF_8);
        assertTrue(generated.contains("<h1 class=\"quiz-title\">H</h1>"));
        assertTrue(generated.contains("type=\"radio\""));
        assertTrue(out.toString("UTF-8").contains("1 вопросов"));
    }

    @Test
    @DisplayName("При ошибках DSL возвращает код 2 и печатает номера строк")
    void reportsDslErrors() throws Exception {
        Path dsl = dir.resolve("broken.dsl");
        Path html = dir.resolve("broken.html");
        Files.write(dsl, "Title T\nнепонятная строка\n".getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int code = Main.run(new String[]{dsl.toString(), html.toString()},
                stream(new ByteArrayOutputStream()), stream(err));

        assertEquals(Main.INVALID_DSL, code);
        assertFalse(Files.exists(html), "при ошибках файл создавать не нужно");
        assertTrue(err.toString("UTF-8").contains("Строка 2"));
        assertTrue(err.toString("UTF-8").contains("UNKNOWN_DIRECTIVE"));
    }

    @Test
    @DisplayName("Без аргументов печатает подсказку и возвращает код 1")
    void reportsUsage() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        int code = Main.run(new String[0], stream(new ByteArrayOutputStream()), stream(err));

        assertEquals(Main.USAGE_ERROR, code);
        assertTrue(err.toString("UTF-8").contains("Использование"));
    }

    @Test
    @DisplayName("Сообщает о несуществующем файле вместо падения")
    void reportsMissingFile() throws Exception {
        Path missing = dir.resolve("missing.dsl");
        Path html = dir.resolve("out.html");
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        int code = Main.run(new String[]{missing.toString(), html.toString()},
                stream(new ByteArrayOutputStream()), stream(err));

        assertEquals(Main.USAGE_ERROR, code);
        assertTrue(err.toString("UTF-8").contains("Не удалось прочитать"));
    }

    private static PrintStream stream(ByteArrayOutputStream bytes) throws IOException {
        return new PrintStream(bytes, true, "UTF-8");
    }
}
