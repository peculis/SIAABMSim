package SIAABMSimAgent;

import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimAgent.ActorProfile.RoleType;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.ManageContract;
import SIAABMSimConnection.ManageDeliverable;
import SIAABMSimConnection.Task;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimSocial.Organization;
import SIAABMSimTask.Contract;

public class GhostSeniorManagerActor extends SeniorManagerActor{
	
GhostManagerActor ghostManager;
	
	public GhostSeniorManagerActor(ActorProfile profile, Organization organization, int x, int y){
		
		super(profile, organization,x, y);
		
	}
	
	protected void assignDeliverablesToManagers(){
		Actor actor;
		Contract contract;
		ManagerActor manager;
		ManageDeliverable task;
		ArrayList<Artifact> deliverableList;
		Artifact deliverable;
		int m;
		m = 0;
		System.out.println("SeniorManager assignDeliverablesToManagers");
		for(int c=0; c<myContracts.size(); c++){
			System.out.println("SeniorManager found Contract");
			contract = myContracts.get(c);
			deliverableList = contract.getDeliverables();
			for(int d=0; d<deliverableList.size(); d++){
				deliverable = deliverableList.get(d);
				System.out.println("SeniorManager " + myProfile.name() + " contract = "+ contract.TaskName() + 
						           " wll assign Deliverable = " + deliverable.getName() + " to Manager " + ghostManager.getName());
				task = new ManageDeliverable(deliverable);
				ghostManager.assignTask(task);
				m = m + 1;
				if(m == nManagers){
					m = 0;
				}
			}
		}
	}
	
	protected void findManagers(){
		
	}
	
	public void assignManager(GhostManagerActor manager){	
		ghostManager = manager;
		if(myContracts.size() == 0){
			getContracts();
		}
		assignDeliverablesToManagers();
	}
	
	protected void getContracts(){
		ManageContract task;
		Contract contract;
		if(myContracts.size() == 0){
			myContracts.addAll(myProfile.organization().getContractList());
			nContracts = myContracts.size();
			for(int c=0; c<nContracts; c++){
				System.out.println("SeniorManager get Contract");
				contract = myContracts.get(c);
				task = new ManageContract(contract);
				this.assignTask(task); //Assign task ManageContract to itself (SeniorManager);
			}
		}
	}
	
	
	public void draw(SimGraphics G){
		myColor = Color.BLACK;
	}
	
	public void step() {
		//double b, m;
		//Contract contract;
		//System.out.println("****** GhostSeniorManager step()");
		Task task;
		TaskType taskType;
		
		for(int t=0; t<myTasks.size(); t++){
			task = myTasks.get(t);
			taskType = task.taskType();
			task.progress();
			//System.out.println("SeniorManager progress task");
			//TBD
		}
	}

}
