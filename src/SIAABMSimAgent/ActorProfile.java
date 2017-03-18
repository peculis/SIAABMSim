package SIAABMSimAgent;

//07 Nov 2009: Implemented dynamic behaviour and dynamic Actor interaction. It is working,
//but it will require lots of tuning.

import java.text.DecimalFormat;

import SIAABMSimUtils.polarBehaviour;
import SIAABMSimUtils.probability;
import SIAABMSimConnection.Interaction;
import SIAABMSimSocial.Organization;
import SIAABMSimSocial.Department;
import SIAABMSimSocial.Team;
import SIAABMSimUtils.simSettings;

public class ActorProfile {
	public enum RoleType {CEO, SeniorManager, Manager, TeamLeader, TeamMember, Expert, Consultant, Generic};
	private String myName; //Agent ID or Name
	private String myOrganizationName; //Agent's Organization Name
	private String myRoleName;
	private Organization myOrganization;
	private Department myDepartment;
	private Team myTeam;
	private Actor myBoss;
	private Actor mySelf;
	private RoleType myRole;
	private int roleWeight;
	private polarBehaviour myBehaviour;
	private double myInitialExperience;
	private double myExperience;
	private	double myInitialKnowledge;
	private double myKnowledge;
	private double myInitialMotivation;
	private double myMotivation;
	private double myLearningFactor;
	private double myTeachingFactor;
	private probability pD;
	private simSettings simS;
	private boolean IamBoss;
	private boolean IamSeniorManager;
	private boolean IamManager;
	private boolean IamTL;
	private boolean IamExpert;

	private double workEffort;
	private double overtimeEffort;
	private double learnEffort;
	private double askHelpEffort;
	private double helpEffort;

	private double learningFactor;

	private DecimalFormat df;


	public ActorProfile(String name, String organization, String role, double behaviour, double experience, double knowledge, double motivation){
		pD = probability.getInstance();
		simS = simSettings.getInstance();
		df = new DecimalFormat("#.##");
		myName = name;
		myOrganizationName = organization;
		myRoleName = role;
		myBehaviour = new polarBehaviour(behaviour);
		myInitialExperience = experience;
		myExperience = experience;
		myInitialKnowledge = knowledge;
		myKnowledge = knowledge;
		myInitialMotivation = motivation;
		myMotivation = motivation;
		IamBoss = false;
		IamSeniorManager = false;
		IamManager = false;
		IamTL = false;
		IamExpert = false;

		myRole = RoleType.TeamMember;

		learningFactor = simS.getLearningFactor();
		myLearningFactor = myLearningFactor();

		//System.out.println("Agent Role: " + agentRole);
		if (role.equals("CEO")) {
			myRole = RoleType.CEO;
			IamBoss = true;
			IamSeniorManager = true;
			roleWeight = 10;
		}
		if (role.equals("SeniorManager")) {
			myRole = RoleType.SeniorManager;
			IamBoss = true;
			IamSeniorManager = true;
			roleWeight = 10;
		}
		if (role.equals("Manager")) {
			myRole = RoleType.Manager;
			IamBoss = true;
			IamManager = true;
			roleWeight = 15;
		}
		if (role.equals("Expert")) {
			myRole = RoleType.Expert;
			IamBoss = false;
			IamExpert = true;
			roleWeight = 5;
		}
		if (role.equals("TeamLeader")) {
			myRole = RoleType.TeamLeader;
			IamBoss = false;
			IamTL = true;
			roleWeight = 5;
		}
		if (role.equals("TeamMember")) {
			myRole = RoleType.TeamMember;
			IamBoss = false;
			roleWeight = 1;
		}
	}


	public String getProfile(){
		String profile;
		profile = myName + "\t" + myOrganizationName + "\t" + myRole + "\t" +
		df.format(myBehaviour.getBehaviour()) + "\t" +
		df.format(myKnowledge) + "\t" +
		df.format(myExperience) + "\t" +
		df.format(myMotivation) + "\n";
		return profile;
	}

	public String name(){
		return myName;
	}

