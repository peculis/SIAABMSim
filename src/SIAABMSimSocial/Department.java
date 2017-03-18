package SIAABMSimSocial;

import SIAABMSimAgent.Actor;
import SIAABMSimAgent.CeoActor;
import SIAABMSimAgent.ManagerActor;
import SIAABMSimAgent.TeamLeaderActor;
import SIAABMSimAgent.TeamMemberActor;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.Management;
import SIAABMSimConnection.SeniorManagement;
import SIAABMSimConnection.Leadership;
import SIAABMSimConnection.Consultancy;
import SIAABMSimConnection.Peer;
import SIAABMSimConnection.Subordination;
import SIAABMSimUtils.probability;

import java.util.ArrayList;

public class Department {

	protected Organization theOrganization;
	protected Actor previousPhaseManager;
	protected Actor nextPhaseManager;
	protected Actor theManager;
	protected ArrayList <Team> teamList;
	protected ArrayList <Actor> teamLeaders;
	protected ArrayList <Actor> staff;
	protected ArrayList <Actor> experts;
	protected ArrayList <Interaction> departmentInteractions;
	protected double performance;
	protected double mood;
	private probability pD; //probabilityDistribution.
	private double collectiveExperience;
	private double collectiveKnowledge;

	public Department(Organization organization, Actor manager){
		theOrganization = organization;
		theManager = manager;
		theManager.setOrganization(organization);
		theManager.setDepartment(this);
		teamList = new ArrayList<Team>();
		teamLeaders = new ArrayList<Actor>();
		departmentInteractions = new ArrayList<Interaction>();
		staff = new ArrayList<Actor>();
		experts = new ArrayList<Actor>();
		pD = pD.getInstance();
		collectiveExperience = 0.0;
		collectiveKnowledge = 0.0;
	}
	
	public void setConstructive(){
		theManager.setConstructive();
		for(int t=0; t<teamList.size(); t++){
			teamList.get(t).setConstructive();
		}
	}
	
	public void setAgressive(){
		theManager.setAgressive();
		for(int t=0; t<teamList.size(); t++){
			teamList.get(t).setAgressive();
		}
	}

	private void calculateCollectiveExperience(){
		double eTM, eTL, eEX;
		eTM = 0.0;
		eTL = 0.0;
		eEX = 0.0;
		for(int a=0; a<teamLeaders.size();a++){
			eTL = eTL + teamLeaders.get(a).getExperience();
		}
		eTL = eTL / teamLeaders.size();
		for(int a=0; a<staff.size();a++){
			eTM = eTM + staff.get(a).getExperience();
		}
		eTM = eTM / staff.size();
		for(int a=0; a<experts.size();a++){
			eEX = eEX + experts.get(a).getExperience();
		}
		eEX = eEX / experts.size();
		if(experts.size() > 0){
			collectiveExperience = (0.2 * eTL + 0.5 * eTM + 0.3 * eEX);
		}
		else{
			collectiveExperience = (0.2 * eTL + 0.8 * eTM);
		}
	}
	
	private void calculateCollectiveKnowledge(){
		double kTM, kTL, kEX;
		kTM = 0.0;
		kTL = 0.0;
		kEX = 0.0;
		for(int a=0; a<teamLeaders.size();a++){
			kTL = kTL + teamLeaders.get(a).getKnowledge();
		}
		kTL = kTL / teamLeaders.size();
		for(int a=0; a<staff.size();a++){
			kTM = kTM + staff.get(a).getKnowledge();
		}
		kTM = kTM / staff.size();
		for(int a=0; a<experts.size();a++){
			kEX = kEX + experts.get(a).getKnowledge();
		}
		kEX = kEX / experts.size();
		if(experts.size() > 0){
			collectiveKnowledge = (0.1 * kTL + 0.7 * kTM + 0.2 * kEX);
		}
		else{
			collectiveKnowledge = (0.1 * kTL + 0.9 * kTM);
		}
	}
	
	public double getCollectiveExperience(){
		calculateCollectiveExperience();
		return collectiveExperience;
	}
	
	public double getCollectiveKnowledge(){
		calculateCollectiveKnowledge();
		return collectiveKnowledge;
	}
	

	public double performance(){
		return performance;
	}

	public double mood(){
		return mood;
	}

	public Organization organization(){
		return theOrganization;
	}

