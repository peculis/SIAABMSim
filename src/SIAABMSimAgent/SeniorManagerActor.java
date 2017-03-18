package SIAABMSimAgent;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;

import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.util.Random;
import java.util.ArrayList;

import SIAABMSimSocial.Organization;
import SIAABMSimAgent.ActorProfile.RoleType;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.Task;
import SIAABMSimConnection.ManagementTask;
import SIAABMSimConnection.ManageContract;
import SIAABMSimConnection.ManageDeliverable;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimTask.Contract;
import SIAABMSimTask.TEOW;
import SIAABMSimUtils.polarBehaviour;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.polarBehaviour.BehaviourStyle;

import java.text.*;

public class SeniorManagerActor extends Actor{
	
	protected ArrayList<Contract> myContracts;
	protected int nContracts;
	private Contract contractInProgress;
	private ArrayList<ManagerActor> myManagerList;
	protected int nManagers;
	private ArrayList<TEOW> myTEOWList;
	private int nTEOWs;

	protected int thisContract;

	private int count;

	private int applicationIncrementDelay = 0;

	private boolean started;

	private int lastInteraction;

	public SeniorManagerActor(ActorProfile profile, Organization organization, int x, int y){
		pD = probability.getInstance();
		simS = simS.getInstance();
		df = new DecimalFormat("#.####");
		myProfile = profile;
		myX = x;
		myY = y;
		myProfile.setOrganization(organization);
		pD = pD.getInstance();
		myContracts = null;
		nContracts = 0;
		thisContract = 0;
		contractInProgress = null;
		fromInteractions = new ArrayList<Interaction>();
		toInteractions = new ArrayList<Interaction>();
		myTasks = new ArrayList<Task>();
		myContracts = new ArrayList<Contract>();
		nContracts = 0;
		myManagerList = new ArrayList<ManagerActor>();
		nManagers = 0;
		myTEOWList = new ArrayList<TEOW>();
		nTEOWs = 0;
		count = 0;
		started = false;
		lastInteraction = 0;
	}
	
	public void iterativeFeedback(Contract contract){
		System.out.println(myRole() + " " + getName() + " received Iterative FeedBack on " + contract.TaskName());
	}

	private boolean inMyTeam(Actor actor){
		for(int a=0; a<myManagerList.size(); a++){
			if(actor == myManagerList.get(a)){
				return true;
			}
		}
		return false;
	}

	protected void processInteractions(){
		Interaction interaction;
		Actor actor;
		ManagerActor manager;
		BehaviourStyle myBehaviourStyle;
		double q, wp;
		int n, s;
		//System.out.println("TeamLeader " + myProfile.name() + " processInteractions");
		s = toInteractions.size();
		myBehaviourStyle = myBehaviourStyle();
		for(int i=1; i<s; i++){
			n = lastInteraction + i;
			if(n >= s){
				n = 0;
			}
			interaction = toInteractions.get(n);
			actor = interaction.getToActor();
			if(inMyTeam(actor)){
				if(actor.myRole() == RoleType.Manager){
					//System.out.println("Manager " + myProfile.name() + " found TeamMember to interact");
					manager = (ManagerActor) actor;
					if(manager.isActive()){
						wp = manager.getCostPerformance();
						if(wp >0){
							//System.out.println("SeniorManager " + myProfile.name() + " Department CostPerformance = " + df.format(wp));
							if(this.getChanceOfEvent("Start")){
								if( wp > 1.0){
									if(this.getChanceOfEvent("OfferHelp")){
										//System.out.println("!!!!!! SeniorManager " + myProfile.name() + " OfferHelp");
										interaction.request("OfferHelp");
									}
									else{
										if(this.getChanceOfEvent("Reprimand")){
											//System.out.println("----- SeniorManager " + myProfile.name() + " Reprimand");
											interaction.request("Reprimand");
										}
									}
								}
								if(wp < 1.0){
									if(this.getChanceOfEvent("Praise")){
										//System.out.println("++++ SeniorManager " + myProfile.name() + " Praise");
										interaction.request("Praise");
									}
								}
							}
						}
					}
				}
			}
		}
	}

