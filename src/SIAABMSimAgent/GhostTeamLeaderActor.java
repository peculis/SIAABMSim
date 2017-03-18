package SIAABMSimAgent;

import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimConnection.Dependency;
import SIAABMSimConnection.DevelopDeliverable;
import SIAABMSimConnection.EngineeringTask;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.ManageTEOW;
import SIAABMSimConnection.ReviewDeliverable;
import SIAABMSimConnection.ReviewTask;
import SIAABMSimConnection.ReworkDeliverable;
import SIAABMSimConnection.ReworkTask;
import SIAABMSimConnection.Task;
import SIAABMSimConnection.Task.TaskState;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimSocial.Organization;
import SIAABMSimTask.TEOW;
import SIAABMSimTask.TEOW.TEOWPhase;

public class GhostTeamLeaderActor extends TeamLeaderActor {
	
	public GhostTeamLeaderActor(ActorProfile profile, Organization organization, int x, int y){

		super(profile, organization,x, y);

	}
	
	public void assignManager(GhostManagerActor manager){	
		myManager = ((GhostManagerActor)manager);
	}
	
	public void assignTeamMember(TeamMemberActor teamMember){	
		myTeamMemberList.add(teamMember);
	}
	
	private void addTaskToTaskPool(Task task){
		myProfile.organization().addTaskToTaskPool(task);
	}
	
	protected void assignTaskToNextTeamMember(Task task){
		int TM;
		TaskType taskType;
		Actor actor;
		EngineeringTask engineeringTask;
		ReviewTask reviewTask;
		ReworkTask reworkTask;

		taskType = task.taskType();
		
		switch(taskType){
		case Engineering:{
			engineeringTask = (EngineeringTask) task;
			engineeringTask.getTEOW().estimate(this); // TL estimates the task
			engineeringTask.getTEOW().effortFactor(0.74 * simS.getEffortFactor());
			engineeringTask.setState(TaskState.NOTALLOCATED);
			addTaskToTaskPool(task);
			assignedTasks.add(task);
			//System.out.println("TeamLeader " + myProfile.name() + 
			//		" ASSIGNED " + task.taskType() + " task to " + actor.getRole() + " " + actor.getName());

			break;
		}
		case Review:{
			reviewTask = (ReviewTask) task;
			reviewTask.getTEOW().estimateReview(this); // TL estimates the task
			reviewTask.setState(TaskState.NOTALLOCATED);
			addTaskToTaskPool(task);
			assignedTasks.add(task);
			break;
		}
		case Rework:{
			reworkTask = (ReworkTask) task;
			reworkTask.getTEOW().estimateRework(this); // TL estimates the task
			reworkTask.getTEOW().effortFactor(0.74 * simS.getEffortFactor());
			reworkTask.setState(TaskState.NOTALLOCATED);
			//System.out.println("TL Asssign REWORK Task to TM = " + actor.getName());
			addTaskToTaskPool(task);
			assignedTasks.add(task);
			//System.out.println("TeamLeader " + myProfile.name() + 
			//		" ASSIGNED " + task.taskType() + " task to " + actor.getRole() + " " + actor.getName());
			break;
		}
		}
	}
	
