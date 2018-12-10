package sample.src.sample.FireBrigade;

import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import sample.*;
import sample.FireBrigadeDummy;
import sample.src.sample.Direction;
import sample.src.sample.State.StateDummy;

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
}
