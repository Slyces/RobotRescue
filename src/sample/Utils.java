package sample;
import java.util.Random;

public class Utils {

    static private Random rand = new Random();

    public static int getRandomIndex(double[] distribution){
        double p = rand.nextDouble();
        double sum = 0.0;
        int i = 0;
        while(sum < p){
            sum += distribution[i];
            i++;
        }
        return i - 1;
    }

    /**
     * Fonction qui normalise les probabilités d'une action en
     * fonction des Q valeurs de l'état courant
     * @param array q_valeurs
     * @return valeur du softmax de cet indice
     */
    public static double[] softmax(double[] array, double beta) {
        double[] values = new double[array.length];
        double sum_array = 0;
        for (double value : array)
            sum_array += Math.exp(value * beta);
        for (int i = 0; i < values.length; i++) {
            values[i] = Math.exp(array[i] * beta) / sum_array;
        }
        return values;
    }

    public static double[] softmax(double[] array) {
        return softmax(array, 8);
    }
}
