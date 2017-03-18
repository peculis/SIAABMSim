package SIAABMSimConnection;

import java.awt.Color;

import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.Artifact;
import SIAABMSimConnection.Task.TaskState;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimTask.TEOW;
import SIAABMSimTask.Contract;

import java.util.ArrayList;

public class ManageDeliverable extends Task {
	private Artifact myDeliverable;
	private ArrayList<Task> TaskList;
	private Task lastTask;

	public ManageDeliverable (Artifact del){
		taskType = TaskType.ManageDeliverable;
		myDeliverable = del;
		deliverable = myDeliverable;
		TaskList = new ArrayList<Task>();
		lastTask = null;
	}
	
	public void increaseProductiveEffort(int n){
		productiveEffort = productiveEffort + n;
		deliverable.increaseProductiveEffort(n);
		//System.out.println("MngDel +++");
	}
	
	public void increaseOverheadEffort(int n){
		overheadEffort = overheadEffort + n;
		deliverable.increaseOverheadEffort(n);
		//System.out.println("MngDel ---");
	}
	
	public int getPlannedEffort(){
		int plannedEffort;
		plannedEffort = 0;
		for(int t=0; t<TaskList.size(); t++){
			plannedEffort = plannedEffort + TaskList.get(t).getPlannedEffort();
		}
		return plannedEffort;
	}

	public int getActualEffort(){
		int actualEffort;
		actualEffort = 0;
		for(int t=0; t<TaskList.size(); t++){
			actualEffort = actualEffort + TaskList.get(t).getActualEffort();
		}
		return actualEffort;
	}
	
	public double getCostPerformance(){
		int nTasks;
		double cp;
		nTasks = 0;
		cp = 0.0;
		for(int t=0; t<TaskList.size(); t++){
			cp = cp + TaskList.get(t).getCostPerformance();
			nTasks = nTasks + 1;
		}
		if(nTasks > 0){
			return cp / nTasks;
		}
		return -1.0;
	}

	public void assign(Actor actor){
		fromThisAgent = actor;
		state = TaskState.ASSIGNED;
		//teowAssigned = true;
		//System.out.println(taskType + " Task " + thisTEOW.getName() + " assigned to " + actor.getRole() + " " + actor.getName());
	}

	public ArrayList<Task> getTasks(){
		return TaskList;
	}

	public Artifact getDeliverable(){
		return myDeliverable;
	}

	public void addTask(Task task){
		TaskList.add(task);
		lastTask = task;
	}
	
	public Task getLastTask(){
		return lastTask;
	}

	public boolean developmentIsComplete(){
		Task task;
		TEOW teow;
		boolean devIsComplete;
		devIsComplete = false;
		for(int t=0; t<TaskList.size(); t++){
			task = TaskList.get(t);
			if(task.taskType() == TaskType.DevelopDeliverable){
				//if((task.state() == TaskState.COMPLETE) || (task.state() == TaskState.READY)){
				if((task.state() == TaskState.COMPLETE)){
					devIsComplete = true;
				}
				else{
					t = TaskList.size();
					devIsComplete = false;
				}
				/*if(task.state() != TaskState.COMPLETE){
					t = TaskList.size();
					devIsComplete = false;
				}
				else{
					devIsComplete = true;
				}
				 */
			}
		}
		return devIsComplete;
	}

	public boolean allAssignedTasksAreComplete(){
		boolean allAssignedTasksAreComplete;
		Task task;
		TEOW teow;
		allAssignedTasksAreComplete = false;
		for(int t=0; t<TaskList.size(); t++){
			task = TaskList.get(t);
			if(task.state() != TaskState.COMPLETE){
				t = TaskList.size();
				allAssignedTasksAreComplete = false;
			}
			else{
				allAssignedTasksAreComplete = true;
				//teow = task.getTEOW();
				//System.out.println(teow.getPhase());
			}
		}
		return allAssignedTasksAreComplete;
	}


	public boolean allAssignedTasksAreReady(){
		boolean allAssignedTasksAreReady;
		Task task;
		allAssignedTasksAreReady = false;
		for(int t=0; t<TaskList.size(); t++){
			task = TaskList.get(t);
			if(task.state() != TaskState.READY){
				t = TaskList.size();
				allAssignedTasksAreReady = false;
			}
			else{
				allAssignedTasksAreReady = true;
			}
		}
		return allAssignedTasksAreReady;
	}
	//public ArrayList<TEOW> getTEOWList(){
	//	return myTEOWs;
	//}

	public void isComplete(){
		state = TaskState.COMPLETE;
	}

	public void isInProgress(){
		state = TaskState.INPROGRESS;
	}

	public int getACWP(){
		return 0;
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
		//ManageDeliverable Task will not be displayed.
	}
}