package SIAABMSimTask;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import SIAABMSimUtils.probability;

public class TransformationMatrix {

	private int mnr;
	private int mnc;
	private double [][]m; // m is the actual matrix.
	private double [][]mr; // mr is the real matrix which contains errors of specification.
	private double [][]mo; // mo is the original matrix that is never modified.
	private String transformationName = "";
	private double kMean, kSD, error;
	private int nIncrements; //number of incremental development steps.
	private double reworkAllowed; //percentage of effort allowed to rework
	private double maxError = 0.5; //allows a maximum error of 50%;
	private probability p;
		
	public TransformationMatrix (String fileName) throws Throwable {
		BufferedReader inputStream = null;
		int i = 0;
		int j = 0;
		int tokenCount = 0;
		String l, n;
		double x;
		kMean = 1.0;
		kSD = 0.0;
		nIncrements = 1;
		reworkAllowed = 0.0;
		
		p = probability.getInstance();
		try {
			inputStream = new BufferedReader (new FileReader (fileName));
			inputStream.mark(2000000);
		    while ((l = inputStream.readLine()) != null){
		    	StringTokenizer st = new StringTokenizer(l);
		    	tokenCount = st.countTokens();
		    	i++;
		    	j = tokenCount;
		    }
		    mnr = i;
		    mnc = j;
		    m = new double [mnr][mnc];
		    mo = new double [mnr][mnc];
		    mr = new double [mnr][mnc];
		    inputStream.reset();
		    i = 0;
		    while ((l = inputStream.readLine()) != null){
				StringTokenizer st = new StringTokenizer(l);
				tokenCount = st.countTokens();
				j = 0;
			    while (st.hasMoreTokens()) {
		          n = st.nextToken(); 
		          x = Double.parseDouble(n);
		          drawError();
		          m [i][j] = x;  //actual matrix
		          mo [i][j] = x; //ideal matrix
		          mr [i][j] = x * (1.0 - error); //in this case the real matrix does not contain errors.
		          if(error > 0.0){
		        	  //System.out.println("ERROR IS GREATER THAN ZERO " + (1.0 - error) + " ************"); 
		          }
		          else{
		        	  //System.out.println("ERROR IS ZERO " + (1.0 - error));
		          }
		          j++;
		        }
			    i++;
			}
		}
		catch (IOException e){
			System.out.print("Problems reading file ");
			System.out.println(fileName);
		}
		finally {
			if (inputStream != null){
				inputStream.close();
			}
		}
	}
	
	
	public TransformationMatrix (TransformationMatrix T){
		    mnr = T.mnr;
		    mnc = T.mnc;
		    kMean = 0.0;
			kSD = 0.0;
			p = probability.getInstance();
		    m = new double [mnr][mnc];
		    mo = new double [mnr][mnc];
		    mr = new double [mnr][mnc];
		    for (int i=0; i<mnr; i++){
		    	for (int j=0; j< mnc; j++){
		    		//drawError();
		    		mo[i][j]= T.mo[i][j];
		    		mr[i][j]= T.mo[i][j];
		    		m[i][j]= T.mo[i][j];
		    	}
			}
	}
	
		
	public TransformationMatrix (int nRows, int nColunms) {
		mnr = nRows;
		mnc = nColunms;
		mo = new double [mnr] [mnc];
		m = new double [mnr][mnc];
	}
		
	public TransformationMatrix (int nRows, int nColunms, double mv [][]) {
	   	mnr = nRows;
		mnc = nColunms;
		mo = new double [mnr] [mnc];
		m = new double [mnr][mnc];
		for (int i=0; i<mnr; i++){
	    	for (int j=0; j< mnc; j++){
	    		mo[i][j]= mv[i][j];
	    		m[i][j]= mv[i][j];
	    	}
		}
	}
		
	public void restoreOriginal (){
	   	for (int i=0; i<mnr; i++){
	    	for (int j=0; j< mnc; j++){
	    		m[i][j]= mo[i][j];
	    	}
		}
	}
	    
	public void clearActual (){
	   	for (int i=0; i<mnr; i++){
	    	for (int j=0; j< mnc; j++){
	    		m[i][j]= 0.0;
	    	}
		}
	}
	    
	public void setActualValue (double value, int row, int column){
		//System.out.println("setActualValue = " + value);
		if(row < mnr && column < mnc){
			m [row][column] = value;
		}
		else{
			System.out.println("Error in TransformationMatrix.setActualValue");
		}
	}
	    
	public double getActualValue (int row, int column){
	   	return m[row][column];
	}
	    
	public double getIdealValue (int row, int column){
	   	return mo[row][column];
	}
	
	public void setName (String name){
		transformationName = name;
	}
	
	public String getName (){
		return transformationName;
	}
	
	public void setKMean(double KM){
		kMean = KM;
	}
	
	public double getKMean(){
		return kMean;
	}
	
	public void setKSD(double KSD){
		kSD = KSD;
	}
	
	public double getKSD(){
		return kSD;
	}
	
	public void setNumberOfIncrements(int numberOfIncrements){
		nIncrements = numberOfIncrements;
	}
	
	public int getNumberOfIncrements(){
		return nIncrements;
	}
	
	public void setReworkAllowed(double thisReworkAllowed){
		reworkAllowed = thisReworkAllowed;
	}
	
	public double getReworkAllowed(){
		return reworkAllowed;
	}
	
	private void drawError(){
		double k;
		k = p.getNormalDistributionSample(kMean, kSD); //Draw Knowledge.
		error = maxError * (1.0 - k); //allows a maximum error of maxError;
	}
	    
	public void set (double mv [][]) {
		for (int i=0; i<mnr; i++){
	    	for (int j=0; j< mnc; j++){
	    		m[i][j]= mv[i][j];
	    	}
		}
	}
	    
	public int nonZero () {
		int nz = 0;
		for (int i=0; i<mnr; i++){
	    	for (int j=0; j< mnc; j++){
	    		if (mo[i][j]!= 0.0) {
	    			nz++;
	    		}
	    	}
		}
		return nz;
	}
	
	public void printDetails (){
		System.out.println("Tranformation " + this.transformationName + 
				" nrows = " + this.nrows() + " ncolumns = " +
				this.ncolumns()+ " ntasks = " + this.nonZero());
	}
	    
	// The following "multiply" method multiplies the matrix by a constant "c"
	// without modifying the original matrix and returns the two dimensional 
	// array = c.m.
	    
	public double [][] multiply (double c) {
		for (int i=0; i<mnr; i++){
			for (int j=0; j< mnc; j++){
		    	m[i][j]= c * mo[i][j];
		    }
		}
	    return m;
	}
	    
	// The following "multiply" method multiplies the matrix by another matrix "c",
	// element by element, without modifying the original matrix and returns the 
	// two dimensional array = c.m.
	    
	public double [][] multiply (double [][] c) {
		for (int i=0; i<mnr; i++){
	    	for (int j=0; j< mnc; j++){
	    		m[i][j]= c[i][j] * mo[i][j];
	    	}
		}
	   	return m;
	}
	    
	public void show(){
	   	for (int i=0; i<mnr; i++){
	    	for (int j=0; j< mnc; j++){
	    		System.out.print(m[i][j]);
	    		System.out.print("\t");
	    	}
	    	System.out.println(" ");
		}
	}
	
	public int nrows (){
		return mnr;
	}
		
	public int ncolumns (){
		return mnc;
	}
		
	public double [][] get (){
		return m;
	}
	
	public double [][] getOriginal (){
		return mo;
	}
}
