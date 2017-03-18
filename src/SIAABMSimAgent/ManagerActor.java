package SIAABMSimAgent;

import java.util.ArrayList;
import java.text.DecimalFormat;
import SIAABMSimAgent.Artifact;
import SIAABMSimAgent.Artifact.ArtifactStatus;
import SIAABMSimSocial.Organization;
import SIAABMSimAgent.ActorProfile.RoleType;
import SIAABMSimConnection.EngineeringTask;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.ReviewTask;
import SIAABMSimConnection.ReworkTask;
import SIAABMSimConnection.Task;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimConnection.Task.TaskState;
import SIAABMSimConnection.ManageDeliverable;
import SIAABMSimConnection.DevelopDeliverable;
import SIAABMSimConnection.ReviewDeliverable;
import SIAABMSimConnection.ReworkDeliverable;
import SIAABMSimConnection.ManageTEOW;
import SIAABMSimConnection.ManagementTask;
import SIAABMSimConnection.LeadershipTask;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimTask.Contract;
import SIAABMSimTask.TEOW;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.simSettings;
import SIAABMSimUtils.polarBehaviour.BehaviourStyle;

import java.awt.Color;
import uchicago.src.sim.gui.SimGraphics;

public class ManagerActor extends Actor{

	private Contract myContract;
	private ArrayList<Artifact> myDeliverables;
	private ArrayList<TEOW> myTEOWList;
	private int nTEOWs;
	private CeoActor myCEO;
	protected ArrayList<TeamLeaderActor> myTeamLeaderList;
	private int nTLs;
	private int nextTeamLeader; //Next TeamLeade that will have a task allocated;
	private ArrayList <Task> assignedTasks;
	private int lastInteraction;
	private double departmentPerformance;
	private double collectiveExperience;
	private double collectiveKnowledge;

	public ManagerActor(ActorProfile profile, Organization organization, int x, int y){
		myProfile = profile;
		myX = x;
		myY = y;
		myProfile.setOrganization(organization);
		pD = pD.getInstance();
		simS = simS.getInstance();
		df = new DecimalFormat("#.##");
		myContract = null;
		fromInteractions = new ArrayList<Interaction>();
		toInteractions = new ArrayList<Interaction>();
		myTasks = new ArrayList<Task>();
		myCEO = null;
		myTeamLeaderList = new ArrayList<TeamLeaderActor>();
		nTLs = 0;
		nextTeamLeader = 0;
		myDeliverables = new ArrayList<Artifact>();
		myTEOWList = new ArrayList<TEOW>();
		assignedTasks = new ArrayList<Task>();
		nTEOWs = 0;
		lastInteraction = 0;
		departmentPerformance = 0.0;
		collectiveKnowledge = 0.0;
		collectiveExperience = 0.0;
		explicitReward = simS.getExplicitReward();
		implicitReward = simS.getImplicitReward();
		probabilityOfInteraction = simS.getInteractionFactor();
		cooperationFactor = simS.getCooperationFactor();
	}

