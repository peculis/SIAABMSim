package SIAABMSimConnection;

import java.awt.Color;

import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.Artifact;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimTask.TEOW;
//import SIAABMSimTask.TEOW.TEOWState;
import SIAABMSimTask.Contract;

import java.util.ArrayList;

public class ManageContract extends Task {
	private Contract thisContract;
	private ArrayList<Artifact> deliverablesList;
	private ArrayList<TEOW> TEOWsList;

	public ManageContract (Contract contract){
		toThatAgent = null;
		thisContract = contract;
		taskType = TaskType.ManageContract;
		deliverablesList = thisContract.getDeliverables();
		TEOWsList = thisContract.getTEOW();
	}

	public void assign(Actor actor){
		fromThisAgent = actor;
		state = TaskState.ASSIGNED;
		//teowAssigned = true;
		//System.out.println(taskType + " Task " + thisTEOW.getName() + " assigned to " + actor.getRole() + " " + actor.getName());
	}

	public Contract getContract(){
		return thisContract;
	}
	
	public ArrayList<Artifact> getDeliverables(){
		return deliverablesList;
	}
	
	public ArrayList<TEOW> getTEOWs(){
		return TEOWsList;
	}

	public void progress(){
		//TBD
	}

	public void draw(SimGraphics G){

		int h, w;
		float xScale, yScale;
		h = G.getDisplayHeight();
		w = G.getDisplayWidth();
		//This scale factor of 100 corresponds to the Display size.
		xScale = w/xSize;
		yScale = h/ySize;
		//ManageContract Task will not be displayed.
	}
}
