package ru.pragm;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static ru.pragm.Ex103_Add.testStaticImport;
import static ru.pragm.Ex103_StaticEnum.STATIC_ONE;

//------------------------------------------------------------------------------------
//What is the first thing that a consructor does? - Cracking the Java Coding Interview
class Ex159{
    static class Zero{
        String message;
        Zero(String partA, String partB){this(partA + partB);}
        Zero(String _message){message = _message;}
        void printMessage(){System.out.println(message);}
    }
    static class One extends Zero{
        One(){super("This is", " the message"); System.out.println(111);}
    }
    static class Two extends One{
        Two(){System.out.println(222);}
    }
    public static void main(String[] args){
        // <- первым делом вызовется конструктор
        //    из класса родителя
        var two = new Two();
        two.printMessage();
    }
}

//----------------------------------------------------------------------------
//How is the Map.values() method working? - Cracking the Java Coding Interview
class Ex158{
    public static void main(String[] args){
        HashMap<String,String> map = new HashMap<>(
                Map.of(
                        "first","111",
                        "second","222",
                        "third","333",
                        "doubleSecond","222"
                )
        );
        System.out.println(map.values());
        // <- .values() мутабелен - то есть можно удалять элементы
        //    удаление скажется на удалении первого найденного
        map.values().remove("222");
        System.out.println(map.values());
    }
}

//----------------------------------------------------------------------------------
//How can you create a Directory with Java I/O? - Cracking the Java Coding Interview
class Ex157{
    public static void main(String[] args) throws IOException{
        String basePath = "/home/x057/000__KOLBASKINS/000__ПРАКТИКА/";
        String newDirectory = "newDirectory/";
        var fileAttributes = PosixFilePermissions
                .asFileAttribute(
                        PosixFilePermissions.fromString("rwxr-xr-x")
                );
        Path   dir      = Path.of(basePath + newDirectory);
        Path createdDir = null;
        try{
            // <- не будет выкидывать ошибку без fileAttributes + createDirectories
            //    createDirectories - будет создавать все промужуточные директории
            createdDir = Files.createDirectory(dir, fileAttributes);
            System.out.println(createdDir.getFileName());
            System.out.println(createdDir);
        }
        catch(Exception e){
            System.out.println("directory already exists");
        }
    }
}

// --------------------------------------------------------------------------------------------
// Differences between an interface and an abstract class? - Cracking the Java Coding Interview
class Ex156{
    static abstract class AbstractExample{
        private String param;
        static String stParam = "static param";
        AbstractExample(String _param){
            param = _param;
        }
        static void doSome(String _param){
            System.out.println(_param);
            System.out.println(stParam);
        }
    }

    interface InterfaceExample{
        // <- can't have a constructor
        static String stParam = "static param";
        void doSome();
        static void doStaticSome(){
            System.out.println(stParam);
        }
    }

    static class Extender extends AbstractExample{
        Extender(String _param){
            super(_param);
        }
    }

    static class Implementor implements InterfaceExample{
        @Override
        public void doSome(){
            System.out.println(stParam);
        }
    }

    public static void main(String[] args){
        var extender = new Extender("some string");
        extender.doSome("some param");
        var implementer = new Implementor();
        implementer.doSome();
    }
}

//----------------------------------------------------------------------------------------
//Why should you prefer private fields and accessors? - Cracking the Java Coding Interview
class Ex155{
    private String one;
    private String two;
    Ex155(){
        one = "111"; two = "222";
    }
    String viewOne(){return one;}
    String viewTwo(){return two;}

    public static void main(String[] args){
        var obj = new Ex155();
        String[] arr = {
                obj.viewOne(),
                obj.viewTwo()
        };
        System.out.println(Arrays.asList(arr));
    }
}

//-----------------------------------------------------------
//What is a MethodHandle - Cracking the Java Coding Interview
class Ex154{
    static class User{
        String name;
        User(String _name){name = _name;}
        String name(){return name;}
    }

    public static void main(String[] args) throws Throwable{
        var lookup = MethodHandles.lookup();
        var constructorMH = lookup.findConstructor(
                User.class,
                MethodType.methodType(void.class,
                                      String.class
                )
        );
        var maria = (User)constructorMH.invokeExact("Maria");
        System.out.println(maria.name());

        var getterMH = lookup.findVirtual(
                User.class,
                "name",
                MethodType.methodType(String.class));
        // <- используется для контекста объекта "maria"
        var name = (String)getterMH.invokeExact(maria);
        System.out.println(name);
    }
}

//---------------------------------------------------------
//What is a Semaphore? - Cracking the Java Coding Interview
class Ex153{

    public static void main(String[] args) throws InterruptedException{
        int numberOfPermits = 4;
        boolean beFair = true; // <- первый заблокированный получит первым доступ
        var semaphore = new Semaphore(numberOfPermits, beFair);

        // <- защищаем блок кода
        semaphore.acquire();
        try{
            System.out.println("from protected code");
        // <- освобождение лучше ставить в finally
        //    иначе застрянем в блоке кода навсегда
        }
        finally{
            semaphore.release();
        }
    }
}

//---------------------------------------------------------------
//How can you loop backward? - Cracking the Java Coding Interview
class Ex152{
    public static void main(String[] args){
        // метода работает лишь для List типа
        List<String> list = new ArrayList<>(List.of("one","two","three"));
        // <- обязательно требуется указать размерность для итератора:
        var iterator = list.listIterator(list.size());

        while(iterator.hasPrevious()){
            var element = iterator.previous();
            System.out.println(element);
        }
    }
}

//----------------------------------------------------------
//What is a text block? - Cracking the Java Coding Interview
class Ex151{
    public static void main(String[] args){
        // <- с помощью \s отбивки мы устанавливаем конечные
        //    пробелы в строку (они войдут до отбивки \s)
        String textBlock = """
        this is a text block   \s
        """;

        // <- Используем шаблонизатор STR для вставки переменной textBlock в шаблон
        // никак шаблонизатор не примут - так что ставим preview в компиляторе
        String textBlockTemplate = STR."""
            beginning
            <code>"HTML"</code>
            \{textBlock}
            ending""";

        System.out.println(textBlockTemplate);
    }
}

//---------------------------------------------------------------
//How is quick sort working? - Cracking the Java Coding Interview
class Ex150{
    public static void main(String[] args){
        // <- а для того чтобы сортировать что угодно
        //    в виде объектов - нужен компаратор
        int[] arr = {1,2,3,4,5,1,2,3};
        Arrays.sort(arr);
        for(var el : arr){
            System.out.println(el);
        }
    }
}

//-----------------------------------------------------------------
//What is the filter() method? - Cracking the Java Coding Interview
class Ex149{
    public static void main(String[] args){
        // <- фильтруем стрим с помощью предиката:
        ArrayList<String> list = new ArrayList<>(List.of("one","one","two"));
        var filterResult = list.stream().filter(el -> {
            return el.equals("one");
        }).collect(Collectors.toList());
        System.out.println(filterResult);
    }
}

//--------------------------------------------------------------------------------------
//How can you create a collection from an Iterable? - Cracking the Java Coding Interview
class Ex148{
    public static void main(String[] args){
        // <- помним что итератор (а точнее итерабл интерфейс)
        //    мы в принципе можем добавить на что угодно
        //    не путаем: iterator - это то что внутри итерабл (то есть сам итератор)
        //    iterable - это любой объект поддерживающий итерабл интерфейс - например тот же лист
        List<String> list = new ArrayList<>(List.of("0", "1", "2", "3"));
        Iterable<String>    iterable    = list;
        Spliterator<String> spliterator = iterable.spliterator();

        // <- поток как мы помним можно запустить и параллельно:
        Stream<String> stream = StreamSupport.stream(spliterator, false);
        stream.forEach(el -> System.out.println(el));
    }
}

//----------------------------------------------------
//What is a Lock? - Cracking the Java Coding Interview
class Ex147{
    public static void main(String[] args){
        var lock = new ReentrantLock();
        try{
            lock.lock();
            // <- так как здесь поток один ошибки не будет
            //    а вот если потоков будет несколько получим ошибку
            lock.tryLock(1, TimeUnit.SECONDS);
            System.out.println("from the locked part of the code");
        }catch(InterruptedException e){
            System.out.println("second lock has failed");
        }finally{
            lock.unlock();
        }
    }
}

