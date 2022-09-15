## Tyomych Tovkach - Spring-Builder
### ВВЕДЕНИЕ
**Цель доклада** - изучить основные концепции инверсии контроля, понять, как работает Spring
изнутри и как его можно кастомизировать.  

Для достижения поставленной цели, мы будем писать приложение по поставленной бизнес-задаче:
`написать приложение, помогающее списывать на экзамене`. В процессе разработки, для того, 
чтобы написать красивый код, у нас появиться необходимость написать некоторую инфраструктуру, 
которая в процессе развития, станет неким аналогом Spring.

**Используемые технологии**: Java, Maven, Lombok, Reflections
### Инициализация проекта
У нас есть пакет `model` с его классом `Exam`, отображающий предметную область экзамена, который
мы будем списывать. А также у нас есть пакет `service`, с его головным, для нас, сервисом 
`ExamCheatingAssistant`, в котором нам будет необходимо: 
* **Разыграть преподавателя при помощи телефонного звонка**
* **Убедиться, что преподаватель покинул аудиторию**
* **Разослать ответы студентам** - наш уникальный алгоритм
```java
public class ExamCheatingAssistant {


    public void start(Exam exam) {
        // todo Разыграть преподавателя при помощи телефонного звонка
        // todo Убедиться, что преподаватель покинул аудиторию
        sendOutAnswersOnExam(exam);
    }

    private void sendOutAnswersOnExam(Exam exam) {
        System.out.println("Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать");
    }

}
```
### Использование принципов SOLID при разработке приложения
* **S**inge Responsibility - класс должен быть лишь одна ответственность
* **O**pen-Close Principle - класс должен быть открыт для расширений, но закрыт для изменений
* **L**iskov Substitution - необходимо, чтобы подклассы могли бы служить заменой для своих суперклассов
* **I**nterface Segregation - необходимо создавать узкоспециализированные интерфейсы, предназначенные для конкретного клиента
* **D**ependency Inversion - необходимо, чтобы объектом зависимости была абстракция, а не что-то конкретное

Таким образом, мы могли бы начать писать код прямо в методе `start(Exam exam)`, но мы должны следовать
принципам SOLID, чтобы написать более удобное в обслуживании, понятное и гибкое программное обеспечение.
Именно поэтому мы будем инкапсулировать ту логику которая может быть использована в других местах.

```java
public class ExamCheatingAssistant {

    private PrankerCaller prankerCaller;
    private Spy spy;

    public void start(Exam exam) {
        // todo Разыграть преподавателя при помощи телефонного звонка
        prankerCaller.call("+7(918)-000-00-00", "Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!");
        // todo Убедиться, что преподаватель покинул аудиторию
        spy.makeSurePersonLeaveRoom();
        sendOutAnswersOnExam(exam);
    }

    private void sendOutAnswersOnExam(Exam exam) {
        System.out.println("Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать");
    }

}
```
Стоит отметить, что `PrankerCaller` и `Spy` это интерфейсы, как и большинство наших сервисов,
так как для разных клиентов могут быть разные имплементации и, если мы привяжемся к конкретной
имплементации у нас в дальнейшем может быть проблема с тем, чтобы её поменять.

```java
public class ConsolePrankerCaller implements PrankerCaller {
    public void call(String phoneNumber, String message) {
        System.out.println("Звоню по номеру: " + phoneNumber + " ;)");
        System.out.println(message);
    }
}
```

```java
public class CalmSpy implements Spy {
    public void makeSurePersonLeaveRoom() {
        System.out.println("Хожу мимо кабинета");
        System.out.println("!!!Все, вижу как, он ушел!!!");
    }
}
```

Теперь проинициализируем объекты в `ExamCheatingAssistant` и попробуем запустить приложение.

```java
public class ExamCheatingAssistant {

    private PrankerCaller prankerCaller = new ConsolePrankerCaller();
    private Spy spy = new CalmSpy();

    public void start(Exam exam) {
        prankerCaller.call("+7(918)-000-00-00", "Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!");
        spy.makeSurePersonLeaveRoom();
        sendOutAnswersOnExam(exam);
    }

    private void sendOutAnswersOnExam(Exam exam) {
        System.out.println("Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать");
    }

}
```

```java
public class Main {
    public static void main(String[] args) {
        ExamCheatingAssistant assistant = new ExamCheatingAssistant();
        assistant.start(new Exam());
    }
}
```
Отлично, наше приложение работает:
```
Звоню по номеру: +7(918)-000-00-00 ;)
Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!
Хожу мимо кабинета
!!!Все, вижу как, он ушел!!!
Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать
```
### Считаем количество зависимостей класса `ExamCheatingAssistant`
Как уже говорилось ранее, наш класс в первую очередь отвечает за то, чтобы разослать студентам
ответы `sendOutAnswersOnExam(Exam exam)`, однако теперь он еще и создает объекты типов `PrankerCaller` и `Spy`, а в дальнейшем
он должен будет знать как их настроить и, если имплементаций данных интерфейсов будет несколько,
то он будет обязан знать, какую именно имплементацию выбрать.

