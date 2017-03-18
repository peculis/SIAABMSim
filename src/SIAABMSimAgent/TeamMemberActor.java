package SIAABMSimAgent;

import java.text.DecimalFormat;
import java.util.ArrayList;

import SIAABMSimAgent.ActorProfile.RoleType;
import SIAABMSimConnection.Interaction;
import SIAABMSimSocial.Organization;
import SIAABMSimConnection.Task;
import SIAABMSimConnection.Task.TaskState;
import SIAABMSimConnection.EngineeringTask;
import SIAABMSimConnection.ReviewTask;
import SIAABMSimConnection.ReworkTask;
import SIAABMSimConnection.EngineeringTask;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimTask.Contract;
import SIAABMSimTask.TEOW;
import SIAABMSimTask.TEOW.TEOWPhase;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.polarBehaviour.BehaviourStyle;

import java.awt.Color;
import uchicago.src.sim.gui.SimGraphics;

public class TeamMemberActor extends Actor{

	//private Task taskInProgress;
	//private int taskETC; //this is a temporary test;
	private String interactionEvent;
	private int lastInteraction;

	public TeamMemberActor(ActorProfile profile, Organization organization, int x, int y){
		myProfile = profile;
		myX = x;
		myY = y;
		pD = pD.getInstance();
		df = new DecimalFormat("#.##");
		fromInteractions = new ArrayList<Interaction>();
		toInteractions = new ArrayList<Interaction>();
		myTasks = new ArrayList<Task>();
		pD = pD.getInstance();
		simS = simS.getInstance();
		myProfile.setOrganization(organization);
		taskInProgress = null;
		interactionEvent = "";
		lastInteraction = 0;
		explicitReward = simS.getExplicitReward();
		implicitReward = simS.getImplicitReward();
		probabilityOfInteraction = simS.getInteractionFactor();
		cooperationFactor = simS.getCooperationFactor();
	}

