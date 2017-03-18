package SIAABMSimConnection;

import java.awt.Color;

import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimAgent.Actor;
import SIAABMSimConnection.Interaction.InteractionState;

public class Leadership extends Interaction{

	public Leadership (Actor fromActor, Actor toActor, String interactionState, double interactionQuality){
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
		double eGap, kGap;
		if(pD.drawChanceFromUniform(probabilityOfInteraction)){
			interactionEvent = event;
			requestEffort();
			responseEffort();
			if(interactionEvent.equals("OfferHelp")){
				//System.out.println(getFromActor().getRole() + " " + getFromActor().getName()+ " OfferHelp " +
				//		getToActor().getRole() + " " + getToActor().getName());
				acceptResponseEffort();
				getToActor().workHarder(false);
				if(toThatActor.getChanceOfEvent("AcceptHelp")){
					fromThisActor.increaseExperience(1);
					fromThisActor.increaseKnowledge(1);
					eGap = fromThisActor.getExperience() - toThatActor.getExperience();
					if(eGap > 0.0){
						toThatActor.increaseExperience(cooperationFactor * fromThisActor.getTeachingFactor(), eGap);
					}
					kGap = fromThisActor.getKnowledge() - toThatActor.getKnowledge();
					if(kGap > 0){
						toThatActor.increaseKnowledge(cooperationFactor * fromThisActor.getTeachingFactor(), kGap);
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
				getToActor().increasePassive(4);
				getToActor().workHarder(true);
			}

			if(interactionEvent.equals("Praise")){
				//System.out.println(getFromActor().getRole() + " " + getFromActor().getName()+ " Praise " +
				//		getToActor().getRole() + " " + getToActor().getName());
				getToActor().increaseMotivation();
				getToActor().increaseConstructive(3);
				getToActor().workHarder(false);
			}
			toThatActor.updateBehaviour(event, fromThisActor);
		}
	}

	private boolean TLtoTM(String event){
		Actor TL, TM;
		TM = toThatActor;
		TL = fromThisActor;

		if(TL.isConstructive()){
			TM.increaseMotivation();
			//TM.increaseMotivation();
			if(TL.getExperience()> TM.getExperience()){
				TM.increaseExperience(cooperationFactor);
			}
			if(TL.getKnowledge() > TM.getKnowledge()){
				TM.increaseKnowledge(cooperationFactor);
			}
			TM.increaseConstructive(3);
			//TM.updateBehaviour(event, TL);
			return true;
		}
		if(TL.isAgressive()){
			TM.decreaseMotivation();
			toThatActor.decreaseMotivation();
			if(!TM.isConstructive()){
				if(TL.getExperience()< TM.getExperience()){
					//TM.decreaseExperience();
				}
				if(TL.getKnowledge() < TM.getKnowledge()){
					//TM.decreaseKnowledge();
				}
			}
			TM.increasePassive(5);
			//TM.updateBehaviour(event, TL);
			return false;
		}

		return true;
	}

	public boolean respond(String event){
		interactionEvent = event;
		return TLtoTM(event);
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