	private void echoMessageToTeam(String message){
		Interaction interaction;
		Actor actor;
		TeamLeaderActor tl;
		for(int i=0; i<toInteractions.size(); i++){
			interaction = toInteractions.get(i);
			actor = interaction.getToActor();
			if(inMyTeam(actor)){
				if(actor.myRole() == RoleType.TeamLeader){
					tl = (TeamLeaderActor) actor;
					tl.interaction(interaction, message);
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

	public void iterativeFeedback(Artifact deliverable){
		//System.out.println(myRole() + " " + getName() + " received Iterative FeedBack on " + deliverable.getName());
		TeamLeaderActor tl;
		for(int a=0; a<myTeamLeaderList.size(); a++){
			tl = myTeamLeaderList.get(a);
			tl.iterativeFeedback();
		}

	}

	private boolean inMyTeam(Actor actor){
		for(int a=0; a<myTeamLeaderList.size(); a++){
			if(actor == myTeamLeaderList.get(a)){
				return true;
			}
		}
		return false;
	}

	private void calculateCollectiveKnowledgeAndExperience(){
		TeamLeaderActor tl;
		for(int a=0; a<myTeamLeaderList.size(); a++){
			tl = myTeamLeaderList.get(a);
			//tl.getCollectiveExperience();
			//tl.getCollectiveKnowledge();
		}
	}

	/*
	private void calculateDepartmentPerformance(){
		double dp;
		int s;
		s = myTeamLeaderList.size();
		dp = 0.0;
		for(int a=0; a< s; a++){
			dp = dp + myTeamLeaderList.get(a).getTeamPerformance();
		}
		departmentPerformance = dp / s;
	}
	 */

	private void calculateCollectiveExperienceAndKnowledge(){
		double tp;
		double e, k;
		TeamLeaderActor tl;
		int teamSize;
		teamSize = myTeamLeaderList.size();
		tp = 0.0;
		e = 0.0;
		k = 0.0;
		for(int a=0; a<teamSize; a++){
			tl = (TeamLeaderActor) myTeamLeaderList.get(a);
			//e = e + tl.getCollectiveExperience();
			//k = k + tl.getCollectiveKnowledge();
		}
		e = e + this.getExperience();
		k = k + this.getKnowledge();
		collectiveExperience = e / teamSize + 1;
		collectiveKnowledge = k / teamSize + 1;

		//System.out.println("TeamLeader " + myProfile.name() + " Team Performance = " + df.format(teamPerformance));
	}

	public double getCostPerformance(){
		double cp, x;
		int nTasks;
		nTasks = 0;
		cp = 0.0;
		for(int t=0; t<myTasks.size(); t++){
			x = myTasks.get(t).getCostPerformance();
			if(x > 0.0){
				cp = cp + x;
				nTasks = nTasks + 1;
			}
		}
		if(nTasks > 0){
			return cp / nTasks;
		}
		else{
			return -1.0;
		}
	}

	protected void processInteractions(){
		Interaction interaction;
		Actor actor;
		TeamLeaderActor teamLeader;
		BehaviourStyle myBehaviourStyle;
		String message;
		double q, wp;
		int n, s, nTeams;
		double dp;
		double delayReprimand;
		double delayPraise;
		double copySeniorBehaviour;
		//System.out.println("Manager " + myProfile.name() + " processInteractions");

		copySeniorBehaviour = simS.getCopySeniorBehaviour();

		if(pD.drawChanceFromUniform(copySeniorBehaviour)){

			if(myCEO != null){
				if(myCEO.isAgressive()){
					//if(this.isConstructive()){
					if(pD.drawChanceFromUniform(0.8)){
						this.setAgressive();
					}
					else{
						if(pD.drawChanceFromUniform(0.3)){
							this.setPassive();
						}
					}
					//}
				}
				if(myCEO.isConstructive()){
					if(pD.drawChanceFromUniform(0.8)){
						this.setConstructive();
					}
				}
				if(myCEO.isPassive()){
					if(this.isConstructive()){
						if(pD.drawChanceFromUniform(0.2)){
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
		dp = 0.0;
		nTeams = 0;
		for(int i=1; i<s; i++){
			n = lastInteraction + i;
			if(n >= s){
				n = 0;
			}
			interaction = toInteractions.get(n);
			if(interaction.isActive()){
				if(pD.drawChanceFromUniform(probabilityOfInteraction)){
					actor = interaction.getToActor();
					if(inMyTeam(actor)){
						if(actor.myRole() == RoleType.TeamLeader){
							//System.out.println("Manager " + myProfile.name() + " found TeamMember to interact");
							teamLeader = (TeamLeaderActor) actor;
							if(teamLeader.isActive()){
								//wp = teamLeader.getTeamPerformance();
								wp = teamLeader.getCostPerformance();
								dp = dp + wp;
								nTeams = nTeams + 1;
								//System.out.println("Manager " + myProfile.name() + " Team CostPerformance = " + df.format(wp));

								rewardModel();
								if(bonusReward){
									//System.out.println("EEEE Manager EXPLICIT Reward");
								}
								else{
									//System.out.println("IIII Manager IMPLICIT Reward");
								}

								if(this.getChanceOfEvent("Start")){
									if( wp > simS.getReprimandThreshold()){
										if(getChanceOfEvent("BAggressive")){
											if(pD.drawChanceFromUniform(0.01)){
												myProfile.increaseAggressive(1);
											}
											//***myProfile.increaseAggressive(3);
										}
										else{
											if(getChanceOfEvent("BConstructive")){
												myProfile.increaseConstructive(1);
											}
										}
										if(this.getChanceOfEvent("OfferHelp")){
											if(bonusReward){
												message = "OfferBonus";
												//interaction.request(message);
												//teamLeader.interaction(interaction, message);
												//System.out.println("--BB Manager " + myProfile.name() + " OfferBonus");
											}
											else{
												message = "OfferHelp";
												//interaction.request(message);
												teamLeader.interaction(interaction, message);
												//System.out.println("--HH Manager " + myProfile.name() + " OfferHelp");
											}
										}
										else{
											if(pD.drawChanceFromUniform(delayReprimand)){
												if(this.getChanceOfEvent("Reprimand")){
													if(bonusReward){
														message = "NoBonus";
														//interaction.request(message);
														teamLeader.interaction(interaction, message);
														//System.out.println("--NB Manager " + myProfile.name() + " NoBonus");
													}
													else{
														message = "Reprimand";
														//interaction.request(message);
														teamLeader.interaction(interaction, message);
														//System.out.println("--RR Manager " + myProfile.name() + " Reprimand");
													}
												}
											}
										}
									}
									if( wp < simS.getPraiseThreshold()){
										if(getChanceOfEvent("BConstructive")){
											myProfile.increaseConstructive(1);
										}
										if(pD.drawChanceFromUniform(delayPraise)){
											if(this.getChanceOfEvent("Praise")){
												if(bonusReward){
													message = "Bonus";
													//interaction.request(message);
													teamLeader.interaction(interaction, message);
													//System.out.println("++BB Manager " + myProfile.name() + " Bonus");
												}
												else{
													message = "Praise";
													//interaction.request(message);
													teamLeader.interaction(interaction, message);
													//System.out.println("++PP Manager " + myProfile.name() + " Praise");
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
			if(nTeams > 0){
				departmentPerformance = dp / nTeams;
			}
			else{
				departmentPerformance = 1.0;
			}
		}
		//System.out.println("Manager " + myProfile.name() + " Department Performance = " + df.format(departmentPerformance));
	}


	public boolean hasAssignedContract(){
		if(myContract == null){
			return false;
		}
		else{
			return true;
		}
	}

	protected void findCEO(){

	}

	protected void findTeamLeaders(){
		Actor actor;
		for(int i=0; i<toInteractions.size(); i++){
			actor = (Actor) toInteractions.get(i).getToAgent();
			if(actor.myRole() == RoleType.TeamLeader){
				myTeamLeaderList.add((TeamLeaderActor)actor);
				nTLs = nTLs +1;
			}
		}
		//nTeamLeaders = myTeamLeaderList.size();
	}

	protected void assignTaskToNextTeamLeader(Task task){
		TeamLeaderActor actor;
		actor = myTeamLeaderList.get(nextTeamLeader);
		actor.assignTask(task);
		//assignedTasks.add(task);
		//System.out.println("Manager " + myProfile.name() + 
		//		" ASSIGNED " + task.taskType() + " to " + actor.myRole() + " " + actor.getName());
		nextTeamLeader = nextTeamLeader + 1;
		if(nextTeamLeader >= myTeamLeaderList.size()){
			nextTeamLeader = 0;
		}
	}

	protected void checkProgress(Task task){

	}

	protected boolean allDependenciesAreComplete(){
		for(int t=0; t<myTEOWList.size(); t++){
			if(! myTEOWList.get(t).allDependenciesAreComplete()){
				return false;
			}
		}
		return true;
	}

	protected void processMyTasks(){
		Artifact deliverable;
		Task task, rTask, lastTask;
		ManageDeliverable mdTask;
		DevelopDeliverable tlTask, dvdTask;
		ReworkDeliverable tlRwTask;
		ReviewDeliverable rvdTask;
		ReworkDeliverable rwdTask;
		ReviewTask reviewTask;
		ArrayList<Task> taskList;
		boolean allTasksComplete;

		if(myCEO == null){
			findCEO();
		}
		if(myTeamLeaderList.size() == 0){
			findTeamLeaders();
		}

		for(int t=0; t<myTasks.size(); t++){
			task = myTasks.get(t);
			if(task.taskType() == TaskType.ManageDeliverable){
				mdTask = (ManageDeliverable) task;
				deliverable = mdTask.getDeliverable();
				thisContract = deliverable.getContract();
				if(mdTask.state() == TaskState.ASSIGNED){
					if(deliverable.BFIAvailable()){
						//System.out.println("Manager " + this.getName() + 
						//		" ProcessMyTasks() found ASSIGNED ManageDeliverable for " + deliverable.getName() +
						//		" task ready to be processed");
						deliverable.acceptNewBFIBaseline();
						//System.out.println("Manager is creating and DevelopDeliverable Task");
						tlTask = new DevelopDeliverable(deliverable);
						tlTask.setEffortFactor(0.74 * simS.getEffortFactor());
						mdTask.addTask(tlTask);
						assignTaskToNextTeamLeader(tlTask);
						mdTask.isInProgress();
						taskInProgress = mdTask;
					}
					if(deliverable.toBeReworked()){
						//Note that when a Deliverable is Reworked the New Baseline is not Accepted.
						//and as far as TL is concern, the Deliverable will be developed again.
						//System.out.println("Manager is creating and ***ReworkDeliverable Task");
						tlTask = new DevelopDeliverable(deliverable);
						//tlTask.setEffortFactor(simS.getReworkFactor());
						tlTask.setEffortFactor(0.74 * simS.getReworkEffortFactor());
						mdTask.addTask(tlTask);
						assignTaskToNextTeamLeader(tlTask);
						mdTask.isInProgress();
					}
				}
				if(mdTask.state() == TaskState.INPROGRESS){
					//System.out.println("Manager " + this.getName() + " ProcessMyTasks() ManageDeliverable task for " 
					//		+ deliverable.getName() + " is INPROGRESS");
					lastTask = mdTask.getLastTask();
					allTasksComplete = false;
					switch (lastTask.taskType()){
					case DevelopDeliverable:{
						dvdTask = (DevelopDeliverable)lastTask;
						if(dvdTask.allAssignedTasksAreComplete()){
							allTasksComplete = true;
						}
						break;
					}
					case ReviewDeliverable:{
						rvdTask = (ReviewDeliverable)lastTask;
						if(rvdTask.allAssignedTasksAreComplete()){
							allTasksComplete = true;
						}
						break;
					}
					case ReworkDeliverable:{
						rwdTask = (ReworkDeliverable)lastTask;
						if(rwdTask.allAssignedTasksAreComplete()){
							allTasksComplete = true;
						}
					}
					}
					if(allTasksComplete){
						switch (lastTask.taskType()){
						case DevelopDeliverable:{
							dvdTask = (DevelopDeliverable)lastTask;
							//System.out.println("Manager is creating and RewiewDeliverable Task");
							rvdTask = new ReviewDeliverable(deliverable);
							rvdTask.setTaskList(dvdTask.getTasks());
							mdTask.addTask(rvdTask);
							//The following LOC is causing problem 7 Sep 2010
							assignTaskToNextTeamLeader(rvdTask);
							mdTask.isInProgress();
							break;
						}
						case ReviewDeliverable:{
							rvdTask = (ReviewDeliverable)lastTask;
							taskList = rvdTask.getTasks();
							//System.out.println("Manager is creating and ReworkDeliverable Task");
							rwdTask = new ReworkDeliverable(deliverable);
							for(int tr=0; tr<taskList.size(); tr++){
								rTask = taskList.get(tr);
								if(rTask.taskType() == TaskType.Review){
									reviewTask = (ReviewTask) rTask;
									if(reviewTask.getRevealedError() > 0.0){
										rwdTask.addTask(reviewTask);
									}
								}
							}
							taskList = rwdTask.getTasks();
							if(taskList.size() >0){
								mdTask.addTask(rwdTask);
								assignTaskToNextTeamLeader(rwdTask);
								mdTask.isInProgress();
							}
							else{
								//System.out.println("+++++ Manager ReviewDeliverable Revealed Error = 0.0 ---------------->>>>>> BASELINE");
								mdTask.getDeliverable().baseline();
								mdTask.setState(TaskState.ASSIGNED);
							}
							//}
							break;
						}
						case ReworkDeliverable:{
							rwdTask = (ReworkDeliverable)lastTask;
							//if(rwdTask.allAssignedTasksAreComplete()){
							if(rwdTask.getError() > 1.0){
								//This code will never be executed.
								rvdTask = new ReviewDeliverable(deliverable);
								rvdTask.setTaskList(rwdTask.getTasks());
								mdTask.addTask(rvdTask);
								assignTaskToNextTeamLeader(rvdTask);
								mdTask.isInProgress();
								//System.out.println("+++++ Manager ReviewDeliverable --->>> REVIEW AGAIN");
								//mdTask.setState(TaskState.ASSIGNED);
							}else{
								//System.out.println("----- Manager ReworkDeliverable Error = 0.0 ==============>>>>>> BASELINE");
								mdTask.getDeliverable().baseline();
								mdTask.setState(TaskState.ASSIGNED);
							}
							//}
							break;
						}
						}
					}
				}
			}
		}
	}

	public void draw(SimGraphics G){
		int r, g, b;
		r = (int) (myProfile.aggressive() * 255.0);
		g = (int) (myProfile.passive() * 255.0);
		b = (int) (myProfile.constructive() * 255.0);
		myColor = new Color (r,g,b);
		G.drawHollowFastRect(Color.YELLOW);
		G.drawFastCircle(myColor);
		//System.out.println("Draw Actor Manager " + myProfile.name());
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
				helpEffort = pD.getIntUniform(0, 1);
				learnEffort = pD.getIntUniform(0, 4);
			}
			if(isConstructive()){
				//Constructive actors will help and learn.
				helpEffort = pD.getIntUniform(2, 4);
				learnEffort = pD.getIntUniform(2, 4);
			}
			if(isPassive()){
				//Passive actors will do nothing.
				helpEffort = 0;
				learnEffort = 0;
			}
		}

		totalEffort = workEffort + overtimeEffort + helpEffort + learnEffort + askHelpEffort;

		if(totalEffort > 0){
			//printDistributedEffort();
		}
	}

	public void step() {
		//System.out.println("step() ManagerActor " + myProfile.name());
		int effort;
		effort = getWorkEffort() + getOvertimeEffort();
		processMyTasks();
		processInteractions();
		//getDepartmentPerformance();
		//increaseOverheadEffort(effort);
		//increaseProductiveEffort(getAvailableEffortForThisStep());
		if(taskInProgress == null){
			forgetting();
		}
		else{
			learning();
		}
		calculateCollectiveKnowledgeAndExperience();
	}

}
