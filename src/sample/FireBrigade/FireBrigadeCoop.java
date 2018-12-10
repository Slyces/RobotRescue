package sample.FireBrigade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import firesimulator.world.FireBrigade;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import sample.*;
import sample.State.QState;
import sample.State.StateCoop;
import sample.State.StateDummy;

/**
   A sample fire brigade agent.
 */
public class FireBrigadeCoop extends FireBrigadeDummy {
	
	public static int ACTION_NUMBER = 6;

    /* Méthodes pour le QLearning 'coop' */

    /*
     * Espace des états :
     *  - Présence d'un feu [bool]
     *  - Niveau d'eau [bool]
     *  - Team-Mate Nord [bool]
     *  - Team-Mate Est [bool]
     *  - Team-Mate Sud [bool]
     *  - Team-Mate Ouest [bool]
     *
     * Espace des actions
     *  - Move Nord
     *  - Move Est
     *  - Move Sud
     *  - Move Ouest
     *  - Respenish Water
     *  - Extinguish Fire
     *
     * Taille de Q : 2**6 x 6
     * */

    /* Etats */
	
	
	@Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE,StandardEntityURN.HYDRANT,StandardEntityURN.GAS_STATION);
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);
        Logger.info("Sample fire brigade connected: max extinguish distance = " + maxDistance + ", max power = " + maxPower + ", max tank = " + maxWater);

        this.me = me();
        
        /* Code d'initialisation */
        Matrix m = Utils.loadCoop();
        old_time = m.time;
        Q = m.matrice;

    }

	private boolean waterLevel() {
    	if (myWater == 0)
        	return false;
        else
        	return true;
    }
	
	public QState getState() {
        return new StateCoop(waterLevel(), isThereFire(), isThereFireBrigades());
    }
	
    public boolean[] isThereFireBrigades() {
    	List<Human> voisins = new ArrayList<Human>();
    	boolean[] retour = new boolean[4];
    	Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE);
    	for (StandardEntity next : e) {
    		Human h = (Human)next;
            if (h == me()) {
                continue;
            }
            voisins.add(h);
    	}
    	for (Human h : voisins) {
    		if (Utils.direction(me(), (rescuecore2.standard.entities.FireBrigade)h) == Direction.NORTH ) {
    			retour[0] = true;
    		}
    		if (Utils.direction(me(), (rescuecore2.standard.entities.FireBrigade)h) == Direction.EAST ) {
    			retour[1] = true;
    		}
    		if (Utils.direction(me(), (rescuecore2.standard.entities.FireBrigade)h) == Direction.SOUTH ) {
    			retour[2] = true;
    		}
    		if (Utils.direction(me(), (rescuecore2.standard.entities.FireBrigade)h) == Direction.WEST ) {
    			retour[3] = true;
    		}
    	}

        return retour;
    }
    
}
