package org.dessertj.samples.records;

public record ComplexNumber(
        double a,
        double bi
) {
    ComplexNumber(double a) {
        this(a, 0);
    }

    ComplexNumber plus(ComplexNumber n) {
        return new ComplexNumber(a + n.a, bi + n.bi);
    }

    ComplexNumber minus(ComplexNumber n) {
        return new ComplexNumber(a - n.a, bi - n.bi);
    }

    ComplexNumber times(ComplexNumber n) {
        return new ComplexNumber(a * n.a - bi * n.bi, a * n.bi + bi * n.a);
    }

    ComplexNumber div(ComplexNumber n) {
        double q = n.a * n.a + n.bi * n.bi;
        return new ComplexNumber((a * n.a + bi * n.bi) / q, (bi * n.a - a * n.bi) / q);
    }

    ComplexNumber power(ComplexNumber n) {
        double p = a * a + bi * bi;
        double r = Math.pow(p, n.a * 0.5) * Math.exp(-n.bi * arg());
        double c = n.a * arg() + 0.5 * n.bi * Math.log(p);
        return new ComplexNumber(r * Math.cos(c), r * Math.sin(c));
    }

    double arg() {
        return Math.atan2(bi, a);
    }
}
