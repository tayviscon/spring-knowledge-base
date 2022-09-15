package ru.tayviscon.infrastructure;

import ru.tayviscon.infrastructure.ApplicationContext;

public interface ObjectConfigurator {
    void configure(Object t, ApplicationContext context);
}
