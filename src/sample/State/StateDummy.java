package sample.State;

public class StateDummy implements QState {
    private int water;
    private boolean fire;
    private static int maxWater = 15000;
    public static int NUMBER = 4;

    public StateDummy(int water, boolean fire) {
        this.water = water;
        this.fire = fire;
    }

    public int isWater() {
		return water;
	}

	public void setWater(int water) {
		this.water = water;
	}

	public boolean isFire() {
		return fire;
	}

	public void setFire(boolean fire) {
		this.fire = fire;
	}

	public int getId() {
        int waterInt = water;
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