Таким образом, наш класс ответственен за:
* Создание объектов
* Настройку объектов
* Выбор правильной имплементации интерфейса
* Отправку ответов студентам

Такой подход напрочь нарушает **Single Responsibility Principle**.
### Идём к инфраструктуре с централизованным местом для создания всех объектов
>Когда в 1994 году появился язык программирования Java, люди не очень понимали принципы ООП,
> потому что основной опыт, который был накоплен - процедуральный. Затем в конце 90-ых люди
> поняли, что в Java есть объекты, их можно создавать, настраивать, передавать в качестве
> параметров, с помощью них можно инкапсулировать логику и начали создавать объекты при
> помощи `new`. Однако чуть позже люди поняли, что написать код, намного менее важно, чем
> сделать его таким, чтобы его можно было потом развивать. Именно в этот момент появился 
> очень модный, молодёжный дизайн паттерн **Factory** и начали для каждого объекта писать
> свои фабрики. Чуть позже ООП перешло на новый этап эволюции, где ядром инфраструктуры 
> являлся некий объект `ObjectFactory`.

Давайте создадим наш `ObjectFactory` и чтобы он был доступен из любой точки нашего код
сделаем его при помощи паттерна **Singleton**.
```java
public class ObjectFactory {
    private static ObjectFactory ourInstance = new ObjectFactory();
    public static ObjectFactory getInstance() {
        return ourInstance;
    }

    private ObjectFactory(){}

    public <T> T createObject(Class<T> type) {
        return null;
    }
}
```
Как мы можем увидеть, у нашего `ObjectFactory` есть ответственность за создание объектов
`createObject(Class<T> type)`. Именно в этом методе нам будет необходимо найти и настроить
правильный объект в зависимости от входного параметра `Class<T> type`.

Стоит отметить, что входными параметрами данного метода могут быть как классы, так и
интерфейсы. И в случае, если это интерфейс, нам необходимо найти подходящую имплементацию.
Для этого создадим интерфейс `Config`, который будет отвечать за возврат подходящей
имплементации, а также сразу реализуем его в классе `JavaConfig`.

```java
public interface Config {
    <T> Class<? extends T> getImplClass(Class<T> ifc);
}
```
```java
public class JavaConfig implements Config {

    private Reflections scanner;

    public JavaConfig(String packageToScan) {
        this.scanner = new Reflections(packageToScan);
    }

    @Override
    public <T> Class<? extends T> getImplClass(Class<T> ifc) {
        Set<Class<? extends T>> classes = scanner.getSubTypesOf(ifc);
        if(classes.size() != 1) {
            throw new RuntimeException(ifc + " has zero or more than one impl");
        }
        return classes.iterator().next();
    }
}
```

`JavaConfig` содержит в себе объект типа `Reflections`, для того, чтобы просматривать иерархию
проекта. Таким образом, в конструкторе, передается пакет для сканирования классов, а метод
`getImplClass(Class<T> ifc)` находит все имплементации переданного интерфейса. Если имплементация
одна, тогда он её возвращает, иначе выбрасывает исключение.

Воспользуемся написанным `Config` в нашем классе `ObjectFactory`. Напомню, что в методе `createObject(Class<T> type)`
нам необходимо проверить, является ли переданный тип интерфейсом, если да, то обратиться к `Confg`,
чтобы понять, какую имплементацию использовать.

```java
public class ObjectFactory {

    private Config config = new JavaConfig("ru.tayviscon");
    private static ObjectFactory ourInstance = new ObjectFactory();
    public static ObjectFactory getInstance() {
        return ourInstance;
    }

    private ObjectFactory(){}

    @SneakyThrows
    public <T> T createObject(Class<T> type) {
        Class<? extends T> implClass = type;
        if(type.isInterface()) {
            implClass = config.getImplClass(type);
        }
        T t = implClass.getDeclaredConstructor().newInstance();
        // todo тут будет много магии
        return t;
    }
}
```
Теперь наш класс `ExamCheatingAssistant` необходимо переработать, что использовать все
преимущества нашего `ObjectFactory`:
```java
public class ExamCheatingAssistant {

    private PrankerCaller prankerCaller = ObjectFactory.getInstance().createObject(PrankerCaller.class);
    private Spy spy = ObjectFactory.getInstance().createObject(Spy.class);

    public void start(Exam exam) {
        prankerCaller.call("+7(918)-000-00-00", "Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!");
        spy.makeSurePersonLeaveRoom();
        sendOutAnswersOnExam(exam);
    }

    private void sendOutAnswersOnExam(Exam exam) {
        System.out.println("Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать");
    }

}
```
Наша архитектура стала намного чище и теперь `ExamCheatingAssistant` ничего не знает о
деталях реализации используемых интерфейсов.
### Недостатки данной инфраструктуры
Давайте представим ситуацию, что наш программист написал два разных реализации интерфейса
`Spy`: `CalmSpy` и `AngrySpy`. 

