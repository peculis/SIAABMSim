package SIAABMSimConnection;

import java.awt.Color;

import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.Artifact;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimTask.TEOW;
import SIAABMSimTask.Contract;

import java.util.ArrayList;

public class ReviewDeliverable extends Task {
	private Artifact myDeliverable;
	private ArrayList<Task> TaskList;
	private Task lastTask;

	public ReviewDeliverable (Artifact del){
		taskType = TaskType.ReviewDeliverable;
		myDeliverable = del;
		deliverable = myDeliverable;
		TaskList = new ArrayList<Task>();
		lastTask = null;
		productiveEffort = 0;
		overheadEffort = 0;
	}
	
	public void increaseProductiveEffort(int n){
		productiveEffort = productiveEffort + n;
		deliverable.increaseProductiveEffort(n);
		//System.out.println("RvDel ###");
	}
	
	public void increaseOverheadEffort(int n){
		overheadEffort = overheadEffort + n;
		deliverable.increaseOverheadEffort(n);
		//System.out.println("RvDel ---");
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
		if(getPlannedEffort() > 0){
			return (double) ((double) getActualEffort() / (double) getPlannedEffort() );
		}
		return -1.0;
	}
	
	public void increaseEffort(int n){
		overheadEffort = overheadEffort + n;
	}

	public void assign(Actor actor){
		fromThisAgent = actor;
		state = TaskState.ASSIGNED;
		//System.out.println(taskType + " Task " + thisTEOW.getName() + " assigned to " + actor.getRole() + " " + actor.getName());
	}
	
	public void setTaskList(ArrayList<Task> tasks){
		TaskList = tasks;
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
	
	public double getError(){
		double error;
		int nrvt;
		Task task;
		ReviewTask reviewTask;
		error = 0.0;
		nrvt = 0;
		for(int t=0; t<TaskList.size();t++){
			task = TaskList.get(t);
			if(task.taskType == TaskType.Review){
				reviewTask = (ReviewTask) task;
				error = error + reviewTask.getError();
				nrvt = nrvt + 1;
			}
		}
		error = error / nrvt;
		return error;
	}
	
	public double getRevealedError(){
		double error;
		int nrvt;
		Task task;
		ReviewTask reviewTask;
		error = 0.0;
		nrvt = 0;
		for(int t=0; t<TaskList.size();t++){
			task = TaskList.get(t);
			if(task.taskType == TaskType.Review){
				reviewTask = (ReviewTask) task;
				error = error + reviewTask.getRevealedError();
				nrvt = nrvt + 1;
			}
		}
		error = error / nrvt;
		return error;
	}
	
	public boolean allAssignedTasksAreComplete(){
		boolean allAssignedTasksAreComplete;
		Task task;
		allAssignedTasksAreComplete = false;
		for(int t=0; t<TaskList.size(); t++){
			task = TaskList.get(t);
			if(task.state() == TaskState.COMPLETE){
				allAssignedTasksAreComplete = true;
			}
			else{
				allAssignedTasksAreComplete = false;
				t = TaskList.size();
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
			if(task.state() == TaskState.READY){
				allAssignedTasksAreReady = true;
			}
			else{
				allAssignedTasksAreReady = false;
				t = TaskList.size();
			}
		}
		return allAssignedTasksAreReady;
	}

	
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
		//DevelopDeliverable Task will not be displayed.
	}
}