	public boolean isAgressive(){
		return myBehaviour.isAgressive();
	}

	public boolean isConstructive(){
		return myBehaviour.isConstructive();
	}

	public boolean isPassive(){
		return myBehaviour.isPassive();
	}

	public String organizationName(){
		return myOrganizationName;
	}

	public void setOrganization(Organization organization){
		myOrganization = organization;
	}
	public Organization organization(){
		return myOrganization;
	}

	public double perceivedSocialMood(){
		double mood;
		mood = myOrganization.socialMood();
		if(mood > 0){
			return mood;
		}
		return 0.1;
	}

	public void setDepartment(Department department){
		myDepartment = department;
	}

	public Department department(){
		return myDepartment;
	}

	public void setTeam(Team team){
		myTeam = team;
	}

	public Team team(){
		return myTeam;
	}

	public void setBoss(Actor boss){
		if(boss != null){
			myBoss = boss;
		}
		else{
			//System.out.println(myRole + " setBoss NULL actor");
		}
	}

	public Actor boss(){
		return myBoss;
	}

	public RoleType role(){
		return myRole;
	}

	public String roleName(){
		return myRoleName;
	}

	public int roleWeight(){
		return roleWeight;
	}

	protected void setBehaviour(double behaviour){
		myBehaviour.setBehaviour(behaviour);
	}


	public polarBehaviour.BehaviourStyle getBehaviourStyle(){
		return myBehaviour.getBehaviourStyle();
	}

	public void printBehaviour(){
		myBehaviour.printBehaviour();
	}

	public double interactionProbablility(){
		return pD.getInteractionProbability(getBehaviourStyle());
	}

	public boolean getChanceOfEevent(String event){
		boolean result;
		if(event.equals("Learning")){
			if(pD.drawChanceFromUniform(learningFactor)){
				return pD.getChanceOfEvent(event, getBehaviourStyle());
			}
			else{
				return false;
			}
		}
		result = pD.getChanceOfEvent(event, getBehaviourStyle());
		if(result){
			//System.out.println(event + " " + result);
		}
		return result;
	}

	public boolean getChanceOfEevent(String event, double factor){
		boolean result;
		if(event.equals("Learning")){
			if(pD.drawChanceFromUniform(learningFactor)){
				return pD.getChanceOfEvent(event, getBehaviourStyle());
			}
			else{
				return false;
			}
		}
		result = pD.getChanceOfEvent(event, factor, getBehaviourStyle());
		if(result){
			//System.out.println(event + " " + result);
		}
		return result;
	}


	public double workEffort(){
		return workEffort;
	}

	public double overtimeEffort(){
		return overtimeEffort;
	}

	public double learnEffort(){
		return learnEffort;
	}

	public double askHelpEffort(){
		return askHelpEffort;
	}

	public double helpEffort(){
		return helpEffort;
	}

	private double getEffort(String event){
		return pD.getEffort(event, getBehaviourStyle());
	}

	public String decimalFormat(double x){
		return df.format(x);
	}

	private void printDistributedEffort(){
		System.out.println("*** Distributed Effort " + myName + " " + getBehaviourStyle() + 
				" WE = " + df.format(workEffort) +
				" OE = " + df.format(overtimeEffort) +
				" LE = " + df.format(learnEffort) +
				" AE = " + df.format(askHelpEffort) +
				" HE = " + df.format(helpEffort) );
	}

	public void distributeEffort(){
		workEffort = getEffort("WorkEffort");
		overtimeEffort = getEffort("OvertimeEffort");;
		learnEffort = getEffort("LearnEffort");;
		askHelpEffort = getEffort("AskHelpEffort");
		helpEffort = getEffort("HelpEffort");
		//printDistributedEffort();
	}


	public polarBehaviour getPolarBehaviour(){
		return myBehaviour;
	}

	public double behaviour(){
		return myBehaviour.getBehaviour();
	}

	public int behaviourIndex(){
		return myBehaviour.getBehaviourIndex();
	}

	public double constructive(){
		return myBehaviour.getConstructive();
	}