```java
public class AngrySpy implements Spy {
    @Override
    public void makeSurePersonLeaveRoom() {
        System.out.println("Хожу мимо кабинета и ворчу: Мне что больше делать нечего");
        System.out.println("Ну наконец он свалил");
    }
}
```

```java
public class CalmSpy implements Spy {
    public void makeSurePersonLeaveRoom() {
        System.out.println("Хожу мимо кабинета");
        System.out.println("!!!Все, вижу как, он ушел!!!");
    }
}
```
Давайте попробуем запустить приложение и посмотрим на результат выполнения:
```
Exception in thread "main" java.lang.RuntimeException: interface ru.tayviscon.service.Spy has zero or more than one impl
	at ru.tayviscon.config.JavaConfig.getImplClass(JavaConfig.java:19)
	at ru.tayviscon.infrastructure.ObjectFactory.createObject(ObjectFactory.java:21)
	at ru.tayviscon.service.ExamCheatingAssistant.<init>(ExamCheatingAssistant.java:9)
	at ru.tayviscon.Main.main(Main.java:8)
```
Наше приложение говорит о том, что не знает какую именно имплементацию подставлять, так как
их у нас теперь две. Поэтому нам необходимо научить наш `Config` выбирать необходимую имплементацию.

Изменим класс `JavaConfig` так, чтобы в конструктор передавался `Map<Class, Class> ifc2ImplClass`,
который содержит информацию о том, какую имплементацию необходимо выбрать для интерфейса.
Метод `getImplClass(Class<T> ifc)` тоже необходимо изменить, так как теперь он сначала будет
ходить в наш `Map<Class, Class> ifc2ImplClass`, смотреть, если ли там информация об интересующем нас
интерфейсе, если да, то будет возвращать её, иначе, будет выполнять код, написанный нами ранее.
```java
public class JavaConfig implements Config {

    private Reflections scanner;

    private Map<Class, Class> ifc2ImplClass;
    public JavaConfig(String packageToScan, Map<Class, Class> ifc2ImplClass) {
        this.ifc2ImplClass = ifc2ImplClass;
        this.scanner = new Reflections(packageToScan);
    }

    @Override
    public <T> Class<? extends T> getImplClass(Class<T> ifc) {
        return ifc2ImplClass.computeIfAbsent(ifc, aClass -> {
            Set<Class<? extends T>> classes = scanner.getSubTypesOf(ifc);
            if(classes.size() != 1) {
                throw new RuntimeException(ifc + " has zero or more than one impl");
            }
            return classes.iterator().next();
        });

    }
}
```
Теперь нам необходимо указать в нашем `ObjectFactory`, данную `Map<Class, Class> ifc2ImplClass`.
```java
private Config config = new JavaConfig("ru.tayviscon", new HashMap<>(Map.of(Spy.class, AngrySpy.class)));
```

Запустим приложение и посморим на результат.
```
Звоню по номеру: +7(918)-000-00-00 ;)
Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!
Хожу мимо кабинета и ворчу: Мне что больше делать нечего
Ну наконец он свалил
Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать
```
### Что за магия происходит в `ObjectFactory`?
Давайте предположим, что у нас появился новый сервис рекламы: `Recommnedator`.
```java
public interface Recommendator {
    void recommend();
}
```

```java
public class RecommendatorImpl implements Recommendator {
    private String socialLinks;
    @Override
    public void recommend() {
        System.out.println("Recommendator: " + socialLinks);
    }
}
```
И мы хотим, чтобы наш `AngrySpy` использовал его.