	public void interaction(Interaction interaction, String message){
		processInteraction(interaction, message);
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
				//The actor prefers Explicit Reward
				System.out.println("+++++++ OfferBonus ExplicitReward");
				myProfile.increaseMotivation(10);
				myProfile.increaseAggressive(2);
				//myProfile.increaseConstructive(1);
			}
			else{
				myProfile.decreaseMotivation(2);
				myProfile.increaseAggressive(1);
				myProfile.increasePassive(2);
			}
		}
		if(message.equals("Reprimand")){
			if(getChanceOfEvent("ImplicitReward")){
				//System.out.println("------- Reprimand ImplicitReward");
				myProfile.decreaseMotivation(5);
				myProfile.increasePassive(2);
			}
			else{
				myProfile.decreaseMotivation(2);
				myProfile.increaseAggressive(2);
				myProfile.increasePassive(2);
			}
		}
		if(message.equals("NoBonus")){
			//System.out.println("NoBonus ExplicitReward");
			if(getChanceOfEvent("ExplicitReward")){
				//The actor prefers Explicit Reward
				//System.out.println("------- NoBonus ExplicitReward");
				myProfile.decreaseMotivation(5);
				myProfile.increasePassive(2);
			}
			else{
				myProfile.decreaseMotivation(2);
				myProfile.increaseAggressive(2);
				myProfile.increasePassive(2);
			}
		}
		if(message.equals("Praise")){
			if(getChanceOfEvent("ImplicitReward")){
				//The actor prefers Implicit Reward
				//System.out.println("Praise ImplicitReward");
				myProfile.increaseMotivation(5);
				//myProfile.increaseConstructive(3);
			}
			else{
				myProfile.decreaseMotivation(3);
				myProfile.increasePassive(5);
				myProfile.increaseConstructive(1);
			}
		}
		if(message.equals("Bonus")){
			if(getChanceOfEvent("ExplicitReward")){
				//The actor prefers Explicit Reward
				//System.out.println("+++++++ OfferBonus ExplicitReward");
				myProfile.increaseMotivation(10);
				//myProfile.increaseConstructive(1);
			}
			else{
				myProfile.decreaseMotivation(2);
				myProfile.increaseAggressive(1);
				myProfile.increasePassive(2);
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
				myProfile.increaseMotivation(1);
				myProfile.increaseConstructive(1);
			}
		}
		if(message.equals("NotAccepted")){
			interaction.decreaseQuality();
			myProfile.decreaseMotivation();
			myProfile.increasePassive(1);
			if(isMyBoss){
				myProfile.decreaseMotivation(2);
				myProfile.increasePassive(2);
			}
		}

		if(fromActor != this){
			updateBehaviour(message, fromActor);
		}

		if(message.equals("Accepted") || message.equals("NotAccepted")){
			//if(toActor.myRole() == RoleType.Expert){
			//System.out.println(myRole() + " " + getName() + " received " + message + " from " + toActor.myRole() + " " + toActor.getName());
			//}
		}
	}

	private void taskCompleted(){ 
		System.out.println("****** Task COMPLETED");
		if(taskInProgress != null){
			taskInProgress.isComplete();
			//myTasks.remove(taskInProgress);
			taskInProgress = null;
		}
	}

	private void taskReady(){ 
		ReviewTask rvt;
		ReworkTask rwt;
		//System.out.println("****** Task READY");
		//taskInProgress.isComplete();
		switch(taskInProgress.taskType()){
		case Engineering: {
			break;
		}
		case Review: {
			rvt = (ReviewTask) taskInProgress;
			if(rvt.getRevealedError() > 0.0){
				//System.out.println("TM " + this.getName() + " ---- ReviewTasks is READY and RevealedError > 0.0");
			}
			break;
		}
		case Rework: {
			rwt = (ReworkTask) taskInProgress;
			//System.out.println("TM " + this.getName() + " ++++ ReworkTask is READY");
			break;
		}
		}
		taskInProgress.isReady();
		//myTasks.remove(taskInProgress);
		taskInProgress = null;
	}

	private void getNextTask(){
		Task task;
		ArrayList<Task> taskPool;
		EngineeringTask engTask;
		ReviewTask reviewTask;
		ReworkTask reworkTask;
		ArrayList <TEOW> TEOWList;
		TEOW teow;
		TEOWPhase teowPhase;
		int assignedTasks, maxAssignedTasks;;
		taskInProgress = null;
		maxAssignedTasks = 1;
		assignedTasks = 0;
		//System.out.println(this.myRole() + " " + this.getName() + " get next task");
		if(myProfile.organization().isAdaptive()){
			//System.out.println(this.myRole() + " " + this.getName() + " Adaptive Organizaton");
			taskPool = (myProfile.organization().getTaskPool());
			for(int t=0; t<taskPool.size(); t++){
				task = taskPool.get(t);
				if(task.state() == TaskState.NOTALLOCATED){
					this.assignTask(task);
					taskInProgress = task;
					taskInProgress.isInProgress();
					if(taskInProgress.taskType() == TaskType.Engineering){
						//System.out.println("@@@ TM = " + this.getName() + " Engineering");
					}
					if(taskInProgress.taskType() == TaskType.Review){
						//System.out.println("@@@ TM = " + this.getName() + " Rewiew");
					}
					if(taskInProgress.taskType() == TaskType.Rework){
						//System.out.println("****** TM = " + this.getName() + " REWORK");
					}
					t = taskPool.size();
					//System.out.println(this.myRole() + " " + this.getName() + 
					//		" next task " + taskInProgress.taskType());
				}
			}
		}
		else{
			if(myTasks.size() >0){
				for(int t=0; t<myTasks.size(); t++){
					task = myTasks.get(t);
					if(task.state() == TaskState.ASSIGNED){
						taskInProgress = task;
						taskInProgress.isInProgress();
						if(taskInProgress.taskType() == TaskType.Review){
							//System.out.println("@@@ TM = " + this.getName() + " Rewiew");
						}
						if(taskInProgress.taskType() == TaskType.Rework){
							//System.out.println("****** TM = " + this.getName() + " REWORK");
						}
						t = myTasks.size();
						//System.out.println(this.myRole() + " " + this.getName() + 
						//		" next task " + taskInProgress.taskType());
					}
				}
			}
		}
	}

	private void wasteEffort(){
		if(taskInProgress != null){
			taskInProgress.wasteEffort();
		}
	}


	public double reportProgress(TEOW teow){
		double progress;
		progress = 0.0;
		return progress;
	}

	public double reportPerformance(){
		double p;
		if(taskInProgress != null){
			return (taskInProgress.getPerformance());
		}
		else{
			return 1.0;
		}
	}

	/*
	public boolean isActive(){
		if(taskInProgress == null){
			return false;
		}
		else{
			return true;
		}
	}
	 */

	protected void processInteractions(){
		Interaction interaction;
		double q;
		int n, s, effort;
		String message;
		Actor toActor, fromActor;
		//System.out.println("TeamMember processInteractions");
		s = toInteractions.size();
		//System.out.println("TeamMember processInteractions s = " + s);

		//OfferHelp
		effort = getHelpEffort();

		if(effort > 0){
			for(int i=1; i<s; i++){
				n = lastInteraction + i;
				if(n >= s){
					n = 0;
				}
				interaction = toInteractions.get(n);
				if(interaction.isActive()){
					if(pD.drawChanceFromUniform(probabilityOfInteraction)){
						q = interaction.getQuality();
						fromActor = interaction.getFromActor();
						toActor = interaction.getToActor();
						if(toActor.myRole() == ActorProfile.RoleType.TeamMember){
							if(!(toActor.areYouMoreExperienced(fromActor) || toActor.areYouMoreKnowledgeable(fromActor))){
								//The Actor to interact with is less knowledgeable or experienced -> OfferHelp.
								if(q >=0.5){
									if(pD.drawChanceFromUniform(q)){
										lastInteraction = n;
										i = s;
										//System.out.println("Interaction Quality = " + df.format(q));
										message = "OfferHelp";
										toActor.interaction(interaction, message);
										//interaction.request("OfferHelp");
										increaseOverheadEffort(effort);
										i = s;
									}
								}
							}
						}
					}
				}
			}

			//AskHelp
			effort = getAskHelpEffort();

			if(effort > 0){
				//System.out.println("TM " + this.getName() + " " + myProfile.getBehaviourStyle() + " AskHelp");
				for(int i=1; i<s; i++){
					n = lastInteraction + i;
					if(n >= s){
						n = 0;
					}
					interaction = toInteractions.get(n);
					if(interaction.isActive()){
						if(pD.drawChanceFromUniform(probabilityOfInteraction)){
							q = interaction.getQuality();
							fromActor = interaction.getFromActor();
							toActor = interaction.getToActor();
							if(toActor.myRole() == ActorProfile.RoleType.Expert){
								if(this.getOrganization() == toActor.getOrganization()){
									//System.out.println("++++++++++++++ Local Interaction Quality = " + df.format(q));
									message = "AskHelp";
									toActor.interaction(interaction, message);
									//interaction.request("AskHelp");
									increaseOverheadEffort(effort);
									lastInteraction = n;
									i = s;
								}
								else{
									if(pD.drawChanceFromUniform(q)){
										//System.out.println("------------- Remote Interaction Quality = " + df.format(q));
										message = "AskHelp";
										toActor.interaction(interaction, message);
										//interaction.request("AskHelp");
										increaseOverheadEffort(effort);
										lastInteraction = n;
										i = s;
									}
								}
							}
							else{
								if(toActor.areYouMoreExperienced(fromActor) || toActor.areYouMoreKnowledgeable(fromActor)){
									if(q >=0.5){
										if(pD.drawChanceFromUniform(q)){
											lastInteraction = n;
											i = s;
											//System.out.println("Interaction Quality = " + df.format(q));
											message = "AskHelp";
											toActor.interaction(interaction, message);
											//interaction.request("AskHelp");
											increaseOverheadEffort(effort);
											i = s;
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

	private void progressTask(){

		//System.out.println(this.myRole() + " " + this.getName() + 
		//		" progress task " + taskInProgress.taskType() + 
		//		" ACWP = " + taskInProgress.getACWP());

		if(taskInProgress != null){

			if(taskInProgress.taskType() == TaskType.Engineering || 
					taskInProgress.taskType() == TaskType.Review ||
					taskInProgress.taskType() == TaskType.Rework){
				//increaseSpentEffort();
				//increaseProductiveEffort();
			}

			/*
			System.out.println(this.myRole() + " " + this.getName() + 
					" progress task " + taskInProgress.taskType() + 
					" ACWP = " + taskInProgress.getACWP() +
					" BAC = " + taskInProgress.getBAC() +
					" ACWP/BAC = " + taskInProgress.getACWPBACRatio());
			 */

			taskInProgress.progress();
			if((taskInProgress.state() == TaskState.COMPLETE) || (taskInProgress.state() == TaskState.READY) ){
				//taskCompleted();
				//taskReady();
			}
			else{
				if(taskInProgress.taskType() == TaskType.Engineering || 
						taskInProgress.taskType() == TaskType.Rework){
					//System.out.println("****** Eng or Rework");
					if(this.isConstructive()){
						if(taskInProgress.getError() <= (0.05 * (1.0 - taskInProgress.actorUsefulKnowledgeRatio()))){
							//taskCompleted(); 
							taskReady();
						}
						else{
							if((double) taskInProgress.getACWPBACRatio() > pD.getDoubleUniform(1.1, 1.5)){
								//taskCompleted(); 
								taskReady();
							}
						}
					}
					else{
						if(this.isAgressive()){
							if(taskInProgress.getError() <= (0.01 * (1.0 + taskInProgress.actorUsefulKnowledgeRatio()))){
								//taskCompleted(); 
								taskReady();
							}
							else{
								if((double) taskInProgress.getACWPBACRatio() > pD.getDoubleUniform(1.3, 2.0)){
									//taskCompleted(); 
									taskReady();
								}
							}
						}
						else{
							if(taskInProgress.getError() <= (0.2 * (1.0 - taskInProgress.actorUsefulKnowledgeRatio()))){
								//taskCompleted(); 
								taskReady();
							}
							else if((double) taskInProgress.getACWPBACRatio() > pD.getDoubleUniform(1.0, 1.1)){
								//taskCompleted(); 
								taskReady();
							}
						}
					}
				}
				else{
					if(taskInProgress.taskType() == TaskType.Review ){
						//System.out.println("****** Review");
						if(this.isConstructive()){	
							if((double) taskInProgress.getACWPBACRatio() > pD.getDoubleUniform(1.0, 1.2)){
								//taskCompleted(); 
								taskReady();
							}
						}
						else{
							if(this.isAgressive()){
								if((double) taskInProgress.getACWPBACRatio() > pD.getDoubleUniform(1.2, 1.5)){
									//taskCompleted(); 
									taskReady();
								}
							}
							else{
								if((double) taskInProgress.getACWPBACRatio() > pD.getDoubleUniform(1.0, 1.1)){
									//taskCompleted(); 
									taskReady();
								}
							}
						}
					}
				}
			}
		}
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
					helpEffort = 0;
				}
				if(isConstructive()){
					//Constructive actors will do the work and offer help to other actors.
					workEffort = pD.getIntUniform(7, 8);
					overtimeEffort = pD.getIntUniform(0, 4);
					e = workEffort + overtimeEffort;
					if((workEffort)< 8){
						helpEffort = helpEffort + (8 - workEffort);
					}
					if( overtimeEffort < 4){
						helpEffort = helpEffort + (4 - overtimeEffort);
					}
					helpEffort = 1;
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

					workEffort = pD.getIntUniform(8, 8);
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
					helpEffort = 0;
				}
				if(isConstructive()){
					workEffort = pD.getIntUniform(7, 8);
					learnEffort = 8 - workEffort;
					askHelpEffort = 1;
					overtimeEffort = 0;
					helpEffort = pD.getIntUniform(2, 4);
					helpEffort = 1;
				}
				if(isPassive()){
					workEffort = pD.getIntUniform(6, 8);
					if(workEffort < 8){
						learnEffort = 1;
					}
					askHelpEffort = 0;
					overtimeEffort = 0;
					helpEffort = pD.getIntUniform(0, 1);
					helpEffort = 0;
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
				helpEffort = 0; 
			}
			if(isConstructive()){
				//Constructive actors will help and learn.
				//helpEffort = pD.getIntUniform(2, 4);
				//learnEffort = pD.getIntUniform(2, 4);
				helpEffort = 0;
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

		int effort;

		interactionEvent = "";
		processInteractions();

		if(pD.drawChanceFromUniform(0.03)){
			if(pD.drawChanceFromUniform(1.0)){
				myProfile.increasePassive(1);
				myProfile.decreaseMotivation(1);
			}
		}

		if(taskInProgress == null){
			getNextTask();
			if(taskInProgress == null){
				forgetting();
			}

		}
		else{
			//System.out.println(myProfile.name() + " " + myProfile.getBehaviourStyle() + 
			//		" Task Performance = " + df.format(reportPerformance()));

			//learning();
			if(taskInProgress.canActorDoIt()){
				//The Actor has sufficient knowledge to execute the task;
				effort = getWorkEffort() + getOvertimeEffort();
				if(effort > 0){
					increaseExperience();
					if(taskInProgress.taskType() == TaskType.Engineering){
						EngineeringTask engTask = (EngineeringTask) taskInProgress;
						thisContract = engTask.getContract();
						increaseProductiveEffort(effort);
						//increaseOverheadEffort(effort);
					}
					if(taskInProgress.taskType() == TaskType.Rework){
						ReworkTask rewTask = (ReworkTask) taskInProgress;
						thisContract = rewTask.getContract();
						increaseProductiveEffort(effort);
						//increaseOverheadEffort(effort);
					}
					if(taskInProgress.taskType() == TaskType.Review){
						ReviewTask revTask = (ReviewTask) taskInProgress;
						thisContract = revTask.getContract();
						//increaseProductiveEffort(effort);
						increaseOverheadEffort(effort);
					}
					progressTask();
				}
			}
			else{
				//The Actor has insufficient knowledge to execute the task;
				effort = getLearningEffort();
				if(effort > 0){
					//System.out.println(myProfile.name() + " " + myProfile.getBehaviourStyle() + " is SelfLearning");
					increaseKnowledge();
					increaseOverheadEffort(effort);
				}
				if(taskInProgress != null){
					if(this.getChanceOfEvent("Working", this.getMotivation()/0.8)){
						effort = getWorkEffort() + getOvertimeEffort();
						if(effort > 0){
							//System.out.println(myProfile.name() + " " + myProfile.getBehaviourStyle() + " is Working without sufficient knowledge");
							increaseExperience();
							if(taskInProgress.taskType() == TaskType.Engineering){
								EngineeringTask engTask = (EngineeringTask) taskInProgress;
								thisContract = engTask.getContract();
								increaseProductiveEffort(effort);
								//increaseOverheadEffort(effort);
							}
							if(taskInProgress.taskType() == TaskType.Rework){
								ReworkTask rewTask = (ReworkTask) taskInProgress;
								thisContract = rewTask.getContract();
								increaseProductiveEffort(effort);
								//increaseOverheadEffort(effort);
							}
							if(taskInProgress.taskType() == TaskType.Review){
								ReviewTask revTask = (ReviewTask) taskInProgress;
								thisContract = revTask.getContract();
								//increaseProductiveEffort(effort);
								increaseOverheadEffort(effort);
							}
							progressTask();
						}
					}
					else{
						//Waste effort because the Actor s not willing to work.
						effort = getWorkEffort() + getOvertimeEffort();
						increaseOverheadEffort(effort); 
						//System.out.println("Waste Effort = " + df.format(effort));
					}
				}
			}
		}
		//printDistributedEffort();
	}

	public void draw(SimGraphics G){
		int r, g, b;
		r = (int) (myProfile.aggressive() * 255.0);
		g = (int) (myProfile.passive() * 255.0);
		b = (int) (myProfile.constructive() * 255.0);
		myColor = new Color (r,g,b);
		//G.drawHollowRoundRect(Color.BLUE);
		G.drawFastCircle(myColor);
		//System.out.println("Draw Actor TeamMeamber " + myProfile.name());
	}

	//System.out.println("step() TeamMemberActor " + myProfile.name());
}

