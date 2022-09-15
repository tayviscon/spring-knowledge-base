package ru.tayviscon.service;

import ru.tayviscon.annotation.InjectProperty;
import ru.tayviscon.annotation.Singleton;

@Singleton
public class RecommendatorImpl implements Recommendator {
    @InjectProperty
    private String socialLinks;

    public RecommendatorImpl() {
        System.out.println("Recommendator was created");
    }

    @Override
    public void recommend() {
        System.out.println("Recommendator: " + socialLinks);
    }
}