```java
public class AngrySpy implements Spy {
    
    private Recommendator recommendator = ObjectFactory.getInstance().createObject(Recommendator.class);
    @Override
    public void makeSurePersonLeaveRoom() {
        System.out.println("Хожу мимо кабинета и ворчу: Мне что больше делать нечего");
        System.out.println("Ну наконец он свалил");
        recommendator.recommend();
    }
}
```
Давайте запустим наше приложение и посмотрим, как оно себя поведет.
```
Звоню по номеру: +7(918)-000-00-00 ;)
Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!
Хожу мимо кабинета и ворчу: Мне что больше делать нечего
Ну наконец он свалил
Recommendator: null
Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать

```
`Recommendator` говорит `null`. Это происходит потому, что мы никак не проинициализировали
переменную `socailLinks` в классе `RecommendatorImpl`. Однако давайте заметим тот факт, что 
проинициализировав её в коде, если мы захотим изменить её значение, нам придется залезть в код
и изменить её значение. Однако мы хотим избежать подобного поведения и поэтому создадим аннотацию
`@InjectProperty`, которая будет символизировать, что данное поле необходимо проинициализировать 
при помощи конфигурационного файла `application.properties`.
```java
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectProperty {
    String value() default "";
}
```
Пометим данной аннотацией поле в которое хотим произвести внедрение значения.
```java
public class RecommendatorImpl implements Recommendator {
    @InjectProperty
    private String socialLinks;
    @Override
    public void recommend() {
        System.out.println("Recommendator: " + socialLinks);
    }
}
```
Теперь пойдем в наш `ObjectFactory`, который на данный момент умеет создавать требуемый объект.
Давайте научим его этот объект после создания еще и настраивать. Для этого мы пройдемся по всем полям
объекта, проверим у них наличие аннотации `"@InjectProperty`, и если она есть то внедрим значение из
конфигурационного файла в данное поле.
```java
public class ObjectFactory {

    private Config config = new JavaConfig("ru.tayviscon", new HashMap<>(Map.of(Spy.class, AngrySpy.class)));
    private static ObjectFactory ourInstance = new ObjectFactory();
    public static ObjectFactory getInstance() {
        return ourInstance;
    }

    private ObjectFactory(){}

    @SneakyThrows
    public <T> T createObject(Class<T> type) {
        Class<? extends T> implClass = type;
        if(type.isInterface()) {
            implClass = config.getImplClass(type);
        }
        T t = implClass.getDeclaredConstructor().newInstance();
        // todo тут будет много магии
        for (Field field : implClass.getDeclaredFields()) {
            InjectProperty annotation = field.getAnnotation(InjectProperty.class);
            String path = ClassLoader.getSystemClassLoader().getResource("application.properties").getPath();
            Stream<String> lines = new BufferedReader(new FileReader(path)).lines();
            Map<String, String> propertiesMap = lines.map(line -> line.split("=")).collect(toMap(arr -> arr[0], arr -> arr[1]));
            String value;
            if(annotation != null) {
                if (annotation.value().isEmpty()) {
                    value = propertiesMap.get(field.getName());
                } else {
                    value =propertiesMap.get(annotation.value());
                }
                field.setAccessible(true);
                field.set(t, value);
            }
        }
        return t;
    }
}
```
Запустим наше приложение и проверим результат.
```
Звоню по номеру: +7(918)-000-00-00 ;)
Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!
Хожу мимо кабинета и ворчу: Мне что больше делать нечего
Ну наконец он свалил
Recommendator: @AngrySpyInstagram
Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать****
```
Однако давайте посмотрим на нашу архитектуру класса `ObjectFactory`. Стоит напомнить, что наш
класс задумывался для того, чтобы создавать объекты. Как вы думаете, что будет, если у нас появится
1000 аннотация? Мне кажется, что этот класс станет не читабельным, так как будет огромное количество
условных операторов, отлавливающих свои аннотации. К тому же при добавлении новой аннотации, нам будет
необходимо изменить существующий метод `createObject(Class<T> type)`, что нарушает **Open-Close Principle**.
Стоит также отметить, что **Open-Close Principle** и **Single Responsibility Principle** ломаются вместе.
Но почему же так произошло? Да потому, что наш класс теперь начал отвечать помимо создания объектов еще и за
их конфигурацию.

Давайте воспользуемся паттерном **Chain of Responsibility**, чтобы вернуть нашему коду гибкость.
Для этого, создадим интерфейс `ObjectConfigurator`, который будет отвечать за конфигурирование 
объекта.

```java
public interface ObjectConfigurator {
    void configure(Object t);
}
```
А также реализуем этот интерфейс для аннотации `@InjectProperty`.