	protected void findManagers(){
		Actor actor;
		for(int i=0; i<toInteractions.size(); i++){
			actor = (Actor) toInteractions.get(i).getToAgent();
			if(actor.myRole() == RoleType.Manager){
				myManagerList.add((ManagerActor)actor);
			}
		}
		nManagers = myManagerList.size();
	}

	protected boolean emptyContract(){
		Contract contract;
		for(int c=0; c<nContracts; c++){
			contract = myContracts.get(c);
			if(contract.getTEOW().size() == 0){
				return true;
			}
		}
		return false;
	}

	//11 Aug 2010
	//get all contracts assigned to the Organization and create and assign ManageContract task to itself (SeniorManager).
	protected void getContracts(){
		ManageContract task;
		Contract contract;
		if(myContracts.size() == 0){
			myContracts.addAll(myProfile.organization().getContractList());
			nContracts = myContracts.size();
			for(int c=0; c<nContracts; c++){
				contract = myContracts.get(c);
				task = new ManageContract(contract);
				this.assignTask(task); //Assign task ManageContract to itself (SeniorManager);
			}
			if(myManagerList.size() == 0){
				findManagers();
			}
			assignDeliverablesToManagers();
		}
	}

	//11 Aug 2010
	//assignContractToManagers will no longer be used.
	private void XassignContractToManagers(){
		Actor actor;
		Contract contract;
		ManagerActor manager;
		ManagementTask task;
		int m;
		m = 0;
		for(int c=0; c<nContracts; c++){
			contract = myContracts.get(c);
			manager = myManagerList.get(m);
			if(! manager.hasAssignedContract()){
				//System.out.println("SeniorManager " + myProfile.name() + " will assign Contract "+ contract.TaskName() + " to Manager");
				task = new ManagementTask(contract);
				manager.assignTask(task);
				m = m + 1;
				if(m == nManagers){
					m = 0;
				}
			}
		}
	}

	//11 Aug 2010
	//Assign Deliverable Artifacts to Managers
	protected void assignDeliverablesToManagers(){
		Actor actor;
		Contract contract;
		ManagerActor manager;
		ManageDeliverable task;
		ArrayList<Artifact> deliverableList;
		Artifact deliverable;
		int m;
		m = 0;
		for(int c=0; c<myContracts.size(); c++){
			contract = myContracts.get(c);
			deliverableList = contract.getDeliverables();
			for(int d=0; d<deliverableList.size(); d++){
				deliverable = deliverableList.get(d);
				manager = myManagerList.get(m);
				//System.out.println("SeniorManager " + myProfile.name() + " contract = "+ contract.TaskName() + 
				//		" wll assign Deliverable = " + deliverable.getName() + " to Manager " + manager.getName());
				task = new ManageDeliverable(deliverable);
				manager.assignTask(task);
				m = m + 1;
				if(m == nManagers){
					m = 0;
				}
			}
		}
	}


	public void draw(SimGraphics G){
		int r, g, b;
		/*
		PointerInfo a = MouseInfo.getPointerInfo();
		Point p  = a.getLocation();
		int x = (int)p.getX();
		int y = (int)p.getY();

		System.out.println("Mouse X = " + x + " Mouse Y = " + y);

		if ((x == myX) && (y == myY)) {
			System.out.println("************************* MOUSE POINTER IS ON Actor SeniorManager " + myProfile.name());
		}
		 */
		r = (int) (myProfile.aggressive() * 255.0);
		g = (int) (myProfile.passive() * 255.0);
		b = (int) (myProfile.constructive() * 255.0);
		myColor = new Color (r,g,b);
		G.drawHollowFastRect(Color.RED);
		G.drawFastCircle(myColor);
	}
	
