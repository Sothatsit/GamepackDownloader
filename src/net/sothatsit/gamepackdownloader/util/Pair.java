package net.sothatsit.gamepackdownloader.util;

public class Pair<A extends Object, B extends Object > {

    private A obj1;
    private B obj2;

    public Pair(A obj1, B obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    public A getObj1() {
        return obj1;
    }

    public B getObj2() {
        return obj2;
    }

}
