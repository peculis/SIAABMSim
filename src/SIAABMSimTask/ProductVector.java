package SIAABMSimTask;

import java.text.*;

public class ProductVector {

	public enum ProductCategory {Application, Capabilities, SysEng, 
		SysArchitect,SoftEng, SoftDev, Nil};
		private ProductCategory productCategory;
		private int vn;
		private double actual []; //This is the actual value of the product.
		private double ideal []; //The idealProduct is defined at initialization or set during operation
		private String productName = "Nil";
		private boolean complete = false;
		private DecimalFormat df;
		private Contract contract;


		public ProductVector (ProductCategory category, int n){
			productCategory = category;
			vn = n;
			actual = new double [vn];
			ideal = new double [vn];

			for (int i=0; i<vn; i++){
				actual[i]= 0.0;
				ideal[i] = 0.0;
			}
			df = new DecimalFormat("#.####");
		}


		//This was being used only once by Transformation to create the IdealPin.
		//With the changes I am implementing n ProductVector, as on 29 May 2010, it may be no longer required.
		public ProductVector (ProductVector p){
			double vp[];
			productCategory = p.productCategory;
			vn = p.size();
			actual = new double [vn];
			ideal = new double [vn];
			vp = p.ideal;
			for (int i=0; i<vn; i++){
				ideal[i] = vp[i];            //ideal value
				actual[i]= 0.0;              //actual value
			}
			df = new DecimalFormat("#.####");
		}



		//This s used only once by TaskConfguration for creating the first ProductVector Need = {1,1,1,1,1,1}
		public ProductVector (ProductCategory category, int n, double s []){
			df = new DecimalFormat("#.####");
			productCategory = category;
			vn = n;
			actual = new double [vn];
			ideal = new double [vn];
			for (int i=0; i<vn; i++){
				ideal[i]= s[i];
				actual[i]= s[i];
			}
		}
		
		public void setContract(Contract c){
			contract = c;
		}
		
		public Contract getContract(){
			return contract;
		}

		// result = p - q
		private void subtract(double p [], double q [], double result []){	
			for (int i=0; i<vn; i++){
				result[i] = (p [i] - q[i]);
			}
		}

		//This is an important method and shall not be removed.
		public void saveIdealProduct(){
			for (int i=0; i<vn; i++){
				ideal[i]= actual[i];
			}
		}

		public void setName (String name){
			productName = name;
		}

		public String getName (){
			return productName;
		}

		public ProductCategory getProductCategory (){
			return productCategory;
		}

		public void printDetails (){
			System.out.println("Product " + this.getName() + " size = " + this.size());
		}

		public void printProduct(){
			System.out.print("Actual " + productName + " ");
			for (int i=0; i<vn; i++){
				System.out.print(df.format(actual[i]) + "   ");
			}
			System.out.println();
		}

		public double getEffectiveness(){
			double e;
			e = 0.0;
			for (int i=0; i<vn; i++){
				e = e + actual[i];
			}
			return e;
		}

		public void printEffectiveness(){
			System.out.println("Actual " + productName + " effectiveness " + df.format(getEffectiveness()));
		}

		public void printIdealProduct(){
			System.out.print("Ideal " + productName + " ");
			for (int i=0; i<vn; i++){
				System.out.print(df.format(ideal[i]) + "   ");
			}
			System.out.println();
		}

		public void setComplete(boolean state){
			complete = state;
		}

		public boolean Complete(){
			return complete;
		}

		public boolean isZero(){
			double value;
			value = 0.0;
			for (int i=0; i<vn; i++){
				value = value + actual[i];
			}
			if(value == 0.0){
				return true;
			}
			else{
				return false;
			}
		}


		public double error(){
			double error;
			error = 0.0;
			for (int i=0; i<vn; i++){
				error = (error + ideal [i] - actual[i]) / vn;
			}
			return error;
		}

		public void set (double s []){
			for (int i=0; i<vn; i++){
				actual[i]= s[i];
			}
		}

		public double [] xget(){
			return actual;
		}

		public double [] get(){
			double result [];
			result = new double[actual.length];
			for (int i=0; i<result.length; i++){
				result[i]= actual[i];
			}
			return result;
		}

		public void setIdeal (double s []){
			for (int i=0; i<vn; i++){
				ideal[i]= s[i];
			}
		}

		public double [] getIdeal(){
			return ideal;
		}

		public double [] getActual(){
			return actual;
		}

		public int size (){
			return vn;
		}

		public double whole (){
			double w = 0.0;
			for (int i=0; i<vn; i++){
				w = w + actual [i];
			}
			return w;
		}

		public void show (){
			for (int i=0; i<vn; i++){
				System.out.print(actual[i]);
				System.out.print("\t");
			}
			System.out.println(" ");
		}


		//This s the main multiplication to produce Pout - 28 May 2010
		//The ActualProduct is multiplied by the TransformationMatrix m and the result goes to the ProductVector v2.
		public void multiply (TransformationMatrix m, ProductVector v2){
			//System.out.println("Begin Vector.multiply (Matrix, Vector)");
			int mnr = m.nrows();
			int mnc = m.ncolumns();
			double mv [][] = new double [mnr][mnc];
			mv = m.get();
			//System.out.println("m.get Vector.multiply (Matrix, Vector)");
			int v2n = v2.size();
			double v2v [] = new double [v2n];
			v2v = v2.get();
			//System.out.println("v.get Vector.multiply (Matrix, Vector)");
			if (vn != mnr){
				System.out.println("Error Vector.multiply (Matrix, Vector) vn != mnr");
				return;
			}
			if (v2n != mnc) {
				System.out.println("Error Vector.multiply (Matrix, Vector) v2n != mnc");
				return;
			}
			//System.out.println("Loop Vector.multiply (Matrix, Vector)");
			for (int j=0; j<mnc; j++){
				v2v[j] = 0.0;
				for (int i=0; i< vn; i++){
					v2v[j] = v2v[j] + actual[i]* mv[i][j];
				}
			}
			v2.set(v2v);
			//System.out.println("End Vector.multiply (Matrix, Vector)");
		}
}
