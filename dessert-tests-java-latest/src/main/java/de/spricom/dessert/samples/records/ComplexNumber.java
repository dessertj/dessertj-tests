package de.spricom.dessert.samples.records;

public record ComplexNumber(
        double a,
        double bi
) {

    ComplexNumber plus(ComplexNumber n) {
        return new ComplexNumber(a + n.a, bi + n.bi);
    }
}
