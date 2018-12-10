package sample.src.sample.FireBrigade;

import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.EntityID;
import sample.src.sample.Direction;
import sample.src.sample.Utils;

import java.util.*;

import static rescuecore2.misc.Handy.objectsToIDs;

/**
   A sample fire brigade agent.
 */
public class FireBrigadeCoop extends FireBrigadeDummy {

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

    /* ------------------ Perceive agents ------------------ */
    boolean isThereFireBrigades(Direction direction) {
        return false;
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
        EntityID target = best.getID();
        Collection<StandardEntity> targets = model.getObjectsInRange(target, maxDistance);
        List<EntityID> path = null;
        if (! targets.isEmpty()) {
            path = search.breadthFirstSearch(me().getPosition(), objectsToIDs(targets));
        }
        sendMove(time, path);
        return 0;
    }
}