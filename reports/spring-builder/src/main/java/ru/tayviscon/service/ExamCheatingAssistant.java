package ru.tayviscon.service;

import ru.tayviscon.annotation.InjectByType;
import ru.tayviscon.model.Exam;

public class ExamCheatingAssistant {

    @InjectByType
    private PrankerCaller prankerCaller;
    @InjectByType
    private Spy spy;

    public void start(Exam exam) {
        prankerCaller.call("+7(918)-000-00-00", "Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!");
        spy.makeSurePersonLeaveRoom();
        sendOutAnswersOnExam(exam);
    }

    private void sendOutAnswersOnExam(Exam exam) {
        System.out.println("Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать");
    }

}
