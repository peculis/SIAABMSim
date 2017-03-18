package SIAABMSimAgent;


import SIAABMSimUtils.probability;
import SIAABMSimUtils.simSettings;
import uchicago.src.sim.gui.Drawable;
import java.awt.*;
import java.text.DecimalFormat;



public abstract class Agent implements Drawable{
	
	protected int myX;
	protected int myY;
	protected java.awt.Color myColor;
	protected probability pD;
	protected simSettings simS;
	protected DecimalFormat df;
	
	public int getX(){
		return myX;
	}
	
	public int getY(){
		return myY;
	}
	
	public Color getColor(){
		return myColor;
	}
	
	public void setX(int x){
		myX = x;
	}
	
	public void setY(int y){
		myY = y;
	}
	
	public void printShortProfile(){
		
	}
	
	public void step() {
		//System.out.println("step() Agent");
	}

}
