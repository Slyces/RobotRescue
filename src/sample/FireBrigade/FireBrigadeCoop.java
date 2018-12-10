package sample.FireBrigade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rescuecore2.log.Logger;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.EntityID;
import sample.State.*;
import sample.*;

import static rescuecore2.misc.Handy.objectsToIDs;

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
     *  - Extinguish Fire
     *  - Respenish Water
     *
     * Taille de Q : 2**6 x 6
     * */

    /* Etats */

    @Override
    protected double act(int action_index, int time) {
        switch (action_index) {
            case 0:
                return moveTowards(Direction.NORTH, time);
            case 1:
                return moveTowards(Direction.EAST, time);
            case 2:
                return moveTowards(Direction.SOUTH, time);
            case 3:
                return moveTowards(Direction.WEST, time);
            case 4:
                return extinguishFire(time);
            case 5:
                return supplyWater(time);
            default:
                assert false;
                return 0;
        }
    }

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

	public QState getState() {
        return new StateCoop(waterLevel(), isThereFire(), isThereFireBrigades());
    }

    /* -------------- Move towards direction --------------- */
    public double moveTowards(Direction direction, int time) {
        Collection<StandardEntity> entities = model.getEntitiesOfType(StandardEntityURN.BUILDING);
        Building best = null;
        FireBrigade me = me();
        double best_dist = 10e10;
        for (StandardEntity e: entities) {
            Building building = (Building) e;
            if (Utils.direction(me, building) == direction) {
                if (best == null || Utils.distance(me, building, direction) < best_dist) {
                    best = building;
                    best_dist = Utils.distance(me, building, direction);
                }
            }
        }
        // Try to get to anything within maxDistance of the target
        EntityID target = best != null ? best.getID() : null;
        Collection<StandardEntity> targets = model.getObjectsInRange(target, maxDistance);
        List<EntityID> path = null;
        if (! targets.isEmpty()) {
            path = search.breadthFirstSearch(me().getPosition(), objectsToIDs(targets));
        }
        sendMove(time, path);
        return 0;
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
