package SIAABMSimAgent;

//07 Nov 2009: Implemented dynamic behaviour and dynamic Actor interaction. It is working,
//but it will require lots of tuning.

import SIAABMSimAgent.ActorProfile.RoleType;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.Task;
import SIAABMSimConnection.Task.TaskState;
import SIAABMSimSocial.Organization;
import SIAABMSimSocial.Department;
import SIAABMSimSocial.Team;
import SIAABMSimUtils.polarBehaviour;
import SIAABMSimTask.Contract;

import java.util.ArrayList;
import java.text.*;


public abstract class Actor extends Agent{

	protected ActorProfile myProfile;
	protected boolean allocated = false;
	protected Interaction interactionBoss;
	protected Interaction interactionExpert;
	protected ArrayList <Interaction> toInteractions;
	protected ArrayList <Interaction> fromInteractions;
	protected ArrayList <Task> myTasks;
	protected Task taskInProgress;
	protected Contract thisContract;

	protected int productiveEffortForThisStep = 0;
	protected int spentEffortForThisStep = 0;
	protected int overheadEffortForThisStep = 0;
	protected int availableEffortForThisStep = 12; // Every step the Actor has 12 units of Effort to spend.

	protected int productiveEffort = 0;
	protected int overheadEffort = 0;
	protected int spentEffort = 0;
	protected int extraEffort = 0;

	protected int workEffort;
	protected int overtimeEffort;
	protected int learnEffort;
	protected int askHelpEffort;
	protected int helpEffort;
	protected int totalEffort;

	protected int myEnergy = 50;

	protected double efficiency = 0.0;
	protected double qualityFactor = 1.0;
	protected boolean workHarder = false;
	
	protected double explicitReward;
	protected double implicitReward;
	protected boolean bonusReward;
	
	protected double probabilityOfInteraction;
	protected double cooperationFactor = 1.0;
	
	protected double eGap = 0.0;
	protected double kGap = 0.0;;

	
	private String decimalFormat(double x){
		return myProfile.decimalFormat(x);
	}
	
	protected void rewardModel(){
		if(pD.drawChanceFromUniform(0.5)){
			if(pD.drawChanceFromUniform(implicitReward)){
				bonusReward = false;
			}
			else{
				bonusReward = true;
			}
		}
		else{
			if(pD.drawChanceFromUniform(explicitReward)){
				bonusReward = true;
			}
			else{
				bonusReward = false;
			}
		}
	}
	
	public void interaction(Interaction interaction, String message){
		
	}
	
	public String myRewardPreference(){
		if(isAgressive()){
			return "Bonus";
		}
		if(isConstructive()){
			return "Recogniton";
		}
		if(pD.drawChanceFromUniform(0.5)){
			return "Bonus";
		}
		return "Recogniton";
	}
	
	protected String typeOfReward(String message){
		if(message.equals("OfferBonus")){
			return "Praise";
		}
		if(message.equals("Reprimand")){
			return "Punish";
		}
		if(message.equals("NoBonus")){
			return "Punish";
		}
		if(message.equals("Praise")){
			return "Praise";
		}
		if(message.equals("Bonus")){
			return "Praise";
		}
		return "Praise";
	}
	
	public void myBehavious(){
		myProfile.myMotivation();
	}
	

	public String getProfile(){
		return myProfile.getProfile();
	}

	public boolean workHarder(){
		return workHarder;
	}

	public void workHarder(boolean v){
		workHarder = v;
		if(workHarder){
			//System.out.println(getName() + " " + getRole() + " WORK HARDER");
		}
		else{
			//System.out.println(getName() + " " + getRole() + " RELAX");
		}
	}

	public Contract getContract(){
		return thisContract;
	}

	public double qualityFactor(){
		return qualityFactor;
	}

