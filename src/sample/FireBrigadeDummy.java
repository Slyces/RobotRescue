package sample;

import static rescuecore2.misc.Handy.objectsToIDs;

import java.sql.Array;
import java.util.*;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;

/**
   A sample fire brigade agent.
 */
public class FireBrigadeDummy extends AbstractSampleAgent<FireBrigade> {
    private static final String MAX_WATER_KEY = "fire.tank.maximum";
    private static final String MAX_DISTANCE_KEY = "fire.extinguish.max-distance";
    private static final String MAX_POWER_KEY = "fire.extinguish.max-sum";

    private int maxWater;
    private int maxDistance;
    private int maxPower;

    private double learningRate = 0.4;
    private double gamma = 0.9;
    public static int ACTION_NUMBER = 3;
    private double[][] Q = new double[StateDummy.NUMBER][ACTION_NUMBER];

    /* Méthodes pour le QLearning 'dummy' */

    public int chooseAction(StateDummy state) {
        double[] row = Q[state.getId()];
        double[] softmax_distribution = Utils.softmax(row);
        return Utils.getRandomIndex(softmax_distribution);
    }

    /*
     * Espace des états
     * {fire, water} (deux booléens)
     * --> 4 états
     *
     * Espace des actions
     *  - 0 : RandomWalk
     *  - 1 : ExtinguishFire
     *  - 2 : SupplyWater
     *
     * Taille de Q : 4 x 3
     * */

    /* Etats */

    /**
     * Etat de la réserve de solution aqueuse
     * @return boolean : True = il reste de l'eau, False = y'en a plus
     */
    private boolean waterLevel() {
        return me().isWaterDefined() && me().getWater() > 0;
    }

    /**
     * Envoie vrai si il y a un feu dans le champ de vision
     * @return boolean : True = l'ensemble des feux proche est non-vide
     */
    private boolean isThereFire() {
        return getBurningBuildings().size() > 0;
    }

    public StateDummy getState() {
        return new StateDummy(waterLevel(), isThereFire());
    }

    /* Actions */

    /**
     * Action : aller chercher de l'eau
     * @return récompense associée à l'action
     */
    public double supplyWater(int time) {
        System.out.println("Going back for water");
        // Head for a refuge
        List<EntityID> path = search.breadthFirstSearch(me().getPosition(), refugeIDs);
        if (path != null) {
            Logger.info("Moving to refuge");
            sendMove(time, path);
        }
        //met -1 si il y a encore de l'eau car sinon reste non stop sur refuge
        return waterLevel() ? -1 : 0.5;
    }

    /**
     * Action : Eteindre le feu le plus proche
     * @return recompense liée à l'action
     */
    public double extinguishFire(int time) {
        System.out.println("Trying to extinguish fire");
        // Find all buildings that are on fire
        Collection<EntityID> all = getBurningBuildings();
        // Can we extinguish any right now?
        for (EntityID next : all) {
//            if (model.getDistance(getID(), next) <= maxDistance && waterLevel()) {
            if (waterLevel()) {
                Logger.info("Extinguishing " + next);
                sendExtinguish(time, next, maxPower);
                return 1;
            }
        }
        return -1;
    }

    public double randomWalk(int time) {
        System.out.println("Moving randomly");
        Logger.info("Moving randomly");
        sendMove(time, randomWalk());
        return 0;
    }

    /* ---------------------------------------------------------------- */
    @Override
    public String toString() {
        return "Dummy fire brigade";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE,StandardEntityURN.HYDRANT,StandardEntityURN.GAS_STATION);
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);
        Logger.info("Sample fire brigade connected: max extinguish distance = " + maxDistance + ", max power = " + maxPower + ", max tank = " + maxWater);

        
        /* Code d'initialisation */
        Q = Utils.load();

    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        System.out.println("===============================================");
    	System.out.println("State :" + getState().toString());
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to channel 1
            sendSubscribe(time, 1);
        }
        for (Command next : heard) {
            Logger.debug("Heard " + next);
        }
        FireBrigade me = me();

        /* Choix d'une action en fonction de Q */
        StateDummy currentState = getState();

        //System.out.println("Current state : " + currentState.toString());

        int action_index = chooseAction(currentState);
        /* On agit, et on récupère la récompenser associée */
        double reward = 0;
        System.out.print("Action : ");
        switch (action_index) {
            case 0:
                reward = randomWalk(time);
                break;
            case 1:
                reward = extinguishFire(time);
                break;
            case 2:
                reward = supplyWater(time);
                break;
            default:
                /* Si on plante ici, c'est qu'on a retourné un indice d'action
                 * imprévu */
                assert false;
        }
        /* Mise à jour de Q en fonction de la récompense */
        StateDummy newState = getState();
        double[] currRow = Q[newState.getId()].clone();
        Arrays.sort(currRow);
        double delta = reward + gamma * currRow[currRow.length - 1]
                                    - Q[currentState.getId()][action_index];
        
        double backup = Q[currentState.getId()][action_index];
        
        Q[currentState.getId()][action_index] += learningRate * delta;
        
        double newvalue = Q[currentState.getId()][action_index];
        System.out.println("eau " + me().getWater());
        System.out.printf("Update : Q[%d][%d] : %f --> %f\n",
                currentState.getId(),
                action_index,
                backup, newvalue);
        
        Utils.save(time, Q);
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
    }

    private Collection<EntityID> getBurningBuildings() {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.BUILDING);
        List<Building> result = new ArrayList<Building>();
        for (StandardEntity next : e) {
            if (next instanceof Building) {
                Building b = (Building)next;
                //seulement si il est atteignable.
                if (b.isOnFire() && model.getDistance(getID(), next.getID()) <= maxDistance) {
                    result.add(b);
                }
            }
        }
        // Sort by distance et surtout si il est en train d'éteindre un batiment, il le met en premier
        Collections.sort(result, new DistanceSorter(location(), model));
        return objectsToIDs(result);
    }

    private List<EntityID> planPathToFire(EntityID target) {
        // Try to get to anything within maxDistance of the target
        Collection<StandardEntity> targets = model.getObjectsInRange(target, maxDistance);
        if (targets.isEmpty()) {
            return null;
        }
        return search.breadthFirstSearch(me().getPosition(), objectsToIDs(targets));
    }
}
