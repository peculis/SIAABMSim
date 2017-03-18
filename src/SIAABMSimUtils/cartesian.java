package SIAABMSimUtils;

public class cartesian {
	
	private double cX;
	private double cY;
	
	public cartesian(){
		cX = 0.0;
		cY = 0.0;
	}
	
	public cartesian (double x, double y){
		cX = x;
		cY = y;
	}
	
	public void setX (double x){
		cX = x;
	}
	
	public double getX(){
		return cX;
	}
	

	public void setY (double y){
		cY = y;
	}
	
	public double getY(){
		return cY;
	}
	
	
	public void set (double x, double y){
		cX = x;
		cY = y;
	}

}
