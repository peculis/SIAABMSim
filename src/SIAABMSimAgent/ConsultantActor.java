package SIAABMSimAgent;

import java.util.ArrayList;
import SIAABMSimSocial.Organization;
import SIAABMSimTask.Contract;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.Task;
import SIAABMSimUtils.probability;
import java.awt.Color;
import uchicago.src.sim.gui.SimGraphics;

public class ConsultantActor extends Actor{
	
	public ConsultantActor(ActorProfile profile, Organization organization, int x, int y){
		myProfile = profile;
		myX = x;
		myY = y;
		myProfile.setOrganization(organization);
		fromInteractions = new ArrayList<Interaction>();
		toInteractions = new ArrayList<Interaction>();
		myTasks = new ArrayList<Task>();
		pD = pD.getInstance();
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
		G.drawHollowRoundRect(Color.YELLOW);
		G.drawFastCircle(myColor);
		//System.out.println("Draw Actor Consultant " + myProfile.name());
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
		double b, m;
		if(this.getChanceOfEvent("Start")){
			processInteractions();
		}
		if(this.getChanceOfEvent("Learning")){
			increaseKnowledge();
		}
		//System.out.println("step() ConsultantActor " + myProfile.name());
	}
}
