package checker.patpar;

import checkers.javari.quals.*;

class Data { int i; }

class Inc extends Xform<Data> {
    int xform(Data d) /*@ReadOnly*/ {
        return d.i++;
    }
}

abstract class Xform<A> {
    abstract int xform(A a) /*@ReadOnly*/;
}

class Indirect<A> {
    A a;
    Xform<A> x;

    public @PolyRead A getA() /*@PolyRead*/ {
        return a;
    }

    public int get() /*@ReadOnly*/ {
        return x.xform(a); // ERROR incompatible types*found   : @ThisMutable A*required: A*
    }
}

public class RoGenerics {
    void foo(Indirect<Data> b, @ReadOnly Indirect<Data> c) {
        Xform<Data> x = b.x;
        x.xform(b.getA());

        @ReadOnly Xform<Data> y = c.x;
        y.xform(c.getA()); // ERROR incompatible types*found   : @ReadOnly Data*required: @Mutable Data*
    }
}