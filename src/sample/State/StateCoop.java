package sample.State;

public class StateCoop implements QState{
	
    private boolean water;
    private boolean fire;
    private boolean[] voisins;
    private static int maxWater = 15000;
    public static int NUMBER = 64;
    
    
    

	public StateCoop(boolean water, boolean fire, boolean[] voisins) {
		super();
		this.water = water;
		this.fire = fire;
		this.voisins = voisins;
	}
	

	public int getId() {
		String binary = "";
		boolean states[] = {this.water, this.fire, this.voisins[0], this.voisins[1], this.voisins[2], this.voisins[3]};
		for (boolean b : states) {
			binary += b ? 1 : 0;
		}
		//string to int
		return Integer.parseInt(binary, 2);
    }
	
	@Override
    public boolean equals(Object s) {
        StateCoop state = (StateCoop) s;
        return  (this.water == state.getWater() && this.fire == state.getFire() && this.voisins == state.getVoisins());
    }
	
	
	
	
	
	//get set
	
	public boolean getWater() {
		return water;
	}

	public void setWater(boolean water) {
		this.water = water;
	}

	public boolean getFire() {
		return fire;
	}

	public void setFire(boolean fire) {
		this.fire = fire;
	}

	public boolean[] getVoisins() {
		return voisins;
	}

	public void setVoisins(boolean[] voisins) {
		this.voisins = voisins;
	}

	public static int getMaxWater() {
		return maxWater;
	}

	public static void setMaxWater(int maxWater) {
		StateCoop.maxWater = maxWater;
	}

}