	public double aggressive(){
		return myBehaviour.getAggressive();
	}

	public double passive(){
		return myBehaviour.getPassive();
	}

	public void setExperience(double experience){
		myExperience = experience;
	}

	public double experience(){
		return myExperience;
	}

	public void setKnowledge(double knowledge){
		//System.out.println("sK");
		myKnowledge = knowledge;
	}

	public double knowledge(){
		return myKnowledge;
	}

	public void setMotivation(double motivation){
		myMotivation = motivation;
	}

	public double motivation(){
		return myMotivation;
	}

	private double myLearningFactor(){
		myLearningFactor =  1.5 * myBehaviour.getConstructive() + 1.0 * myBehaviour.getAggressive() + 
		0.5 * myBehaviour.getPassive() + 1.0 * myMotivation;
		return myLearningFactor;
	}

	public double myTeachingFactor(){
		myTeachingFactor =  1.5 * myBehaviour.getConstructive() + 1.0 * myBehaviour.getAggressive() + 
		0.5 * myBehaviour.getPassive() + 1.0 * myMotivation;
		return myTeachingFactor;
	}


	public void increaseExperience(){
		double deltaE;
		//deltaE = simS.getLearningFactor() * 0.05 * myLearningFactor * simS.getExperienceChangeRate();
		//deltaE = 0.5 * simS.getLearningFactor() * myLearningFactor * simS.getExperienceChangeRate();
		deltaE =  0.1 * simS.getLearningFactor() * simS.getExperienceChangeRate();
		myExperience = myExperience + deltaE;
		if(myExperience > 1.0){
			myExperience = 1.0;
		}
		//myExperience = 1.0;
	}

	public void increaseExperience(double teachingFactor){
		double myLF = myLearningFactor();
		double deltaE;
		//deltaE = simS.getLearningFactor() * teachingFactor * 0.05 * myLearningFactor * simS.getExperienceChangeRate();
		//deltaE = 0.5 * simS.getLearningFactor() * teachingFactor * myLearningFactor * simS.getExperienceChangeRate();
		deltaE =  10.0 * simS.getCooperationFactor() * simS.getExperienceChangeRate();
		myExperience = myExperience + deltaE;
		if(myExperience > 1.0){
			myExperience = 1.0;
		}
		//myExperience = 1.0;
	}

	public void increaseExperience(double teachingFactor, double experienceGap){
		double myLF = myLearningFactor();
		double deltaE;
		//deltaE = 1.0 * simS.getCooperationFactor() * teachingFactor * myLearningFactor * simS.getExperienceChangeRate() * experienceGap;
		//deltaE =  50.0 * simS.getCooperationFactor() * simS.getExperienceChangeRate() * experienceGap;
		deltaE =  0.5 * simS.getCooperationFactor() * experienceGap;
		//System.out.println(myRoleName + " ++E " + deltaE);
		myExperience = myExperience + deltaE;
		if(myExperience > 1.0){
			myExperience = 1.0;
		}
		//myExperience = 1.0;
	}

	public void decreaseExperience(){
		//Decreasing Experience will also use the ForgettingRate - 10 Nov 2010
		//myExperience = myExperience - 0.1 * simS.getForgettingRate();
		if(myExperience < myInitialExperience){
			myExperience = myInitialExperience;
		}
		//myExperience = 1.0;
	}

	public void increaseKnowledge(){
		double myLF = myLearningFactor();
		double deltaK;
		//deltaK = simS.getLearningFactor() *  0.05 * myLearningFactor * simS.getLearningRate();
		deltaK = 0.0 * simS.getLearningFactor() *  myLearningFactor * simS.getLearningRate();
		deltaK = 0.1 * simS.getLearningFactor() * simS.getLearningRate();
		//System.out.println("L " + myRoleName + " ++K " + deltaK);
		myKnowledge = myKnowledge + deltaK;
		if(deltaK > 0){
			//System.out.println(myRoleName + "1 ++K " + deltaK);
		}
		if(myKnowledge > 1.0){
			myKnowledge = 1.0;
		}
		//myKnowledge = 1.0;
	}

