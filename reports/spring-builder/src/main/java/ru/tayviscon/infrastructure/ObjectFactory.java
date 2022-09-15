package ru.tayviscon.infrastructure;

import lombok.SneakyThrows;
import ru.tayviscon.annotation.PostConstract;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toMap;

public class ObjectFactory {

    private List<ProxyConfigurator> proxyConfigurators = new ArrayList<>();
    private List<ObjectConfigurator> configurators = new ArrayList<>();
    private ApplicationContext context;
    @SneakyThrows
    public  ObjectFactory(ApplicationContext context){
        this.context = context;
        for (Class<? extends ObjectConfigurator> aClass : context.getConfig().getScanner().getSubTypesOf(ObjectConfigurator.class)) {
            configurators.add(aClass.getDeclaredConstructor().newInstance());
        }
        for (Class<? extends ProxyConfigurator> aClass : context.getConfig().getScanner().getSubTypesOf(ProxyConfigurator.class)) {
            proxyConfigurators.add(aClass.getDeclaredConstructor().newInstance());
        }
    }

    @SneakyThrows
    public <T> T createObject(Class<T> implClass) {

        T t = implClass.getDeclaredConstructor().newInstance();
        // todo тут будет много магии
        for (ObjectConfigurator configurator : configurators) {
            configurator.configure(t, context);
        }

        for (Method method : implClass.getMethods()) {
            if (method.isAnnotationPresent(PostConstract.class)) {
                method.invoke(t);
            }
        }
        for (ProxyConfigurator proxyConfigurator : proxyConfigurators) {
            t = (T) proxyConfigurator.warpWithProxy(t, implClass);
        }

        return t;
    }
}
