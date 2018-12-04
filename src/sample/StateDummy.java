package sample;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class StateDummy {
    private boolean water;
    private boolean fire;
    public static int NUMBER = 4;

    public StateDummy(boolean water, boolean fire) {
        this.water = water;
        this.fire = fire;
    }

    public int getId() {
        int waterInt = water ? 1 : 0;
        int fireInt = fire ? 1 : 0;
        return waterInt * 2 + fireInt;
    }

    @Override
    public String toString() {
        return "{ water: " + water + "; fire: " + fire + " }";
    }

    @Override
    public boolean equals(Object s) {
        StateDummy state = (StateDummy) s;
        return  (this.water == state.water && this.fire == state.fire);
    }
}