	public double getCostPerformance(){
		if(taskInProgress != null){
			return taskInProgress.getCostPerformance();
		}
		else{
			return -1.0;
		}
	}
	public double workEfficiency(){
		double spent;
		double extra;
		spent = (double) (productiveEffortForThisStep + overheadEffortForThisStep);
		if(productiveEffortForThisStep > 8){
			extra = productiveEffortForThisStep - 8;
			if(workHarder){
				efficiency = (double) ((productiveEffortForThisStep - extra) + 0.6 * extraEffort) / productiveEffortForThisStep;
			}
			else{
				efficiency = (double) ((productiveEffortForThisStep - extra) + 1.2 * extraEffort) / productiveEffortForThisStep;
			}
			if(efficiency > 1.0){
				qualityFactor = 1.0 / efficiency;
			}
			else{
				qualityFactor = 1.0;
			}
		}
		else{
			extra = 0;
			efficiency = 1.0;
			qualityFactor = 1.0;
		}
		return efficiency;

	}

	
	public double taskQuality(){
		if(taskInProgress != null){
			return taskInProgress.quality();
		}
		else{
			return 1.0;
		}
	}

	public boolean areYouMoreExperienced(Actor actor){
		if(this.getExperience() > actor.getExperience()){
			return true;
		}
		return false;
	}

	public boolean areYouMoreKnowledgeable(Actor actor){
		if(this.getKnowledge() > actor.getKnowledge()){
			return true;
		}
		return false;
	}
	public double workPerformance(){
		return (workEfficiency() * qualityFactor());
		//return (taskPerformance() * taskQuality());
	}

	public void setAttributes(String name, String orgName, String role,
			double behaviour, double experience, double knowledge, double motivation){
		if(name.equals(this.getName()) && role.equals(this.getRoleName())){
			this.setBehaviour(behaviour);
			this.setExperience(experience);
			this.setKnowledge(knowledge);
			this.setMotivation(motivation);	
		}
	}

	public boolean isActive(){
		TaskState state;
		for(int t = myTasks.size() -1; t >= 0; t--){
			state = myTasks.get(t).state();
			if(state == TaskState.ASSIGNED || state == TaskState.INPROGRESS){
				return true;
			}
		}
		return false;
	}


	//Inform Effort

	public double getIdealEffort(){
		double effort;
		effort = 0;
		for(int t=0; t<myTasks.size(); t++){
			effort = effort + myTasks.get(t).getIdealEffort();
		}
		return effort;
	}

	public double getPlannedEffort(){
		double effort;
		effort = 0;
		for(int t=0; t<myTasks.size(); t++){
			effort = effort + myTasks.get(t).getPlannedEffort();
		}
		return effort;
	}

	public double getActualEffort(){
		double effort;
		effort = 0;
		for(int t=0; t<myTasks.size(); t++){
			effort = effort + myTasks.get(t).getActualEffort();
		}
		return effort;
	}

	public double requiredKnowledge(){
		int nTasks;
		Task task;
		double rK, requiredK;
		nTasks = 0;
		requiredK = 0.0;
		for(int t=0; t<myTasks.size(); t++){
			task = myTasks.get(t);
			if(task.state() == TaskState.ASSIGNED || task.state() == TaskState.INPROGRESS){
				rK = task.requiredKnowledge();
				//if(rK > 0.0){
				nTasks = nTasks +1;
				requiredK = requiredK + rK;
				//}
			}
		}
		if(nTasks > 0){
			return requiredK/nTasks;
		}
		else{
			return 0.0;
		}
	}

	public double availableKnowledge(){
		int nTasks;
		Task task;
		double aK, availableK;
		nTasks = 0;
		availableK = 0.0;
		for(int t=0; t<myTasks.size(); t++){
			task = myTasks.get(t);
			if(task.state() == TaskState.ASSIGNED || task.state() == TaskState.INPROGRESS){
				aK = task.availableKnowledge(this);
				//if(aK > 0.0){
				nTasks = nTasks +1;
				availableK = availableK + aK;
				//}
			}
		}
		if(nTasks > 0){
			return availableK/nTasks;
		}
		else{
			return 0.0;
		}
	}

	public int getWorkEffort(){
		int e = workEffort;
		workEffort = 0;
		return e;
	}

	public int getOvertimeEffort(){
		int e = overtimeEffort;
		overtimeEffort = 0;
		return e;
	}

	public int getLearningEffort(){
		int e = learnEffort;
		learnEffort = 0;
		return e;
	}

	public int getAskHelpEffort(){
		int e = askHelpEffort;
		askHelpEffort = 0;
		return e;
	}

	public int getHelpEffort(){
		int e = helpEffort;
		//helpEffort = 0;
		return e;
	}

