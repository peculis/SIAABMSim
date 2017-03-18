package SIAABMSimConnection;
import java.awt.Color;

import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.Artifact;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimTask.TEOW;
import SIAABMSimTask.Contract;


public class ManagementTask extends Task{

	private Contract thisContract;
	private Artifact thisDeliverable;
	private TEOW thisTEOW;

	public ManagementTask (Contract contract){
		//fromThisAgent = null;
		thisDeliverable = null;
		thisTEOW = null;
		toThatAgent = null;
		thisContract = contract;
		taskType = TaskType.Management;
		productiveEffort = 0;
		overheadEffort = 0;
	}
	
	public ManagementTask (Artifact deliverable){
		//fromThisAgent = null;
		thisContract = null;
		thisTEOW = null;
		thisDeliverable = deliverable;
		taskType = TaskType.Management;
	}
	
	public ManagementTask (TEOW teow){
		//fromThisAgent = null;
		thisContract = null;
		thisDeliverable = null;
		thisTEOW = teow;
		toThatAgent = thisTEOW.getDependency().toThatAgent;
		taskType = TaskType.Management;
	}
	
	public void increaseEffort(int n){
		overheadEffort = overheadEffort + n;
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
		//teowAssigned = true;
		//System.out.println(taskType + " Task " + thisTEOW.getName() + " assigned to " + actor.getRole() + " " + actor.getName());
	}

	public Artifact getArtifact(){
		return (Artifact) toThatAgent;
	}

	public void setToComplete(){
		state = TaskState.COMPLETE;
		//thisTEOW.setReworked(true);
	}
	
	public Contract getContract(){
		return thisContract;
	}

	public TEOW getTEOW(){
		return thisTEOW;
	}

	public void progress(){
		double motivation, experience, knowledge;
		Actor actor;
		if(state == TaskState.INPROGRESS){
			actor = (Actor) fromThisAgent;
			motivation = actor.getMotivation();
			experience = actor.getExperience();
			knowledge = actor.getKnowledge();
			//thisTEOW.progress(motivation, experience, knowledge);
		}
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
			//G.drawLink(Color.CYAN, (int) xScale * fromThisAgent.getX(), (int) xScale * toThatAgent.getX(), 
			//		                (int) yScale * fromThisAgent.getY(), (int) yScale * toThatAgent.getY());
		}
		if(state == TaskState.INPROGRESS){
			//G.drawLink(Color.RED, (int) xScale * fromThisAgent.getX(), (int) xScale * toThatAgent.getX(), 
			//		                (int) yScale * fromThisAgent.getY(), (int) yScale * toThatAgent.getY());
		}
	}

}
