package org.dessertj.samples.nesting;

public class Car {

    @FunctionalInterface
    public interface Action {
        void go();
    }

    public class Engine {

        public class Carburetor {
            public void consume(double gas, Action pipe) {
                new Action() {
                    @Override
                    public void go() {
                        pipe.go();
                    }
                }.go();
                Engine.this.burn(gas);
            }
        }

        private Carburetor carburetor = new Carburetor();
        private Gearing gearing;

        private void burn(double gas) {
        }
    }

    class Gearing {
        private final Wheel[] wheels;

        public Gearing(Wheel[] wheels) {
            this.wheels = wheels;
        }
    }

    class Mirror$1$Left {

    }

    interface Window {

    }

    record Wheel(double tread) {

    }

    private final Window frontWindow = new Window() {};
    private final Mirror$1$Left mirror = new Mirror$1$Left();
    private final Engine engine;

    public Car() {
        var wheels = new Wheel[4];
        for (int i = 0; i < wheels.length; i++) {
            wheels[i] = new Wheel(5.2);
        }
        Gearing gearing = new Gearing(wheels);
        engine = new Engine();
        engine.gearing = gearing;
    }

    public void drive() {
        record Control(Engine engine, Gearing gearing, Wheel... wheels) {}

        Control control = new Control(engine, engine.gearing, engine.gearing.wheels);
        control.engine.carburetor.consume(2.0, () -> System.out.print("."));
    }

    public static void main(String[] args) {
        new Car().drive();
    }
}
