package sample;

import java.io.Serializable;

public class Matrix implements Serializable{

	private static final long serialVersionUID = 1L;
	public double[][] matrice;
	
	public Matrix(double[][] list) {
		this.matrice = list;
	}
}