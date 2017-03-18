package SIAABMSimAgent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import SIAABMSimSocial.Organization;
import SIAABMSimConnection.Dependency;
import SIAABMSimTask.Contract;
import SIAABMSimTask.TEOW;
import SIAABMSimTask.TEOW.TEOWPhase;
import SIAABMSimAgent.ActorProfile.RoleType;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.Task;
import SIAABMSimConnection.Task.TaskState;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimConnection.EngineeringTask;
import SIAABMSimConnection.ReviewTask;
import SIAABMSimConnection.ReworkTask;
import SIAABMSimConnection.LeadershipTask;
import SIAABMSimConnection.ManageDeliverable;
import SIAABMSimConnection.DevelopDeliverable;
import SIAABMSimConnection.ReviewDeliverable;
import SIAABMSimConnection.ReworkDeliverable;
import SIAABMSimConnection.ManageTEOW;
import SIAABMSimUtils.polarBehaviour.BehaviourStyle;
import SIAABMSimConnection.Task.TaskType;
import SIAABMSimUtils.probability;
import java.awt.Color;
import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimUtils.simSettings;

public class TeamLeaderActor extends Actor{

	protected ArrayList <TEOW> myTEOWList;
	protected ArrayList <TeamMemberActor> myTeamMemberList;
	protected ArrayList <ExpertActor> myExpertList;
	protected int nTeamMembers;
	protected int nextTeamMember; //Next TeamMember that will have a task allocated;
	protected int nExperts;
	protected int nextExpert;
	protected double expertRatio;
	protected ArrayList <Task> assignedTasks;
	protected ManagerActor myManager;
	//protected Task taskInProgress;
	protected String interactionEvent;
	protected int lastInteraction;
	protected double teamPerformance;
	protected double collectiveKnowledge;
	protected double collectiveExperience;

	public TeamLeaderActor(ActorProfile profile, Organization organization, int x, int y){
		myProfile = profile;
		myX = x;
		myY = y;
		pD = pD.getInstance();
		simS = simS.getInstance();
		df = new DecimalFormat("#.##");
		myProfile.setOrganization(organization);
		fromInteractions = new ArrayList<Interaction>();
		toInteractions = new ArrayList<Interaction>();
		myTEOWList = new ArrayList<TEOW>();
		myTeamMemberList = new ArrayList<TeamMemberActor>();
		myExpertList = new ArrayList<ExpertActor>();
		assignedTasks = new ArrayList<Task>();
		pD = pD.getInstance();
		nTeamMembers = 0;
		nextTeamMember = 0;
		nextExpert = 0;
		expertRatio = 0.0;
		myManager = null;
		myTasks = new ArrayList<Task>();
		taskInProgress = null;
		interactionEvent = "";
		lastInteraction = 0;
		teamPerformance = 0.0;
		collectiveKnowledge = 0.0;
		collectiveExperience = 0.0;
		explicitReward = simS.getExplicitReward();
		implicitReward = simS.getImplicitReward();
		probabilityOfInteraction = simS.getInteractionFactor();
		cooperationFactor = simS.getCooperationFactor();
		//System.out.print("TeamLeader ");
		//printBehaviour();
	}