	public void increaseKnowledge(double teachingFactor){
		double myLF = myLearningFactor();
		double deltaK;
		//deltaK = simS.getLearningFactor() * teachingFactor * 0.05 * myLearningFactor * simS.getLearningRate();
		//deltaK = 0.5 * simS.getLearningFactor() * teachingFactor * myLearningFactor * simS.getLearningRate();
		deltaK = 0.0 * simS.getLearningFactor() * simS.getLearningRate();
		deltaK = 10.0 * simS.getCooperationFactor() * simS.getLearningRate();
		//System.out.println("TT " + myRoleName + " ++K " + deltaK);
		myKnowledge = myKnowledge + deltaK;
		if(deltaK > 0){
			//System.out.println(myRoleName + "2 ++K " + deltaK);
		}
		if(myKnowledge > 1.0){
			myKnowledge = 1.0;
		}
		//myKnowledge = 1.0;
	}

	public void increaseKnowledge(double teachingFactor, double knowledgeGap){
		double myLF = myLearningFactor();
		double deltaK;
		//deltaK = 1.0 * simS.getCooperationFactor() * teachingFactor * myLearningFactor * simS.getLearningRate() * knowledgeGap;
		//deltaK = 0.5 * simS.getCooperationFactor() * simS.getLearningRate() * knowledgeGap;
		//deltaK = 50.0 * simS.getCooperationFactor() * simS.getLearningRate() * knowledgeGap;
		deltaK = 0.5 * simS.getCooperationFactor() * knowledgeGap;
		myKnowledge = myKnowledge + deltaK;
		if(deltaK > 0){
			//System.out.println(myRoleName + "3 ++K " + deltaK);
		}
		if(myKnowledge > 1.0){
			myKnowledge = 1.0;
		}
		//myKnowledge = 1.0;
	}

	public void decreaseKnowledge(){
		//myKnowledge = myKnowledge - 0.1 * simS.getForgettingRate();
		if(myKnowledge < myInitialKnowledge){
			myKnowledge = myInitialKnowledge;
		}
		//myKnowledge = 1.0;
	}

	public void increaseMotivation(){
		if(getChanceOfEevent("Motivated")){
			myMotivation = myMotivation + (perceivedSocialMood() * simS.getMotivationChangeRate());
			//myMotivation = myMotivation + 1.0 * simS.getMotivationChangeRate();
			if(myMotivation > 1.2){
				myMotivation = 1.2;
				//System.out.println("+++ MMM = 1.0");
			}
			else{
				//System.out.println("++++++ MMM");
			}
			//increaseConstructive(2);
		}
	}
	public void increaseMotivation(int n){
		if(getChanceOfEevent("Motivated")){
			myMotivation = myMotivation + (perceivedSocialMood() * n * simS.getMotivationChangeRate());
			//System.out.println("+++ MMM");
			if(myMotivation > 1.2){
				myMotivation = 1.2;
				//System.out.println("+++ MMM = 1.2");
			}
			else{
				//System.out.println("++++++ MMM");
			}
			//increaseConstructive(2);
		}
	}

	public void decreaseMotivation(){
		if(getChanceOfEevent("Demotivated")){
			//myMotivation = myMotivation - ((1.0 - perceivedSocialMood()) * simS.getMotivationChangeRate());
			myMotivation = myMotivation - simS.getMotivationChangeRate();
			if(myMotivation < 0.4){
				myMotivation = 0.4;
				//System.out.println("--- MMM = 0.4");
			}
			else{
				//System.out.println("--- MMM");
			}
			//increasePassive(10);
			//increaseConstructive(-1);
		}
	}

	public void decreaseMotivation(int n){
		if(getChanceOfEevent("Demotivated")){
			//myMotivation = myMotivation - ((1.0/perceivedSocialMood()) * (n * simS.getMotivationChangeRate()));
			myMotivation = myMotivation - ((1.0 - perceivedSocialMood()) * n * simS.getMotivationChangeRate());
			//System.out.println("--- MMM");
			if(myMotivation < 0.6){
				myMotivation = 0.6;
				//System.out.println("--- MMM = 0.4");
			}
			else{
				//System.out.println("--- MMM");
			}
			//increasePassive(10);
			//increaseConstructive(-1);
		}
	}