	public void updateEffort(){
		Contract myBossContract = null;;
		Actor boss = null;
		int ballanceEffort;

		productiveEffortForThisStep = 0;
		overheadEffortForThisStep = 0;
		//spentEffortForThisStep = 0;
		availableEffortForThisStep = 12;
		myEnergy = myEnergy + 0;
		myProfile.distributeEffort();
		workEffort = 8;
		overtimeEffort = 0;
		learnEffort = 0;
		askHelpEffort = 0;
		helpEffort = 0;
		totalEffort = 0;
		extraEffort = 0;


		if(taskInProgress != null){
			if(taskInProgress.canActorDoIt()){ 
				//The actor has a task assigned and knows how to do it.
				//No learning or ask for help is needed.
				learnEffort = 0;
				askHelpEffort = 0;
				if(isAgressive()){
					totalEffort = pD.getIntUniform(9, 12);
					if(totalEffort < 12){
						helpEffort = pD.getIntUniform(0, 12 - totalEffort);
					}
					else{
						helpEffort = 0;
					}
				}
				if(isConstructive()){
					totalEffort = pD.getIntUniform(6, 10);
					if(totalEffort < 10){
						helpEffort = pD.getIntUniform(1, 10 - totalEffort);
					}
					else{
						helpEffort = 0;
					}
				}
				if(isPassive()){
					totalEffort = pD.getIntUniform(6, 8);
					if(totalEffort < 8){
						helpEffort = pD.getIntUniform(0, 8 - totalEffort);
					}
					else{
						helpEffort = 0;
					};
				}
			}
			else{
				//The actor has a task assigned and does not know how to do it.
				//Learning or ask for help is needed.
				if(isAgressive()){
					totalEffort = pD.getIntUniform(9, 12);
					learnEffort = pD.getIntUniform(4, 10);
					askHelpEffort = pD.getIntUniform(0, 1);
					helpEffort = 0;
				}
				if(isConstructive()){
					totalEffort = pD.getIntUniform(8, 10);
					learnEffort = pD.getIntUniform(2, 6);
					askHelpEffort = pD.getIntUniform(1, 2);
					helpEffort = pD.getIntUniform(1, 3);
				}
				if(isPassive()){
					totalEffort = pD.getIntUniform(6, 8);
					learnEffort = pD.getIntUniform(0, 1);
					askHelpEffort = pD.getIntUniform(0, 1);
					helpEffort = pD.getIntUniform(0, 1);
				}
			}
			workEffort = totalEffort - (helpEffort + learnEffort + askHelpEffort);
			overtimeEffort = totalEffort - 8;
			if(overtimeEffort < 0){
				overtimeEffort = 0;
			}
			if(workEffort < 0){
				workEffort = 0;
			}

			if(totalEffort > 0){
				//printDistributedEffort();
			}
		}
		else{
			//The actor does not have tasks assigned, i.e. no productive work.
			workEffort = 0;
			learnEffort = 0;
			askHelpEffort = 0;
			if(isAgressive()){
				totalEffort = pD.getIntUniform(7, 8);
				helpEffort = pD.getIntUniform(0, totalEffort);;
			}
			if(isConstructive()){
				totalEffort = pD.getIntUniform(6, 10);
				helpEffort = pD.getIntUniform(6, totalEffort);
			}
			if(isPassive()){
				totalEffort = pD.getIntUniform(4, 8);
				helpEffort = pD.getIntUniform(0, totalEffort);;
			}
		}
		//Calculates how much extra work the actor has accumulated.
		if(totalEffort > 8){
			extraEffort = extraEffort + (totalEffort - 8);
		}
		else{
			extraEffort = extraEffort - (8 - totalEffort)/2;
			if(extraEffort < 0){
				extraEffort = 0;
			}
		}
		if(totalEffort > 0){
			//printDistributedEffort();
		}
	}

	//30 May 2010
	//This is the original SeniorManager step() that used to work before.
	//SeniorManager simply assign Contracts or TEOW to its Managers. Managers are suppose to allocate tasks to Team Leaders
	public void step() {
		Task task;
		int effort;
		effort = getWorkEffort() + getOvertimeEffort();
		//TaskType taskType;

		//getContracts will be executed only once;
		//getContracts will create ManageContract tasks and assign them to the SeniorManager
		//getContracts will also allocated Deliverables to Managers.

		if(myContracts.size() == 0){
			getContracts();
		}

		for(int t=0; t<myTasks.size(); t++){
			task = myTasks.get(t);
			//taskType = task.taskType();
			task.progress();
		}
		processInteractions();
		increaseOverheadEffort(effort);
		//increaseProductiveEffort(getAvailableEffortForThisStep());
	}

}
