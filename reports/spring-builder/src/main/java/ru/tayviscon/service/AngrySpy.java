package ru.tayviscon.service;

import ru.tayviscon.annotation.InjectByType;
import ru.tayviscon.annotation.PostConstract;

public class AngrySpy implements Spy {

    @InjectByType
    private Recommendator recommendator;

    @PostConstract
    public void init() {
        System.out.println(recommendator.getClass());
    }

    @Override
    public void makeSurePersonLeaveRoom() {
        System.out.println("Хожу мимо кабинета и ворчу: Мне что больше делать нечего");
        System.out.println("Ну наконец он свалил");
        recommendator.recommend();
    }
}
