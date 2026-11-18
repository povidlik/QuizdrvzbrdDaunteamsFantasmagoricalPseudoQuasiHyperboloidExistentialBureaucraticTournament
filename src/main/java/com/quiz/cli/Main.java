package com.quiz.cli;

import com.quiz.generator.HtmlGenerator;
import com.quiz.parser.DslParser;
import com.quiz.parser.ParseError;
import com.quiz.parser.ParseResult;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Точка входа: читает DSL-файл и сохраняет готовую HTML-форму теста.
 *
 * <pre>java -jar quiz-dsl.jar test.dsl test.html</pre>
 *
 * <p>Коды возврата: 0 — успех, 1 — неверные аргументы или ошибки ввода-вывода,
 * 2 — в DSL есть ошибки.
 */
public final class Main {

    static final int OK = 0;
    static final int USAGE_ERROR = 1;
    static final int INVALID_DSL = 2;

    private Main() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length != 2) {
            err.println("Использование: java -jar quiz-dsl.jar <входной.dsl> <выходной.html>");
            return USAGE_ERROR;
        }

        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);

        String source;
        try {
            source = new String(Files.readAllBytes(input), StandardCharsets.UTF_8);
        } catch (IOException e) {
            err.println("Не удалось прочитать " + input + ": " + e.getMessage());
            return USAGE_ERROR;
        }

        ParseResult result = new DslParser().parse(source);
        if (result.hasErrors()) {
            err.println("Ошибки в DSL:");
            for (ParseError error : result.errors()) {
                err.println("  " + error);
            }
            return INVALID_DSL;
        }

        String html = new HtmlGenerator().generate(result.quiz());
        try {
            Files.write(output, html.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            err.println("Не удалось записать " + output + ": " + e.getMessage());
            return USAGE_ERROR;
        }

        out.println("Готово: " + output + " (" + result.quiz().size() + " вопросов)");
        return OK;
    }
}