	public Actor manager(){
		return theManager;
	}

	public void addTeam(Team team){
		teamList.add(team);
	}

	public ArrayList<Team> getTeams(){
		return teamList;
	}

	private void createTeamInteractions(double density){
		Team team;
		for(int t=0; t< teamList.size(); t++){
			team = teamList.get(t);
			team.createInteractions(density);
			departmentInteractions.addAll(team.getInteractions());
		}
	}

	public void createInteractions(double density){
		Actor actor;
		Interaction interactionA, interactionB;
		getTeamLeaders();
		getTeamMembers();
		getExperts();

		//Create interaction between CEO and the Manager.
		actor = theOrganization.getCEO();
		actor.allocated(true);
		interactionA = new SeniorManagement(actor, theManager, "ON", 0.5);
		actor.addToInteraction(interactionA);
		theManager.addFromInteraction(interactionA);
		theManager.allocated(true);
		departmentInteractions.add(interactionA);
		theManager.setBoss(actor);
		interactionB = new Subordination(theManager, actor, "ON", 0.5);
		actor.addFromInteraction(interactionB);
		theManager.addToInteraction(interactionB);
		departmentInteractions.add(interactionB);
		interactionA.setReverseInteraction(interactionB);
		interactionB.setReverseInteraction(interactionA);

		//Create interactions between the Manager and TLs.
		for(int a=0; a<teamLeaders.size(); a++){
			actor = teamLeaders.get(a);
			interactionA = new Management(theManager, actor, "ON", 0.5);
			theManager.addToInteraction(interactionA);
			actor.addFromInteraction(interactionA);
			departmentInteractions.add(interactionA);
			interactionB = new Subordination(actor, theManager, "ON", 0.5);
			theManager.addFromInteraction(interactionB);
			actor.addToInteraction(interactionB);
			departmentInteractions.add(interactionB);
			interactionA.setReverseInteraction(interactionB);
			interactionB.setReverseInteraction(interactionA);
			//theManager is set as TL Boss in the TeamLeaderActor class findMyManager().
		}
		if(density > 0.0){
			//Create interactions between the Manager and TM and Experts and TM
			for(int a=0; a<staff.size(); a++){
				if(pD.drawChanceFromUniform(density)){
					actor = staff.get(a);
					interactionA = new Management(theManager, actor, "ON", 0.5);
					theManager.addToInteraction(interactionA);
					actor.addFromInteraction(interactionA);
					departmentInteractions.add(interactionA);
					interactionB = new Subordination(actor, theManager, "ON", 0.5);
					theManager.addFromInteraction(interactionB);
					actor.addToInteraction(interactionB);
					departmentInteractions.add(interactionB);
					interactionA.setReverseInteraction(interactionB);
					interactionB.setReverseInteraction(interactionA);
				}
			}
			//Create interactions between the Manager and Experts
			for(int a=0; a<experts.size(); a++){
				if(pD.drawChanceFromUniform(density)){
					actor = experts.get(a);
					interactionA = new Consultancy(theManager, actor, "ON", 0.5);
					theManager.addToInteraction(interactionA);
					actor.addFromInteraction(interactionA);
					departmentInteractions.add(interactionA);
					interactionB = new Subordination(actor, theManager, "ON", 0.5);
					theManager.addFromInteraction(interactionB);
					actor.addToInteraction(interactionB);
					departmentInteractions.add(interactionB);
					interactionA.setReverseInteraction(interactionB);
					interactionB.setReverseInteraction(interactionA);
				}
			}
		}
		createTeamInteractions(density);
	}

	public ArrayList<Actor> getTeamLeaders(){
		teamLeaders.clear();
		for(int t=0; t<teamList.size(); t++){
			teamLeaders.add(teamList.get(t).teamLeader());
		}
		return staff;
	}

	public ArrayList<Actor> getTeamMembers(){
		staff.clear();
		for(int t=0; t<teamList.size(); t++){
			staff.addAll(teamList.get(t).getTeamMembers());
		}
		return staff;
	}

	public ArrayList<Actor> getExperts(){
		experts.clear();
		for(int t=0; t<teamList.size(); t++){
			experts.addAll(teamList.get(t).getExperts());
		}
		return experts;
	}

	public ArrayList<Interaction> getInteractions(){
		return departmentInteractions;
	}

	public void step(){

	}

}