	protected void processMyTasks(){
		Task task, taskTM;
		int listSize;
		Task tmTask;
		Artifact deliverable;
		ArrayList <Dependency> dependencies;
		ArrayList <Task> taskList;
		boolean allAssignedTasksAreComplete;
		//LeadershipTask thisTask;
		DevelopDeliverable ddTask;
		ReviewDeliverable rvTask;
		ReworkDeliverable rwTask;
		ManageTEOW thisTask;
		EngineeringTask engineeringTask;
		ReviewTask reviewTask;
		ReworkTask reworkTask;
		TEOW teow;
		TEOWPhase teowPhase;
		//System.out.println("GhostTL processMyTasks()");
		//System.out.println(this.getRole() + " " + this.getName() + " ProcessMyTasks()");
		//if(taskInProgress == null){
		for(int t=0; t<myTasks.size(); t++){
			task = myTasks.get(t);
			switch(task.taskType()){
			case DevelopDeliverable:{
				ddTask = (DevelopDeliverable) task;
				switch(ddTask.state()){
				case ASSIGNED:{
					//System.out.println("DVD TL " + this.getName() + " ProcessMyTasks() found ASSIGNED DevelopDeliverable task");
					deliverable = ddTask.getDeliverable();
					dependencies = deliverable.getDependencies();
					for(int d=0; d<dependencies.size(); d++){
						//System.out.println("DVD     TL ");
						teow = new TEOW(dependencies.get(d));
						engineeringTask = new EngineeringTask(teow);
						assignTaskToNextTeamMember(engineeringTask);
						ddTask.addTask(engineeringTask);
					}
					ddTask.isInProgress();
					taskInProgress = ddTask;
					break;
				}
				case INPROGRESS:{
					taskList = ddTask.getTasks();
					for(int tk=0; tk<taskList.size(); tk++){
						taskTM = taskList.get(tk);
						if(taskTM.state() == TaskState.READY){						
							switch(taskTM.taskType()){
							case Engineering:{
								taskTM.isComplete();
								//the following three lines of code are used when the TL Actor decides to do Review and then Rework
								//reviewTask = new ReviewTask(taskTM.getTEOW());
								//assignTaskToNextTeamMember(reviewTask);
								//ddTask.addTask(reviewTask);
								break;
							}
							case Review:{
								taskTM.isComplete();
								if(taskTM.getRevealedError() > 0.0){
									reworkTask = new ReworkTask(taskTM.getTEOW());
									assignTaskToNextTeamMember(reworkTask);
									ddTask.addTask(reworkTask);
								}
								break;
							}
							case Rework:{
								taskTM.isComplete();
								if(taskTM.getError() > 0.05){
									reviewTask = new ReviewTask(taskTM.getTEOW());
									assignTaskToNextTeamMember(reviewTask);
									ddTask.addTask(reviewTask);
								}
								break;
							}
							}
						}
					}
				}
				break;
				}
				deliverable = ddTask.getDeliverable();
				if(ddTask.allAssignedTasksAreComplete()){
					ddTask.isComplete();
					//mdTask.getDeliverable().baseline();
					//System.out.println("TL " + this.getName() + " all Development assigned tasks are COMPLETE for " + 
					//		deliverable.getName());
				}
				break;
			}
			case ReviewDeliverable:{
				rvTask = (ReviewDeliverable) task;
				switch(rvTask.state()){
				case ASSIGNED:{
					taskList = rvTask.getTasks();
					listSize = taskList.size();
					//System.out.println("RVD --- TL " + this.getName() + 
					//		" ProcessMyTasks() found ASSIGNED ReviewDeliverable task TaskList Size = " + listSize);
					for(int rvt=0; rvt<listSize; rvt++){
						taskTM = taskList.get(rvt);
						if((taskTM.taskType() == TaskType.Engineering) && taskTM.state() == TaskState.COMPLETE){
							//if((taskTM.taskType() == TaskType.Engineering)){
							//System.out.println("RVD --- TL ");
							if(pD.drawChanceFromUniform(simS.getReviewFactor())){
							teow = taskList.get(rvt).getTEOW();
							reviewTask = new ReviewTask(teow);
							assignTaskToNextTeamMember(reviewTask);
							rvTask.addTask(reviewTask);
							}
						}
					}
					rvTask.isInProgress();
					taskInProgress = rvTask;
					break;
				}
				case INPROGRESS:{
					taskList = rvTask.getTasks();
					for(int rvt=0; rvt<taskList.size(); rvt++){
						taskTM = taskList.get(rvt);
						if(taskTM.state() == TaskState.READY){
							taskTM.isComplete();	
						}
					}
					break;
				}
				}
				deliverable = rvTask.getDeliverable();
				if(rvTask.allAssignedTasksAreComplete()){
					rvTask.isComplete();
				}
				break;
			}
			case ReworkDeliverable:{
				rwTask = (ReworkDeliverable) task;
				switch(rwTask.state()){
				case ASSIGNED:{
					taskList = rwTask.getTasks();
					listSize = taskList.size();
					//System.out.println("RWD +++ TL " + this.getName() + 
					//		" ProcessMyTasks() found ASSIGNED ReworkDeliverable task TaskList Size = " + listSize);
					for(int rwt=0; rwt<listSize; rwt++){
						taskTM = taskList.get(rwt);
						//System.out.println("+++++++++");
						if((taskTM.taskType() == TaskType.Review) && taskTM.state() == TaskState.COMPLETE){
							//if(reviewTask.state() == TaskState.COMPLETE){
							//if((taskTM.taskType() == TaskType.Review)){
							//System.out.println("RWD +++ TL ");
							reviewTask = (ReviewTask) taskTM;
							//teow = taskList.get(rwt).getTEOW();
							if(reviewTask.getRevealedError() > 0.0){
								teow = reviewTask.getTEOW();
								reworkTask = new ReworkTask(teow);
								assignTaskToNextTeamMember(reworkTask);
								rwTask.addTask(reworkTask);
							}
						}
						else{
							//System.out.println("TaskType = " + taskTM.taskType() + " State = " + taskTM.state());
							
						}
					}
					rwTask.isInProgress();
					taskInProgress = rwTask;
					break;
				}
				case INPROGRESS:{
					taskList = rwTask.getTasks();
					for(int rwt=0; rwt<taskList.size(); rwt++){
						taskTM = taskList.get(rwt);
						if(taskTM.state() == TaskState.READY){
							taskTM.isComplete();	
						}
					}
					break;
				}
				}
				deliverable = rwTask.getDeliverable();
				if(rwTask.allAssignedTasksAreComplete()){
					rwTask.isComplete();
				}
				break;
			}
			}
		}

	}
	
	public void draw(SimGraphics G){
		myColor = Color.BLACK;
	}
	
	
	public void step() {	
		//System.out.println("Ghost TL step()");
		processMyTasks(); 	
	}

}
