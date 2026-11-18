package com.quiz.generator;

import com.quiz.model.Quiz;
import com.quiz.parser.DslParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlGeneratorTest {

    private static final String EXAMPLE = String.join("\n",
            "Title My title",
            "H1 Super Test",
            "",
            "? 2+2= r",
            "- 4 +",
            "- 5",
            "- 6 e",
            "",
            "? Second question c",
            "- A",
            "- B +",
            "- C +e",
            "",
            "? Text question t",
            "_ My answer");

    private final DslParser parser = new DslParser();
    private final HtmlGenerator generator = new HtmlGenerator();

    @Test
    @DisplayName("Генерирует законченный HTML-документ в UTF-8")
    void generatesCompleteDocument() {
        String html = generate(EXAMPLE);

        assertTrue(html.startsWith("<!DOCTYPE html>"), "нет доктайпа");
        assertTrue(html.contains("<meta charset=\"UTF-8\">"), "нет кодировки");
        assertTrue(html.trim().endsWith("</html>"), "документ не закрыт");
        assertTrue(html.contains("<script>"), "нет скрипта проверки");
    }

    @Test
    @DisplayName("Title попадает в заголовок вкладки, H1 — в заголовок страницы")
    void rendersTitleAndHeading() {
        String html = generate(EXAMPLE);

        assertTrue(html.contains("<title>My title</title>"));
        assertTrue(html.contains("<h1 class=\"quiz-title\">Super Test</h1>"));
    }

    @Test
    @DisplayName("Для вопроса r рисуются радиокнопки с именем q1")
    void rendersRadioGroupForSingleChoice() {
        String html = generate(EXAMPLE);

        assertEquals(3, count(html, "type=\"radio\""));
        assertEquals(3, count(html, "name=\"q1\""));
        assertTrue(html.contains("data-type=\"r\""));
    }

    @Test
    @DisplayName("Для вопроса c рисуются чекбоксы с именем q2")
    void rendersCheckboxesForMultipleChoice() {
        String html = generate(EXAMPLE);

        assertEquals(3, count(html, "type=\"checkbox\""));
        assertEquals(3, count(html, "name=\"q2\""));
        assertTrue(html.contains("data-type=\"c\""));
    }

    @Test
    @DisplayName("Для вопроса t рисуется текстовое поле q3")
    void rendersTextInputForTextQuestion() {
        String html = generate(EXAMPLE);

        assertTrue(html.contains("type=\"text\" class=\"answer\" name=\"q3\""));
    }

    @Test
    @DisplayName("В скрипт встраиваются правильные ответы")
    void embedsAnswerKey() {
        String html = generate(EXAMPLE);

        assertTrue(html.contains("{type:\"r\", correct:[0]}"));
        assertTrue(html.contains("{type:\"c\", correct:[1,2]}"));
        assertTrue(html.contains("{type:\"t\", answer:\"My answer\"}"));
    }

    @Test
    @DisplayName("HTML в тексте вопроса экранируется")
    void escapesHtmlInQuestionText() {
        String html = generate(String.join("\n",
                "? Что такое <b>тег</b>? r",
                "- вариант <script>",
                "- другой +e"));

        assertTrue(html.contains("&lt;b&gt;тег&lt;/b&gt;"));
        assertTrue(html.contains("вариант &lt;script&gt;"));
        assertFalse(html.contains("<b>тег</b>"));
        assertFalse(html.contains("вариант <script>"));
    }

    @Test
    @DisplayName("Закрывающий тег script в ответе не ломает страницу")
    void doesNotBreakScriptOnClosingTag() {
        String html = generate(String.join("\n",
                "? Опасный вопрос t",
                "_ </script><script>alert(1)</script>"));

        assertEquals(1, count(html, "</script>"));
    }

    @Test
    @DisplayName("Style становится css-классом body")
    void appliesStyleClass() {
        String html = generate("Style dark\n? Вопрос r\n- a +e");

        assertTrue(html.contains("<body class=\"test-style-dark\">"));
    }

    @Test
    @DisplayName("Без Style используется тема default")
    void usesDefaultStyle() {
        String html = generate("? Вопрос r\n- a +e");

        assertTrue(html.contains("<body class=\"test-style-default\">"));
    }

    @Test
    @DisplayName("Вопросы нумеруются по порядку")
    void numbersQuestions() {
        String html = generate(EXAMPLE);

        assertTrue(html.contains("1. 2+2="));
        assertTrue(html.contains("2. Second question"));
        assertTrue(html.contains("3. Text question"));
    }

    private String generate(String dsl) {
        Quiz quiz = parser.parse(dsl).quiz();
        return generator.generate(quiz);
    }

    private static int count(String text, String fragment) {
        int count = 0;
        int index = text.indexOf(fragment);
        while (index >= 0) {
            count++;
            index = text.indexOf(fragment, index + fragment.length());
        }
        return count;
    }
}
