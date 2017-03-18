package SIAABMSimAgent;

import java.util.ArrayList;
import SIAABMSimSocial.Organization;
import SIAABMSimTask.Contract;
import SIAABMSimTask.TEOW;
import SIAABMSimTask.TEOW.TEOWPhase;
import SIAABMSimAgent.ActorProfile.RoleType;
import SIAABMSimConnection.EngineeringTask;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.ReviewTask;
import SIAABMSimConnection.ReworkTask;
import SIAABMSimConnection.Task;
import SIAABMSimConnection.Task.TaskState;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.polarBehaviour.BehaviourStyle;

import java.awt.Color;
import uchicago.src.sim.gui.SimGraphics;

public class ExpertActor extends Actor{

	private String interactionEvent;
	protected int lastInteraction;

	public ExpertActor(ActorProfile profile, Organization organization, int x, int y){
		myProfile = profile;
		myX = x;
		myY = y;
		myProfile.setOrganization(organization);
		pD = pD.getInstance();
		simS = simS.getInstance();
		fromInteractions = new ArrayList<Interaction>();
		toInteractions = new ArrayList<Interaction>();
		myTasks = new ArrayList<Task>();
		lastInteraction = 0;
		probabilityOfInteraction = simS.getInteractionFactor();
		cooperationFactor = simS.getCooperationFactor();
	}

	private void processInteraction(Interaction interaction, String message){
		Actor fromActor, toActor;
		fromActor = interaction.getFromActor();
		boolean isMyBoss;
		toActor = interaction.getToActor();
		isMyBoss = (fromActor == getBoss());
		
		if(!(message.equals("Accepted") || message.equals("NotAccepted"))){
			//System.out.println(myRole() + " " + getName() + " received " + message + " from " + fromActor.myRole() + " " + fromActor.getName());
		}
		
		//OneWay
		if(message.equals("OfferBonus")){

		}
		if(message.equals("Reprimand")){

		}
		if(message.equals("NoBonus")){

		}
		if(message.equals("Praise")){

		}
		if(message.equals("Bonus")){

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

		}
		if(message.equals("NotAccepted")){
			interaction.decreaseQuality();

		}
		
		if(fromActor != this){
			updateBehaviour(message, fromActor);
		}
		
		//if(interaction.getFromActor().myRole() == RoleType.Expert){
		if(message.equals("Accepted") || message.equals("NotAccepted")){
			//System.out.println(myRole() + " " + getName() + " received " + message + " from " + toActor.myRole() + " " + toActor.getName());
		}
		//}
	}

	public void interaction(Interaction interaction, String message){
		processInteraction(interaction, message);
	}

	private boolean inMyOrganization(Actor actor){
		if(actor.getOrganization() == this.getOrganization()){
			return true;
		}
		return false;
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
		//System.out.println(this.getRole() + " " + this.getName() + " get next task");
		if(myProfile.organization().isAdaptive()){
			//System.out.println(this.getRole() + " " + this.getName() + " Adaptive Organizaton");
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
					//System.out.println(this.getRole() + " " + this.getName() + 
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
						//System.out.println(this.getRole() + " " + this.getName() + 
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

	protected void processInteractions(){
		Interaction interaction;
		Actor actor;
		TeamLeaderActor teamLeader;
		BehaviourStyle myBehaviourStyle;
		String message;
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
			if(interaction.isActive()){
				actor = interaction.getToActor();
				if((actor.myRole() == RoleType.TeamMember || actor.myRole() == RoleType.TeamLeader ) && inMyOrganization(actor)){
					if(pD.drawChanceFromUniform(probabilityOfInteraction)){
						if(pD.drawChanceFromUniform(0.01)){
							if(pD.getChanceOfEvent("Start", myBehaviourStyle) && pD.getChanceOfEvent("OfferHelp", myBehaviourStyle)){
								message = "OfferHelp";
								actor.interaction(interaction, message);
								//interaction.request("OfferHelp");
								//System.out.println("----------- Expert " + myProfile.name() + " " + message +
								// 		" to " + actor.getRole() + " " + actor.getName());
							}
						}
					}

				}
			}
		}

	}

	private void progressTask(){

		//System.out.println(this.getRole() + " " + this.getName() + 
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
			System.out.println(this.getRole() + " " + this.getName() + 
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

	public void step() {

		int effort;

		interactionEvent = "";
		processInteractions();

		if(taskInProgress == null){
			getNextTask();
			if(taskInProgress == null){
				forgetting();
			}
			//else{
			//	learning();
			//}
		}
		else{
			//System.out.println(myProfile.name() + " " + myProfile.getBehaviourStyle() + 
			//		" Task Performance = " + df.format(reportPerformance()));

			learning();
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
						increaseProductiveEffort(effort);
						//increaseOverheadEffort(effort);
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
				effort = getWorkEffort() + getOvertimeEffort();
				if(taskInProgress != null){
					if(this.getChanceOfEvent("Working")){
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
		G.drawHollowRoundRect(Color.GREEN);
		G.drawFastCircle(myColor);
		//System.out.println("Draw Actor Expert " + myProfile.name());
	}

}
