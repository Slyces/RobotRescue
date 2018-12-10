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
 * A sample fire brigade agent.
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
    public String getAgentName() {
        return "coop";
    }

    @Override
    protected double act(int action_index, int time) {
    	System.out.print("Action index " + action_index + " :");
        switch (action_index) {
            case 0:
                System.out.println("Moving towards North");
                return moveTowards(Direction.NORTH, time);
            case 1:
				System.out.println("Moving towards East");
                return moveTowards(Direction.EAST, time);
            case 2:
				System.out.println("Moving towards South");
                return moveTowards(Direction.SOUTH, time);
            case 3:
				System.out.println("Moving towards West");
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
		double best_dist = -1;
		for (StandardEntity e : entities) {
			Building building = (Building) e;
			if (Utils.direction(me, building) == direction) {
				if (best == null || Utils.distance(me, building, direction) > best_dist) {
					best = building;
					best_dist = Utils.distance(me, building, direction);
				}
			}
		}
		// Try to get to anything within maxDistance of the target
		EntityID target = best != null ? best.getID() : null;
		System.out.println("Best is " + (best != null ? best.toString() : "null"));
        if (target != null) {
            List<EntityID> bestIDs = new ArrayList<>();
            bestIDs.add(target);
            List<EntityID> path = search.breadthFirstSearch(me.getPosition(), bestIDs);
            if (path != null) {
                sendMove(time, path);
                System.out.println(path.toString());
                return 0;
            }
		}
		randomWalk(time);
        System.out.println("Randomwalked !!!");
		return 0;
	}

	public boolean[] isThereFireBrigades() {
		List<Human> voisins = new ArrayList<>();
		boolean[] retour = new boolean[4];
		Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE);
		for (StandardEntity next : e) {
			Human h = (Human) next;
			if (h == me()) {
				continue;
			}
			voisins.add(h);
		}
		for (Human h : voisins) {
			if (model.getDistance(getID(), h.getID()) <= 10000) {

				if (Utils.direction(me(), (rescuecore2.standard.entities.FireBrigade) h) == Direction.NORTH) {
					retour[0] = true;
				}
				if (Utils.direction(me(), (rescuecore2.standard.entities.FireBrigade) h) == Direction.EAST) {
					retour[1] = true;
				}
				if (Utils.direction(me(), (rescuecore2.standard.entities.FireBrigade) h) == Direction.SOUTH) {
					retour[2] = true;
				}
				if (Utils.direction(me(), (rescuecore2.standard.entities.FireBrigade) h) == Direction.WEST) {
					retour[3] = true;
				}
			}
		}

		return retour;
	}
}
