package ru.tayviscon.annotation.handlers;

import lombok.SneakyThrows;
import ru.tayviscon.infrastructure.ApplicationContext;
import ru.tayviscon.infrastructure.ObjectConfigurator;
import ru.tayviscon.annotation.InjectByType;

import java.lang.reflect.Field;

public class InjectByTypeAnnotationObjectConfigurator implements ObjectConfigurator {
    @Override
    @SneakyThrows
    public void configure(Object t, ApplicationContext context) {
        for (Field field : t.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectByType.class)) {
                Object object = context.getObject(field.getType());
                field.setAccessible(true);
                field.set(t, object);
            }
        }
    }
}
