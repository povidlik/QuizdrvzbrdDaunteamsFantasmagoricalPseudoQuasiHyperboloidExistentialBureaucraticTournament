package com.quiz.generator;

import com.quiz.model.Option;
import com.quiz.model.Question;
import com.quiz.model.Quiz;

/**
 * Генерирует автономную HTML-страницу с формой теста.
 *
 * <p>Результат — один файл: разметка, минимальные стили и скрипт, который
 * проверяет ответы и показывает «Результат: N из M». Никаких внешних
 * ресурсов не требуется, страница открывается двойным кликом.
 */
public final class HtmlGenerator {

    private static final String DEFAULT_CSS =
            "  body { margin: 0; padding: 32px 16px; background: #f4f4f5; color: #18181b;"
                    + " font-family: 'Segoe UI', Arial, sans-serif; }\n"
                    + "  .quiz { max-width: 720px; margin: 0 auto; background: #fff; padding: 24px 28px;"
                    + " border-radius: 12px; box-shadow: 0 2px 12px rgba(0, 0, 0, .08); }\n"
                    + "  .quiz-title { margin-top: 0; font-size: 26px; }\n"
                    + "  .question { border-left: 3px solid #d4d4d8; padding: 8px 0 8px 14px;"
                    + " margin: 18px 0; }\n"
                    + "  .question.correct { border-left-color: #16a34a; }\n"
                    + "  .question.wrong { border-left-color: #dc2626; }\n"
                    + "  .question-text { font-weight: 600; margin: 0 0 10px; }\n"
                    + "  .option { display: block; padding: 6px 0; cursor: pointer; }\n"
                    + "  .option input { margin-right: 8px; }\n"
                    + "  .answer { width: 100%; box-sizing: border-box; padding: 8px 10px;"
                    + " border: 1px solid #d4d4d8; border-radius: 6px; font-size: 15px; }\n"
                    + "  .check { margin-top: 8px; padding: 10px 18px; border: 0; border-radius: 6px;"
                    + " background: #18181b; color: #fff; font-size: 15px; cursor: pointer; }\n"
                    + "  .check:hover { background: #3f3f46; }\n"
                    + "  .result { margin-top: 16px; font-size: 18px; font-weight: 600; }\n";

    private static final String SCRIPT_BODY =
            "  var form = document.getElementById('quiz-form');\n"
                    + "  var result = document.getElementById('result');\n"
                    + "  var questions = form.querySelectorAll('.question');\n"
                    + "  form.addEventListener('submit', function (event) {\n"
                    + "    event.preventDefault();\n"
                    + "    var score = 0;\n"
                    + "    for (var i = 0; i < questions.length; i++) {\n"
                    + "      var key = KEY[i];\n"
                    + "      var ok = checkAnswer(questions[i], key);\n"
                    + "      questions[i].className = 'question ' + (ok ? 'correct' : 'wrong');\n"
                    + "      if (ok) { score++; }\n"
                    + "    }\n"
                    + "    result.textContent = 'Результат: ' + score + ' из ' + questions.length;\n"
                    + "  });\n"
                    + "  function checkAnswer(question, key) {\n"
                    + "    if (key.type === 't') {\n"
                    + "      var input = question.querySelector('input.answer');\n"
                    + "      return input.value.trim().toLowerCase() === key.answer.trim().toLowerCase();\n"
                    + "    }\n"
                    + "    var inputs = question.querySelectorAll('input');\n"
                    + "    var checked = [];\n"
                    + "    for (var j = 0; j < inputs.length; j++) {\n"
                    + "      if (inputs[j].checked) { checked.push(Number(inputs[j].value)); }\n"
                    + "    }\n"
                    + "    if (checked.length !== key.correct.length) { return false; }\n"
                    + "    for (var k = 0; k < checked.length; k++) {\n"
                    + "      if (key.correct.indexOf(checked[k]) === -1) { return false; }\n"
                    + "    }\n"
                    + "    return true;\n"
                    + "  }\n";

