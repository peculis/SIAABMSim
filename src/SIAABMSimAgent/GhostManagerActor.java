package SIAABMSimAgent;

import java.awt.Color;

import SIAABMSimAgent.ActorProfile.RoleType;
import SIAABMSimConnection.Interaction;
import SIAABMSimSocial.Organization;

import uchicago.src.sim.gui.SimGraphics;

public class GhostManagerActor extends ManagerActor{

	public GhostManagerActor(ActorProfile profile, Organization organization, int x, int y){

		super(profile, organization,x, y);

	}
	
	public void assignTeamLeader(GhostTeamLeaderActor teamLeader){	
		myTeamLeaderList.add((GhostTeamLeaderActor) teamLeader);
	}
	
	protected void findTeamLeaders(){
		
	}

	public void draw(SimGraphics G){
		myColor = Color.BLACK;
	}

	public void step() {
		//System.out.println("Ghost Manager step()");
		processMyTasks();
	}

}
