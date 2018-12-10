package sample.src.sample.FireBrigade;

import static rescuecore2.misc.Handy.objectsToIDs;

import java.util.*;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Refuge;
import sample.*;
import sample.src.sample.State.QState;
import sample.src.sample.State.StateDummy;

/**
   A sample fire brigade agent.
 */
public class FireBrigadeDummy extends AbstractSampleAgent<FireBrigade> {
    private static final String MAX_WATER_KEY = "fire.tank.maximum";
    private static final String MAX_DISTANCE_KEY = "fire.extinguish.max-distance";
    private static final String MAX_POWER_KEY = "fire.extinguish.max-sum";

    protected int maxWater;
    protected int maxDistance;
    protected int maxPower;

    protected double learningRate = 0.4;
    protected double gamma = 0.9;
    public static int ACTION_NUMBER = 3;
    protected double[][] Q = new double[sample.src.sample.State.StateDummy.NUMBER][ACTION_NUMBER];

    private boolean learn = true;

	public FireBrigade me = null;
	
	private int myWater = 15000;
	private int old_time;


    /* Méthodes pour le QLearning 'dummy' */

    public int chooseAction(QState state) {
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
    @SuppressWarnings("null")
	private int waterLevel() {
    	if (myWater == 0)
        	return 0;
        else
        	return 1;
    }

    /**
     * Envoie vrai si il y a un feu dans le champ de vision
     * @return boolean : True = l'ensemble des feux proche est non-vide
     */
    private boolean isThereFire() {
        return getBurningBuildings().size() > 0;
    }

    public QState getState() {
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
        List<EntityID> path = search.breadthFirstSearch(me.getPosition(), refugeIDs);
        if (path != null) {
            Logger.info("Moving to refuge");
            sendMove(time, path);
        }
        if (location() instanceof Refuge) {
        	myWater = me().getWater();
        }
        //met -1 si il y a encore de l'eau car sinon reste non stop sur refuge
        int temp = waterLevel();
        return temp == 0 ? 0.5 : -0.2;
    }

    /**
     * Action : Eteindre le feu le plus proche
     * @return recompense liée à l'action
     */
    public double extinguishFire(int time) {
        System.out.println("Trying to extinguish fire");
        // Find all buildings that are on fire
        Collection<EntityID> all = getBurningBuildings();
        System.out.println(all);
        // Can we extinguish any right now?
        for (EntityID next : all) {
//            if (model.getDistance(getID(), next) <= maxDistance && waterLevel()) {
            if (myWater > 0) {
                Logger.info("Extinguishing " + next);
                sendExtinguish(time, next, maxPower);
                sendSpeak(time, 1, ("Extinguishing " + next).getBytes());
                myWater = Math.max(myWater - 3000, 0);
                return 1;
            }
        }
        return -1;
    }

    public double randomWalk(int time) {
        System.out.println("Moving randomly");
        Logger.info("Moving randomly");
        sendMove(time, randomWalk());
        return waterLevel() == 0 ? -0.2 : 0.1;
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

        this.me = me();
        
        /* Code d'initialisation */
        Matrix m = Utils.load();
        old_time = m.time;
        Q = m.matrice;

    }

    protected double act(int action_index, int time) {
        /* On agit, et on récupère la récompenser associée */
        switch (action_index) {
            case 0:
                return randomWalk(time);
            case 1:
                return extinguishFire(time);
            case 2:
                return supplyWater(time);
            default:
                /* Si on plante ici, c'est qu'on a retourné un indice d'action
                 * imprévu */
                assert false;
                return 0;
        }
    }

    protected void improveTable(QState currentState, double reward, int action_index, int time) {
        /* Mise à jour de Q en fonction de la récompense */
        QState newState = getState();
        double[] currRow = Q[newState.getId()].clone();
        Arrays.sort(currRow);
        double delta = reward + gamma * currRow[currRow.length - 1]
                - Q[currentState.getId()][action_index];

        double backup = Q[currentState.getId()][action_index];

        Q[currentState.getId()][action_index] += learningRate * delta;

        double newvalue = Q[currentState.getId()][action_index];

        System.out.printf("Update : Q[%d][%d] : %f --> %f\n",
                currentState.getId(),
                action_index,
                backup, newvalue);
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to channel 1
            sendSubscribe(time, 1);
        }
        for (Command next : heard) {
            Logger.debug("Heard " + next);
        }

        /* Choix d'une action en fonction de Q */
        QState currentState = getState();
        int action_index = chooseAction(currentState);

        /* Réalisation de l'action */
        double reward = act(action_index, time);

        if (learn) {
            this.me = me();
            improveTable(currentState, reward, action_index, time);
            Utils.save(time,old_time, Q);
            Utils.writeCSV(time,old_time, Q);
            Utils.printQtable(Q);
        }
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
        return search.breadthFirstSearch(me.getPosition(), objectsToIDs(targets));
    }
}
