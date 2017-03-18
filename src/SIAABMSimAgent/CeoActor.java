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
import SIAABMSimUtils.simSettings;
import SIAABMSimUtils.polarBehaviour.BehaviourStyle;

import java.text.*;

public class CeoActor extends Actor{

	protected ArrayList<Contract> myContracts;
	protected int nContracts;
	protected Contract contractInProgress;
	protected ArrayList<ManagerActor> myManagerList;
	protected int nManagers;
	protected ArrayList<TEOW> myTEOWList;
	protected ArrayList<ManageDeliverable> assignedTasksList;
	protected int nTEOWs;

	private int count;

	private int applicationIncrementDelay = 0;

	private boolean started;

	private int lastInteraction;

	public CeoActor(ActorProfile profile, Organization organization, int x, int y){
		pD = probability.getInstance();
		df = new DecimalFormat("#.####");
		myProfile = profile;
		myX = x;
		myY = y;
		myProfile.setOrganization(organization);
		pD = pD.getInstance();
		simS = simS.getInstance();
		myContracts = null;
		nContracts = 0;
		contractInProgress = null;
		fromInteractions = new ArrayList<Interaction>();
		toInteractions = new ArrayList<Interaction>();
		myTasks = new ArrayList<Task>();
		myContracts = new ArrayList<Contract>();
		nContracts = 0;
		myManagerList = new ArrayList<ManagerActor>();
		assignedTasksList = new ArrayList<ManageDeliverable>();
		nManagers = 0;
		myTEOWList = new ArrayList<TEOW>();
		nTEOWs = 0;
		count = 0;
		started = false;
		lastInteraction = 0;
		explicitReward = simS.getExplicitReward();
		implicitReward = simS.getImplicitReward();
		probabilityOfInteraction = simS.getInteractionFactor();
		cooperationFactor = simS.getCooperationFactor();
	}


	private void echoMessageToTeam(String message){
		Interaction interaction;
		Actor actor;
		ManagerActor manager;
		for(int i=0; i<toInteractions.size(); i++){
			interaction = toInteractions.get(i);
			actor = interaction.getToActor();
			if(inMyTeam(actor)){
				if(actor.myRole() == RoleType.Manager){
					manager = (ManagerActor) actor;
					manager.interaction(interaction, message);
				}
			}
		}
	}

	public void interaction(Interaction interaction, String message){
		processInteraction(interaction, message);
		//System.out.println(myRole() + " " + getName() + " received " + message + " from " + interaction.getFromActor().myRole());
		if(!message.equals("AskHelp")){
			if((interaction.getFromActor().myRole() == RoleType.CEO) || (interaction.getFromActor().myRole()== RoleType.SeniorManager)){
				echoMessageToTeam(message);
			}
		}
	}