//-------------------------------------------------------
//What is a toMap()? - Cracking the Java Coding Interview
class Ex146{
    public static void main(String[] args){
        enum City{MOSCOW, SPB}
        class User{
            String name;
            City city;
            User(String _name, City _city){
                name = _name; city = _city;
            }
            String getName(){return name;}
            City getCity(){return city;}

            @Override
            public String toString(){
                return getCity() + ":" + getName();
            }
        }
        ArrayList<User> users = new ArrayList<>(List.of(
                new User("Ivan",City.MOSCOW),
                new User("Maria",City.SPB),
                new User("Vadim",City.MOSCOW)
        ));
        var mapA = users.stream()
                // <- ключи должны быть уникальными:
                // <- первая функция собирает ключ, вторая функция собирает значение
                .collect(Collectors.toMap(User::getName, User::getCity));
        System.out.println(mapA);

        // <- вот такая вот нездоровая конструкция
        //    нужна чтобы группировку сделать по какому то признаку
        //    заметим, что здесь собираются строки
        var mapB = users.stream()
                .collect(
                        Collectors.groupingBy(
                                User::getCity,
                                Collectors.mapping(User::getName,Collectors.toList())
                        )
                );
        System.out.println(mapB);

        // <- а здесь же будут собираться не строки - а целые объекты:
        var mapС = users.stream()
                .collect(
                        Collectors.groupingBy(User::getCity)
                );
        System.out.println(mapС);

        // <- с другой стороны можно просто HashMap тащить вниз по стриму:
        //    как видим тут все значительно читабельней:
        HashMap<City, ArrayList<String>> cityToUsername = new HashMap<>();
        users.stream().forEach(el -> {
           if(!cityToUsername.containsKey(el.getCity()))
               cityToUsername.put(el.getCity(),new ArrayList<>());
           cityToUsername.get(el.getCity()).add(el.getName());
        });
        System.out.println(cityToUsername);
    }
}

//------------------------------------------------------------
//What is a fall through? - Cracking the Java Coding Interview
class Ex145{
    public static void main(String[] args){
        enum DayOfWeek{Monday, Tuesday, Thursday, Friday, Saturday, Sunday}
        var day = DayOfWeek.Monday;
        switch(day){
            case Monday, Tuesday, Thursday, Friday:
                System.out.println("go to school");
                break;  // <- забытие элемента и вызывает "fall through"
            case Saturday, Sunday:
                System.out.println("holidays");break;
        }
        // <- тоже самое, но с использованием switch expression:
        var whatToDo = switch(day){
            case Monday, Tuesday, Thursday, Friday -> "go to school";
            case Saturday, Sunday -> "holiday";
            // <- просто так не получится задать
            //    throw new Exception(..)
            default -> throw new IllegalStateException("Invalid day");
        };
        System.out.println(whatToDo);
    }
}

//----------------------------------------------------------
//What is an entry set? - Cracking the Java Coding Interview
class Ex144{
    public static void main(String[] args){
        HashMap<String, String> map = new HashMap<>(Map.of(
                "one","111",
                "two","222",
                "thr","333"));
        System.out.println(map);
        //var entries = map.entrySet();
        //entries.clear();
        //System.out.println(map);
        for(var iterator = map.entrySet().iterator(); iterator.hasNext(); ){
            var entry = iterator.next();
            System.out.println(entry);
            // <- через итератор можно дропнуть элемент из мэпа:
            if(entry.getKey().equals("one")) iterator.remove();
        }
        System.out.println(map);
    }
}

//--------------------------------------------------------------
//What is a Fork Join pool? - Cracking the Java Coding Interview
class Ex143{
    public static void main(String[] args){
        // fork-join pool is a main thread pool
        Thread thrA = new Thread(()-> {
            System.out.println("thrA");
        });
        Thread thrB = new Thread(()->{
            System.out.println("thrB");
        });
        // <- отправиться в fork-join pool напрямую
        thrA.start();
        thrB.start();
    }
}

//---------------------------------------------------------
//What is a deep copy? - Cracking the Java Coding Interview
class Ex142{
    private static <T extends Serializable> T deepCopy(T obj) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (T) ois.readObject();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException{
        // shallow copy - копирует ссылки
        // deep copy - копирует память
        // самый логичный вариант сделать гарантированный deep copy - это сериализация + восстановление
        class Address implements Serializable{
            String street;
            int building;
        }
        class User implements Serializable{
            String name;
            Address address;
        }

        var originalUser = new User();
        originalUser.name = "Ivan";
        originalUser.address = new Address();
        originalUser.address.street = "First Ave.";
        originalUser.address.building = 100;

        var copyUser = deepCopy(originalUser);
        originalUser.address.street = "Second Ave.";
        System.out.println(copyUser.address.street);
        System.out.println(originalUser.address.street);
    }
}

//-----------------------------------------------------
//What is a Deque? - Cracking the Java Coding Interview
class Ex141{
    public static void main(String[] args){
        // <- может быть BlockingDeque
        //    который будет блокироваться до тех пор пока элемент
        //    успешно не добавиться
        var arrDeque = new ArrayDeque<String>();
        arrDeque.add("one");
        arrDeque.add("two");
        arrDeque.add("thr");
        var latest = arrDeque.removeLast();
        var first = arrDeque.removeFirst();
        System.out.println(first);
        System.out.println(arrDeque);
        System.out.println(latest);
    }
}

//-------------------------------------------------------------------
//What is a Compact Constructor? - Cracking the Java Coding Interview
class Ex140{
    public static void main(String[] args){
        record User(String name){
            // было:
            //User(String name){
            //    this.name = Objects.requireNonNull(name);
            //}
            // стало:
            User{
                name = Objects.requireNonNull(name);
            }
        }
        var user = new User("Ivan");
        System.out.println(user);
        // сам по себе конструктор может использоваться
        // для того чтобы делать например защитную копию в памяти:
        class City{
            String name;
        }
        record Country(City[] cities){
            Country{
                City[] citiesCopy = new City[cities.length];
                for(int i = 0; i<cities.length; i++){
                    citiesCopy[i] = cities[i];
                }
                cities = citiesCopy;
            }
        }
    }
}

//------------------------------------------------------------------------
//What is the diamond problem in OOP? - Cracking the Java Coding Interview
class Ex139{
    public static void main(String[] args){
        interface OneAble{
            default void doProblem(){
                System.out.println("OneAble");
            }
            default void noProblem(){
                System.out.println("no problem");
            }
        }
        interface TwoAble{
            default void doProblem(){
                System.out.println("TwoAble");
            }
        }
        class ProblemMe implements OneAble,TwoAble{
            @Override
            public void doProblem(){
                // <- дополнительный классификатор
                //    выход через super
                OneAble.super.doProblem();
                TwoAble.super.doProblem();
                noProblem(); // <- работает прямо так
            }
        }
        var pm = new ProblemMe();
        pm.doProblem();
    }
}