```java
public class InjectPropertyAnnotationObjectConfigurator implements ObjectConfigurator {
    
    private Map<String, String> propertiesMap  = new HashMap<>();

    @SneakyThrows
    public InjectPropertyAnnotationObjectConfigurator() {
        String path = ClassLoader.getSystemClassLoader().getResource("application.properties").getPath();
        Stream<String> lines = new BufferedReader(new FileReader(path)).lines();propertiesMap = lines.map(line -> line.split("=")).collect(toMap(arr -> arr[0], arr -> arr[1]));
    }

    @Override
    @SneakyThrows
    public void configure(Object t) {
        Class<?> implClass = t.getClass();
        for (Field field : implClass.getDeclaredFields()) {
            InjectProperty annotation = field.getAnnotation(InjectProperty.class);
            String value;
            if(annotation != null) {
                if (annotation.value().isEmpty()) {
                    value = propertiesMap.get(field.getName());
                } else {
                    value =propertiesMap.get(annotation.value());
                }
                field.setAccessible(true);
                field.set(t, value);
            }
        }
    }
}
```
Теперь нам необходимо "Подружить" `ObjectFactory` с `ObjectConfigurator`. Для этого создадим
в нем `List<ObjectConfigurator>`. Таким образом, наша фабрика при создании должна найти все
реализации интерфейса `ObjectConfigurator`, для этого нам необходимо научить наш `Confg` отдавать
свой настроенный `scanner`.

```java
public interface Config {
    <T> Class<? extends T> getImplClass(Class<T> ifc);
    Reflections getScanner();
}
```

```java
public class JavaConfig implements Config {

    @Getter
    private Reflections scanner;

    private Map<Class, Class> ifc2ImplClass;
    public JavaConfig(String packageToScan, Map<Class, Class> ifc2ImplClass) {
        this.ifc2ImplClass = ifc2ImplClass;
        this.scanner = new Reflections(packageToScan);
    }

    @Override
    public <T> Class<? extends T> getImplClass(Class<T> ifc) {
        return ifc2ImplClass.computeIfAbsent(ifc, aClass -> {
            Set<Class<? extends T>> classes = scanner.getSubTypesOf(ifc);
            if(classes.size() != 1) {
                throw new RuntimeException(ifc + " has zero or more than one impl");
            }
            return classes.iterator().next();
        });

    }
}
```
В конструкторе класса `ObjectFactory` мы настраиваем конфигуратор, забираем у него сканер и
ищем все реализации `ObjectConfigurator` и заполняем ими `List<ObjectConfigurator>`. Затем в
методе `createObject(Class<T> type)` после создания объекта нам необходимо пройтись по всем
конфигураторам и вызвать и них метод `configure(Object t)` на нашем созданном объекте.
```java
public class ObjectFactory {

    private List<ObjectConfigurator> configurators = new ArrayList<>();

    private Config config;
    private static ObjectFactory ourInstance = new ObjectFactory();
    public static ObjectFactory getInstance() {
        return ourInstance;
    }

    @SneakyThrows
    private ObjectFactory(){
        config = new JavaConfig("ru.tayviscon", new HashMap<>(Map.of(Spy.class, AngrySpy.class)));
        for (Class<? extends ObjectConfigurator> aClass : config.getScanner().getSubTypesOf(ObjectConfigurator.class)) {
            configurators.add(aClass.getDeclaredConstructor().newInstance());
        }
    }

    @SneakyThrows
    public <T> T createObject(Class<T> type) {
        Class<? extends T> implClass = type;
        if(type.isInterface()) {
            implClass = config.getImplClass(type);
        }
        T t = implClass.getDeclaredConstructor().newInstance();
        
        for (ObjectConfigurator configurator : configurators) {
            configurator.configure(t);
        }

        return t;
    }
}
```
### Ещё один недостаток данной архитектуры
Давайте представим, что начальник нам сказал: Новая версия Java плохо взаимодействует с Lombok, 
поэтому давайте уберем все аннотации этой библиотеки, в частности `@SneakyThrows` с метода 
`createObject(Class<T> type)` в классе `ObjectFatory` и теперь будем стандартно пробрасывать 
все исключения. В связи с этим все классы использующие `ObjectFactory` для создания объектов
перестанут компилироваться, так как мы не обрабатываем исключения. Таким образом, мы приходим
к выводу, что произошла утечка инфраструктуры внутрь бизнес логики, так как каждый класс,
использующий `ObjectFactory` знает его API.

