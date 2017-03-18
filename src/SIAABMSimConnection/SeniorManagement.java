package SIAABMSimConnection;

import java.awt.Color;
import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimAgent.Actor;


public class SeniorManagement extends Interaction{

	public SeniorManagement (Actor fromActor, Actor toActor, String interactionState, double interactionQuality){
		fromThisAgent = fromActor;
		toThatAgent = toActor;

		fromThisActor = fromActor;
		toThatActor = toActor;

		type = InteractionType.SeniorManagement;

		myColor = Color.MAGENTA;
		//myColor = new Color(255,100,100);

		simS = simS.getInstance();
		pD = pD.getInstance();
		probabilityOfInteraction = simS.getInteractionFactor();

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
				//System.out.println("Management " + getFromActor().getRole() + " " + getFromActor().getName()+ " OfferHelp " +
				//		getToActor().getRole() + " " + getToActor().getName());
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
				//System.out.println("Management " + getFromActor().getRole() + " " + getFromActor().getName()+ " Reprimand "+
				//		getToActor().getRole() + " " + getToActor().getName());
				getToActor().decreaseMotivation();
				getToActor().workHarder(true);
				//getToActor().increaseAgressive(5);
				getToActor().increasePassive(4);
				getFromActor().increaseAgressive(1);
			}

			if(interactionEvent.equals("Praise")){
				//System.out.println("Management " + getFromActor().getRole() + " " + getFromActor().getName()+ " Praise " +
				//		getToActor().getRole() + " " + getToActor().getName());
				getToActor().increaseMotivation();
				getToActor().workHarder(false);
				getToActor().increaseConstructive(1);
				//getToActor().increaseAgressive(1);
				getFromActor().increaseConstructive(1);
			}
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
			toThatActor.updateBehaviour(event, fromThisActor);
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
