package sample;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import kernel.ui.ScoreTable;

public class Utils {

	static private String csv_path = "modules/sample/file/"; 
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
    
    
    public static void score(int time) {
    	if (time == 10) {
	    // nothing happens
    	}
    }
    
    
    public static void writeCSV(int time,int old_time, double[][] list) {
    	int new_time = time + old_time;
    	if (new_time == 1 || new_time == 100 || new_time == 300 || new_time == 500) {
    		@SuppressWarnings("unused")
			BufferedWriter br = null;
    		try {
    			br = new BufferedWriter(new FileWriter(csv_path+"agent_tick" + (time+old_time) + ".csv"));
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

    private static String repeat(String str, int n) {
        return  new String(new char[n]).replace("\0", str);
    }

    public static void printQtable(double[][] Qtable) {
    	int rows = Qtable.length;
    	int cols = Qtable[0].length;
    	int size = 6 + 2;
    	String h = "━";
    	String v = "┃";
    	String first = "┏" + repeat(repeat(h, size) + "┳", cols - 1) + repeat(h, size) + "┓\n";
    	String middle = "┣" + repeat(repeat(h, size) + "╋", cols - 1) + repeat(h, size) + "┫\n";
    	String last = "┗" + repeat(repeat(h, size) + "┻", cols - 1) + repeat(h, size) + "┛\n";
    	StringBuilder string = new StringBuilder(first);
        for (int i = 0; i < rows; i++) {
            string.append(v);
            double[] prob = softmax(Qtable[i]);
            for (int j = 0; j < cols; j++) {
                string.append(" " + String.format("%2.4f", prob[j]) + " ");
                string.append(v);
            }
            string.append("\n");
            if (i < rows - 1)
                string.append(middle);
        }
        string.append(last);
        System.out.print(string.toString());
	}
    
    
    public static Matrix load() {
    	ObjectInputStream ois = null;

        try {
          final FileInputStream fichier = new FileInputStream(csv_path+"Q_table.ser");
          ois = new ObjectInputStream(fichier);
          final Matrix m = (Matrix) ois.readObject();
          return m;
        } catch (final java.io.IOException e) {
        	//file not found
        	double[][] Q = new double[StateDummy.NUMBER][FireBrigadeDummy.ACTION_NUMBER];
        	for (int state = 0; state < StateDummy.NUMBER; state++) {
                for (int action = 0; action < FireBrigadeDummy.ACTION_NUMBER; action++) {
                    Q[state][action] = 0;
                }
            }
        	
        	Matrix m = new Matrix(0, Q);
        	return m;
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
    
    
    public static void save(int time, int old_time, double[][] Q) {
    	if (time %50 == 0) {
    		System.out.println("save File");
    		Matrix m = new Matrix(time + old_time, Q);
    	    ObjectOutputStream oos = null;

    	    try {
    	      final FileOutputStream fichier = new FileOutputStream(csv_path+"Q_table.ser");
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