	private void processInteraction(Interaction interaction, String message){
		Actor fromActor, toActor;
		boolean isMyBoss;
		fromActor = interaction.getFromActor();
		toActor = interaction.getToActor();
		isMyBoss = (fromActor == getBoss());
		fromActor.increaseOverheadEffort(2);
		toActor.increaseOverheadEffort(2);

		if(!(message.equals("Accepted") || message.equals("NotAccepted"))){
			//System.out.println(myRole() + " " + getName() + " received " + message + " from " + fromActor.myRole() + " " + fromActor.getName());
		}

		//OneWay
		if(message.equals("OfferBonus")){
			if(getChanceOfEvent("ExplicitReward")){
				myProfile.increaseMotivation();
			}
		}
		if(message.equals("Reprimand")){
			if(getChanceOfEvent("ImplicitReward")){
				myProfile.decreaseMotivation();
			}
		}
		if(message.equals("NoBonus")){
			if(getChanceOfEvent("ExplicitReward")){
				myProfile.decreaseMotivation();
			}
		}
		if(message.equals("Praise")){
			if(getChanceOfEvent("ImplicitReward")){
				myProfile.increaseMotivation();
			}
		}
		if(message.equals("Bonus")){
			if(getChanceOfEvent("ExplicitReward")){
				myProfile.increaseMotivation();
			}
		}
		//Requests
		if(message.equals("AskHelp")){
			if(getChanceOfEvent("Help")){
				fromActor.interaction(interaction, "Accepted");
				eGap = toActor.getExperience() - fromActor.getExperience();
				if(eGap > 0.0){
					fromActor.increaseExperience(cooperationFactor * toActor.getTeachingFactor(), eGap);
				}
				kGap = toActor.getKnowledge() - fromActor.getKnowledge();
				if(kGap > 0.0){
					fromActor.increaseKnowledge(cooperationFactor * toActor.getTeachingFactor(), kGap);
				}
			}
			else{
				fromActor.interaction(interaction, "NotAccepted");
			}
		}
		if(message.equals("OfferHelp")){
			if(getChanceOfEvent("AcceptHelp")){
				fromActor.interaction(interaction, "Accepted");
				eGap = fromActor.getExperience() - toActor.getExperience();
				if(eGap > 0.0){
					toActor.increaseExperience(cooperationFactor * fromActor.getTeachingFactor(), eGap);
				}
				kGap = fromActor.getKnowledge() - toActor.getKnowledge();
				if(kGap > 0.0){
					toActor.increaseKnowledge(cooperationFactor * fromActor.getTeachingFactor(), kGap);
				}
			}
			else{
				fromActor.interaction(interaction, "NotAccepted");
			}
		}
		//Responses
		if(message.equals("Accepted")){
			interaction.increaseQuality();
			myProfile.increaseMotivation();
			myProfile.increaseConstructive(1);
			if(isMyBoss){
				myProfile.increaseMotivation();
				myProfile.increaseConstructive(2);
			}
		}
		if(message.equals("NotAccepted")){
			interaction.decreaseQuality();
			myProfile.decreaseMotivation();
			myProfile.increasePassive(1);
			if(isMyBoss){
				myProfile.decreaseMotivation();
				myProfile.increasePassive(2);
			}
		}

		if(fromActor != this){
			updateBehaviour(message, fromActor);
		}

		if(message.equals("Accepted") || message.equals("NotAccepted")){
			if(toActor.myRole() == RoleType.Expert){
				//System.out.println(myRole() + " " + getName() + " received " + message + " from " + toActor.myRole() + " " + toActor.getName());
			}
		}
	}