Чтобы исправить этот недостаток, давайте создадим аннотацию `@InjectByType`, которую будем
ставить над полями, нуждающимися во внедрение объекта.
```java
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectByType {
}
```
Имплементриуем интерфейс `ObjectConfigurator` и создадим класс `InjectByTypeAnnotationObjectConfigaurator`, 
который будем проходить по всем полям класса и искать у них соответствующую аннотацию,
а при помощи `ObjectFactory` создавать необходимый экземпляр на основании типа поля.
```java
public class InjectByTypeAnnotationObjectConfigurator implements ObjectConfigurator {
    @Override
    @SneakyThrows
    public void configure(Object t) {
        for (Field field : t.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectByType.class)) {
                Object object = ObjectFactory.getInstance().createObject(field.getType());
                field.setAccessible(true);
                field.set(t, object);
            }
        }
    }
}
```
Стоит отметить, что мы только создали аннотацию и написали к ней реализацию, а она уже используется
нашей фабрикой для настройки объектов. Теперь класс `ExamCheatingAssistant` выглядит следующим образом:
```java
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
```
При этом, как можно увидеть теперь наш класс абсолютно чист, у него есть только одна зависимость,
а мы думаем лишь о реализации бизнес-логики. Однако, если мы попробуем запустить приложение, то
получим исключение:
```
Exception in thread "main" java.lang.NullPointerException: Cannot invoke "ru.tayviscon.service.PrankerCaller.call(String, String)" because "this.prankerCaller" is null
	at ru.tayviscon.service.ExamCheatingAssistant.start(ExamCheatingAssistant.java:15)
	at ru.tayviscon.Main.main(Main.java:9)
```
Причем это исключение появляется по очень смешной причине. Давайте посмотрим наш класс `Main`,
в котором экземпляр `ExamCheatingAssistant` создается при помощи `new`.
```java
public class Main {
    public static void main(String[] args) {
        ExamCheatingAssistant assistant = new ExamCheatingAssistant();
        assistant.start(new Exam());
    }
}
```
Таким образом, как только у нас появляется **Инверсия Контроля** нельзя больше классы
создавать через `new`. Давайте перепишем класс `Main`.
```java
public class Main {
    public static void main(String[] args) {
        ExamCheatingAssistant assistant = ObjectFactory.getInstance().createObject(ExamCheatingAssistant.class);
        assistant.start(new Exam());
    }
}
```
### Проблема Singleton
Большинство сервисов, которые мы пишем должны быть singleton, ибо зачем нам создавать, а затем
копить экземпляры классов, если мы можем пользоваться одним экземпляром.

Для примера давайте в классе `RecommendatorImpl` создадим конструктор, который будет выводить 
информацию о том, что объект был создан.

```java
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
```
И представим, что наш класс `ConsolePrankerCaller` тоже хочет пользоваться нашим `RecommendatorImpl`:
```java
public class ConsolePrankerCaller implements PrankerCaller {
    @InjectByType
    private Recommendator recommendator;
    public void call(String phoneNumber, String message) {
        System.out.println("Звоню по номеру: " + phoneNumber + " ;)");
        System.out.println(message);
    }
}
```
Давайте теперь запустим приложение и посмотрим на результат:
```
Recommendator was created
Recommendator was created
Звоню по номеру: +7(918)-000-00-00 ;)
Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!
Хожу мимо кабинета и ворчу: Мне что больше делать нечего
Ну наконец он свалил
Recommendator: @AngrySpyInstagram
Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать
```
Как мы видим наш `Recommendator` было создан два раза. Давайте научим нашу инфраструктуру
кешировать классы помеченные аннотацией `@Singleton`. Для этого, создадим аннотацию:
```java
@Retention(RetentionPolicy.RUNTIME)
public @interface Singleton {
}
```
Создадим класс `ApplicationContext`, который в конструктор в качестве параметра будет принимать
`Config`, а также будет иметь метод `getObject(Class<T> type)`, для получения объекта из контекста.
Стоит также отметить, что именно `ApplicationContext` будет кешировать объекты, помеченные аннотацией
`@Singleton`. Таким образом метод `getObject(Class<T> type)` будет проверять, сохранен ли уже объект 
этого типа в кеше, если да, то вернет его, иначе сходит в конфиг и узнает какая имплементация нужна,
затем на её основе создаст объект и, проверив у него наличия аннотации `@Singleton`  либо кеширует его,
либо нет.
```java
public class ApplicationContext {
    @Getter
    private Config config;
    @Setter
    private ObjectFactory factory;
    private Map<Class, Object> cache = new ConcurrentHashMap<>();;

    public ApplicationContext(Config config) {
        this.config = config;
    }

    public <T> T getObject(Class<T> type) {
        if (cache.containsKey(type)) {
            return (T) cache.get(type);
        }
        Class<? extends T> implClass = type;
        if(type.isInterface()) {
            implClass = config.getImplClass(type);
        }

        T t = factory.createObject(implClass);

        if(implClass.isAnnotationPresent(Singleton.class)){
            cache.put(type, t);
        }
        return t;

    }
}
```
Давайте теперь внесем изменения в класс `ObjectFactory`, который теперь должен зависеть от `ApplicationContext`
и вот почему: посмотрите на класс `InjectByTypeAnnotaitonObjectConfigurator`, который создает объекты при помощи
`ObjectFactory`, а наша логика по кешированию находится в классе `ApplicationContext`, таким образом наш объект
должен создаваться при помощи метода `getObject(Class<T> type)` класса `ApplicationContext`. Именно поэтому класс 
`InjectByTypeAnnotationObjectConfigurator` в качестве параметра должен принимать `ApplicationContext` и создавать с
его помощью объекты. А я вам напомню, кто вызывает методы класса `InjectByTypeAnnotationObjectConfigurator` это класс
`ObjectFactory`. Давайте теперь перепишем некоторые классы:

```java
public interface ObjectConfigurator {
    void configure(Object t, ApplicationContext context);
}
```
```java
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
```
```java
public class ObjectFactory {

    private List<ObjectConfigurator> configurators = new ArrayList<>();
    private ApplicationContext context;
    @SneakyThrows
    public  ObjectFactory(ApplicationContext context){
        this.context = context;
        for (Class<? extends ObjectConfigurator> aClass : context.getConfig().getScanner().getSubTypesOf(ObjectConfigurator.class)) {
            configurators.add(aClass.getDeclaredConstructor().newInstance());
        }
    }

    @SneakyThrows
    public <T> T createObject(Class<T> implClass) {

        T t = implClass.getDeclaredConstructor().newInstance();
        // todo тут будет много магии
        for (ObjectConfigurator configurator : configurators) {
            configurator.configure(t, context);
        }

        return t;
    }
}
```
Стоит также отметить, что у нас появилась циклическая зависимость класс `ApplicationContext`
зависит от `ObjectFactory`, а `ObjectFactory` в свою очередь зависти от `ApplicationContext`.
Поэтому давайте создадим класс `Application` с методом `run(String packageToScan, Map<Class, Class>) ifc2ImplClass`.
```java
public class Application {
    public static ApplicationContext run(String packageToScan, Map<Class,Class> ifc2ImplClass) {
        Config config = new JavaConfig(packageToScan, ifc2ImplClass);
        ApplicationContext context = new ApplicationContext(config);
        ObjectFactory objectFactory = new ObjectFactory(context);
        context.setFactory(objectFactory);
        return context;
    }
}
```
Теперь запуск приложения будет выглядеть следующим образом:
```java
public class Main {
    public static void main(String[] args) {
        ApplicationContext context = Application.run("ru.tayviscon", new HashMap<>(Map.of(Spy.class, AngrySpy.class)));
        ExamCheatingAssistant assistant = context.getObject(ExamCheatingAssistant.class);
        assistant.start(new Exam());
    }
}
```
Давайте запустим приложение и просмотрим на результат:
Приложение падает! Для того, чтобы это исправить необходимо перейти в класс `AngrySpy` и увидеть,
что для создания объекта `Recommendator` необходимо воспользоваться аннотацией `@InjectByType`, а не 
создавать объект через `ObjectFactory`. Данная ошибка иллюстрирует факт того, что чем позже у нас появляется
инверсия контроля, тем больше ошибок придется исправить.

Давайте запустим приложение и просмотрим на результат:
```
Recommendator was created
Recommendator was created
Звоню по номеру: +7(918)-000-00-00 ;)
Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!
Хожу мимо кабинета и ворчу: Мне что больше делать нечего
Ну наконец он свалил
Recommendator: @AngrySpyInstagram
Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать
```
Ничего не поменялось, но мы ведь и аннотацию `@Singleton` над классом `RcommendatorImpl` не поставили:
```java
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
```
Давайте запустим приложение и просмотрим на результат:
```
Recommendator was created
Звоню по номеру: +7(918)-000-00-00 ;)
Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!
Хожу мимо кабинета и ворчу: Мне что больше делать нечего
Ну наконец он свалил
Recommendator: @AngrySpyInstagram
Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать
```
Как мы видим на этот раз экземпляр класса `RecommendatorImpl` создался ровно один раз,
а значит наша аннотация работает.
### А я тут конструктор попробовал создать и все сломалось!!!
Давайте в классе `AngrySpy` создадим конструктор и попробуем вывести конкретный экземпляр внедренного
`Recommendator`:
```java
public class AngrySpy implements Spy {

    @InjectByType
    private Recommendator recommendator;

    public AngrySpy() {
        System.out.println(recommendator.getClass());
    }

    @Override
    public void makeSurePersonLeaveRoom() {
        System.out.println("Хожу мимо кабинета и ворчу: Мне что больше делать нечего");
        System.out.println("Ну наконец он свалил");
        recommendator.recommend();
    }
}
```
Давайте запустим приложение и просмотрим на результат:
```
Recommendator was created
Exception in thread "main" java.lang.reflect.InvocationTargetException
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:499)
	at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:480)
	at ru.tayviscon.infrastructure.ObjectFactory.createObject(ObjectFactory.java:37)
	at ru.tayviscon.infrastructure.ApplicationContext.getObject(ApplicationContext.java:31)
	at ru.tayviscon.annotation.handlers.InjectByTypeAnnotationObjectConfigurator.configure(InjectByTypeAnnotationObjectConfigurator.java:16)
	at ru.tayviscon.infrastructure.ObjectFactory.createObject(ObjectFactory.java:40)
	at ru.tayviscon.infrastructure.ApplicationContext.getObject(ApplicationContext.java:31)
	at ru.tayviscon.Main.main(Main.java:15)
Caused by: java.lang.NullPointerException: Cannot invoke "Object.getClass()" because "this.recommendator" is null
	at ru.tayviscon.service.AngrySpy.<init>(AngrySpy.java:13)
	... 11 more

```
Проблема в том, что мы внедряем наши объекты через поля, а значит сначала создается объект
и лишь потом внедряются значения. Простыми словами: мы не можем воспитать, еще не родившегося ребенка.

