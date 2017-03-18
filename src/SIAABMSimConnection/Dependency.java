package SIAABMSimConnection;

import SIAABMSimAgent.Agent;
import SIAABMSimAgent.Artifact;
import SIAABMSimUtils.probability;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.awt.Color;
import uchicago.src.sim.gui.SimGraphics;


public class Dependency extends Connection{
	
	private double thisCoefficient;
	private int thisRow;
	private int thisColumn;
	private double Kin;
	private double Kout;
	
	private probability p;
	private DecimalFormat df;
	
	public Dependency (Artifact toArtifact, Artifact fromArtifact, double coefficient, int row, int column){
		
		Artifact a;
		double kMean, kSD;
		
		fromThisAgent = fromArtifact;
		toThatAgent = toArtifact;
		thisCoefficient = coefficient;
		thisRow = row;
		thisColumn = column;
		p = probability.getInstance();
		df = new DecimalFormat("#.##");
		
		a = (Artifact) fromThisAgent;
		kMean = a.getTransformation().getKMean();
		kSD = a.getTransformation().getKSD();
		Kin = p.getNormalDistributionSample(kMean, kSD);
		//Kin = 1.0;
		
		//System.out.println("******* Kin kMean= " + df.format(kMean) + " KinkSD = " + df.format(kSD));
		
		a = (Artifact) toThatAgent;
		kMean = a.getTransformation().getKMean();
		kSD = a.getTransformation().getKSD();
		Kout = p.getNormalDistributionSample(kMean, kSD);
		//Kout = 1.0;
		
		//System.out.println("******* Kout kMean= " + df.format(kMean) + " KoutkSD = " + df.format(kSD));
		
		//System.out.println("******* Kin = " + df.format(Kin) + " Kout = " + df.format(Kout));
	}
	
	public int getRow(){
		return thisRow;
	}
	
	public int getColumn(){
		return thisColumn;
	}
	
	public void draw(SimGraphics G){
		
		int h, w;
		float xScale, yScale;
		h = G.getDisplayHeight();
		w = G.getDisplayWidth();
		//This scale factor of 100 corresponds to the Display size.
		xScale = w/xSize;
		yScale = h/ySize;
		
		G.drawLink(Color.WHITE, (int) xScale * fromThisAgent.getX(), (int) xScale * toThatAgent.getX(), 
				                (int) yScale * fromThisAgent.getY(), (int) yScale * toThatAgent.getY());
		
	}
	
	public Artifact getFromArtifact(){
		return (Artifact) fromThisAgent;
	}
	
	public Artifact getToArtifact(){
		return (Artifact) toThatAgent;
	}
	
	public double getDependencyCoefficient(){
		return thisCoefficient;
	}
	
	public boolean isDependencyReady(){
		Artifact a;
		a = (Artifact) fromThisAgent;
		return a.isReady();
	}
	
	public boolean isDependencyComplete(){
		Artifact a;
		a = (Artifact) toThatAgent;
		return a.isComplete();
	}
	
	public double getKin(){
		return Kin;
	}
	
	public double getKout(){
		return Kout;
	}

}
