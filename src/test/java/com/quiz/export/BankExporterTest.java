package com.quiz.export;

import com.quiz.model.AnswerType;
import com.quiz.model.Option;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankExporterTest {

    private final BankExporter exporter = new BankExporter();

    @Test
    @DisplayName("Начинает файл с window.questionBank и содержит метаданные")
    void writesQuestionBankHeader() {
        Quiz quiz = new Quiz("Тест", "Заголовок", "dark", Collections.<Question>emptyList());

        String js = exporter.generate(quiz);

        assertTrue(js.startsWith("window.questionBank = {"));
        assertTrue(js.trim().endsWith("};"));
        assertTrue(js.contains("\"title\": \"Тест\""));
        assertTrue(js.contains("\"heading\": \"Заголовок\""));
        assertTrue(js.contains("\"style\": \"dark\""));
    }

    @Test
    @DisplayName("Раскладывает вопросы по категориям в формате банка")
    void groupsQuestionsByCategory() {
        Quiz quiz = new Quiz("T", "H", "default", Arrays.asList(
                question("HTML"),
                question("HTML"),
                question("CSS")));

        String js = exporter.generate(quiz);

        assertTrue(js.contains("\"HTML\": 1"));
        assertTrue(js.contains("\"CSS\": 1"));
        assertTrue(js.indexOf("\"HTML\"") < js.indexOf("\"CSS\""));
    }

    @Test
    @DisplayName("Вопрос без категории попадает в группу «Общие»")
    void exportsUncategorizedQuestions() {
        Quiz quiz = new Quiz("T", "H", "default", Arrays.asList(question("")));

        String js = exporter.generate(quiz);

        assertTrue(js.contains("\"" + Quiz.DEFAULT_CATEGORY + "\""));
    }

    @Test
    @DisplayName("Вопрос с выбором выгружается с вариантами и флагами correct")
    void exportsChoiceQuestion() {
        List<Option> options = Arrays.asList(new Option("<a>", true), new Option("<link>", false));
        Quiz quiz = new Quiz("T", "H", "default", Arrays.asList(
                Question.singleChoice("Ссылка?", options, "HTML")));

        String js = exporter.generate(quiz);

        assertTrue(js.contains("\"type\": \"r\""));
        assertTrue(js.contains("\"text\": \"<a>\""));
        assertTrue(js.contains("\"correct\": true"));
        assertTrue(js.contains("\"correct\": false"));
    }

    @Test
    @DisplayName("Текстовый вопрос выгружается с answer и без options")
    void exportsTextQuestion() {
        Quiz quiz = new Quiz("T", "H", "default", Arrays.asList(
                Question.textAnswer("Атрибут?", "alt", "HTML")));

        String js = exporter.generate(quiz);

        assertTrue(js.contains("\"type\": \"t\""));
        assertTrue(js.contains("\"answer\": \"alt\""));
        assertFalse(js.contains("options"));
    }

    @Test
    @DisplayName("Не экранирует угловые скобки в тексте вопросов")
    void keepsHtmlTagsReadable() {
        Quiz quiz = new Quiz("T", "H", "default", Arrays.asList(
                Question.textAnswer("Тег <a>?", "a", "HTML")));

        String js = exporter.generate(quiz);

        assertTrue(js.contains("Тег <a>?"));
        assertFalse(js.contains("\\u003c"));
    }

    @Test
    @DisplayName("Количество вопросов на категорию можно задать")
    void usesConfiguredQuestionsPerCategory() {
        Quiz quiz = new Quiz("T", "H", "default", Arrays.asList(question("HTML")));

        String js = new BankExporter(3).generate(quiz);

        assertTrue(js.contains("\"HTML\": 3"));
    }

    private static Question question(String category) {
        List<Option> options = new ArrayList<>(Arrays.asList(new Option("a", true), new Option("b", false)));
        return new Question("Вопрос " + category, AnswerType.SINGLE, options, null, category);
    }
}
