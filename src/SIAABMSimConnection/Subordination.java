package SIAABMSimConnection;

import java.awt.Color;

import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimAgent.Actor;
import SIAABMSimConnection.Interaction.InteractionState;
import SIAABMSimConnection.Interaction.InteractionType;

public class Subordination extends Interaction{

	public Subordination (Actor fromActor, Actor toActor, String interactionState, double interactionQuality){
		fromThisAgent = fromActor;
		toThatAgent = toActor;

		fromThisActor = fromActor;
		toThatActor = toActor;

		type = InteractionType.Leadership;

		myColor = Color.BLUE;
		//myColor = new Color(200,200,0);

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
			responseEffort();
			if(interactionEvent.equals("OfferHelp")){
				//System.out.println(getFromActor().getRole() + " " + getFromActor().getName()+ " OfferHelp " +
				//		getToActor().getRole() + " " + getToActor().getName());
				acceptResponseEffort();
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
					//getFromActor().increaseMotivation();
					//getFromActor().increaseConstructive(1);
				}
				getToActor().increaseMotivation();
				getToActor().increaseConstructive(1);	
			}

			if(interactionEvent.equals("Reprimand")){
				//System.out.println(getFromActor().getRole() + " " + getFromActor().getName()+ " Reprimand "+
				//		getToActor().getRole() + " " + getToActor().getName());
				getToActor().decreaseMotivation();
				getToActor().increasePassive(5);
			}

			if(interactionEvent.equals("Praise")){
				//System.out.println(getFromActor().getRole() + " " + getFromActor().getName()+ " Praise " +
				//		getToActor().getRole() + " " + getToActor().getName());
				getToActor().increaseMotivation();
				getToActor().increaseConstructive(3);
			}
		}
	}

	private boolean TMtoTL(String event){
		Actor TL, TM;
		TL = toThatActor;
		TM = fromThisActor;
		if(TL.getChanceOfEvent(event)){
			if(TL.isConstructive()){
				TM.increaseMotivation();
				//TM.increaseMotivation();
				if(TL.getExperience()> TM.getExperience()){
					TM.increaseExperience(2);
				}
				if(TL.getKnowledge() > TM.getKnowledge()){
					TM.increaseKnowledge(3);
				}
				//TM.increaseConstructive(3);
				TM.updateBehaviour(event,TL);
			}
			if(TL.isAgressive()){
				TM.decreaseMotivation();
				TM.decreaseMotivation();
				if(!TM.isConstructive()){
					if(TL.getExperience()< TM.getExperience()){
						TM.decreaseExperience();
					}
					if(TL.getKnowledge() < TL.getKnowledge()){
						TM.decreaseKnowledge();
					}
				}
				//TM.increasePassive(5);
			}
			TM.updateBehaviour(event,TL);
			return true;
		}
		else {
			return false;
		}
	}

	public boolean respond(String event){
		interactionEvent = event;
		return TMtoTL(event);
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
		/*
			G.drawLink(myColor, (int) xScale * fromThisAgent.getX(), (int) xScale * toThatAgent.getX(), 
					(int) yScale * fromThisAgent.getY(), (int) yScale * toThatAgent.getY());
		 */
	}

}