    /**
     * @param quiz разобранный тест
     * @return HTML-страница целиком
     */
    public String generate(Quiz quiz) {
        StringBuilder html = new StringBuilder(4096);

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"ru\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        html.append("<title>").append(escapeHtml(pageTitle(quiz))).append("</title>\n");
        html.append("<style>\n").append(DEFAULT_CSS).append("</style>\n");
        html.append("</head>\n");

        html.append("<body class=\"test-style-").append(escapeHtml(quiz.style())).append("\">\n");
        html.append("<main class=\"quiz\">\n");
        if (!quiz.heading().isEmpty()) {
            html.append("<h1 class=\"quiz-title\">").append(escapeHtml(quiz.heading())).append("</h1>\n");
        }

        html.append("<form id=\"quiz-form\" novalidate>\n");
        for (int i = 0; i < quiz.questions().size(); i++) {
            appendQuestion(html, quiz.questions().get(i), i + 1);
        }
        html.append("<button type=\"submit\" class=\"check\">Проверить ответы</button>\n");
        html.append("</form>\n");
        html.append("<p id=\"result\" class=\"result\" role=\"status\"></p>\n");
        html.append("</main>\n");

        appendScript(html, quiz);

        html.append("</body>\n</html>\n");
        return html.toString();
    }

    private static String pageTitle(Quiz quiz) {
        if (!quiz.title().isEmpty()) {
            return quiz.title();
        }
        return quiz.heading();
    }

    private static void appendQuestion(StringBuilder html, Question question, int number) {
        html.append("<section class=\"question\" data-type=\"")
                .append(question.type().code()).append("\">\n");
        html.append("<p class=\"question-text\">").append(number).append(". ")
                .append(escapeHtml(question.text())).append("</p>\n");

        switch (question.type()) {
            case SINGLE:
                appendOptions(html, question, number, "radio");
                break;
            case MULTIPLE:
                appendOptions(html, question, number, "checkbox");
                break;
            case TEXT:
                html.append("<input type=\"text\" class=\"answer\" name=\"q").append(number)
                        .append("\" autocomplete=\"off\">\n");
                break;
            default:
                throw new IllegalStateException("Неизвестный тип вопроса: " + question.type());
        }

        html.append("</section>\n");
    }

    private static void appendOptions(StringBuilder html, Question question, int number, String inputType) {
        for (int i = 0; i < question.options().size(); i++) {
            Option option = question.options().get(i);
            String id = "q" + number + "_" + i;
            html.append("<label class=\"option\" for=\"").append(id).append("\">")
                    .append("<input type=\"").append(inputType).append("\" id=\"").append(id)
                    .append("\" name=\"q").append(number).append("\" value=\"").append(i).append("\">")
                    .append("<span>").append(escapeHtml(option.text())).append("</span>")
                    .append("</label>\n");
        }
    }

    private static void appendScript(StringBuilder html, Quiz quiz) {
        html.append("<script>\n(function () {\n");
        html.append("  var KEY = [\n");
        for (int i = 0; i < quiz.questions().size(); i++) {
            html.append("    ").append(answerKey(quiz.questions().get(i)));
            if (i < quiz.questions().size() - 1) {
                html.append(',');
            }
            html.append('\n');
        }
        html.append("  ];\n");
        html.append(SCRIPT_BODY);
        html.append("})();\n</script>\n");
    }

    private static String answerKey(Question question) {
        StringBuilder key = new StringBuilder();
        key.append("{type:\"").append(question.type().code()).append('"');

        if (question.hasOptions()) {
            key.append(", correct:[");
            boolean first = true;
            for (int i = 0; i < question.options().size(); i++) {
                if (question.options().get(i).correct()) {
                    if (!first) {
                        key.append(',');
                    }
                    key.append(i);
                    first = false;
                }
            }
            key.append(']');
        } else {
            key.append(", answer:\"").append(escapeJs(question.answer())).append('"');
        }

        key.append('}');
        return key.toString();
    }

    /** Экранирование для HTML-текста и значений атрибутов. */
    static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '"':
                    out.append("&quot;");
                    break;
                case '\'':
                    out.append("&#39;");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }

    /** Экранирование для строкового литерала внутри {@code <script>}. */
    static String escapeJs(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\':
                    out.append("\\\\");
                    break;
                case '"':
                    out.append("\\\"");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '<':
                    out.append("\\u003C");
                    break;
                case '>':
                    out.append("\\u003E");
                    break;
                case '&':
                    out.append("\\u0026");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }
}
