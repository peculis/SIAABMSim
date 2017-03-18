package SIAABMSimAgent;

import java.util.ArrayList;
import SIAABMSimSocial.Organization;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.Task;
import SIAABMSimUtils.probability;
import java.awt.Color;
import uchicago.src.sim.gui.SimGraphics;

public class GhostActor extends Actor{
	
	public GhostActor(ActorProfile profile, Organization organization, int x, int y){
		myProfile = profile;
		myX = x;
		myY = y;
		myProfile.setOrganization(organization);
		pD = pD.getInstance();
		fromInteractions = new ArrayList<Interaction>();
		toInteractions = new ArrayList<Interaction>();
		myTasks = new ArrayList<Task>();
	}
	
	public void draw(SimGraphics G){
		myColor = Color.BLACK;
		//G.drawHollowFastRect(Color.BLACK);
		//G.drawFastOval(myColor);
		//System.out.println("Draw Actor Ghost " + myProfile.name());
	}
	
	public void step() {
		//System.out.println("step() GhostActor " + myProfile.name());
	}

}
