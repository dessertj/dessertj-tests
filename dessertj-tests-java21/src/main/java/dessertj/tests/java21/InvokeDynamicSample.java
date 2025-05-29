package dessertj.tests.java21;

public class InvokeDynamicSample {

    public static void main(String[] args) {
        Runnable r = () -> System.out.println("Hello, invokedynamic!");
        r.run();
    }
}
