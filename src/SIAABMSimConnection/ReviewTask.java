package SIAABMSimConnection;

import java.awt.Color;

import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.Artifact;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimTask.Contract;
import SIAABMSimTask.TEOW;

public class ReviewTask extends Task{
	
	
	public ReviewTask (TEOW teow){
		//fromThisAgent = null;
		thisTEOW = teow;
		toThatAgent = thisTEOW.getDependency().toThatAgent;
		deliverable = (Artifact) toThatAgent;
		taskType = TaskType.Review;
		productiveEffort = 0;
		overheadEffort = 0;
	}
	
	public Contract getContract(){
		return deliverable.getContract();
	}
	
	public void increaseProductiveEffort(int n){
		productiveEffort = productiveEffort + n;
		deliverable.increaseProductiveEffort(n);
		//System.out.println("RV ###");
	}
	
	public void increaseOverheadEffort(int n){
		overheadEffort = overheadEffort + n;
		//deliverable.increaseOverheadEffort(n);
		//System.out.println("RV ---");
	}

	
	public double requiredKnowledge(){
		if(thisTEOW != null){
			return thisTEOW.requiredKnowledge();
		}
		else{
			return 0.0;
		}
	}
	
	public double availableKnowledge(Actor actor){
		if(thisTEOW != null){
			return thisTEOW.availableKnowledge(actor);
		}
		else{
			return 0.0;
		}
	}
	
	public void assign(Actor actor){
		fromThisAgent = actor;
		state = TaskState.ASSIGNED;
		thisTEOW.sendForReview();
		//thisTEOW.setReviewed(false);
		//thisTEOW.setSentForRework(false);
		//thisTEOW.setReworked(false);
		//System.out.println(taskType + " Task " + thisTEOW.getName() + " assigned to " + actor.getRole() + " " + actor.getName());
	}
	
	public Artifact getArtifact(){
		return (Artifact) toThatAgent;
	}
	
	public void isComplete(){
		state = TaskState.COMPLETE;
	}
	
	public void setState(TaskState thisState){
		state = thisState;
		//thisTEOW.setState(convertState(thisState));
	}
	
	//Set and Get EVM parameters

	public void setPV(int pv){
		thisTEOW.setPV(pv);
	}

	public int getPV(){
		return thisTEOW.getPV();
	}


	public void setEV(int ev){
		thisTEOW.setEV(ev);
	}

	public int getEV(){
		return thisTEOW.getEV();
	}

	public double getPercentageComplete(){
		return thisTEOW.getPercentageComplete();
	}

	public void setETC(int etc){
		thisTEOW.setETC(etc);
	}

	public int getETC(){
		return thisTEOW.getETC();
	}

	public void setBAC(int bac){
		thisTEOW.setBAC(bac);
	}

	public int getBAC(){
		return thisTEOW.getBAC();
	}

	public int getEBAC(){
		return thisTEOW.getEBAC();
	}

	public int getACWP(){
		return thisTEOW.getACWP();
	}

	public double getACWPBACRatio(){
		return thisTEOW.getACWPBACRatio();
	}

	public double getSV(){
		return thisTEOW.getSV();
	}

	public double getSPI(){
		return thisTEOW.getSPI();
	}

	public double getError(){
		return thisTEOW.getError();
	}
	
	public double getRevealedError(){
		return thisTEOW.getRevealedError();
	}

	public void estimate(Actor actor){
		thisTEOW.estimateReview(actor);
	}
	
	public void estimate(){
		thisTEOW.estimateReview((Actor) fromThisAgent);
	}

	public void estimateETC(){
		thisTEOW.estimateETC((Actor) fromThisAgent);
	}


	public void progress(){
		thisTEOW.progress((Actor) fromThisAgent);
	}
	
	public void wasteEffort(){
		thisTEOW.wasteEffort();
	}
	
public void draw(SimGraphics G){
		
		int h, w;
		float xScale, yScale;
		h = G.getDisplayHeight();
		w = G.getDisplayWidth();
		//This scale factor of 100 corresponds to the Display size.
		xScale = w/xSize;
		yScale = h/ySize;
		if(state == TaskState.ASSIGNED){
			G.drawLink(Color.CYAN, (int) xScale * fromThisAgent.getX(), (int) xScale * toThatAgent.getX(), 
					                (int) yScale * fromThisAgent.getY(), (int) yScale * toThatAgent.getY());
		}
		if(state == TaskState.INPROGRESS){
			G.drawLink(Color.YELLOW, (int) xScale * fromThisAgent.getX(), (int) xScale * toThatAgent.getX(), 
					                (int) yScale * fromThisAgent.getY(), (int) yScale * toThatAgent.getY());
		}
	}

}
