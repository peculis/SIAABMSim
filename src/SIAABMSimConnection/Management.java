package SIAABMSimConnection;

import java.awt.Color;

import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.ActorProfile.RoleType;

public class Management extends Interaction{

	public Management (Actor fromActor, Actor toActor, String interactionState, double interactionQuality){
		fromThisAgent = fromActor;
		toThatAgent = toActor;

		fromThisActor = fromActor;
		toThatActor = toActor;

		type = InteractionType.Management;

		myColor = Color.MAGENTA;
		//myColor = new Color(255,100,100);

		simS = simS.getInstance();
		pD = pD.getInstance();
		probabilityOfInteraction = simS.getInteractionFactor();

		cooperationFactor = (int) simS.getCooperationFactor();

		if (interactionState.equals("ON")){
			state = InteractionState.Active;
		}
		else{
			state = InteractionState.Innactive;
		}
		quality = interactionQuality;
	}

	public void request(String event){
		if(pD.drawChanceFromUniform(probabilityOfInteraction)){
			interactionEvent = event;
			requestEffort();
			if(interactionEvent.equals("OfferHelp")){
				//System.out.println("Management " + getFromActor().myRole() + " " + getFromActor().getName()+ " OfferHelp " +
				//		getToActor().myRole() + " " + getToActor().getName());
				acceptResponseEffort();
				getToActor().workHarder(false);
				if(toThatActor.getChanceOfEvent("AcceptHelp")){
					fromThisActor.increaseExperience(1);
					fromThisActor.increaseKnowledge(1);
					if(fromThisActor.getExperience()> toThatActor.getExperience()){
						toThatActor.increaseExperience(cooperationFactor);
					}
					if(fromThisActor.getKnowledge() > toThatActor.getKnowledge()){
						toThatActor.increaseKnowledge(cooperationFactor);
					}
					getToActor().increaseMotivation();
					getToActor().increaseConstructive(3);
					getFromActor().increaseMotivation();
					getFromActor().increaseConstructive(1);
				}
				getToActor().increaseMotivation();
				getToActor().increaseConstructive(1);
			}

			if(interactionEvent.equals("Reprimand")){
				//System.out.println("Management " + getFromActor().myRole() + " " + getFromActor().getName()+ " Reprimand "+
				//		getToActor().myRole() + " " + getToActor().getName());
				getToActor().decreaseMotivation();
				getToActor().workHarder(true);
				//getToActor().increaseAgressive(5);
				if(getToActor().myRole() == RoleType.Manager){
					if(getToActor().getChanceOfEvent("Aggressive")){
						getToActor().increaseAgressive(1);
					}
				}
				else{
					if(getToActor().getChanceOfEvent("Passive")){
						getToActor().increasePassive(2);
					}
				}
				if(getFromActor().getChanceOfEvent("Aggressive")){
					getFromActor().increaseAgressive(1);
				}
			}

			if(interactionEvent.equals("Praise")){
				//System.out.println("Management " + getFromActor().myRole() + " " + getFromActor().getName()+ " Praise " +
				//		getToActor().myRole() + " " + getToActor().getName());
				getToActor().increaseMotivation();
				getToActor().increaseConstructive(1);
				getToActor().workHarder(false);
				//getToActor().increaseAgressive(1);
				getFromActor().increaseConstructive(1);
			}
			toThatActor.updateBehaviour(event, fromThisActor);
			//fromThisActor.updateBehaviour(toThatActor);
		}
	}

	public boolean respond(String event){
		interactionEvent = event;
		responseEffort();
		if(toThatActor.getChanceOfEvent(event)){
			if(fromThisActor.isConstructive()){
				toThatActor.increaseMotivation();
				//toThatActor.increaseMotivation();
				if(fromThisActor.getExperience()> toThatActor.getExperience()){
					toThatActor.increaseExperience(cooperationFactor);
				}
				if(fromThisActor.getKnowledge() > toThatActor.getKnowledge()){
					toThatActor.increaseKnowledge(cooperationFactor);
				}
			}
			if(fromThisActor.isAgressive()){
				toThatActor.decreaseMotivation();
				toThatActor.decreaseMotivation();
				if(!toThatActor.isConstructive()){
					if(toThatActor.getExperience()< fromThisActor.getExperience()){
						//fromThisActor.decreaseExperience();
					}
					if(toThatActor.getKnowledge() < fromThisActor.getKnowledge()){
						//fromThisActor.decreaseKnowledge();
					}
				}
			}
			else{

			}
			//toThatActor.updateBehaviour(fromThisActor);
			return true;
		}
		else {
			return false;
		}
	}

	public void draw(SimGraphics G){
		int h, w;
		float xScale, yScale;
		h = G.getDisplayHeight();
		w = G.getDisplayWidth();
		myColor = fromThisAgent.getColor();
		//This scale factor of 100 corresponds to the Display size.
		xScale = w/xSize;
		yScale = h/ySize;

		G.drawLink(myColor, (int) xScale * fromThisAgent.getX(), (int) xScale * toThatAgent.getX(), 
				(int) yScale * fromThisAgent.getY(), (int) yScale * toThatAgent.getY());
	}

}
