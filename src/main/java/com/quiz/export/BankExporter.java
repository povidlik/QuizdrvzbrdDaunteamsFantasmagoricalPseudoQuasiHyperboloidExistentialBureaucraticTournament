package com.quiz.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.quiz.model.Option;
import com.quiz.model.Question;
import com.quiz.model.Quiz;

import java.util.List;
import java.util.Map;

/**
 * Выгружает разобранный тест в формат {@code question-bank.js}, который читает
 * страница викторины.
 *
 * <p>Вопросы раскладываются по категориям через {@link Quiz#categories()},
 * поэтому директива {@code Category} из DSL становится ключом в
 * {@code categories}. Формат совпадает с ручным банком вопросов.
 */
public final class BankExporter {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final int questionsPerCategory;

    public BankExporter() {
        this(1);
    }

    /**
     * @param questionsPerCategory сколько вопросов страница берёт из каждой категории
     */
    public BankExporter(int questionsPerCategory) {
        if (questionsPerCategory < 1) {
            throw new IllegalArgumentException("Количество вопросов на категорию должно быть положительным");
        }
        this.questionsPerCategory = questionsPerCategory;
    }

    public String generate(Quiz quiz) {
        JsonObject root = new JsonObject();
        root.addProperty("title", quiz.title());
        root.addProperty("heading", quiz.heading());
        root.addProperty("style", quiz.style());

        JsonObject counts = new JsonObject();
        JsonObject categories = new JsonObject();
        for (Map.Entry<String, List<Question>> entry : quiz.categories().entrySet()) {
            counts.addProperty(entry.getKey(), questionsPerCategory);

            JsonArray questions = new JsonArray();
            for (Question question : entry.getValue()) {
                questions.add(question(question));
            }
            categories.add(entry.getKey(), questions);
        }

        root.add("questionsPerCategory", counts);
        root.add("categories", categories);

        return "window.questionBank = " + GSON.toJson(root) + ";\n";
    }

    private static JsonObject question(Question question) {
        JsonObject json = new JsonObject();
        json.addProperty("text", question.text());
        json.addProperty("type", question.type().code());
        if (question.hasOptions()) {
            JsonArray options = new JsonArray();
            for (Option option : question.options()) {
                JsonObject item = new JsonObject();
                item.addProperty("text", option.text());
                item.addProperty("correct", option.correct());
                options.add(item);
            }
            json.add("options", options);
        } else {
            json.addProperty("answer", question.answer());
        }
        return json;
    }
}
