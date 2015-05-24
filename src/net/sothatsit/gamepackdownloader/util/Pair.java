package net.sothatsit.gamepackdownloader.util;

public class Pair<T extends Object> {

    private T obj1;
    private T obj2;

    public Pair(T obj1, T obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    public T getObj1() {
        return obj1;
    }

    public T getObj2() {
        return obj2;
    }

}
