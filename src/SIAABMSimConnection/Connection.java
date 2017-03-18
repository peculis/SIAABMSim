package SIAABMSimConnection;

import SIAABMSimAgent.Agent;
import SIAABMSimUtils.probability;
import uchicago.src.sim.gui.Drawable;
import java.awt.*;

public abstract class Connection implements Drawable{
	
	protected Agent fromThisAgent;
	protected Agent toThatAgent;
	protected java.awt.Color myColor;
	protected int xSize = 100;
	protected int ySize = 100;
	
	public Agent getFromAgent(){
		return fromThisAgent;
	}
	
	public Agent getToAgent(){
		return toThatAgent;
	}
	
	public int getX(){
		return fromThisAgent.getX();
	}
	
	public int getY(){
		return fromThisAgent.getY();
	}
	
	public Color getColor(){
		return myColor;
	}
	
	public void setSpaceSize(int x, int y){
		xSize = x;
		ySize = y;
	}
	
}
