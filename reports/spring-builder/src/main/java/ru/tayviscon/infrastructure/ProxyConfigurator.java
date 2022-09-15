package ru.tayviscon.infrastructure;

public interface ProxyConfigurator {
    Object warpWithProxy(Object t, Class implClass);
}