//--------------------------------------------------------------------------------
//Difference between Enumeration and Iterator - Cracking the Java Coding Interview
class Ex138{
    public static void main(String[] args){
        // Enumeration старый формат данных померший
        // до момента появления коллекций
        // сейчас используется лишь итератор
        ArrayList<Integer> list = new ArrayList<>(List.of(1,2,3,4,5));
        var iterator = list.stream().iterator();
        while(iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
}

//-------------------------------------------------------------
//What is a for each loop? - Cracking the Java Coding Interview
class Ex137{
    public static void main(String[] args){
        // в случае если в элементе есть поддержка
        // Iterable - его перебирать можно с помощью for-each
        // JVM создает итератор - и проходится по нему как в примере выше
        var list = List.of(1,2,3,4,5);
        for(var el : list){
            System.out.println(el);
        }

        // итерабл объекты также содержат .forEach
        // с консюмером внутри - через лямбду:
        list.forEach(System.out::println);
        // или через тип:
        Consumer<Integer> consumer = System.out::println;
        list.forEach(consumer);
    }
}

//--------------------------------------------------------------
//What is a defensive copy? - Cracking the Java Coding Interview
class Ex136{
    // вообще defensive copy это название паттерна когда ты делаешь
    // новый объект по факту его получения или возврата
    static ArrayList<String> doDefensiveCopy(ArrayList<String> list){
        return new ArrayList<>(list);
    }

    public static void main(String[] args){
        ArrayList<String> listA = new ArrayList<>(List.of("1","2","3"));
        var listB = doDefensiveCopy(listA);
        System.out.println(List.of(listA, listB));
        listA.set(0,"111");
        System.out.println(List.of(listA, listB));
    }
}

//-------------------------------------------------------------------------------
//What type of ExecutorService can you cite? - Cracking the Java Coding Interview
class Ex135{
    public static void main(String[] args) throws ExecutionException, InterruptedException{
        var oneThreadA = Executors.newSingleThreadExecutor();
        // <- без return это runnable
        oneThreadA.submit(()->{
            System.out.println("task from the executor service");
        });
        // <- не забываем отключать сервисы
        //    иначе программа продолжит выполняться
        oneThreadA.shutdownNow();

        // <- аналогичный из стандартного спринга
        var fixedThreadPool = Executors.newFixedThreadPool(5);
        // <- с return это callable:
        //    как видим параметры легко отъезжают в потоки
        //    однако учитываем историю с кешированиями
        String toThread = "put me to thread";
        ArrayList<String> putMeToThread = new ArrayList<>(List.of("one","two"));
        AtomicReference<ArrayList<String>> listAtomicReference = new AtomicReference<>(
                new ArrayList<>(List.of(">1",">2",">3"))
        );
        var future = fixedThreadPool.submit(() -> {
            // <- как понимаем здесь решение более предсказуемое
            System.out.println(listAtomicReference.get());
            System.out.println(toThread);
            System.out.println(putMeToThread);
            System.out.println("task with the return");
            return 12;
        });
        System.out.println(future.get());
        fixedThreadPool.shutdownNow();

        var factory = Thread.ofVirtual().name("thread name").factory();
        var thread = factory.newThread(()->{
            System.out.println("virtual thread");
        });
        System.out.println(thread.getName());
        thread.start();

        // <- немного подождем виртуальный поток чтобы стартанул:
        Thread.sleep(100L);
    }
}

//----------------------------------------------------------------------------------
//What is an entry, and how can you create one? - Cracking the Java Coding Interview
class Ex134{
    public static void main(String[] args){
        HashMap<String, Integer> map = new HashMap<>(Map.of("one",111,"two",222));
        for(var entry : map.entrySet()){
            System.out.println(entry);
        }
        var newEntry = Map.entry("thr",333);
        // <- вообще корректная единица
        Map.Entry<String,Integer> newEntry2 = Map.entry("fou",444);

        var newMap = Map.ofEntries(newEntry,newEntry2);
        System.out.println(newMap);

        // <- объединение мэпов:
        map.putAll(newMap);
        System.out.println(map);
    }
}

//----------------------------------------------------------------------------------------
//What are the main operations of a stack or a queue? - Cracking the Java Coding Interview
class Ex133{
    public static void main(String[] args){
        // queue / stack отличаются порядком добавляемых элементов
        // хотя .pop / .peek будут работать аналогично
        Deque<String> deque = new ArrayDeque<>();
        deque.push("one");
        deque.push("two");
        deque.push("thr");
        System.out.println(deque);
        //---
        Stack<String> stack = new Stack<>();
        stack.push("one");
        stack.push("two");
        stack.push("thr");
        System.out.println(stack);
        //---
        System.out.println(deque.peek());
        deque.pop();
        System.out.println(deque);

        System.out.println(stack.peek());
        stack.pop();
        System.out.println(stack);
    }
}

//---------------------------------------------------------
//What is a Component? - Cracking the Java Coding Interview
class Ex132{
    public static void main(String[] args){
        // component это составная часть record
        // получаемая с помощью рефлексии
        record User(String name, Integer age){}
        var components = User.class.getRecordComponents();
        for(var comp : components){
            System.out.println(comp);
            System.out.println(comp.getName());
        }
    }
}

//------------------------------------------------------------------
//What does encapsulation mean? - Cracking the Java Coding Interview
class Ex131{
    public static void main(String[] args){
        // доступ ко всем стейтам достигается через методы:
        class Encapsulated{
            private String one = "one";
            String getOne(){return one;}
            void setOne(String _one){this.one = _one;}
        }
        var enc = new Encapsulated();
        System.out.println(enc.getOne());
    }
}

//---------------------------------------------------------------------
//What is a Sequenced Collection ? - Cracking the Java Coding Interview
class Ex130{
    public static void main(String[] args){
        // в коллекции появляется получение/удаление
        // первого и последнего элемента
        // отмечаем, что это просто интерфейс над листом
        SequencedCollection<String> collection = new LinkedList<>();
        collection.add("one");
        collection.add("two");
        collection.add("thr");
        System.out.println(List.of(
                collection.getFirst(),
                collection.getLast()
        ));
        collection.removeFirst();
        System.out.println(collection);

    }
}

//-----------------------------------------------------------------------
//What are the visibility modifiers? - Cracking the Java Coding Interview
class Ex129{
    public static void main(String[] args){
        class Visibility{
            private String one = "only inside of this class";
            protected String two = "inside of the package / extended";
            public String thr = "access everywhere";
            String fou = "'package private' same package";
        }
        System.out.println(Visibility.class);
    }
}

//--------------------------------------------------------------
//What is a red black tree? - Cracking the Java Coding Interview
class Ex128{
    public static void main(String[] args){
        // это структура данных реализуемая в TreeMap
        // сохраняет сортировку по ключам
        // поэтому при итерации ключи сортированны
        var tree = new TreeMap<String, String>(
                Map.of("aaa","111","ccc","222","bbb","333")
        );
        System.out.println(tree);
    }
}

//---------------------------------------------------------------------------------------------
//What is the difference between an Exception and an Error - Cracking the Java Coding Interview
class Ex127{
    public static void main(String[] args){
        // не смотря на то что мы можем выкидывать и то и другое
        // считается что Error выкидывается JVM, а исключение
        // это то что происходит на уровне кода обычного
        try{
            throw new Exception("exception");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        try{
            throw new Error("error");
        }catch(Error e){
            System.out.println(e.getMessage());
        }
    }
}

//What is a switch statement? - Cracking the Java Coding Interview
class Ex126{
    public static void main(String[] args){
        // switch теперь принимает любой объект:
        String test = "testing";
        var what = switch(test){
            case "testing" -> 111;
            case "some" -> 222;
            default -> 333;
        };
        System.out.println(what);

        // можно сравнивать типы объектов:
        Shape shape = new Rectangle(5, 10);

        int result = switch (shape) {
            case Rectangle r -> calculateRectangleArea(r);
            case Circle c -> calculateCircleArea(c);
            default -> throw new IllegalArgumentException("Unknown shape");
        };

        System.out.println(result);
    }
    static interface Shape { }
    static record Rectangle(double length, double width) implements Shape { }
    static record Circle(double radius) implements Shape { }

    private static int calculateRectangleArea(Rectangle rectangle) {
        return (int)(rectangle.length * rectangle.width);
    }

    private static int calculateCircleArea(Circle circle) {
        return (int) (Math.PI * circle.radius() * circle.radius());
    }
}

//---------------------------------------------------------------------------
//How is a CopyOnWriteArrayList working? - Cracking the Java Coding Interview
class Ex125{
    public static void main(String[] args){
        // преимущество в многопотоке - если используется лишь чтения
        // при изменении пересоздает себя целиком (чем обеспечивает устойчивость к много потоку)
        // однако при чтении работает быстро
        var cor = new CopyOnWriteArrayList<String>(List.of("one","two","thr"));
        System.out.println(cor);
    }
}

//-----------------------------------------------------------------------------------
//Can you create a stream with from an Iterable? - Cracking the Java Coding Interview
class Ex124{
    public static void main(String[] args){
        var listOriginal = new ArrayList<String>(List.of("one","two","thr"));
        // <- стрим можно создать именно из итерабл
        //    не заходя внутрь отдельного объекта
        Stream.of(listOriginal).forEach(el -> System.out.println(el));

        // <- с помощью StreamSupport можно еще и параллельность организовать,
        //    обращаем внимание на .spliterator
        var stream = StreamSupport.stream(listOriginal.spliterator(), true);
        stream.forEach(el -> System.out.println(el));

    }
}

//----------------------------------------------------------------------------------------------
//What is the difference between a Runnable and a Callable? - Cracking the Java Coding Interview
class Ex123{
    public static void main(String[] args) throws ExecutionException, InterruptedException{
        // runnable - не возвращает значение
        // callable - возвращает
        // runnable - можно запустить само по себе
        // callable - требует горшка

        Runnable runnable = () -> {
            System.out.println("runnable");
        };
        var thread1 = new Thread(runnable);
        thread1.start();

        var thread2 = Thread.ofVirtual().unstarted(runnable);
        thread2.start();

        // <- обращаем внимание на создание горшка из Executors
        ExecutorService es = Executors.newCachedThreadPool();
        Callable callable = () -> {
            return 111;
        };
        var future = es.submit(callable);
        // <- не забываем отключать es
        //    есди вызввать now вариант исполнение до
        //    выполнения потока внутри es здесь даже не дойдет
        //es.shutdownNow();
        es.shutdown();

        // <- значение то из future так что его можно обработать
        //    даже после остановки es
        System.out.println(future.get());
    }
}

//------------------------------------------------------
//What is a Vector? - Cracking the Java Coding Interview
class Ex122{
    // old API before Collections -> ArrayList
}

//-----------------------------------------------------------------
//What does shallow copy mean? - Cracking the Java Coding Interview
class Ex121{
    public static void main(String[] args){
        // копируем ссылку на объект
        class Some{String one = "111";}
        var someA = new Some();
        var someB = someA;
        someA.one = "222";
        System.out.println(someB.one);
    }
}

//-----------------------------------------------------------------------------------------
//What does reentrant means for synchronization? - Cracking the Java Coding Interview #java
class Ex120{
    static Object lock = new Object();
    // по сути это заход в синхронизируемый блок
    // при наличии возможности (например ключа)
    // при наличии ключа: если в классе используется synchronized
    // то это на самом деле использование this, а при том же объекте
    // в потоке - получится re-entrancy возможность
    // мы рассмотрим вариант просто с входом двух разных потоков в функцию:
    //
    // <- это будет наша функция под атакой
    static void doSome(String whoIsIt) throws InterruptedException{
        // <- вот эта зона это зона в которую могут зайти
        //    несколько потоков зараз (поэтому и re-ent..)
        System.out.println("Reentrant:" + whoIsIt);
        synchronized(lock){
            Thread.sleep(1000L);
            System.out.println("syncZone:" + whoIsIt);
            Thread.sleep(1000L);
        }
        System.out.println("out:" + whoIsIt);
    }

    public static void main (String[] args){
        Runnable thA = () -> {
            try{doSome("thA");}catch(Exception e){}
        };
        Runnable thB = () -> {
            try{doSome("thB");}catch(Exception e){}
        };
        (new Thread(thA)).start();
        (new Thread(thB)).start();
    }
}

//--------------------------------------------------------------------------------------
//How can you use flatMap to filter a stream? - Cracking the Java Coding Interview #java
class Ex119{
    public static void main(String[] args){
        // <- суть flatMap это преобразовать поток в другой поток
        //    (варианты могут быть самыми разными)
        //    например мы можем из потока в два элемента сделать четыре:
        ArrayList<String> source = new ArrayList<>(List.of("one","two"));
        source.stream().flatMap(el -> {
            ArrayList<Object> addition = new ArrayList<>(List.of("a", "b"));
            return addition.stream().map(el2 -> {return el + el2;});
        })
        .forEach(el -> System.out.println(el));

        // <- использование .flatMap для фильтрации
        ArrayList<String> src = new ArrayList<>(List.of("1","one","2"));
        var res =
        src.stream().flatMap(el -> {
           try{
               return Stream.of(Integer.parseInt(el));
           } catch(Exception e){
               return Stream.empty();   // <- элемент будет пропущен
           }
        }).toList();
        System.out.println(res);
    }
}

//----------------------------------------------------------------------------------------
//How can you create a Map to hold 20 elements? - Cracking the Java Coding Interview #java
class Ex118{
    public static void main(String[] args){
        // здесь история скорее про кеширование
        // внутри мэпа есть массив - и когда он перерастает
        // определенное число - он перезатирается и переписывается
        // здесь мы пытаемся процесс чуть оптимизировать и ускорить
        HashMap<String, Integer> mapA = HashMap.newHashMap(2);
        HashMap<String, Integer>  mapB = LinkedHashMap.newHashMap(2);
        mapA.put("one",111);
        mapA.put("two",222);
        mapA.put("thr",333);
        System.out.println(mapA);
    }
}

//------------------------------------------------------------------------------------
//How is the List.sublist() method working? - Cracking the Java Coding Interview #java
class Ex117{
    public static void main(String[] args){
        // дает мутабельное view от листа
        // с индекса до индекс (повторение индекса даст один элемент):
        ArrayList<String> list = new ArrayList<>(List.of("one","two","thr","fou"));
        var subList = list.subList(1,3);
        System.out.println(subList);
        // <- индексы стали локализованными:
        subList.set(0,"111");
        System.out.println(list);

        var l1 = List.of(1,2,3);
        var l2 = List.of(4,5,6);
        var cl = new ArrayList<Integer>();
        // <- список добавляется в сублист то есть
        //    в локальный индекс
        cl.subList(0,0).addAll(l1); // <- добавляем в индекс 0
        cl.subList(1,1).addAll(l2); // <- добавляем в индекс 1
        System.out.println(cl);
        // <- view даст возможность вырезать субмассив
        cl.subList(1,3).clear();
        System.out.println(cl);
    }
}

//-----------------------------------------------------------------------------------
//What is an enumeration? - Cracking the Java Coding Interview #codinginterview #java
class Ex116{
    enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
    public static void main(String[] args){
        // <- прямо как и создан для мэппингов:
        HashMap<DayOfWeek, String> activities = new HashMap<>(
                Map.of(
                        DayOfWeek.MONDAY, "sleep",
                        DayOfWeek.TUESDAY, "eat",
                        DayOfWeek.WEDNESDAY, "run",
                        DayOfWeek.THURSDAY, "song",
                        DayOfWeek.FRIDAY, "sleep again",
                        DayOfWeek.SATURDAY,"another sleep",
                        DayOfWeek.SUNDAY,"celebrate"
                )
        );
        System.out.println(activities.get(DayOfWeek.MONDAY));

        // <- энум это объект передаваемый через статические инстансы и
        //    может иметь свойства
        //    если нам нужен синглетон - это будет такой же паттерн
        //    но с одним единственным статическим объектом (очевидно)
        enum EnumWithValues{
            // <- это статические объекты
            ONE(100), TWO(200);
            // <- а это нормальный класс
            private int length;
            EnumWithValues(int _length){this.length = _length;}
            public int getLength(){return length;}
        }
        // <- получаем значение:
        System.out.println(EnumWithValues.ONE.getLength());
    }
}

//-------------------------------------------------------------------------------------------------
//What are the four fundamental functional interfaces in Java? - Cracking the Java Coding Interview
class Ex115{
    // вообще эти интерфейсы больше предназначены
    // для того чтобы их параметрами делать методов (как лямбды)
    // однако их можно задействовать и inplace для вызовов
    // двойные параметры начинаются с Bi.. тройные с Tri..
    // специализированные [Тип]Consumer -> DoubleConsumer
    // для функций : return всегда последний параметр
    public static void main(String[] args){
        // -1-
        Consumer<String> cons = (el) -> {System.out.println(el);};
        cons.accept("hello world");

        // -2-
        Supplier<String> supl = () -> {return "hello world";};
        System.out.println(supl.get());

        // -3-
        Function<String,Integer> func = (str) -> {return str.length();};
        System.out.println(func.apply("hello world"));

        // -4-
        Predicate<String> pred = (str) -> {return str.equals("hello world");};
        System.out.println(pred.test("hello world"));

        // плюс унарный и бинарный операторы:
        UnaryOperator<String> unary = (el) -> {return el+"!";};
        BinaryOperator<String> binary = (elA,elB) -> {return elA+elB;};
        System.out.println(List.of(
                unary.apply("hello"),
                binary.apply("hello ", "world")
        ));
    }
}

//-----------------------------------------------------------------------------------------
//What is the first thing a consructor is doing? - Cracking the Java Coding Interview #java
class Ex114{
    static class Zero{
        String message;
        Zero(String partA, String partB){this(partA + partB);}
        Zero(String _message){message = _message;}
        void printMessage(){System.out.println(message);}
    }
    static class One extends Zero{
        One(){super("This is", " the message"); System.out.println(111);}
    }
    static class Two extends One{
        Two(){System.out.println(222);}
    }
    public static void main(String[] args){
        // <- первым делом вызовется конструктор
        //    из класса родителя
        var two = new Two();
        two.printMessage();
    }
}

//--------------------------------------------------------------------------------------------
//What does Thread.interrupt() do? - Cracking the Java Coding Interview #codinginterview #java
class Ex113{
    // это отправка сигнала, а не жесткое прерывание
    public static void main(String[] args) throws InterruptedException{
        Thread thr = new Thread(()->{
            // отрабатываем две ситуации:
            // 1) в нормальном флоу получили отметку прерывания
            // 2) получили отметку прерывания пока спали в виде исключения
            boolean interrupted = false;
            while(!interrupted && !Thread.currentThread().isInterrupted()){
                System.out.println("tick");
                try{Thread.sleep(1000L);}catch(Exception e){
                    interrupted = true;
                }
            }
        });
        thr.start();
        Thread.sleep(5000L);
        thr.interrupt(); // <- отправка сигнала
    }
}

//-----------------------------------------------------------------------------
//What is Iterable? - Cracking the Java Coding Interview #codinginterview #java
class Ex112{
    public static void main(String[] args){
        class IterateWithMe implements Iterable<String>{
            ArrayList<String> data = new ArrayList<>(List.of("one","two","thr"));
            @Override
            public Iterator<String> iterator(){
                return data.iterator();
            }
        }
        for(var el : new IterateWithMe()){
            System.out.println(el);
        }

        // кастомный итератор в свою очередь можно создать так:
        var itr = new Iterator<Integer>(){
            int index = 0;
            public boolean hasNext(){return index < 3;}
            public Integer next(){return index++;}
        };
        while(itr.hasNext()){
            System.out.println(itr.next());
        }

        // теперь создадим iterable из итератора:
        var itrA = new Iterator<Integer>(){
            int index = 0;
            public boolean hasNext(){return index < 3;}
            public Integer next(){return index++;}
        };
        Iterable<Integer> iterableA = () -> {return itrA;};
        for(var el : iterableA){
            System.out.println(el);
        }
    }
}

//-------------------------------------------------------------------------------------------------
//What is the no-arg empty constructor? - Cracking the Java Coding Interview #codinginterview #java
class Ex111{
    public static void main(String[] args){
        // компилятор всегда при отсутсвтии
        // любого другого конструктора добавляет пустой
        // однако если есть не пустой - пустой потребуется добавить руками
        class WithoutConstructor{
            String test(){return "test";}
        }
        var wc = new WithoutConstructor();
        class SomeConstructor{
            String param;
            SomeConstructor(){this("param");}
            SomeConstructor(String _param){this.param = _param;}
        }
        var cc = new SomeConstructor();
    }
}

//------------------------------------------------------------------
//What is multiple inheritance? - Cracking the Java Coding Interview
class Ex110{
    // m.inheritance
    // 1) type : interfaces
    // 2) behavior : methods from superclass / default from interfaces
    // 3) state : only via composition / aggregation
    static class One{
        void doSome(){System.out.println("one");}
    }
    static class Two{
        void doSome(){System.out.println("two");}
    }
    static class Composite{
        One one = new One();
        Two two = new Two();
        void doSome(){
            one.doSome();
            two.doSome();
        }
    }

    public static void main(String[] args){
        (new Composite()).doSome();
    }
}

//------------------------------------------------------
//What is a Future? - Cracking the Java Coding Interview
class Ex109{
    public static void main(String[] args) throws ExecutionException, InterruptedException{
        var exec = Executors.newSingleThreadExecutor();
        var future = exec.submit(()->{return 111;});
        System.out.println(future.state());
        var result = future.get();
        System.out.println(result);
        exec.shutdownNow();
    }
}

//----------------------------------------------------------------------
//What is the return type of max()? - Cracking the Java Coding Interview
class Ex108{
    public static void main(String[] args){
        // здесь имеется ввиду работа с Optional
        // который требуется вставлять в любой метод который возвращает
        // чтото или непонятно что
        class WithOptional{
            Optional<String> retString(int number){
                return switch(number){
                  case 1 -> Optional.of("one");
                  case 2 -> Optional.of("two");
                  default -> Optional.empty();
                };
            }
        }
        var wo = new WithOptional();
        var exist = wo.retString(1).get();
        // ,orElse это по сути .get но с добавлением default варианта
        var non = wo.retString(33).orElse("default");
        System.out.println(List.of(
                exist,
                non
        ));

        // ситуация с optional также логична когда стрим просто пустой:
        // ArrayList<Integer> list = new ArrayList<>(List.of(1,1,1));
        ArrayList<Integer> list = new ArrayList<>();
        var res0 = list.stream().max(Integer::compareTo);
        System.out.println(res0.orElse(111));
    }
}

//------------------------------------------------------------------------
//How can you create a prefilled map? - Cracking the Java Coding Interview
class Ex107{
    public static void main(String[] args){
        var pm = new HashMap<String,String>(Map.of("one","111","two","222"));
        System.out.println(pm);

        var e1 = Map.entry("One","111");
        var e2 = Map.entry("Two","222");
        // не стоит использвоать без обертки конструктора
        // иначе получим иммутабл тип:
        // var pm2 = Map.ofEntries(e1,e2)
        var pm2 = new HashMap<String,String>(Map.ofEntries(e1,e2));
        System.out.println(pm2);
        System.out.println(pm2.getClass());
    }
}

//---------------------------------------------------------------
//What does Comparable mean? - Cracking the Java Coding Interview
class Ex106{
    public static void main(String[] args){
        class CompareMe implements Comparable<CompareMe>
        {
            int value;
            CompareMe(int _value){value = _value;}
            @Override
            public int compareTo(CompareMe o){
                if(o.value > value) return -1;
                if(o.value < value) return 1;
                return 0;
            }

            @Override
            public String toString(){
                return ""+value;
            }
        }
        var cmpA = new CompareMe(100);
        var cmpB = new CompareMe(200);

        // для организации же сортировок
        // нам потребуется компаратор (в который можно как раз функциональность
        // от Comparable и вставить):
        Comparator<CompareMe> comparator = (o1, o2) -> {
            return o1.compareTo(o2);
        };

        ArrayList<CompareMe> list = new ArrayList<>(List.of(
                new CompareMe(100),
                new CompareMe(50),
                new CompareMe(20),
                new CompareMe(777)
        ));

        list.sort(comparator);
        System.out.println(list);

        // компаратор как видим это просто лямбда:
        list.sort((e1, e2) -> {
            return switch(e1.compareTo(e2)){
                case 1 -> -1;
                case -1 -> 1;
                default -> 0;
            };
        });
        System.out.println(list);

        // при наличии Comparable
        // реализации объекты можно сортировать стримом
        // без компаратора:
        ArrayList<CompareMe> list1 = new ArrayList<>(
                List.of(new CompareMe(3),
                        new CompareMe(1),
                        new CompareMe(2)));
        List<CompareMe> sortedList = list1.stream()
                .sorted()
                .collect(Collectors.toList());
        System.out.println(sortedList);

    }
}

//---------------------------------------------------------------------
//Why is Thread.stop() deprecated? - Cracking the Java Coding Interview
class Ex105{
    // теперь поток можно остановить лишь
    // через внутренний флаг, отслеживание исключение при сне и
    // Thread.currentThread().isInterrupted()
    // считается, что иначе мы создаем раздрай в состоянии программы
    // как то не закрытые ресурсы (не понятно на какой момент их закрывать)
}

//--------------------------------------------------------------------
//What is a downstream collector? - Cracking the Java Coding Interview
class Ex104{
    public static void main(String[] args){
        var list = List.of(1,2,3);

        // стандартный типовой коллектор это или к листу или к списку
        var set = list.stream().collect(Collectors.toSet());
        System.out.println(set);

        // возможна также групировка:
        class Person{
            String name; String getName(){return name;}
            int value;
            Person(String _name, int _value){
                name = _name; value = _value;
            }

            @Override
            public String toString(){
                return ""+name+":"+value;
            }
        }
        var listP = List.of(
                new Person("Ivan", 1),
                new Person("Ivan", 2),
                new Person("Greg", 3)
        );
        Map<String,List<Person>> personByName =
                                 listP.stream().collect(Collectors.groupingBy(Person::getName));
        System.out.println(personByName);
    }
}

//-----------------------------------------------------------
//What does static mean? - Cracking the Java Coding Interview
class Ex103_Add {
    static void testStaticImport() {
        System.out.println("static import");
    }
}
enum Ex103_StaticEnum{
    STATIC_ONE, STATIC_TWO
}
// Main class demonstrating usage of static imports and nested classes
class Ex103 {
    public static void main(String[] args) {

        // Nested class within the main method
        class TestStatic {
            static String getOne() { return one; }
            static String one;

            // Static initializer block
            static {
                one = "one";
            }
        }

        // Demonstrating access to static members
        System.out.println(List.of(
                TestStatic.one,
                TestStatic.getOne()
        ));

        // используем также статические импорты
        // для прямого доступа к методам и стейтам
        System.out.println(STATIC_ONE);
        testStaticImport();
    }
}

//----------------------------------------------------------------
//What is the type ? super T? - Cracking the Java Coding Interview
class Ex102{
    public static void main(String[] args){
        // типичная игра с типами обычно строиться следующим образом:
        abstract class Common{
            void doSome(){}
        }
        class A extends Common{
            @Override
            public void doSome(){System.out.println("do some A");}
        }
        class B extends Common{
            @Override
            public void doSome(){System.out.println("do some B");}
        }
        // <- это вот ОПИСАНИЕ типа для отдельного класса тут есть T
        // а для задания можно использовать как раз производное
        // <? super SomeClasss>
        class TypeUser<T extends Common> {
            public void callDoSome(T element) {
                // <- гарантированный вызов
                element.doSome();
            }
        }
        // <- принимает все что имеет суперклассом строку
        //    как мы помним шаблонизатор это же просто каст
        Consumer<? super String> cons = (el) -> {System.out.println(el);};
        cons.accept("some");
    }
}

//---------------------------------------------------------------------------------------------
//How can you check is a class is an extension of another? - Cracking the Java Coding Interview
class Ex101{
    public static void main(String[] args){
        class One {}
        class Two extends One{}
        class Thr extends Two{}

        var one = new One();
        var two = new Two();
        var thr = new Thr();

        System.out.println(List.of(
                one.getClass().isInstance(two), // true
                two.getClass().isInstance(one), // false
                one.getClass().isInstance(thr), // true

                // может ли переменная с этим типом быть назначенной
                // типом указанным в параметре:
                one.getClass().isAssignableFrom(two.getClass()), // true
                two.getClass().isAssignableFrom(one.getClass()) // false
                ));
    }
}

//--------------------------------------------------------------
//What is a race condition? - Cracking the Java Coding Interview
class Ex100{
    public static void main(String[] args) throws InterruptedException{
        // возникает когда два потока одновременно пытаются
        // читать и писать одну переменную
        // при отсутствии синхронизации
        ArrayList<Integer> target = new ArrayList<>(List.of(100));
        // один поток будет увеличивать счетчик:
        Thread threadA = new Thread(()->{
           for(int i = 0; i < 1_000_000; i++){
               var value = target.get(0);
               value += 1;
               target.set(0,value);
           }
            System.out.println("A is ended");
        });
        // второй поток будет уменьшать
        Thread threadB = new Thread(()->{
            for(int i = 0; i < 1_000_000; i++){
                var value = target.get(0);
                value -= 1;
                target.set(0,value);
            }
            System.out.println("B is ended");
        });
        threadA.start();
        threadB.start();
        // казалось бы должно быть 100
        // так как миллион раз увеличили и миллион уменьшили
        // а вот нет - мы получаем неведомо что
        // это называется inconsisten state
        Thread.sleep(3000L);
        System.out.println(target);
    }
}

//-------------------------------------------------------------
//What is a daemon thread? - Cracking the Java Coding Interview
class Ex099{
    public static void main(String[] args) throws InterruptedException{
        Thread thDeamon = new Thread(()->{
           int counter = 10;
           while(counter-- > 0){
               System.out.println("tick"+counter);
               try{Thread.sleep(500L);}catch(Exception e){};
           }
        });
        // <- демон тред будет работать лишь в случае
        //    если работает основной поток к которому он прикреплен
        //    как только основной поток закончится - и daemon закончится
        //    Virtual Thread всего deamon=true
        thDeamon.setDaemon(true);
        thDeamon.start();
        Thread.sleep(3000L);
    }
}

//---------------------------------------------------------
//What is a collector? - Cracking the Java Coding Interview
class Ex098{
    public static void main(String[] args){
        // коллектор это редюсер в хвосте стрима
        // коллектор можно написать самостоятельно
        // можно обрабатывать с ним мэппинги на базе ключей и значений (пример здесь есть,
        // но это выглядит очень уродливо всё)
        ArrayList<Integer> arr = new ArrayList<>(List.of(1,2,3));
        var set = arr.stream().collect(Collectors.toSet());
        System.out.println(set);
    }
}

//--------------------------------------------------------------------------------
//What is a ConcurrentModification Exception? - Cracking the Java Coding Interview
class Ex097{
    public static void main(String[] args){
        // никакого отношения это к мультипроцессорному программированию не имеет
        // просто есть попытка модернизации коллекции в процессе итерации
        ArrayList<String> arr = new ArrayList<>(List.of("one","two","thr"));
        try{
            boolean once = true;
            for(var el: arr){
                if(once){
                    once = false;
                    arr.add("four");
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }

    }
}

//------------------------------------------------------------------------------------------
//How can you invoke a method using the Reflection API? - Cracking the Java Coding Interview
class Ex096{
    public static void main(String[] args) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException{
        class Base{
            public void doSomething(String param){
                System.out.println(param);
            }
        }
        // история очень простая - мы получаем метод
        // после чего по отношению к контексту (то есть объекту)
        // вызываем с требуемыми параметрами:
        // (если вызывается приватный метод - то он сначала делается публичным - потом вызывается)
        var    c = Base.class;
        Method m = c.getMethod("doSomething", String.class);
        var    o = new Base();
        m.invoke(o,"parameter");
    }
}

//------------------------------------------------------
//What is a Record? - Cracking the Java Coding Interview
class Ex095{
    public static void main(String[] args){
        // компилятор добавляет автоматически конструктор и
        // доступы к полям, плюс запись является не модифицируемой
        // то есть после задания полей конструктором - их изменить не выйдет
        record User(String name, Integer age){}
        var user = new User("Ivan",34);
        System.out.println(user);
    }
}

//----------------------------------------------------------------
//What is an atomic variable? - Cracking the Java Coding Interview
class Ex094{
    public static void main(String[] args) throws InterruptedException{
        // автомарная переменная это нечто в сопровождении семафора на доступ
        // таким образом это нечто можно нормально передавать в потоки
        // не боясь что оно или зажуется где то в кеше - или к нему будет проблема с доступом
        // все базовые переменные вроде int,long - все атомарные и так
        // однако - все что касается транзакций - это все таки к базе данных
        class User{
            String name;
            User(String _name){name = _name;}
            void setName(String _name){name = _name;}
            @Override
            public String toString(){
                return super.toString();
            }
        }
        var user = new User("Ivan");
        AtomicReference<User> userRef = new AtomicReference<>();
        userRef.set(user); // <- теперь её можно транслировать в потоки
        Thread thr = new Thread(()->{
            userRef.get().setName("John");
        });
        thr.start();
        thr.join();
        System.out.println(userRef.get().name);
    }
}

//---------------------------------------------------------------------
//What is a short-circuit method ? - Cracking the Java Coding Interview
class Ex093{
    public static void main(String[] args){
        // методы для досрочного прерывания стримов:
        ArrayList<Integer> arr = new ArrayList<>(List.of(1,2,3,4,5));
        System.out.println(List.of(
                arr.stream().takeWhile(el -> el < 3).collect(Collectors.toSet()),
                arr.stream().limit(3).collect(Collectors.toSet()),
                arr.stream().allMatch(el -> el < 4),
                arr.stream().anyMatch(el -> el < 4),
                arr.stream().findFirst().orElse(-1),
                arr.stream().findAny().orElse(-1)
        ));
    }
}

//-----------------------------------------------------------
//What is a Constructor? - Cracking the Java Coding Interview
class Ex092{
    class BaseConstructor{String test; BaseConstructor(String _test){test=_test;}}
    class Constructor extends BaseConstructor{
        static String one;
        String two;
        // вызывется при создании шаблона класса
        static {
            one = "111";
        }
        // вызовется при создании первого объекта класса
        {
            two = "222";
        }
        String name;
        // инициализация вышестоящего объекта
        Constructor(String _name){super(_name);name = _name;}
        // вызов собственного конструктора
        Constructor(){this("test");}
    }
}

//---------------------------------------------------------------------
//When should you use ? extends T? - Cracking the Java Coding Interview
class Ex091{
    // используется при задании дженериков
    // называется wildcard
    abstract class Common{
        void doSome(){}
    }
    class A extends Common{
        @Override
        public void doSome(){System.out.println("do some A");}
    }
    class B extends Common{
        @Override
        public void doSome(){System.out.println("do some B");}
    }
    // <- это вот ОПИСАНИЕ типа для отдельного класса тут есть T
    // а для задания можно использовать как раз производное
    // <? super SomeClasss>
    class TypeUser<T extends Common> {
        public void callDoSome(T element) {
            // <- гарантированный вызов
            element.doSome();
        }
    }
}

//-----------------------------------------------------------
//What is an annotation? - Cracking the Java Coding Interview
class Ex090{

    // аннотация нужна для маркировки чего то чем то
    // и считывания этой маркироквки

    // создаем аннотацию:
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface Range{
        int begin() default 0;
        int end();
    }
    class RangeUser{
        // размещаем аннотацию
        @Range(begin=50,end=80)
        public int testField = 100;
    }

    public static void main(String[] args) throws NoSuchFieldException{
        // читаем аннотацию:
        Class c = RangeUser.class;
        var f = c.getField("testField");
        System.out.println(f.accessFlags());
        var annotations = f.getAnnotations();
        System.out.println(annotations.length);
        Range rangeAnnotation = f.getAnnotation(Range.class);
        if(rangeAnnotation!=null){
            System.out.println(List.of(
                    rangeAnnotation.begin(),
                    rangeAnnotation.end()
            ));
        }
    }
}

//---------------------------------------------------------------------------------
//How can you model a concurrent task in Java? - Cracking the Java Coding Interview
class Ex089{
    // два варианта runnable / callable
    public static void main(String[] args) throws ExecutionException, InterruptedException{
        // не выбрасывает исключений:
        Runnable runnable = ()->{
            System.out.println("runnable");
        };
        // может выбрасывать исключения (
        // их придется обрабатывать специфически)
        Callable<String> callable = ()->{
            String name = "callable";
            System.out.println(name);
            return name;
        };

        (new Thread(runnable)).start();
        Thread.ofVirtual().start(runnable);

        var es = Executors.newSingleThreadExecutor();
        var future = es.submit(callable);
        System.out.println(future.get());

        es.shutdownNow();
        es.close();
    }
}

//------------------------------------------------------------------------------------
//Comparing Doubles with a comparator of Numbers? - Cracking the Java Coding Interview
class Ex088{
    public static void main(String[] args){
        Comparator<Number> numberComparator = (elA,elB) -> {
            if(elA.doubleValue() > elA.doubleValue()) return 1;
            if(elB.doubleValue() > elA.doubleValue()) return -1;
            return 0;
        };
        ArrayList<Double> list = new ArrayList<>(List.of(
                0.1, 0.2, 0.05, 0.19
        ));
        list.sort(numberComparator);
        System.out.println(list);
    }
}

//-----------------------------------------------------------------------------------------
//Difference between toList() and Collectors.toList()? - Cracking the Java Coding Interview
class Ex087{
    public static void main(String[] args){
        ArrayList<Integer> list = new ArrayList<>(List.of(1,2,3,4));
        var mutable = list.stream().collect(Collectors.toList());
        var immutable = list.stream().toList(); // быстрее
        System.out.println(List.of(mutable,immutable));
    }
}

//--------------------------------------------------------------------------------------------
//What is the difference between a Collection and a List? - Cracking the Java Coding Interview
class Ex086{
    public static void main(String[] args){
        // list - упорядочен для итерации (итерация всегда будет однотипной)
        // collection - нет в терминах Java коллекция это как раз мэппинг
        // и есть еще set - в котором нет повторений
        // и queue/stack который стек только в разном порядке
        List<String> list = new ArrayList<>();
        list.add("1"); list.add("2"); list.add("3");
        System.out.println(list);
        // <- не корректно, как видим это просто лист
        Collection<String> collection = new ArrayList<>();
        collection.add("1"); collection.add("2"); collection.add("3");
        System.out.println(collection);
    }
}

//----------------------------------------------------------------------------------------------
//Creating an instance of a Class using the Reflection API? - Cracking the Java Coding Interview
class Ex085{
    public static void main(String[] args)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException{
        class Box{
            void doStaff(){
                System.out.println("do staff");
            }
        }
        var c = Box.class;
        var ctor = c.getDeclaredConstructor();
        // <- сюда можно передавать параметры
        Box box = ctor.newInstance();
        box.doStaff();
    }
}

//--------------------------------------------------------
//What is a deadlock? - Cracking the Java Coding Interview
class Ex084{
    static class Resource {
        private boolean locked = false;
        public synchronized void lock() throws InterruptedException {
            while (locked) {wait();}
            locked = true;
        }
        public synchronized void unlock() {
            locked = false;
            notifyAll();
        }
    }
    static Resource resource1 = new Resource();
    static Resource resource2 = new Resource();
    public static void main(String[] args){
        // взаимный захват синхронизирующих элементов
        // или ресурсов потоками - он может произойти, а может и не произойти
        // но факт остается фактом - так делать не стоит
        // решений много: самое простое - не пытаться вообще ничего захватывать
        // если полного комплекта для исполнения просто нет
        // а для "справедливости" есть симафор - с флагом fair=true
        Thread thread1 = new Thread(() -> {
            try {
                System.out.println("Thread 1: Trying to acquire resource 1");
                resource1.lock();
                System.out.println("Thread 1: Acquired resource 1");

                System.out.println("Thread 1: Trying to acquire resource 2");
                resource2.lock();
                System.out.println("Thread 1: Acquired resource 2");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                resource1.unlock();
                resource2.unlock();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                System.out.println("Thread 2: Trying to acquire resource 2");
                resource2.lock();
                System.out.println("Thread 2: Acquired resource 2");

                System.out.println("Thread 2: Trying to acquire resource 1");
                resource1.lock();
                System.out.println("Thread 2: Acquired resource 1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                resource1.unlock();
                resource2.unlock();
            }
        });
        thread1.start();
        thread2.start();
    }
}

//----------------------------------------------------------------------------------------------------
//What list can you pass to a method that takes a List of Number? - Cracking the Java Coding Interview
class Ex083{
    public static void main(String[] args){
        // дело в том что невозможно расширяться по list типу
        // в отношении его содержимого:
        //List<Number> listOfNumbers = new ArrayList<Integer>();
        // то есть тип должен всегда 100% совпадать:
        List<Number> listOfNumbers = new ArrayList<Number>();
    }
}

//--------------------------------------------------------------------------------------------------
//Difference between and intermediate and a terminal operation? - Cracking the Java Coding Interview
class Ex082{
    public static void main(String[] args){
        // промежуточная операция возвращает поток
        // а конечная терминальная - элемент данных
        // однако его можно дальше спаковать в поток и продолжить развлекаться
        List<String> list = new ArrayList<>(List.of("one","two","thr"));
        // то есть терминальная операция может быть и не терминальной:
        var count = list.stream().toList().stream().count();
        System.out.println(count);
    }
}

//---------------------------------------------------------------------
//What does passing by value mean? - Cracking the Java Coding Interview
class Ex081{
    // считается что в java все передается по значению
    // однако объекты передаются через ссылку, но не строка, и не врапперы примитивов
    // поведение надо сказать престранное:
    static void passByValue(String value){System.out.println(value);}
    static void passByRef(Ex081 ref){System.out.println(ref);}
    static void passByValue(Integer value){value = 111;}

    public static void main(String[] args){
        Integer a = 222;
        passByValue(a);
        System.out.println(a);
    }
}

//-------------------------------------------------------------------------------------------
//How can you declare a generic type on a static method? - Cracking the Java Coding Interview
class Ex080{
    public static void main(String[] args){
        // для обычного класса декларация дженерика выноситься в класс
        class Box<T>{
            private T t;
            Box(T t){this.t = t;}

        // для дженерика в статическом методе так не выйдет
        // придется сигнатуру метода выписывать целиком:
            static <T> Box<T>
            copy(Box<T> box){
                return new Box<>(box.t);
            }
        }
    }
}

//-----------------------------------------------------------------------------------------
//How can you update a field using the Reflection API? - Cracking the Java Coding Interview
class Ex079{
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException{
        class Box{
            // <- если не установлено public
            //    придется дополнительно изменять классификатор доступа
            //    Field.setAccessible(true)
            public String value;
            Box(String _value){value = _value;}
        }
        Box box = new Box("valueA");
        Class c = Box.class;
        var f = c.getField("value");
        f.set(box,"valueB");
        System.out.println(box.value);
    }
}

//-------------------------------------------------------------------------------
//How can you join Strings with a separator? - Cracking the Java Coding Interview
class Ex078{
    public static void main(String[] args){
        String one = "one";
        String two = "two";
        var result = String.join(",",one,two);
        System.out.println(result);

        ArrayList<String> list = new ArrayList<>(List.of("one","two"));
        var joiner = new StringJoiner(",","<",">");
        list.forEach(joiner::add);
        System.out.println(joiner.toString());

        var res = list.stream().collect(Collectors.joining(",","{","}")).toString();
        System.out.println(res);
    }
}

//-------------------------------------------------------------
//How is Set.of() working? - Cracking the Java Coding Interview
class Ex077{
    public static void main(String[] args){
        // в сете всегда уникальные элементы:
        // а Set.of создает не мутабельный сет (попытка вставить дубликат
        // будет давать исключение)
        var set = Set.of("one","two","three");
        System.out.println(set);
    }
}

//-------------------------------------------------------------------------------
//How can you remove elements from a Stream? - Cracking the Java Coding Interview
class Ex076{
    public static void main(String[] args){
        // используем очевидно фильтр
        // можно также использовать и flatMap/mapMulti
        var list = List.of(1,2,3,4,5,6,7);
        var res = list.stream().filter(el -> el > 3).toList();
        System.out.println(res);
    }
}

//---------------------------------------------------------------
//How can you stop a Thread? - Cracking the Java Coding Interview
class Ex075{
    // никак - можно лишь отказаться от получения результата если у нас фьюча
    // но поток то все равно выполнится (это CompletableFuture)
    // или передавать сигнал, а внутри отлавливать двумя способами:
    // 1) исключением если мы спим
    // 2) флагом isInterupted
    // можно также подвязаться на атомарный флаг - но это какая то глупость уже
}

//------------------------------------------------------------
//What is a generic type? - Cracking the Java Coding Interview
class Ex074{
    public static void main(String[] args){
        // отмечаем порядок задания:
        // сначала пишем класс - потом <..> расширение и в него
        // вставляем все типы через запятую которые будут кастоваться
        // от объекта:
        record Box<T> (T t){}
        class Some<T>{
            T t;
            Some(T _t){t = _t;}
        }
    }
}

//--------------------------------------------------------------------
//How is synchronization working? - Cracking the Java Coding Interview
class Ex073{
    public static void main(String[] args){
        // синхронизация это однократная блокировка (в отличае от симафора)
        // на какой то ресурс - при этом объект блокировки отмечается
        // через какой то идентификатор - так как идентификация у объектов
        // java уникальная - обычно используют её
        // при блокировке на блок synchronized используется объект this
        // из класса где она стоит - поэтому возможно re-entrancy
        // синхронизироваться можно:
        // 1. семафором (описан в одном из вопросов)
        // 2. модификатором synchronized метода (самый простой и не оптимизированный)
        // 3. локом (не забывая try -> finally где будем снимать лок)
        // 4. блоком synchronized для изолирования переменных
        // --
        // когда блокировка снимается объекты получают извещение .notifyAll
        // это можно делать и руками - но лучше не ввязываться
        class FuncSync{
            synchronized void doSome(){System.out.println("some");}
            void blockSync(){
                synchronized(this){
                    System.out.println("some");
                }
            }
        }
    }
}

//----------------------------------------------------------------------------------------
//What is the difference between map() and flatMap()? - Cracking the Java Coding Interview
class Ex072{
    public static void main(String[] args){
        // map изменяет элемент и возвращает элемент
        // flatMap возвращает стрим в любом виде хоть в том что был - хоть в измеенном
        ArrayList<String> source = new ArrayList<>(List.of("one","two"));
        source.stream().flatMap(el -> {
                    ArrayList<Object> addition = new ArrayList<>(List.of("a", "b"));
                    return addition.stream().map(el2 -> {return el + el2;});
                })
                .forEach(el -> System.out.println(el));
    }
}

//----------------------------------------------------------------------------------------
//What is the difference between Runnable and Thread? - Cracking the Java Coding Interview
class Ex071{
    public static void main(String[] args){
        // Runnable это интерфейс (лямбда в терминах Java это интерфейс)
        Runnable run = ()->{
            System.out.println("111");
        };
        Thread thr = new Thread(run);
        // с помощью thr.run() потом можно исполнить
        // в текущем (сделать .join)
        thr.start();
    }
}

//--------------------------------------------------------------
//How is List.of() working? - Cracking the Java Coding Interview
class Ex070{
    public static void main(String[] args){
        // производит не модифицируемый лист
        // его можно использовать как инициализатор мутируемого
        // так и напрямую для стримов например:
        var unList = List.of(1,2,3);
        var moList = new ArrayList<Integer>(List.of(1,2,3));
        System.out.println(List.of(
                unList,moList
        ));
    }
}

//-------------------------------------------------------------------
//What is a sealed type in Java? - Cracking the Java Coding Interview
class Ex069{
    sealed interface SomeAble
            permits Some{
        void doSome();
    }
    // так как разрешение ставиться лишь на имя
    // конкретного класса - его нужно запечатать через final
    // то есть запретить от него наследоваться
    static final class Some implements SomeAble{
        @Override
        public void doSome(){
            System.out.println("some");
        }
    }

    public static void main(String[] args){
        // sealed типы это типы которые можно расширять
        // лишь в сторону специально указанных классов
        // может быть что угодно: класс, абстр.класс, интерфейс
        Some some = new Some();
        some.doSome();
    }
}

//---------------------------------------------------------------------------
//How can you find duplicates in a list? - Cracking the Java Coding Interview
class Ex068{
    public static void main(String[] args){
        // ищем дубликаты так:
        // преобразуемся в Set
        // и сравниваем размеры
        var arr = new ArrayList<String>(List.of("one","two","one"));
        // можно также через .stream() + .distinct() + .count()
        var set = arr.stream().collect(Collectors.toSet());
        System.out.println(List.of(
                arr.size(),
                set.size()
        ));
        // теперь если найден факт присустствия дупликатов
        // остается их найти (дупликаты то разные бывают - кто по 2 кто побольше)
        // строим гистограмму:
        Map<String, Long> histogram =
                          arr.stream().collect(
                            Collectors.groupingBy(Function.identity(), Collectors.counting())
                          );
        System.out.println(histogram);
        // ну и теперь можно получить индексы этих элементов
        // и делать с ними что заблагорассудиться (это же лист в конце то концов)
    }
}

//-------------------------------------------------------------------
//What is the class named Class? - Cracking the Java Coding Interview
class Ex067{
    public static void main(String[] args){
        // рефлексия:
        Class c = Ex067.class;
        List.of(c.getMethods())
                .stream()
                .map(el -> {return el.getName();})
                .forEach(System.out::println);
    }
}

//-----------------------------------------------------------------------------
//How can you create a File with Java I/O? - Cracking the Java Coding Interview
class Ex066{
    public static void main(String[] args) throws IOException{
        Path path = Path.of("/home/x057/000__KOLBASKINS/000__ПРАКТИКА/file.txt");
        Files.createFile(path);
        String dataToWrite = "Пример данных для записи";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toString()))) {
            writer.write(dataToWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//----------------------------------------------------------------
//How can you start a Thread? - Cracking the Java Coding Interview
class Ex065{
    public static void main(String[] args) throws ExecutionException, InterruptedException{
        // поток можно запустить двумя способами:
        // стартануть поток (раннабл)
        // поместить таску (раббл-коллабл) в горшек потоков через сабмит
        (new Thread(()->{
            System.out.println("hello");
        })).start();

        var es = Executors.newCachedThreadPool();
        var result = es.submit(()->{
            return 111;
        });
        System.out.println(result.get());
    }
}

//-----------------------------------------------------------------------------
//How can you create an unmodifiable list? - Cracking the Java Coding Interview
class Ex064{
    public static void main(String[] args){
        // существует еще и Collections.unmodifiebleList()
        // но она строит view - сам лист то будет редактируем
        // поэтому предпочитаемый вариант List.of:
        var umList = List.of(1,2,3);
    }
}

//---------------------------------------------------------------------
//What is the var keyword in Java? - Cracking the Java Coding Interview
class Ex063{
    public static void main(String[] args){
        //var -> auto in C++
        // особое использование это создание не именованного объекта:
        // здесь типа то как такового нет, однако как видно
        // есть доступ даже до внутренних методов
        var obj = new Object(){
            void doSome(){
                System.out.println("do some");
            }
        };
        obj.doSome();
    }
}

//-----------------------------------------------------------------------------------
//Can you cite some methods from the Stream API? - Cracking the Java Coding Interview
class Ex062{
    // map - преобразователь при прохождении
    // filter - фильтр собственно с предикатом
    // reduce - обобщатель собиратель
    // остальные: toList, forEach, findAny, findFirst
    // flatMap, distinct, sorted
    public static void main(String[] args){
        int[] numbers = {1, 2, 3, 4, 5};
        int sum = Arrays.stream(numbers)
                // нуль - это при отсусттвии элементов в потоке
                // второе это агрегаторная функция где последующая операыия будет
                // будет брать за основу результат предыдущей
                .reduce(0, Integer::sum);
        System.out.println(sum);
    }
}

//--------------------------------------------------------------------
//How is Arrays.asList() working? - Cracking the Java Coding Interview
class Ex061{
    public static void main(String[] args){
        // элементы можно изменять
        // но нельзя удалять - так как источник данных
        // для листа сохраняется - а это просто массив
        var list = Arrays.asList(1,2,3,4,5);
        System.out.println(list);
    }
}

//----------------------------------------------------------------------------------------------
//How does a Set knows that it already contains an element? - Cracking the Java Coding Interview
class Ex060{
    public static void main(String[] args){
        // используется очевидно хеш от элемента
        // естественно для того чтобы элемент был определяем в массиве
        // требуется .hashCode() + .equals() реализация (без неё .contains()
        // просто работать не будет)
        //-
        // не рекомендуется мутировать объекты (например изменить поле) по факту размещения в сете
        // так как хеш то пересчитан не будет - и определение не будет нормально работать
    }
}

//------------------------------------------------------------------------------------
//What is the maximum length of a String in Java? - Cracking the Java Coding Interview
class Ex059{
    public static void main(String[] args){
        System.out.println(Integer.MAX_VALUE);
    }
}

//----------------------------------------------------------------------------------
//How many objects can you put in a Collection? - Cracking the Java Coding Interview
class Ex058{
    public static void main(String[] args){
        System.out.println(Integer.MAX_VALUE);
    }
}

//-----------------------------------------------------------------------------
//What are the four Java I/O base classes? - Cracking the Java Coding Interview
class Ex057{
    public static void main(String[] args){
        // Reader
        // Writer
        // InputStream
        // OutputStream
        // плюс их буфферизованные врапперы
        //-
        // однако рекомендуется использовать
        // Factory. <- и нужные ридер / райтер
    }
}

//------------------------------------------------------------------------------------------
//What are the different categories of design patterns? - Cracking the Java Coding Interview
class Ex046{
    // Creational: Builder, Factory, Singleton
    // Structural: Decorator, Facade, Proxy
    // Behavioral: Iterator, Template, Method, Visitor
}

//---------------------------------------------------------------
//How does a SortedSet work? - Cracking the Java Coding Interview
class Ex044{
    public static void main(String[] args){
        // элементы должны поддерживать comparable интерфейс
        SortedSet<String> set = new TreeSet<>(List.of("aaa","ccc","bbb"));
        System.out.println(set);
    }
}

//-------------------------------------------------------------------------------------
//How can you open a file for reading binary data? - Cracking the Java Coding Interview
class Ex042{
    public static void main(String[] args) throws IOException{
        Path path = Path.of("/home/x057/000__KOLBASKINS/000__ПРАКТИКА/file.txt");
        var stream = Files.newInputStream(path);
        // читаем каким нито методом:
        var bytes = stream.readAllBytes();
        // и не забываем закрывать (можно сделать в try-with)
        stream.close();
    }
}

//------------------------------------------------------------------------------
//How can you open a file for writing text? - Cracking the Java Coding Interview
class Ex040{
    public static void main(String[] args) throws IOException{
        Path path = Path.of("/home/x057/000__KOLBASKINS/000__ПРАКТИКА/file.txt");
        String data = "data";
        var writer = Files.newBufferedWriter(path,
                                             StandardCharsets.UTF_8,
                                             StandardOpenOption.CREATE,
                                             StandardOpenOption.APPEND);
        writer.write(data);
        writer.close();
    }
}

//
//How can you create a Comparator? - Cracking the Java Coding Interview
class Ex038{
    public static void main(String[] args){
        // здесь мы создаем компаратор не в виде напрямую
        // лямбды - а в виде лямбды - но которая ссылается на сравнение
        // через вызов функции объекта (в нашем случае User::getName)
        class User{
            String name = "name";
            String getName(){return name;}
            String getNameAgain(){return name;}
        }
        Comparator
                .comparing(User::getName)
                .thenComparing(User::getNameAgain)
                .reversed();
    }
}

//
//What is a default method? - Cracking the Java Coding Interview
class Ex024{
    // суть default в интерфейсе
    // перетащить pure функцию - которая будет доступна
    // вызовом из класса - часто привязывается к константам и энумам
    // внутри этого интерфейса (которые управляют его поведением)
    interface WithDefault{
        String value = "111";
        default void doSome(){
            System.out.println(value);
        }
    }
}

//
//What is a method reference? - Cracking the Java Coding Interview
class Ex023{
    // единственное что не очевидно - не показываются
    // параметры в вызове (они подразумеваются)
    DoubleUnaryOperator opA = d -> Math.sqrt(d);
    DoubleUnaryOperator opB = Math::sqrt;

    IntBinaryOperator opC = (a,b) -> Integer.max(a,b);
    IntBinaryOperator opD = Integer::max;

    Consumer<String> c = s -> System.out.println(s);
    Consumer<String> d = System.out::println;

    Supplier<List<String>> supA = () -> new ArrayList<>();
    Supplier<List<String>> supB = ArrayList::new;
}

class Main{
    public static void main(String[] args) throws Throwable{
        Ex044.main(null);
    }
}