Для решения этой проблемы используются методы `init()`, для того, чтобы пометить метод как инициализирующий,
мы будем использовать аннотацию `@PostConstract`:
```java
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstract {
}
```
Теперь класс `AngrySpy` выглядит следующим образом:
```java
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
```
Нам также необходимо добавить обработку этой аннотации в `ObjectFactory`:
```java
public class ObjectFactory {

    private List<ObjectConfigurator> configurators = new ArrayList<>();
    private ApplicationContext context;
    @SneakyThrows
    public  ObjectFactory(ApplicationContext context){
        this.context = context;
        for (Class<? extends ObjectConfigurator> aClass : context.getConfig().getScanner().getSubTypesOf(ObjectConfigurator.class)) {
            configurators.add(aClass.getDeclaredConstructor().newInstance());
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

        return t;
    }
}
```
Давайте запустим приложение и просмотрим на результат:
```
Recommendator was created
class ru.tayviscon.service.RecommendatorImpl
Звоню по номеру: +7(918)-000-00-00 ;)
Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!
Хожу мимо кабинета и ворчу: Мне что больше делать нечего
Ну наконец он свалил
Recommendator: @AngrySpyInstagram
Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать
```
### Проксирование объектов
Давайте представим, что мы хотим писать в консоль какой-то вывод, если пользователь использует 
`@Deprecated` класс. Для этого пометим класс `ConsolePrankerCaller` соответствующей аннотацией:
```java
@Deprecated
public class ConsolePrankerCaller implements PrankerCaller {
    @InjectByType
    private Recommendator recommendator;
    public void call(String phoneNumber, String message) {
        System.out.println("Звоню по номеру: " + phoneNumber + " ;)");
        System.out.println(message);
    }
}
```
Также создадим новый тип конфигураторов - `ProxyConfigurator` в качестве интерфейса, и сразу
же его имплементируем в классе `DeprecatedhandlerProxyConfigurator`:
```java
public interface ProxyConfigurator {
    Object warpWithProxy(Object t, Class implClass);
}
```
```java
public class DeprecatedHandlerProxyConfigurator implements ProxyConfigurator {
    @Override
    public Object warpWithProxy(Object t, Class implClass) {
        if (implClass.isAnnotationPresent(Deprecated.class)) {
            return Proxy.newProxyInstance(implClass.getClassLoader(), implClass.getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("Что же ты делаешь? Не стоит использовать Deprecated классы");
                    return method.invoke(t,args);
                }
            });
        } else {
            return t;
        }
    }
}
```
Теперь наш `ProxyConfigurator` необходимо подружить с `ObjectFactory`. Для этого заведем
`List<ProxyConfigurator>` и по аналогии с `ObjectConfigurator` в конструкторе `ObjectFactory`
найдем все реализации `ProxyConfigurator` и заполним им `List<ProxyConfigurator>`. А затем после
вызова init методов пройдемся по коллекции `List<ProxyConfigurator>`.
```java
public class ObjectFactory {

    private List<ProxyConfigurator> proxyConfigurators = new ArrayList<>();
    private List<ObjectConfigurator> configurators = new ArrayList<>();
    private ApplicationContext context;

    @SneakyThrows
    public ObjectFactory(ApplicationContext context) {
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
```
Давайте запустим приложение и просмотрим на результат:
```
Recommendator was created
class ru.tayviscon.service.RecommendatorImpl
Что же ты делаешь? Не стоит использовать Deprecated классы
Звоню по номеру: +7(918)-000-00-00 ;)
Здравствуйте, вас беспокоит начальник, вы мне срочно нужны, бросайте все дела и быстро ко мне!!!
Хожу мимо кабинета и ворчу: Мне что больше делать нечего
Ну наконец он свалил
Recommendator: @AngrySpyInstagram
Рассылка ответов всем студентам - Все ответы разосланы, можно отдыхать
```