package SIAABMSimUtils;

public class polar {
	
	private double pR;
	private double pTeta;
	
	public polar() {
		pR = 0.0;
		pTeta = 0.0;
	}
	
	public polar (double r, double teta){
		pR = r;
		pTeta = teta;
	}
	
	public void setR (double r){
		pR = r;
	}
	
	public double getR(){
		return pR;
	}
	
	public void setTeta (double teta){
		pTeta = teta;
	}
	
	public double getTeta (){
		return pTeta;
	}
	
	public void set(double r, double teta){
		pR = r;
		pTeta = teta;
	}

}
