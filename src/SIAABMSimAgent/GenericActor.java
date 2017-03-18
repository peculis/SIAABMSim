package SIAABMSimAgent;

import java.util.ArrayList;
import SIAABMSimSocial.Organization;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.Task;
import SIAABMSimUtils.probability;
import java.awt.Color;
import uchicago.src.sim.gui.SimGraphics;

public class GenericActor extends Actor{
	
	public GenericActor(ActorProfile profile, Organization organization, int x, int y){
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
		int r, g, b;
		//r = (int) myProfile.aggressive() * 255;
		//g = (int) myProfile.passive() * 255;
		//b = (int) myProfile.constructive() * 255;
		r = (int) (myProfile.aggressive() * 255.0);
		g = (int) (myProfile.passive() * 255.0);
		b = (int) (myProfile.constructive() * 255.0);
		myColor = new Color (r,g,b);
		G.drawHollowFastRect(Color.GREEN);
		G.drawFastOval(myColor);
		//System.out.println("Draw Actor Generic " + myProfile.name());
	}
	
	public void step() {
		double b, m;
		b = myProfile.behaviour();
		b = b + 0.002;
		if (b > 1.0) { 
			b = 0.0;
		}
		//myProfile.setBehaviour(b);
		/*
		m = myProfile.motivation();
		m = m + 0.002;
		if (m > 1.0) { 
			m = 0.0;
		}
		myProfile.setMotivation(m);
		*/
		//System.out.println("step() GenericActor " + myProfile.name());
	}

}
