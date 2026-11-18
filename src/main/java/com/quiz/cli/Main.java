package com.quiz.cli;

import com.quiz.export.BankExporter;
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
 * <pre>
 * java -jar quiz-dsl.jar test.dsl test.html
 * java -jar quiz-dsl.jar --bank test.dsl question-bank.js
 * </pre>
 *
 * <p>Первый режим собирает автономную форму, второй — банк вопросов для страницы
 * викторины. Коды возврата: 0 — успех, 1 — неверные аргументы или ошибки
 * ввода-вывода, 2 — в DSL есть ошибки.
 */
public final class Main {

    static final int OK = 0;
    static final int USAGE_ERROR = 1;
    static final int INVALID_DSL = 2;

    static final String BANK_FLAG = "--bank";

    private Main() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        boolean bank = args.length > 0 && BANK_FLAG.equals(args[0]);
        int expected = bank ? 3 : 2;
        if (args.length != expected) {
            printUsage(err);
            return USAGE_ERROR;
        }

        Path input = Paths.get(args[bank ? 1 : 0]);
        Path output = Paths.get(args[bank ? 2 : 1]);

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

        if (result.quiz().isEmpty()) {
            err.println("В тесте нет ни одного вопроса");
            return INVALID_DSL;
        }

        String content = bank
                ? new BankExporter().generate(result.quiz())
                : new HtmlGenerator().generate(result.quiz());
        try {
            Files.write(output, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            err.println("Не удалось записать " + output + ": " + e.getMessage());
            return USAGE_ERROR;
        }

        out.println((bank ? "Банк: " : "Готово: ") + output + " (" + result.quiz().size() + " вопросов)");
        return OK;
    }

    private static void printUsage(PrintStream err) {
        err.println("Использование:");
        err.println("  java -jar quiz-dsl.jar <входной.dsl> <выходной.html>");
        err.println("  java -jar quiz-dsl.jar " + BANK_FLAG + " <входной.dsl> <question-bank.js>");
    }
}
