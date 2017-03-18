package SIAABMSimUtils;

public class coordinates {
	
	private polar p;
	private cartesian c;
	
	public coordinates(){
		p = new polar();
		c = new cartesian();
	}
	
	private void convertToPolar(){
		double x, y, r, teta;
		x = c.getX();
		y = c.getY();
		r = Math.sqrt(x*x + y*y);
		teta = Math.atan(y/x);
		p.set(r, teta);
	}
	
	private void convertToCartesian(){
		double r, teta;
		r = p.getR();
		teta = p.getTeta();
		c.set(r * Math.sin(teta), r * Math.cos(teta));
	}
	
	public void setCartesian(double x, double y){
		c.set(x, y);
		convertToPolar();
	}
	
	public cartesian getCartesian(){
		return c;
	}
	
	public void setPolar(double r, double teta){
		p.set(r, teta);
		convertToCartesian();
	}
	
	public polar getPolar(){
		return p;
	}
	
	public double getX(){
		return c.getX();
	}
	
	public double getY(){
		return c.getY();
	}
	
	public double getR(){
		return p.getR();
	}
	
	public double getTeta(){
		return p.getTeta();
	}

}
