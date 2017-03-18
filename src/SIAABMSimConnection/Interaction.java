package SIAABMSimConnection;

import SIAABMSimAgent.Actor;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.simSettings;
import java.awt.Color;
import uchicago.src.sim.gui.SimGraphics;


public abstract class Interaction extends Connection{

	public enum InteractionState {Active, Innactive};
	public enum InteractionType {Boss, SeniorManagement, Management, Leadership, Consultancy, Peer, RemotePeer, Acquaintance};


	protected Actor fromThisActor;
	protected Actor toThatActor;

	protected Interaction reverseInteraction;

	protected InteractionState state;
	protected String interactionEvent;
	protected String responseEvent;
	protected double probabilityOfInteraction;
	protected double quality;
	protected Color myColor;
	protected InteractionType type;

	protected probability pD;
	protected simSettings simS;
	
	protected double cooperationFactor;


	public boolean isActive(){
		if(state == InteractionState.Active){
			return true;
		}
		else{
			pD = pD.getInstance();
			if(pD.drawChanceFromUniform(0.10)){
				quality = 0.5;
				state = InteractionState.Active;
				//System.out.println("++++++++++");
				if(reverseInteraction != null){
					reverseInteraction.setQuality(0.5);
				}
				return true;
			}
			return false;
		}
	}

	public double getQuality(){
		return quality;
	}

	public void setQuality(double q){
		if(quality < q){
			quality = q;
			state = InteractionState.Active;
			//System.out.println("^^^^^^^^^^");
		}
	}

	public void increaseQuality(){
		quality = quality + 0.1;
		if(quality < 1.0){
			quality = quality + 0.1;
		}
		if(quality >= 1.0){
			quality = 1.0;
		}
		if(quality > 0.0){
			state = InteractionState.Active;
		}
	}

	public void decreaseQuality(){
		quality = quality - 0.1;
		if(quality > 0.0){
			quality = quality - 0.1;
		}
		if(quality <= 0.0){
			quality = 0.0;
			state = InteractionState.Innactive;
		}
	}

	public void setReverseInteraction(Interaction interaction){
		reverseInteraction = interaction;
	}

	public void increaseReverseInteractionQuality(){
		if(reverseInteraction != null){
			reverseInteraction.increaseQuality();
			//System.out.println("^^^^^^ increaseReverseInteractionQuality to " + reverseInteraction.getQuality());
		}
	}

	public void requestEffort(){
		fromThisActor.increaseOverheadEffort(1);
		//System.out.println(type + " *** " + interactionEvent);
	}

	public void responseEffort(){
		toThatActor.increaseOverheadEffort(1);
		//System.out.println(type + " --- Response " + interactionEvent);
	}

	public void acceptResponseEffort(){
		fromThisActor.increaseOverheadEffort(2);
		toThatActor.increaseOverheadEffort(2);
		//System.out.println(type + " +++ Accept " + interactionEvent);
	}

	public Actor getFromActor(){
		return (Actor) fromThisAgent;
	}

	public Actor getToActor(){
		return (Actor) toThatAgent;
	}

	public InteractionType getType(){
		return type;
	}

	protected void increaseToActorKnowledge(){
		if(fromThisActor.getKnowledge() > toThatActor.getKnowledge()){
			toThatActor.increaseKnowledge();
		}
	}

	public void start(){
		state = InteractionState.Active;
		interactionEvent = "";
	}

	public void finish(){
		state = InteractionState.Innactive;
		interactionEvent = "";
	}

	public void request(String event){
		interactionEvent = event;
	}

	public boolean respond(String event){
		interactionEvent = event;
		return true;
	}

}