	private void echoMessageToTeam(String message){
		Interaction interaction;
		Actor actor;
		TeamMemberActor tm;
		String myMessage = message;
		boolean chanceOfEvent = false;
		chanceOfEvent = myProfile.getChanceOfEevent(myMessage);

		for(int i=0; i<toInteractions.size(); i++){
			interaction = toInteractions.get(i);
			actor = interaction.getToActor();
			if(inMyTeam(actor)){
				if(actor.myRole() == RoleType.TeamMember){
					tm = (TeamMemberActor) actor;
					if(chanceOfEvent){
						tm.interaction(interaction, myMessage);
					}
				}
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

	public void interaction(Interaction interaction, String message){
		processInteraction(interaction, message);
		if(!message.equals("AskHelp")){
			if(interaction.getFromActor().myRole() == RoleType.Manager){
				echoMessageToTeam(message);
			}
		}
	}

	public void iterativeFeedback(){
		//System.out.println(myRole() + " " + getName() + " received Iterative FeedBack");
		Actor actor;
		for(int a=0; a<myTeamMemberList.size(); a++){
			if(pD.drawChanceFromUniform(2.0 * simS.getIterativeLearningFactor())){
				actor = myTeamMemberList.get(a);
				actor.increaseExperience(0.10);
				actor.increaseKnowledge(0.10);
			}
		}
	}

	private boolean inMyTeam(Actor actor){
		for(int a=0; a<myTeamMemberList.size(); a++){
			if(actor == myTeamMemberList.get(a)){
				return true;
			}
		}
		return false;
	}

	private void calculateTeamPerformance(){
		double tp;
		TeamMemberActor tm;
		int i;
		i = 0;
		tp = 0.0;
		for(int a=0; a<myTeamMemberList.size(); a++){
			tm = (TeamMemberActor) myTeamMemberList.get(a);
			if(tm.isActive()){
				i = i +1;
				tp = tp + tm.workPerformance();
			}
		}
		if(i>0){
			teamPerformance = tp / i;
		}
		//System.out.println("TeamLeader " + myProfile.name() + " Team Performance = " + df.format(teamPerformance));
	}

	private void calculateCollectiveExperienceAndKnowledge(){
		double tp;
		double e, k;
		Actor actor;
		TeamMemberActor tm;
		ExpertActor exp;
		int teamSize;
		teamSize = myTeamMemberList.size();
		tp = 0.0;
		e = 0.0;
		k = 0.0;
		for(int a=0; a<teamSize; a++){
			actor = myTeamMemberList.get(a);
			if(actor.myRole() == RoleType.Expert){
				System.out.println("expert in the team");
			}
			e = e + actor.getExperience();
			k = k + actor.getKnowledge();
		}
		e = e + this.getExperience();
		k = k + this.getKnowledge();
		collectiveExperience = e / teamSize + 1;
		collectiveKnowledge = k / teamSize + 1;

		//System.out.println("TeamLeader " + myProfile.name() + " Team Performance = " + df.format(teamPerformance));
	}

	/*
	public double getCollectiveExperience(){
		calculateCollectiveExperienceAndKnowledge();
		return collectiveExperience;
	}

	public double getCollectiveKnowledge(){
		calculateCollectiveExperienceAndKnowledge();
		return collectiveKnowledge;
	}

	public double getTeamPerformance(){
		return teamPerformance;
	}
	 */

	public double getCostPerformance(){
		if(taskInProgress != null){
			return taskInProgress.getCostPerformance();
		}
		else{
			return -1.0;
		}
	}

	protected void processInteractions(){
		Interaction interaction;
		Actor actor;
		TeamMemberActor teamMember;
		BehaviourStyle myBehaviourStyle;
		String message;
		double q, wp;
		int n, s;
		//System.out.println("TeamLeader " + myProfile.name() + " processInteractions");

		if(myManager != null){
			if(myManager.isAgressive()){
				if(this.isConstructive()){
					if(pD.drawChanceFromUniform(0.2)){
						this.setAgressive();
					}
					else{
						if(pD.drawChanceFromUniform(0.05)){
							this.setPassive();
						}
					}
				}
			}
			if(myManager.isConstructive()){
				if(pD.drawChanceFromUniform(0.1)){
					this.setConstructive();
				}
			}
			if(myManager.isPassive()){
				if(this.isConstructive()){
					if(pD.drawChanceFromUniform(0.1)){
						this.setPassive();
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
				if(interaction.isActive()){
					if(pD.drawChanceFromUniform(probabilityOfInteraction)){
						actor = interaction.getToActor();
						if(inMyTeam(actor)){
							if(actor.myRole() == RoleType.TeamMember){
								//System.out.println("TeamLeader " + myProfile.name() + " found TeamMember to interact");
								teamMember = (TeamMemberActor) actor;
								wp = teamMember.workPerformance();
								if(this.getChanceOfEvent("Start")){
									rewardModel();
									if( wp < 0.7){
										if(getChanceOfEvent("Aggressive")){
											myProfile.increaseAggressive(3);
										}
										else{
											if(getChanceOfEvent("Constructive")){
												myProfile.increaseConstructive(1);
											}
										}
										if(this.getChanceOfEvent("OfferHelp")){
											message = "OfferHelp";
											teamMember.interaction(interaction, message);
											//interaction.request(message);
											//System.out.println("TeamLeader " + myProfile.name() + " " + message);
										}
										else{
											if(this.getChanceOfEvent("Reprimand")){
												message = "Reprimand";
												//*****teamMember.interaction(interaction, message);
												//interaction.request(message);
												//System.out.println("TeamLeader " + myProfile.name() + " " + message);
											}
										}
									}
									else{
										if(getChanceOfEvent("Constructive")){
											myProfile.increaseConstructive(1);
										}
										if(this.getChanceOfEvent("Praise")){
											message = "Praise";
											//*****teamMember.interaction(interaction, message);
											//interaction.request(message);
											//System.out.println("TeamLeader " + myProfile.name() + " " + message);
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

	protected void findMyTeamMembers(){
		Actor actor;
		for(int a=0; a<toInteractions.size(); a++){
			actor = (Actor) toInteractions.get(a).getToAgent();
			if(actor.getBoss() == this){
				if((actor.myRole() == RoleType.TeamMember)){
					myTeamMemberList.add((TeamMemberActor) actor);
				}
				if((actor.myRole() == RoleType.Expert)){
					myExpertList.add((ExpertActor) actor);
				}
			}
			else{
				//System.out.println("Remote Actor: not in my Team");
			}
		}
		//System.out.println("TeamLeader " + myProfile.name() + " found " + myTeamMemberList.size() + " TeamMembers");
	}

	protected void findMyManager(){
		Actor actor;
		boolean foundManager = false;

		for(int a=0; a<toInteractions.size(); a++){
			actor = (Actor) toInteractions.get(a).getToAgent();
			if(actor.myRole() == RoleType.Manager){
				myManager = (ManagerActor) actor;
				setBoss(myManager);
				//System.out.println("TeamLeader " + myProfile.name() + " found Manager " + myManager.getName());
				a = fromInteractions.size();
				foundManager = true;
			}
		}
		if(!foundManager){
			//System.out.println("!!!!!!!!!!! TeamLeader DID NOT find its Manager");
		}
	}

	protected void assignTaskToNextExpert(Task task){
		int TE;
		TaskType taskType;
		Actor actor;
		EngineeringTask engineeringTask;
		ReviewTask reviewTask;
		ReworkTask reworkTask;
		//System.out.println("TL assignTaskToNextTeamMember()");
		taskType = task.taskType();
		if(nExperts > 0){
			TE = nextExpert;
			actor = myExpertList.get(TE);
			//actor.assignTask(task);
			//assignedTasks.add(task);

			if(actor.getBoss() != this){
				//System.out.println("TL assignTaskTo Remote TeamMember");
			}

			switch(taskType){
			case Engineering:{
				engineeringTask = (EngineeringTask) task;
				engineeringTask.getTEOW().estimate(this); // TL estimates the task
				actor.assignTask(task);
				assignedTasks.add(task);
				//System.out.println("TeamLeader " + myProfile.name() + 
				//		" ASSIGNED " + task.taskType() + " task to " + actor.myRole() + " " + actor.getName());

				break;
			}
			case Review:{
				reviewTask = (ReviewTask) task;
				reviewTask.getTEOW().estimateReview(this); // TL estimates the task
				actor.assignTask(task);
				assignedTasks.add(task);
				break;
			}
			case Rework:{
				reworkTask = (ReworkTask) task;
				reworkTask.getTEOW().estimateRework(this); // TL estimates the task
				//System.out.println("TL Asssign REWORK Task to TM = " + actor.getName());
				actor.assignTask(task);
				assignedTasks.add(task);
				//System.out.println("TeamLeader " + myProfile.name() + 
				//		" ASSIGNED " + task.taskType() + " task to " + actor.myRole() + " " + actor.getName());
				break;
			}
			}

			TE = TE +1;
			if(TE == nExperts){
				TE = 0;
			}
			nextExpert = TE;
		}
	}

	protected void assignTaskToNextTeamMember(Task task){
		int TM;
		TaskType taskType;
		Actor actor;
		EngineeringTask engineeringTask;
		ReviewTask reviewTask;
		ReworkTask reworkTask;
		if(nTeamMembers > 0){
			//System.out.println("TL assignTaskToNextTeamMember()");
			taskType = task.taskType();
			TM = nextTeamMember;
			actor = myTeamMemberList.get(TM);
			//actor.assignTask(task);
			//assignedTasks.add(task);

			if(actor.getBoss() != this){
				//System.out.println("TL assignTaskTo Remote TeamMember");
			}

			switch(taskType){
			case Engineering:{
				engineeringTask = (EngineeringTask) task;
				engineeringTask.getTEOW().estimate(this); // TL estimates the task
				actor.assignTask(task);
				assignedTasks.add(task);
				//System.out.println("TeamLeader " + myProfile.name() + 
				//		" ASSIGNED " + task.taskType() + " task to " + actor.myRole() + " " + actor.getName());

				break;
			}
			case Review:{
				reviewTask = (ReviewTask) task;
				reviewTask.getTEOW().estimateReview(this); // TL estimates the task
				actor.assignTask(task);
				assignedTasks.add(task);
				break;
			}
			case Rework:{
				reworkTask = (ReworkTask) task;
				reworkTask.getTEOW().estimateRework(this); // TL estimates the task
				//System.out.println("TL Asssign REWORK Task to TM = " + actor.getName());
				actor.assignTask(task);
				assignedTasks.add(task);
				//System.out.println("TeamLeader " + myProfile.name() + 
				//		" ASSIGNED " + task.taskType() + " task to " + actor.myRole() + " " + actor.getName());
				break;
			}
			}

			TM = TM +1;
			if(TM == nTeamMembers){
				TM = 0;
			}
			nextTeamMember = TM;
		}
	}

	protected void checkProgress(Task task){

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
		//System.out.println("TL processMyTasks()");
		if(myManager == null){
			//System.out.println("MMMMMMMMMMMMMMMMMMMM");
			findMyManager();
		}
		if(nTeamMembers == 0){
			findMyTeamMembers();
			nTeamMembers = myTeamMemberList.size();
			nExperts = myExpertList.size();
			if(nTeamMembers > 0){
				expertRatio = (double) nExperts / (double) nTeamMembers;
			}
		}
		//System.out.println(this.myRole() + " " + this.getName() + " ProcessMyTasks()");
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
						teow.effortFactor(ddTask.getEffortFactor());
						engineeringTask = new EngineeringTask(teow);
						assignTaskToNextTeamMember(engineeringTask);
						ddTask.addTask(engineeringTask);
					}
					ddTask.isInProgress();
					taskInProgress = ddTask;
					break;
				}
				case INPROGRESS:{
					//System.out.println("11111 INPROGRESS");
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
								//if(taskTM.getRevealedError() > 0.0){
								//	reworkTask = new ReworkTask(taskTM.getTEOW());
								//	assignTaskToNextTeamMember(reworkTask);
								//	ddTask.addTask(reworkTask);
								//}
								break;
							}
							case Rework:{
								taskTM.isComplete();
								//if(taskTM.getError() > 0.05){
								//	reviewTask = new ReviewTask(taskTM.getTEOW());
								//	assignTaskToNextTeamMember(reviewTask);
								//	ddTask.addTask(reviewTask);
								//}
								break;
							}
							}
						}
					}
				}
				break;
				}
				deliverable = ddTask.getDeliverable();
				thisContract = deliverable.getContract();
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
								teow.effortFactor(taskTM.getEffortFactor());
								reviewTask = new ReviewTask(teow);
								if(pD.drawChanceFromUniform(expertRatio)){
									assignTaskToNextExpert(reviewTask);
									//System.out.println("*** Assigned Review Task to Expert");
								}
								else{
									assignTaskToNextTeamMember(reviewTask);
									//System.out.println("Assigned Review Task to TeamMember");
								}
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
				deliverable = rvTask.getDeliverable();
				thisContract = deliverable.getContract();
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
								//System.out.println("Revealed Error = " + df.format(reviewTask.getRevealedError()));
								teow = reviewTask.getTEOW();
								teow.effortFactor(taskTM.getEffortFactor());
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
				thisContract = deliverable.getContract();
				if(rwTask.allAssignedTasksAreComplete()){
					rwTask.isComplete();
				}
				break;
			}
			}
		}
	}


	public boolean taskInProgress(){
		Task task;
		for(int t=0; t<assignedTasks.size(); t++){
			task = assignedTasks.get(t);
			if(task.state() == TaskState.INPROGRESS){
				return true;
			}
		}
		return false;
	}

	public boolean isActive(){
		if(taskInProgress()){
			return true;
		}
		else{
			return false;
		}
	}


	public void assignTask(Task task){
		ManageTEOW thisTask;
		task.assign(this); //The Task State is automatically changed to ASSIGNED.
		myTasks.add(task);
		//System.out.println("Task = " + task.taskType() + " assigned to " + this.myRole() + " Actor " + myProfile.name());
	}

	public void draw(SimGraphics G){
		int r, g, b;
		r = (int) (myProfile.aggressive() * 255.0);
		g = (int) (myProfile.passive() * 255.0);
		b = (int) (myProfile.constructive() * 255.0);
		myColor = new Color (r,g,b);
		G.drawHollowFastRect(Color.BLUE);
		G.drawFastCircle(myColor);
		//System.out.println("Draw Actor TeamLeader " + myProfile.name());
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
		double b, m;
		Interaction interaction;
		int effort;
		effort = getWorkEffort() + getOvertimeEffort();

		//Following is what TeamLeaders really do.
		if(taskInProgress == null){
			forgetting();
		}
		else{
			learning();
		}
		processMyTasks();
		if(taskInProgress()){
			//System.out.println("TL TaskInProgress");
			//increaseOverheadEffort(effort - 2);
			//increaseProductiveEffort(getAvailableEffortForThisStep());
			increaseExperience();
			processInteractions();
			calculateTeamPerformance();
		}
		if(this.getChanceOfEvent("Learning")){
			effort = getLearningEffort();
			//increaseOverheadEffort(2);
			increaseKnowledge();
		}
		//System.out.println("step() TeamLeaderActor " + myProfile.name());
	}

}
