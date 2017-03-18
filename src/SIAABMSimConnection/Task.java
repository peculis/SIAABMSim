package SIAABMSimConnection;

//04 Sep 2009: This is the new Class Task that extends Connection.
//Task is now a Connection between an Actor and an Artifact through a TEOW

import java.awt.Color;

import uchicago.src.sim.gui.SimGraphics;
import SIAABMSimAgent.Agent;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.Artifact;
import SIAABMSimTask.TEOW;
import SIAABMSimTask.TEOW.TEOWPhase;

public abstract class Task extends Connection{

	public enum TaskType {Engineering, Review, Rework, Familiarization, Training, Learning, 
		Leadership, Management, ManageContract, ManageDeliverable, DevelopDeliverable,
		ReviewDeliverable, ReworkDeliverable, ManageTEOW};

		public enum TaskState { NOTALLOCATED, ASSIGNED, INPROGRESS, READY, STOPPED, COMPLETE};
		
		protected Artifact deliverable;

		protected TaskType taskType;
		protected TEOW thisTEOW;
		protected TaskState state;
		protected double effortFactor = 1.0;

		protected int productiveEffort = 0;
		protected int overheadEffort = 0;

		public void assign(Actor actor){
			fromThisAgent = actor;
			state = TaskState.ASSIGNED;
			//thisTEOW.setState(TEOWState.ASSIGNED);
		}
		
		public Actor getActor(){
			return (Actor) fromThisAgent;
		}


		public void XincreaseProductiveEffort(int n){
			productiveEffort = productiveEffort + n;
		}

		public void XincreaseOverheadEffort(int n){
			overheadEffort = overheadEffort + n;
		}

		public int XgetProductiveEffort(){
			return productiveEffort;
		}

		public int XgetOverheadEffort(){
			return overheadEffort;
		}

		public int XgetTotalEffort(){
			return (productiveEffort + overheadEffort);
		}

		public double actorUsefulKnowledgeRatio(){
			return 0.0;
		}

		public void setEffortFactor(double factor){
			effortFactor = factor;
		}

		public double getEffortFactor(){
			return effortFactor;
		}

		public TEOW getTEOW(){
			return thisTEOW;
		}

		public double quality(){
			if(thisTEOW != null){
				return thisTEOW.quality();
			}
			else{
				return 1.0;
			}
		}

		public double getPerformance(){
			if(thisTEOW != null){
				return thisTEOW.getPerformance();
			}
			return 1.0;
		}

		public TEOWPhase getTEOWPhase(){
			if(thisTEOW != null){
				return thisTEOW.getPhase();
			}
			return TEOWPhase.FINISHED;
		}

		public double getRevealedError(){
			if(thisTEOW != null){
				return thisTEOW.getRevealedError();
			}
			return 0.0;
		}

		public boolean canActorDoIt(){
			return true;
		}

		public double requiredKnowledge(){
			return 0.0;
		}

		public double availableKnowledge(Actor actor){
			return 0.0;
		}

		public TaskState state(){
			return state;
		}

		public TaskType taskType(){
			return taskType;
		}

		public void isInProgress(){
			state = TaskState.INPROGRESS;
		}

		public void isReady(){
			state = TaskState.READY;
			thisTEOW.isReady();
		}

		public void isComplete(){
			state = TaskState.COMPLETE;
		}

		public void setState(TaskState thisState){
			state = thisState;
		}


		//Set and Get EVM parameters

		public int getIdealEffort(){
			if(thisTEOW == null){
				return 0;
			}
			return thisTEOW.getIdealEffort();
		}

		public int getPlannedEffort(){
			if(thisTEOW == null){
				return 0;
			}
			return thisTEOW.getPlannedEffort();
		}

		public int getActualEffort(){
			if(thisTEOW == null){
				return 0;
			}
			return thisTEOW.getActualEffort();
			//return getTotalEffort();
		}

		public double getCostPerformance(){
			if(getPlannedEffort() > 0){
				return (double) ((double) getActualEffort() / (double) getPlannedEffort() );
			}
			return 1.0;
		}

		public void setPV(int pv){

		}

		public int getPV(){
			return 0;
		}


		public void setEV(int ev){

		}

		public int getEV(){
			return 0;
		}

		public double getPercentageComplete(){
			return 0;
		}

		public void setETC(int etc){

		}

		public int getETC(){
			return 0;
		}

		public void setBAC(int bac){

		}

		public int getACWP(){
			return thisTEOW.getACWP();
		}

		public int getBAC(){
			return 0;
		}

		public int getEBAC(){
			return 0;
		}

		//public int getACWP(){
		//	return ACWP;
		//}

		public double getSV(){
			return 0;
		}

		public double getSPI(){
			return 0;
		}

		public double getError(){
			return 0;
		}

		public double getACWPBACRatio(){
			return 0;
		}

		public void estimate(Actor actor){

		}

		public void estimate(){

		}

		public void estimateETC(){

		}

		public void progress(){

		}

		public void wasteEffort(){

		}

}
