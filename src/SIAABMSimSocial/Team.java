package SIAABMSimSocial;

import SIAABMSimAgent.Actor;
import SIAABMSimAgent.CeoActor;
import SIAABMSimAgent.ManagerActor;
import SIAABMSimAgent.TeamLeaderActor;
import SIAABMSimAgent.TeamMemberActor;
import SIAABMSimAgent.ExpertActor;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.Management;
import SIAABMSimConnection.Leadership;
import SIAABMSimConnection.Subordination;
import SIAABMSimConnection.Peer;
import SIAABMSimConnection.Consultancy;
import SIAABMSimUtils.probability;

import java.util.ArrayList;

public class Team {

	protected Organization theOrganization;
	protected Actor theCeo;
	protected Department theDepartment;
	protected Actor theManager;
	protected Actor theTeamLeader;
	protected ArrayList <Actor> teamMembers;
	protected ArrayList <Actor> experts;
	protected ArrayList <Interaction> teamInteractions;
	protected double performance;
	protected double mood;
	protected double colletiveKnowledge;
	protected double collectiveExperience;
	private probability pD; //probabilityDistribution.

	public Team(Department department, Actor teamLeader){
		theDepartment = department;
		theTeamLeader = teamLeader;
		theOrganization = theDepartment.organization();
		teamLeader.setOrganization(theDepartment.organization());
		teamLeader.setDepartment(theDepartment);
		teamLeader.setTeam(this);
		teamMembers = new ArrayList<Actor>();
		experts = new ArrayList<Actor>();
		teamInteractions = new ArrayList<Interaction>();
		pD = pD.getInstance();
	}
	
	public void setConstructive(){
		theTeamLeader.setConstructive();
		for(int e=0; e<experts.size();e++){
			experts.get(e).setConstructive();
		}
	}
	
	public void setAgressive(){
		theTeamLeader.setAgressive();
		for(int e=0; e<experts.size();e++){
			experts.get(e).setPassive();
		}
	}

	public double performance(){
		return performance;
	}

	public double mood(){
		return mood;
	}

	public Organization organization(){
		return theDepartment.organization();
	}

	public Department department(){
		return theDepartment;
	}

	public Actor teamLeader(){
		return theTeamLeader;
	}

	public ArrayList<Actor> getTeamMembers(){
		return teamMembers;
	}

	public ArrayList<Actor> getExperts(){
		return experts;
	}

	public Actor getExpert(){
		int nExperts;
		nExperts = experts.size();
		if(nExperts >0){
			return experts.get(pD.getIntUniform(0, nExperts-1));
		}
		else{
			return null;
		}
	}

	public void addTeamMember(Actor member){
		member.setTeam(this);
		member.setDepartment(theDepartment);
		member.setOrganization(theOrganization);
		member.setBoss(theTeamLeader);
		teamMembers.add(member);
	}

	public void addExpert(Actor expert){
		expert.setTeam(this);
		expert.setDepartment(theDepartment);
		expert.setOrganization(theOrganization);
		expert.setBoss(theTeamLeader);
		experts.add(expert);
	}

	public void createInteractions(double density){
		Actor actor, actorb;
		Interaction interactionA, interactionB;
		//Create interactions between TL and TM and Experts and TM
		for(int a=0; a<teamMembers.size(); a++){
			actor = teamMembers.get(a);
			interactionA = new Leadership(theTeamLeader, actor, "ON", 0.5);
			theTeamLeader.addToInteraction(interactionA);
			actor.addFromInteraction(interactionA);
			teamInteractions.add(interactionA);
			interactionB = new Subordination(actor, theTeamLeader, "ON", 0.5);
			theTeamLeader.addFromInteraction(interactionB);
			actor.addToInteraction(interactionB);
			teamInteractions.add(interactionB);
			interactionA.setReverseInteraction(interactionB);
			interactionB.setReverseInteraction(interactionA);
			
			//Create interactions between Experts and TM
			for(int b=0; b<experts.size();b++){
				actorb = experts.get(b);
				interactionA = new Consultancy(actor, actorb, "ON", 0.5);
				actor.addToInteraction(interactionA);
				actorb.addFromInteraction(interactionA);
				teamInteractions.add(interactionA);
				interactionB = new Consultancy(actorb, actor, "ON", 0.5);
				actor.addFromInteraction(interactionB);
				actorb.addToInteraction(interactionB);
				teamInteractions.add(interactionB);
				interactionA.setReverseInteraction(interactionB);
				interactionB.setReverseInteraction(interactionA);
			}
		}
		//Create interactions between TL and Experts
		for(int a=0; a<experts.size(); a++){
			actor = experts.get(a);
			interactionA = new Consultancy(theTeamLeader, actor, "ON", 0.5);
			theTeamLeader.addToInteraction(interactionA);
			actor.addFromInteraction(interactionA);
			teamInteractions.add(interactionA);
			interactionB = new Consultancy(actor, theTeamLeader, "ON", 0.5);
			theTeamLeader.addFromInteraction(interactionB);
			actor.addToInteraction(interactionB);
			teamInteractions.add(interactionB);
			interactionA.setReverseInteraction(interactionB);
			interactionB.setReverseInteraction(interactionA);
		}
		//Create interactions between TM
		if(density > 0.0){
			for(int a=0; a<teamMembers.size(); a++){
				actor = teamMembers.get(a);
				for(int b=0; b<teamMembers.size();b++){
					actorb = teamMembers.get(b);
					if(actor != actorb){
						if(pD.drawChanceFromUniform(density)){
							interactionA = new Peer(actor, actorb, "ON", 0.5);
							actor.addToInteraction(interactionA);
							actorb.addFromInteraction(interactionA);
							teamInteractions.add(interactionA);
							interactionB = new Peer(actorb, actor, "ON", 0.5);
							actor.addFromInteraction(interactionB);
							actorb.addToInteraction(interactionB);
							teamInteractions.add(interactionB);
							interactionA.setReverseInteraction(interactionB);
							interactionB.setReverseInteraction(interactionA);
						}
					}
				}
			}
		}
	}

	public ArrayList<Interaction> getInteractions(){
		return teamInteractions;
	}


	public void step(){

	}

}
