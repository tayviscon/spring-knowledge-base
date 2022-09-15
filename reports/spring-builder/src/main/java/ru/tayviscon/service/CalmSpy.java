package ru.tayviscon.service;

public class CalmSpy implements Spy {
    public void makeSurePersonLeaveRoom() {
        System.out.println("Хожу мимо кабинета");
        System.out.println("!!!Все, вижу как, он ушел!!!");
    }
}
