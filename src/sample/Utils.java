package sample;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    
    
    public static void writeCSV(int time, double[][] list,String name) {
    	if (time == 50) {
    		@SuppressWarnings("unused")
			BufferedWriter br = null;
    		try {
    			br = new BufferedWriter(new FileWriter("modules/sample/src/sample/agent_"+name+"_tick"+time+".csv"));
    		} catch (IOException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
        	StringBuilder sb = new StringBuilder();

        	// Append strings from array
        	for (double[] a : list) {
        		for (double b :a) {
        			sb.append(b);
        	    	sb.append(",");
        		}
        	 sb.append("\n");
        	}
        	
        	try {
				br.write(sb.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    
    public static double[][] load() {
    	ObjectInputStream ois = null;

        try {
          final FileInputStream fichier = new FileInputStream("modules/sample/src/sample/Q_table.ser");
          ois = new ObjectInputStream(fichier);
          final Matrix m = (Matrix) ois.readObject();
          return m.matrice;
        } catch (final java.io.IOException e) {
        	//file not found
        	double[][] Q = new double[StateDummy.NUMBER][FireBrigadeDummy.ACTION_NUMBER];
        	for (int state = 0; state < StateDummy.NUMBER; state++) {
                for (int action = 0; action < FireBrigadeDummy.ACTION_NUMBER; action++) {
                    Q[state][action] = 0;
                }
            }
        	return Q;
        } catch (final ClassNotFoundException e) {
          e.printStackTrace();
        } finally {
          try {
            if (ois != null) {
              ois.close();
            }
          } catch (final IOException ex) {
            ex.printStackTrace();
          }
        }
		return null;
    }
    
    
    public static void save(int time, double[][] Q) {
    	if (time %50 == 0) {
    		System.out.println("save File");
    		Matrix m = new Matrix(Q);
    	    ObjectOutputStream oos = null;

    	    try {
    	      final FileOutputStream fichier = new FileOutputStream("modules/sample/src/sample/Q_table.ser");
    	      oos = new ObjectOutputStream(fichier);
    	      oos.writeObject(m);
    	      oos.flush();
    	    } catch (final java.io.IOException e) {
    	      e.printStackTrace();
    	    } finally {
    	      try {
    	        if (oos != null) {
    	          oos.flush();
    	          oos.close();
    	        }
    	      } catch (final IOException ex) {
    	        ex.printStackTrace();
    	      }
    	    }
    	}
    }
}

