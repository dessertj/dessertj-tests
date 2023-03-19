package org.dessertj.samples.patterns;

public final class SwitchSamples {

    private SwitchSamples() {
    }

    public static String byNumber(int n) {
        return switch(n) {
            case 0 -> "zero";
            case 1 -> "one";
            default -> "any";
        };
    }

    public static double asDouble(Object o) {
        return switch (o) {
            case Number n -> n.doubleValue();
            case String s when s.length() > 0 -> Double.parseDouble(s);
            case null -> 0.0;
            default ->  Double.NaN;
        };
    }
}
