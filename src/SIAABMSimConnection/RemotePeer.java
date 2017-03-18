package SIAABMSimConnection;

import java.awt.Color;

import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimAgent.Actor;
import SIAABMSimConnection.Interaction.InteractionState;

public class RemotePeer extends Interaction{

	public RemotePeer (Actor fromActor, Actor toActor, String interactionState, double interactionQuality){
		fromThisAgent = fromActor;
		toThatAgent = toActor;

		fromThisActor = fromActor;
		toThatActor = toActor;

		type = InteractionType.RemotePeer;

		myColor = Color.BLUE;
		//myColor = new Color(200,200,0);

		simS = simS.getInstance();
		pD = pD.getInstance();
		probabilityOfInteraction = simS.getInteractionFactor();
		cooperationFactor = simS.getCooperationFactor();

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
			if(event.equals("OfferHelp")){
				requestEffort();
				//System.out.println("Consultancy " + fromThisActor.getRole() + " " + fromThisActor.getName() + " " +
				//		event + " to " + toThatActor.getRole() + " " + toThatActor.getName());
				if(respond(event)){
					acceptResponseEffort();
					//System.out.println("Consultancy " + toThatActor.getRole() + " " + toThatActor.getName() + " ACCEPTED");
				}
				else{
					//System.out.println("Consultancy " + toThatActor.getRole() + " " + toThatActor.getName() + " DID NOT ACCEPT");
				}
				toThatActor.updateBehaviour(event, fromThisActor);
			}
			if(event.equals("AskHelp")){
				requestEffort();
				//System.out.println("Consultancy " + fromThisActor.getRole() + " " + fromThisActor.getName() + " " +
				//		event + " to " + toThatActor.getRole() + " " + toThatActor.getName());
				if(respond(event)){
					acceptResponseEffort();
					//System.out.println("Consultancy " + toThatActor.getRole() + " " + toThatActor.getName() + " HELPED");
				}
				else{
					//System.out.println("Consultancy " + toThatActor.getRole() + " " + toThatActor.getName() + " DID NOT HELP");
					//fromThisActor.decreaseMotivation();
					decreaseQuality();
					fromThisActor.decreaseMotivation();
				}
				fromThisActor.updateBehaviour(event, toThatActor);
			}
		}
	}

	public boolean respond(String event){
		double eGap, kGap;
		responseEvent = event;
		responseEffort();
		if(event.equals("OfferHelp")){
			responseEvent = "AcceptHelp";
		}
		if(event.equals("AskHelp")){
			responseEvent = "Help";
		}
		if(event.equals("OfferHelp")){
			increaseReverseInteractionQuality();
			if(toThatActor.getChanceOfEvent(responseEvent)){
				//System.out.println("*** ACCEPTED HELP from Remote Peer");
				toThatActor.increaseMotivation();
				eGap = fromThisActor.getExperience() - toThatActor.getExperience();
				if(eGap > 0.0){
					toThatActor.increaseExperience(cooperationFactor * fromThisActor.getTeachingFactor(), eGap);
				}
				kGap = fromThisActor.getKnowledge() - toThatActor.getKnowledge();
				if(kGap > 0.0){
					toThatActor.increaseKnowledge(cooperationFactor * fromThisActor.getTeachingFactor(), kGap);
				}
				return true;
			}
			else{
				//System.out.println("*** DID NOT ACCEPT HELP from Remote Peer");
				return false;
			}
		}
		if(event.equals("AskHelp")){
			if(toThatActor.getChanceOfEvent(responseEvent)){
				//System.out.println("*** Remote Peer will HELP");
				toThatActor.increaseMotivation();
				eGap = toThatActor.getExperience() - fromThisActor.getExperience();
				if(eGap > 0.0){
					fromThisActor.increaseExperience(cooperationFactor * toThatActor.getTeachingFactor(), eGap);
				}
				kGap = toThatActor.getKnowledge() - fromThisActor.getKnowledge();
				if(kGap > 0.0){
					fromThisActor.increaseKnowledge(cooperationFactor * toThatActor.getTeachingFactor(), kGap);
				}
				return true;
			}
			else{
				//System.out.println("### Remote Peer IS BUSY");
				return false;
			}
		}
		return false;
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
		if(!isActive()){
			myColor = Color.black;
		}
		G.drawLink(myColor, (int) xScale * fromThisAgent.getX(), (int) xScale * toThatAgent.getX(), 
				(int) yScale * fromThisAgent.getY(), (int) yScale * toThatAgent.getY());
	}
}