	public void increaseAggressive(int n){
		String s;
		double increment;
		increment = n * 0.1;
		//System.out.println("AAA Increase Aggressive");
		s = "Aggressive";
		if(IamBoss){
			s = "BAggressive";
		}
		if(getChanceOfEevent(s)){
			//System.out.println("AAA Increase Aggressive");
			myBehaviour.increaseAggressive(increment);
		}
		//increaseConstructive(-1);
	}

	public void increaseConstructive(int n){
		String s;
		double increment;
		increment = n * 0.2;
		s = "Constructive";
		//System.out.println("CCC Increase Constructive");
		if(IamBoss){
			s = "BConstructive";
		}
		if(getChanceOfEevent(s)){
			//System.out.println("CCC Increase Constructive");
			myBehaviour.increaseConstructive(increment * perceivedSocialMood());
			//myBehaviour.increaseConstructive(n);
			increaseMotivation();
			//increaseMotivation();
			//increaseMotivation();
			//increaseMotivation();
		}
		//increasePassive(-1);
	}

	public void increasePassive(int n){
		String s;
		double increment;
		increment = n * 0.2;
		//System.out.println("PPP Increase Passive");
		s = "Passive";
		if(IamBoss){
			s = "BPassive";
		}
		if(getChanceOfEevent(s)){
			//System.out.println("+++PPP");
			//System.out.println("PPP Increase Passive");
			myBehaviour.increasePassive(increment / perceivedSocialMood());
			//myBehaviour.increasePassive(n);
			decreaseMotivation();
			decreaseMotivation();
		}
		//increaseConstructive(-1);
	}

	public void setAggressive(){
		myBehaviour.setAgressive();
	}

	public void setDoer(){
		myBehaviour.setDoer();
	}

	public void setConstructive(){
		myBehaviour.setConstructive();
	}

	public void setNice(){
		myBehaviour.setNice();
	}

	public void setPassive(){
		myBehaviour.setPassive();
	}

	public void setNeutral(){
		myBehaviour.setNeutral();
	}

	protected boolean IamBoss(){
		return (myRole == RoleType.CEO || myRole == RoleType.SeniorManager || myRole == RoleType.Manager || myRole == RoleType.TeamLeader);
	}

	protected boolean isMyBoss(Actor actor){
		if(actor == null || myBoss == null){
			//System.out.println("-------------------- My Boss is NULL");
			return false;
		}
		else{
			if(myBoss.getName().equals(actor.getName())){
				//System.out.println("++++++++++++++++++ Is MyBoss");
				return true;
			}
			return false;
		}
	}

	public void printProfile(){
		System.out.println("Actor " + myName + 
				" Role = " + myRoleName + 
				" B = " + df.format(myBehaviour.getBehaviour()) +
				" E = " + df.format(myExperience) +
				" K = " + df.format(myKnowledge) +
				" M = " + df.format(myMotivation));
	}

	public void printShortProfile(){
		System.out.println(myName + " " + myRoleName);
	}

	private void flipBehaviour(){
		int s = 0;
		if(pD.drawChanceFromUniform(simS.getFlipBehaviourProbability())){
			s = pD.getIntUniform(1, 6);
			if(s == 1){
				setAggressive();
			}
			if(s == 2){
				setDoer();
			}
			if(s == 3){
				setConstructive();
			}
			if(s == 4){
				setNice();
			}
			if(s == 5){
				setPassive();
			}
			if(s == 6){
				setNeutral();
			}
		}
	}

