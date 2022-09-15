package ru.tayviscon;

import ru.tayviscon.infrastructure.Application;
import ru.tayviscon.infrastructure.ApplicationContext;
import ru.tayviscon.model.Exam;
import ru.tayviscon.service.AngrySpy;
import ru.tayviscon.service.ExamCheatingAssistant;
import ru.tayviscon.service.Spy;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = Application.run("ru.tayviscon", new HashMap<>(Map.of(Spy.class, AngrySpy.class)));
        ExamCheatingAssistant assistant = context.getObject(ExamCheatingAssistant.class);
        assistant.start(new Exam());
    }
}
