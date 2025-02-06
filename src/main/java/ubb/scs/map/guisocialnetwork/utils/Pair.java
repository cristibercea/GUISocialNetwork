package ubb.scs.map.guisocialnetwork.utils;

import java.util.Objects;

/**
 * A template class that stores a pair of 2 different data types objects
 * @param <A> the first element (X) is of type A
 * @param <B> the second element (Y) is of type B
 */
public class Pair<A,B> {
    private A x;
    private B y;
    public Pair(A a, B b) {
        x = a;
        y = b;
    }
    public A getX() {
        return x;
    }
    public B getY() {
        return y;
    }
    public void setX(A a) {
        x = a;
    }
    public void setY(B b) {
        y = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(x, pair.x) && Objects.equals(y, pair.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