	public void myMotivation(){
		polarBehaviour.BehaviourStyle myStyle;
		flipBehaviour();
		myStyle = this.getBehaviourStyle();
		switch(myStyle){
		case Aggressive:{
			if(pD.drawChanceFromUniform(0.05)){
				increaseMotivation();
			}
			break;
		}
		case Doer:{
			if(pD.drawChanceFromUniform(0.10)){
				increaseMotivation();
			}
			break;
		}
		case Constructive:{
			if(pD.drawChanceFromUniform(0.20)){
				increaseMotivation();
			}
			break;
		}
		case Nice:{
			if(pD.drawChanceFromUniform(0.20)){
				decreaseMotivation();
				decreaseMotivation();	
			}
			if(pD.drawChanceFromUniform(0.20)){
				increaseMotivation();
			}
			break;
		}
		case Passive:{
			if(pD.drawChanceFromUniform(0.80)){
				decreaseMotivation();	
				decreaseMotivation();	
				decreaseMotivation();
				decreaseMotivation();	
				decreaseMotivation();
				decreaseMotivation();
			}
			break;
		}
		case Neutral:{
			if(pD.drawChanceFromUniform(0.60)){
				decreaseMotivation();
				decreaseMotivation();	
				decreaseMotivation();	
				decreaseMotivation();

			}
			break;
		}
		}
	}

