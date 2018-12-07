package sample;

import java.io.Serializable;

public class Matrix implements Serializable{

	private static final long serialVersionUID = 1L;
	public double[][] matrice;
	public String name;
	public int time;
	
	public Matrix(int time, double[][] list) {
		this.matrice = list;
		this.time = time;
	}
}