package ru.tayviscon.service;

import ru.tayviscon.annotation.InjectByType;

@Deprecated
public class ConsolePrankerCaller implements PrankerCaller {
    @InjectByType
    private Recommendator recommendator;
    public void call(String phoneNumber, String message) {
        System.out.println("Звоню по номеру: " + phoneNumber + " ;)");
        System.out.println(message);
    }
}