	public void updateBehaviour(String event, Actor fromActor){
		polarBehaviour.BehaviourStyle myStyle, fromActorStyle;
		double myBehaviour;
		if(event.equals("")){
			//System.out.println("************************ NULL Event");
		}
		else{
			//System.out.println(event);
		}
		if(fromActor == null){
			System.out.println("NULL fromActor");
		}
		else{
			//System.out.print("fromActor = ");
			//fromActor.printShortProfile();
			//System.out.print("toActor = ");
			//printShortProfile();
			myStyle = this.getBehaviourStyle();
			myBehaviour = this.behaviour();
			fromActorStyle = fromActor.myBehaviourStyle();
			if(IamBoss()){
				if(isMyBoss(fromActor)){
					//I am a Boss and I am interacting with MyBoss.
					switch(fromActorStyle){
					case Aggressive:{
						if(event.equals("Reprimand")){
							if(IamSeniorManager){

							}
							if(IamManager){
								setAggressive();
							}
							if(IamTL){
								if(pD.drawChanceFromUniform(0.8)){
									setDoer();
								}
								else{
									setPassive();
								}
							}
						}
						if(event.equals("Praise")){
							increaseConstructive(3);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(3);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("BossAtoS " + event);
						break;
					}
					case Doer:{
						if(event.equals("Reprimand")){
							if(IamSeniorManager){

							}
							if(IamManager){
								setDoer();
							}
							if(IamTL){
								if(pD.drawChanceFromUniform(0.8)){
									setDoer();
								}
								else{
									setNice();
								}
							}
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(2);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("BossAtoS " + event);
						break;
					}
					case Constructive:{
						if(event.equals("Reprimand")){
							if(IamSeniorManager){

							}
							if(IamManager){
								setDoer();
							}
							if(IamTL){
								if(pD.drawChanceFromUniform(0.2)){
									setDoer();
								}
								else{
									increasePassive(3);
								}
							}
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(2);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("BossAtoS " + event);
						break;
					}
					case Nice:{
						if(event.equals("Reprimand")){
							if(IamSeniorManager){

							}
							if(IamManager){
								setDoer();
							}
							if(IamTL){
								if(pD.drawChanceFromUniform(0.2)){
									setDoer();
								}
								else{
									increasePassive(3);
								}
							}
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(2);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("BossAtoS " + event);
						break;
					}
					case Passive:{
						if(event.equals("Reprimand")){
							increaseAggressive(1);
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(2);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("BossPtoS " + event);
						break;
					}
					case Neutral:{
						if(event.equals("Reprimand")){
							increaseAggressive(1);
						}
						if(event.equals("Praise")){
							increaseConstructive(1);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(1);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("BossTtoS " + event);
						break;
					}
					}
				}
				else{
					//I am a Boss and I am interacting with one of my Subordinates.
					switch(fromActorStyle){
					case Aggressive:{
						if(event.equals("Reprimand")){
							increaseAggressive(2);
						}
						if(event.equals("Praise")){
							increaseConstructive(1);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(1);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("SAtoBoss " + event);
						break;
					}
					case Doer:{
						if(event.equals("Reprimand")){
							increaseAggressive(2);
						}
						if(event.equals("Praise")){
							increaseConstructive(1);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(1);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("SDtoBoss " + event);
						break;
					}
					case Constructive:{
						if(event.equals("Reprimand")){
							increaseAggressive(2);
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(2);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("SCtoBoss " + event);
						break;
					}
					case Nice:{
						if(event.equals("Reprimand")){
							increaseAggressive(2);
						}
						if(event.equals("Praise")){
							increaseConstructive(1);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(1);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("SNtoBoss " + event);
						break;
					}
					case Passive:{
						if(event.equals("Reprimand")){
							increaseAggressive(2);
						}
						if(event.equals("Praise")){
							increaseConstructive(1);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(1);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("SPtoBoss " + event);
						break;
					}
					case Neutral:{
						if(event.equals("Reprimand")){
							increaseAggressive(1);
						}
						if(event.equals("Praise")){
							increaseConstructive(1);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(1);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("STtoBoss " + event);
						break;
					}
					}
				}

			}
			else{
				if(isMyBoss(fromActor)){
					//I am NOT a Boss, I am a Subordinate and  I am interacting with MyBoss.
					switch(fromActorStyle){
					case Aggressive:{
						if(event.equals("Reprimand")){
							increasePassive(2);
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(1);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("BossAtoS " + event);
						break;
					}
					case Doer:{
						if(event.equals("Reprimand")){
							increasePassive(2);
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(1);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("BossDtoS " + event);
						break;
					}
					case Constructive:{
						if(event.equals("Reprimand")){
							increasePassive(2);
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
						}
						if(event.equals("OfferHelp")){

							increaseConstructive(1);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("BossCtoS " + event);
						break;
					}
					case Nice:{
						if(event.equals("Reprimand")){
							increasePassive(1);
						}
						if(event.equals("Praise")){
							increaseConstructive(1);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(1);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("BossNtoS " + event);
						break;
					}
					case Passive:{
						if(event.equals("Reprimand")){
							increasePassive(2);
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(2);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("BossPtoS " + event);
						break;
					}
					case Neutral:{
						if(event.equals("Reprimand")){
							increasePassive(1);
						}
						if(event.equals("Praise")){
							increaseConstructive(1);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(1);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("BossTtoS " + event);
						break;
					}
					}
				}
				else{
					//I am NOT a Boss, I am a Subordinate, and  am interacting with an Actor that is NOT MyBoss.
					switch(fromActorStyle){
					case Aggressive:{
						if(event.equals("Reprimand")){
							increaseAggressive(2);
							increasePassive(1);
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(2);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("PPA " + event);
						break;
					}
					case Doer:{
						if(event.equals("Reprimand")){
							increaseAggressive(2);
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(2);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("PPD " + event);
						break;
					}
					case Constructive:{
						if(event.equals("Reprimand")){
							increaseAggressive(2);
						}
						if(event.equals("Praise")){
							increaseConstructive(1);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(1);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("PPC " + event);
						break;
					}
					case Nice:{
						if(event.equals("Reprimand")){
							increaseAggressive(2);
						}
						if(event.equals("Praise")){
							increaseConstructive(1);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(2);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("PPN " + event);
						break;
					}
					case Passive:{
						if(event.equals("Reprimand")){
							increaseAggressive(1);
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(2);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("PPP " + event);
						break;
					}
					case Neutral:{
						if(event.equals("Reprimand")){
							increaseAggressive(1);
						}
						if(event.equals("Praise")){
							increaseConstructive(2);
							//}
						}
						if(event.equals("OfferHelp")){
							increaseConstructive(2);
						}
						if(event.equals("AskHelp")){

						}
						//System.out.println("PPT " + event);
						break;
					}
					}
				}
			}
		}
		myLearningFactor = myLearningFactor();
		myMotivation();

	}
}
