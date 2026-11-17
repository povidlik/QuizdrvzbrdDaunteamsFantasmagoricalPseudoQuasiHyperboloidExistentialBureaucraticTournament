window.questionBank = {
  title: "Тест по веб-разработке",
  heading: "Основы веб-разработки",
  style: "default",
  questionsPerCategory: {
    HTML: 1,
    CSS: 1,
    JavaScript: 1
  },
  categories: {
    HTML: [
      {
        text: "Какой HTML-тег создает ссылку?",
        type: "r",
        options: [
          { text: "<a>", correct: true },
          { text: "<link>", correct: false },
          { text: "<href>", correct: false }
        ]
      },
      {
        text: "Выберите семантические элементы HTML.",
        type: "c",
        options: [
          { text: "<main>", correct: true },
          { text: "<footer>", correct: true },
          { text: "<div>", correct: false }
        ]
      },
      {
        text: "Какой атрибут изображения задает альтернативный текст?",
        type: "t",
        answer: "alt"
      }
    ],
    CSS: [
      {
        text: "Какое CSS-свойство меняет цвет текста?",
        type: "r",
        options: [
          { text: "color", correct: true },
          { text: "font-style", correct: false },
          { text: "background-color", correct: false }
        ]
      },
      {
        text: "Какие свойства задают внутренние и внешние отступы?",
        type: "c",
        options: [
          { text: "padding", correct: true },
          { text: "margin", correct: true },
          { text: "opacity", correct: false }
        ]
      },
      {
        text: "Какое значение display включает flex-разметку?",
        type: "t",
        answer: "flex"
      }
    ],
    JavaScript: [
      {
        text: "Какой оператор проверяет строгое равенство в JavaScript?",
        type: "r",
        options: [
          { text: "===", correct: true },
          { text: "=", correct: false },
          { text: "=>", correct: false }
        ]
      },
      {
        text: "Какие из перечисленных значений являются примитивными типами JavaScript?",
        type: "c",
        options: [
          { text: "string", correct: true },
          { text: "boolean", correct: true },
          { text: "array", correct: false }
        ]
      },
      {
        text: "Какой метод преобразует JSON-текст в значение JavaScript?",
        type: "t",
        answer: "JSON.parse"
      }
    ]
  }
};