	public void iterativeFeedback(Contract contract){
		ArrayList<Artifact> deliverableList;
		Artifact deliverable;
		ManageDeliverable task;
		ManagerActor manager;
		//System.out.println(getRole() + " " + getName() + " received Iterative FeedBack on " + contract.TaskName());
		deliverableList = contract.getDeliverables();
		for(int d=0; d<deliverableList.size(); d++){
			deliverable = deliverableList.get(d);
			for(int t=0; t<assignedTasksList.size(); t++){
				task = assignedTasksList.get(t);
				if(deliverable == task.getDeliverable()){
					manager = (ManagerActor) task.getActor();
					manager.iterativeFeedback(deliverable);
					t = assignedTasksList.size();
				}
			}
		}
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
		String message;
		double q, wp;
		int n, s;
		double delayReprimand;
		double delayPraise;
		Actor myCustomer;
		Contract firstContract;
		Organization org;
		double copySeniorBehaviour;

		//System.out.println("CEO " + myProfile.name() + " processInteractions");
		
		copySeniorBehaviour = simS.getCopySeniorBehaviour();
		
		if(pD.drawChanceFromUniform(copySeniorBehaviour)){
			if(myContracts.size() > 0){
				firstContract = myContracts.get(0);
				//System.out.println("1");
			}
			else{
				firstContract = null;
				//System.out.println("2");
			}

			if(firstContract != null){
				//System.out.println("3");
				org = firstContract.getCustomer();
				if(org != null){
					myCustomer = org.getCEO();
				}
				else{
					myCustomer = null;
				}
			}
			else{
				myCustomer = null;
				//System.out.println("4");
			}

			if(myCustomer != null){
				//System.out.println("5");
				if(myCustomer.isAgressive()){
					//if(this.isConstructive()){
					if(pD.drawChanceFromUniform(0.8)){
						this.setAgressive();
					}
					else{
						if(pD.drawChanceFromUniform(0.05)){
							//this.setPassive();
						}
					}
					//}
				}
				if(myCustomer.isConstructive()){
					if(pD.drawChanceFromUniform(0.6)){
						this.setConstructive();
					}
				}
				if(myCustomer.isPassive()){
					if(this.isConstructive()){
						if(pD.drawChanceFromUniform(0.05)){
							this.setPassive();
						}
					}
				}
			}
		}

		if(simS.getDelayReprimand() == 0){
			delayReprimand = 1.0;
		}
		else{
			if(simS.getDelayReprimand()  < 0){
				delayReprimand = 0.0;
			}
			else{
				delayReprimand = (double) pD.totalDuration() / (double) simS.getDelayReprimand();
				if(delayReprimand > 1.0){
					delayReprimand = 1.0;
				}
			}
		}

		if(simS.getDelayPraise() == 0){
			delayPraise = 1.0;
		}
		else{
			if(simS.getDelayReprimand() < 0){
				delayPraise = 0.0;
			}
			else{
				delayPraise = (double) pD.totalDuration() / (double) simS.getDelayPraise() ;
				if(delayPraise > 1.0){
					delayPraise = 1.0;
				}
			}
		}

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
						if(pD.drawChanceFromUniform(probabilityOfInteraction)){
							wp = manager.getCostPerformance();

							rewardModel();

							if(wp >0){
								//System.out.println("CEO " + myProfile.name() + " Department CostPerformance = " + df.format(wp));
								if(getChanceOfEvent("BAggressive")){
									if(pD.drawChanceFromUniform(0.01)){
										myProfile.increaseAggressive(1);
									}
								}
								else{
									if(getChanceOfEvent("BConstructive")){
										myProfile.increaseConstructive(1);
									}
								}
								if(this.getChanceOfEvent("Start")){
									if( wp > simS.getReprimandThreshold()){
										if(this.getChanceOfEvent("OfferHelp")){
											if(bonusReward){
												message = "OfferBonus";
												//interaction.request(message);
												//manager.interaction(interaction, message);
												//System.out.println("--BB Manager " + myProfile.name() + " OfferBonus");
											}
											else{
												message = "OfferHelp";
												//interaction.request(message);
												manager.interaction(interaction, message);
												//System.out.println("--HH Manager " + myProfile.name() + " OfferHelp");
											}
										}
										else{
											//System.out.println("CEO DelayReprimand = " + simS.getDelayReprimand());
											if(pD.drawChanceFromUniform(delayReprimand)){
												if(this.getChanceOfEvent("Reprimand")){
													if(bonusReward){
														message = "NoBonus";
														//interaction.request(message);
														manager.interaction(interaction, message);
														//System.out.println("--NB Manager " + myProfile.name() + " NoBonus");
													}
													else{
														message = "Reprimand";
														//interaction.request(message);
														manager.interaction(interaction, message);
														//System.out.println("--RR Manager " + myProfile.name() + " Reprimand");
													}
												}
											}
										}
									}
									if(wp < simS.getPraiseThreshold()){
										if(getChanceOfEvent("BConstructive")){
											myProfile.increaseConstructive(1);
										}
										if(pD.drawChanceFromUniform(delayPraise)){
											if(this.getChanceOfEvent("Praise")){
												if(bonusReward){
													message = "Bonus";
													//interaction.request(message);
													manager.interaction(interaction, message);
													//System.out.println("++BB CEO " + myProfile.name() + " Bonus");
												}
												else{
													message = "Praise";
													//interaction.request(message);
													manager.interaction(interaction, message);
													//System.out.println("++PP CEO " + myProfile.name() + " Praise");
												}
											}
										}
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
	//get all contracts assigned to the Organization and create and assign ManageContract task to itself (CEO).
	protected void getContracts(){
		ManageContract task;
		Contract contract;
		if(myContracts.size() == 0){
			myContracts.addAll(myProfile.organization().getContractList());
			nContracts = myContracts.size();
			for(int c=0; c<nContracts; c++){
				contract = myContracts.get(c);
				contract.assignSeniorManager(this);
				task = new ManageContract(contract);
				this.assignTask(task); //Assign task ManageContract to itself (CEO);
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
				//System.out.println("CEO " + myProfile.name() + " will assign Contract "+ contract.TaskName() + " to Manager");
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
				//System.out.println("CEO " + myProfile.name() + " contract = "+ contract.TaskName() + 
				//		" wll assign Deliverable = " + deliverable.getName() + " to Manager " + manager.getName());
				task = new ManageDeliverable(deliverable);
				assignedTasksList.add(task);
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
			System.out.println("************************* MOUSE POINTER IS ON Actor CEO " + myProfile.name());
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
		int e;

		spentEffortForThisStep = productiveEffortForThisStep + overheadEffortForThisStep;
		extraEffort = extraEffort +  (spentEffortForThisStep - 8);
		if(extraEffort < 0){
			extraEffort = 0;
		}

		productiveEffortForThisStep = 0;
		overheadEffortForThisStep = 0;
		spentEffortForThisStep = 0;

		availableEffortForThisStep = 12;
		myEnergy = myEnergy + 0;
		// myProfile.distributeEffort();
		workEffort = 0;
		overtimeEffort = 0;
		learnEffort = 0;
		askHelpEffort = 0;
		helpEffort = 0;
		totalEffort = 0;
		//extraEffort = 0;


		if(taskInProgress != null){
			if(taskInProgress.canActorDoIt()){ 
				//The actor has a task assigned and knows how to do it.
				//No learning or ask for help is needed.
				//The actor may choose helping other actors.

				workEffort = 0;
				overtimeEffort = 0;
				helpEffort = 0;
				learnEffort = 0;
				askHelpEffort = 0;

				if(isAgressive()){
					//Aggressive actors will work as much as they can and
					//they may choose to help other actors.
					workEffort = pD.getIntUniform(8, 8);
					overtimeEffort = pD.getIntUniform(1, 4);
					e = workEffort + overtimeEffort;
					if(overtimeEffort < 4){
						helpEffort = helpEffort + 1;
					}
				}
				if(isConstructive()){
					//Constructive actors will do the work and offer help to other actors.
					workEffort = pD.getIntUniform(6, 8);
					overtimeEffort = pD.getIntUniform(0, 4);
					e = workEffort + overtimeEffort;
					if((workEffort)< 8){
						helpEffort = helpEffort + (8 - workEffort);
					}
					if( overtimeEffort < 4){
						helpEffort = helpEffort + (4 - overtimeEffort);
					}
				}
				if(isPassive()){
					//Passive actors will work but hot hard.
					//Passive actors will not offer help.
					workEffort = pD.getIntUniform(6, 8);
					overtimeEffort = 0;
					helpEffort = 0;
				}
			}
			else{
				//The actor has a task assigned and does not know how to do it.
				//The actor may choose to learn and ask for help;
				learnEffort = 0;
				askHelpEffort = 0;
				helpEffort = 0;
				if(isAgressive()){
					//Aggressive actors will work and learn as much as possible.
					//Aggressive actors will not ask help.
					//Aggressive actors will not help other actors in this situation.

					workEffort = pD.getIntUniform(6, 8);
					overtimeEffort = pD.getIntUniform(2, 4);
					helpEffort = 0;
					learnEffort = 0;
					askHelpEffort = 0;
					e = workEffort + overtimeEffort;
					if(workEffort < 8){
						learnEffort = learnEffort + (8 - workEffort);
					}
					if(overtimeEffort > 0){
						learnEffort = overtimeEffort;
						overtimeEffort = 0;
					}
				}
				if(isConstructive()){
					workEffort = pD.getIntUniform(2, 5);
					learnEffort = 7 - workEffort;
					askHelpEffort = 1;
					overtimeEffort = 0;
					helpEffort = pD.getIntUniform(2, 4);
				}
				if(isPassive()){
					workEffort = pD.getIntUniform(4, 8);
					if(workEffort < 8){
						learnEffort = 1;
					}
					askHelpEffort = 0;
					overtimeEffort = 0;
					helpEffort = pD.getIntUniform(2, 4);
				}
			}
			//workEffort = totalEffort - (helpEffort + learnEffort + askHelpEffort);
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
			//The actor may choose to help others and learn.
			workEffort = 0;
			overtimeEffort = 0;
			helpEffort = 0;
			learnEffort = 0;
			askHelpEffort = 0;
			if(isAgressive()){
				//Aggressive actors may help and learn.
				helpEffort = pD.getIntUniform(6, 8);
				learnEffort = 8 - helpEffort;
			}
			if(isConstructive()){
				//Constructive actors will help and learn.
				helpEffort = pD.getIntUniform(6, 8);
				learnEffort = 8 - helpEffort;
			}
			if(isPassive()){
				//Passive actors will do nothing.
				helpEffort = pD.getIntUniform(4, 8);
				learnEffort = 8 - helpEffort;
			}
		}

		totalEffort = workEffort + overtimeEffort + helpEffort + learnEffort + askHelpEffort;

		if(totalEffort > 0){
			//printDistributedEffort();
		}
	}

	//CEO simply assign Contracts or TEOW to its Managers. Managers are suppose to allocate tasks to Team Leaders
	public void step() {
		Task task;
		int effort;
		effort = getWorkEffort() + getOvertimeEffort();
		//TaskType taskType;

		//getContracts will be executed only once;
		//getContracts will create ManageContract tasks and assign them to the CEO
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
		int nContracts = myContracts.size();
		for(int c=0; c<nContracts; c++){
			thisContract = myContracts.get(c);
			//increaseOverheadEffort(effort / nContracts);
		}
		//increaseProductiveEffort(getAvailableEffortForThisStep());
	}

}