	protected void printDistributedEffort(){
		System.out.print("Distributed Effort " + getName() + " " + myRole() + " " + myBehaviourStyle() + 
				" WE = " + workEffort +
				" OE = " + overtimeEffort +
				" LE = " + learnEffort +
				" AE = " + askHelpEffort +
				" HE = " + helpEffort);
		if(extraEffort > 0){
			System.out.print(" xxx EXTRA Effort = " + extraEffort);
		}
		System.out.println("");
	}

	public void updateEffort(){
		Contract myBossContract = null;;
		Actor boss = null;
		
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
				if(this.getChanceOfEvent("Working")){
					workEffort = (int) (myProfile.workEffort() * availableEffortForThisStep);
				}
				if(this.getChanceOfEvent("Overtime") || workHarder){
					overtimeEffort = (int) (myProfile.overtimeEffort() * availableEffortForThisStep);
				}
			}
			else{
				if(this.getChanceOfEvent("Learning")){
					learnEffort = (int) (myProfile.learnEffort() * availableEffortForThisStep);
				}
				if(this.getChanceOfEvent("AskHelp")){
					askHelpEffort = (int) (myProfile.askHelpEffort() * availableEffortForThisStep);
				}
				if(this.getChanceOfEvent("Working")){
					workEffort = (int) (myProfile.workEffort() * availableEffortForThisStep);
				}
				if(this.getChanceOfEvent("Overtime") || workHarder){
					overtimeEffort = (int) (myProfile.overtimeEffort() * availableEffortForThisStep);
				}
			}
		}
		else{
			workEffort = 0;
		}
		if(this.getChanceOfEvent("OfferHelp")){
			helpEffort = (int) (myProfile.helpEffort() * availableEffortForThisStep);
		}
		totalEffort = workEffort + overtimeEffort + learnEffort + askHelpEffort + helpEffort;
		if(totalEffort > availableEffortForThisStep){
			extraEffort = extraEffort + (totalEffort - availableEffortForThisStep);
		}
		else{
			extraEffort = extraEffort - (availableEffortForThisStep - totalEffort)/2;
			if(extraEffort < 0){
				extraEffort = 0;
			}
		}
		if(totalEffort > 0){
			//printDistributedEffort();
		}
	}

	public int getProductiveEffort(){
		return productiveEffort;
	}

	public void increaseProductiveEffort(int n){
		Actor myBoss;
		Contract myBossContract;
		if(thisContract != null){
			productiveEffortForThisStep = productiveEffortForThisStep + n;
			productiveEffort = productiveEffort + n;
			thisContract.increaseProductiveEffort(n);
		}
		else{
			myBoss = getBoss();
			if(myBoss != null){
				myBossContract = myBoss.getContract();
				if(myBossContract != null){
					productiveEffortForThisStep = productiveEffortForThisStep + n;
					productiveEffort = productiveEffort + n;
					myBossContract.increaseProductiveEffort(n);
				}
				else{
					//System.out.println("^^^^^^^^^");
					//System.out.println("+++ I am " + myProfile.role() + " my Boss is " + getBoss().getRole() + " myContract is still NULL");
				}
			}
		}
	}


	public void increaseOverheadEffort(int n){
		Actor myBoss;
		Contract myBossContract;
		if(thisContract != null){
			overheadEffortForThisStep = overheadEffortForThisStep + n;
			overheadEffort = overheadEffort + n;
			thisContract.increaseOverheadEffort(n);
			//if(getRole() != RoleType.TeamMember){
			//System.out.println(getRole() + "---");
			//}
		}
		else{
			myBoss = getBoss();
			if(myBoss != null){
				myBossContract = getBoss().getContract();
				if(myBossContract != null){
					overheadEffortForThisStep = overheadEffortForThisStep + n;
					overheadEffort = overheadEffort + n;
					myBossContract.increaseOverheadEffort(n);
					//if(getRole() != RoleType.TeamMember){
					//System.out.println(getRole() + "*--");
					//}
				}
				else{
					//System.out.println("--- I am " + myProfile.role() + " my Boss is " + getBoss().getRole() + " myContract is still NULL");
				}
			}
			else{
				//System.out.println("--- I am " + myProfile.role() + " my Boss is NULL");
			}
		}
		//spentEffort = productiveEffort + overheadEffort;
	}

	public int getSpentEffort(){
		return (productiveEffort + overheadEffort);
	}

	public int myActualEffort(){
		return  (productiveEffort + overheadEffort);
	}

	public boolean isPassive(){
		return myProfile.isPassive();
	}

	public boolean isAgressive(){
		return myProfile.isAgressive();
	}

	public boolean isConstructive(){
		return myProfile.isConstructive();
	}

	public boolean getChanceOfEvent(String event){
		return myProfile.getChanceOfEevent(event);
	}
	
	public boolean getChanceOfEvent(String event, double factor){
		return myProfile.getChanceOfEevent(event, factor);
	}


	private Interaction bestInteraction(){
		int a;
		Interaction interaction;
		a = pD.getIntUniform(0, toInteractions.size()-1);
		if (a > 0){
			interaction = toInteractions.get(a);
			for(int i=a; i<toInteractions.size();i++){
				if(toInteractions.get(i).getToActor().isActive()){
					if(toInteractions.get(i).getQuality() > interaction.getQuality()){
						interaction = toInteractions.get(i);
					}
				}
			}
		}
		else{
			interaction = null;
		}
		return interaction;
	}

	public void setBoss(Actor boss){
		Interaction interaction;
		myProfile.setBoss(boss);
		for(int i=0; i<toInteractions.size(); i++){
			interaction = toInteractions.get(i);
			if(interaction.getToActor() == boss){
				interactionBoss = interaction;
				i = toInteractions.size();
			}
		}
	}

	protected void setBehaviour(double behaviour){
		myProfile.setBehaviour(behaviour);
	}

	public void setAgressive(){
		myProfile.setAggressive();
	}
	
	public void setDoer(){
		myProfile.setDoer();
	}

	public void setConstructive(){
		myProfile.setConstructive();
	}
	
	public void setNice(){
		myProfile.setNice();
	}

	public void setPassive(){
		myProfile.setPassive();
	}
	
	public void setNeutral(){
		myProfile.setNeutral();
	}

	protected void setExperience(double experience){
		myProfile.setExperience(experience);
	}

	protected void setKnowledge(double knowledge){
		myProfile.setKnowledge(knowledge);
	}

	protected void setMotivation(double motivation){
		myProfile.setMotivation(motivation);
	}

	public void addToInteraction(Interaction interaction){
		toInteractions.add(interaction);
	}

	public ArrayList <Interaction> ToInteractions(){
		return toInteractions;
	}

	public void addFromInteraction(Interaction interaction){
		fromInteractions.add(interaction);
	}

	public ArrayList <Interaction> FromInteractions(){
		return fromInteractions;
	}

	public void assignTask(Task task){
		task.assign(this); //The Task State is automatically changed to ASSIGNED.
		myTasks.add(task);
		//System.out.println("Task = " + task.taskType() + " assigned to " + myProfile.role() + " Actor " + myProfile.name());
	}

	public ArrayList<Task> myTasks(){
		return myTasks;
	}

	public String getName (){
		return myProfile.name();
	}

	public String getOrganizationName (){
		return myProfile.organizationName();
	}

	public void setOrganization(Organization organization){
		myProfile.setOrganization(organization);
	}

	public Organization getOrganization(){
		return myProfile.organization();
	}

	public void setDepartment(Department department){
		myProfile.setDepartment(department);
	}

	public Department getDepartment(){
		return myProfile.department();
	}

	public void setTeam(Team team){
		myProfile.setTeam(team);
	}

	public Team getTeam(){
		return myProfile.team();
	}

	public Actor getBoss(){
		return myProfile.boss();
	}
	
	public Actor getActor(){
		return this;
	}

	public RoleType myRole (){
		return myProfile.role();
	}

	private String getRoleName(){
		return myProfile.roleName();
	}

	public int roleWeight(){
		return myProfile.roleWeight();
	}

	public double getExperience (){
		return myProfile.experience();
	}

	public double getKnowledge (){
		return myProfile.knowledge();
	}

	public double getBehaviour (){
		return myProfile.behaviour();
	}

	public double getTeachingFactor(){
		return myProfile.myTeachingFactor();
	}

	public polarBehaviour.BehaviourStyle myBehaviourStyle(){
		return myProfile.getBehaviourStyle();
	}

	public double getConstructive (){
		return myProfile.constructive();
	}

	public double getPassive (){
		return myProfile.passive();
	}

	public double getAggressive (){
		return myProfile.aggressive();
	}

	public double getMotivation (){
		return myProfile.motivation();
	}

	public void allocated(boolean allocation){
		allocated = allocation;
	}
	public boolean isAllocated (){
		return allocated;
	}

	public void increaseAgressive(int n){
		//myProfile.increaseAggressive(n);
		//System.out.println("AAA Increase Aggressive");
	}

	public void increasePassive(int n){
		//myProfile.decreaseMotivation();
		//System.out.println("PPP Increase Passive");
	}
	
	public void increaseConstructive(int n){
		//myProfile.increaseConstructive(n);
		//myProfile.increaseMotivation();
		//System.out.println("CCC Increase Constructive");
	}
	
	public void updateBehaviour(String event, Actor fromActor){
		myProfile.updateBehaviour(event, fromActor);
	}

	public void increaseMotivation(){
		//myProfile.increaseMotivation();
		//System.out.println("MMMM Increase Motivation");
	}

	public void decreaseMotivation(){
		//myProfile.decreaseMotivation();
		//System.out.println("-------- Decrease Motivation");
	}

	public void increaseExperience(){
		myProfile.increaseExperience();
		//System.out.println("EEEE Increase Experience");
	}

	public void increaseExperience(double n){
		myProfile.increaseExperience(n);
		//System.out.println("EEEE Increase Experience");
	}

	public void increaseExperience(double k, double g){
		myProfile.increaseExperience(k, g);
		//System.out.println("EEEE Increase Experience");
	}

	public void decreaseExperience(){
		myProfile.decreaseExperience();
		//System.out.println("DEDEDE Decrease Experience");
	}

	public void increaseKnowledge(){
		myProfile.increaseKnowledge();
		//System.out.println("KKKK Increase Knowledge");
	}

	public void increaseKnowledge(double n){
		myProfile.increaseKnowledge(n);
		//System.out.println("KKKK Increase Knowledge");
	}

	public void increaseKnowledge(double k, double g){
		myProfile.increaseKnowledge(k, g);
		//System.out.println("KKKK Increase Knowledge");
	}

	public void decreaseKnowledge(){
		myProfile.decreaseKnowledge();
		//System.out.println("DKDKDK Decrease Knowledge");
	}

	public void forgetting(){
		if(this.getChanceOfEvent("Forgetting")){
			myProfile.decreaseExperience();
			myProfile.decreaseKnowledge();
			//demotivating();
			//System.out.println(myProfile.role() + " " + myProfile.name() + " is FORGETTING");
		}
	}

	public void demotivating(){
		if(this.getChanceOfEvent("Demotivated")){
			//myProfile.decreaseMotivation();
			if(this.getChanceOfEvent("Passive")){
				myProfile.increasePassive(10);
			}
		}
	}

	public void learning(){
		if(this.getChanceOfEvent("Learning")){
			myProfile.increaseExperience();
			myProfile.increaseKnowledge();
			//System.out.println(myProfile.role() + " " + myProfile.name() + " is LEARNING");
		}
	}
	
	public void printShortProfile(){
		myProfile.printShortProfile();
	}

	/*
	public void printShortProfile(){
		System.out.println("Actor " + getName() + " Role = " + getRole() + " X = " + getX() + " Y = " + getY());
	}

	public void printProfile(){
		myProfile.printProfile();
	}
	 */

	protected void processInteractions(){
		Interaction interaction;
		//System.out.println("**** Chance of Interaction");
		interaction = this.bestInteraction();
		if(interaction != null){
			interaction.request("Start");
			/*
			if(interaction.getFromActor().requestInteraction(interaction, "Start")){
				//System.out.println("######## Interaction Accepted");
				interaction.increaseQuality();
			}
			else{
				interaction.decreaseQuality();
			}
			 */
		}
		else{
			//System.out.println("############# processInteraction() Interaction is NULL");
		}
	}

	public void step() {
		//System.out.println("step() Actor");
	}

}
