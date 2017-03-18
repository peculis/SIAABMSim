package SIAABMSimTask;

//Coded by Ricardo Peculis.
//Initial: sometime in 2009.

import SIAABMSimSocial.Organization;
import SIAABMSimAgent.Artifact;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.CeoActor;
import SIAABMSimAgent.SeniorManagerActor;
import SIAABMSimConnection.Dependency;
import SIAABMSimTask.ProductVector.ProductCategory;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.simSettings;

import java.util.ArrayList;

public class Contract {

	private Actor seniorManager;
	private Organization customer;
	private Organization provider;
	private boolean assigned;
	private boolean inProgress;
	private String thisTask;
	private String previousTask;
	private String nextTask;
	private Transformation transformation; //Transformation subject to this Baseline;

	private ProductVector Pin; // 10Aug2010
	private ProductVector Pout; // 10Aug2010
	private TransformationMatrix m; // 10Aug2010

	private ArrayList <Artifact> myBFI; //Buyer Furnished Information.
	private ArrayList <Artifact> myDeliverables; //Deliverables.
	private ArrayList <TEOW> myTEOWList;


	private boolean contractIsComplete;
	private boolean started;
	private int contractAward; //value of this contract.
	private int targetDuration;
	private int targetIncrementDuration;
	private int targetIncrementEffort;
	private int totalDuration; //duration of this contract.
	private int incrementDuration; //duration of this increment
	private int incrementEffort; //effort of this increment

	private int productiveEffort;
	private int overheadEffort;
	private int totalEffort; //effort of this contract.
	private int targetEffort;

	private double myEfficiency = 0.0;
	private double myAverageEfficiency = 0.0;
	private int nUpdates = 0;
	private boolean increasedPE = false;
	private boolean increasedOE = false;


	private simSettings simulationSettings;

	private int nIncrements;
	private int currentIncrement;
	private double reworkFactor;

	//private DevelopmentPhase devPhase;
	//private ArrayList <DevelopmentPhase> devPhaseList; //List of Development Phases (one or many).

	private Contract firstContract;
	private Contract previousContract;
	private Contract nextContract;

	private boolean deliveredBFI;
	private int newBFIVersion;
	private int currentBFIVersion;

	private boolean deliverablesReady;
	private boolean deliverablesDelivered;
	private int currentDeliverablesVersion;

	//Contract is another form to describe a Transformation. Contracts are created by TaskConfiguration.

	public Contract (Transformation sow, ArrayList <Artifact> BFI, boolean Complete){
		double a;
		//System.out.println("ENTER Create Contract");

		provider = null;
		customer = null;
		transformation = sow;
		assigned = false;
		inProgress = false;

		deliverablesReady = false;
		deliverablesDelivered = false;
		currentDeliverablesVersion = 0;

		firstContract = null;
		previousContract = null;
		nextContract = null;

		targetDuration = 0;
		targetIncrementDuration = 0;
		productiveEffort = 0;
		overheadEffort = 0;
		totalEffort = 0;
		targetEffort = 0;
		targetIncrementEffort = 0;
		totalDuration = 0;
		incrementDuration = 0;
		totalEffort = 0;
		incrementEffort = 0;

		Pin = transformation.getPin();
		Pout = transformation.getPout();
		Pout.setContract(this);
		thisTask = Pout.getName();
		previousTask = Pin.getName();
		nextTask = "nil";
		m = transformation.getTransformation();
		seniorManager = null;
		sow.setContract(this);

		//devPhaseList = new ArrayList<DevelopmentPhase>();
		contractIsComplete = Complete;

		simulationSettings = simulationSettings.getInstance();
		nIncrements = simulationSettings.getNumberOfTaskIncrements();
		currentIncrement = 0;
		reworkFactor = simulationSettings.getReworkFactor();

		myDeliverables = new ArrayList<Artifact>(); // 10Aug2010
		myTEOWList = new ArrayList<TEOW>(); // 10Aug2010

		if(contractIsComplete){
			//deliveredBFI = true;
			started = true;
			newBFIVersion = 1;
			currentBFIVersion = 0;
			myBFI = new ArrayList<Artifact>(); //BFIList will be empty;
			//13Aug2010 ****** transformation.acceptPin();
			transformation.updateIdealPout();
			//startNewDevelopmentPhase();

			//System.out.println("CONTRACT " + thisTask + " was cretated COMPLETE");
		}
		else{
			//deliveredBFI = false;
			started = false;
			newBFIVersion = 0;
			myBFI = BFI;
			currentBFIVersion = 0;
			//startNewDevelopmentPhase();
		}
		createDeliverables();
		transformation.setBFI(myBFI);
		transformation.setDeliverables(myDeliverables);
		//System.out.println(" EXIT Create Contract");
	}

	public void incrementDuration(){
		totalDuration = totalDuration + 1;
		incrementDuration = incrementDuration + 1;
	}

	public void setContractTargets(int effort, int duration){
		targetEffort = effort;
		targetDuration = duration;
	}

	public void setIncrementTargets(int effort, int duration){
		targetIncrementEffort = effort;
		targetIncrementDuration = duration;
	}

	public int getTargetDuration(){
		return targetDuration;
	}

	public int getTargetIncrementDuration(){
		return targetIncrementDuration;
	}

	public int getTargetEffort(){
		return targetEffort;
	}

	public int getTargetIncrementEffort(){
		return targetIncrementEffort;
	}

	public int getDuration(){
		//return totalDuration / 2;
		return totalDuration;
	}

	public int getIncrementDuration(){
		return incrementDuration;
	}


	public void increaseProductiveEffort(int n){
		increasedPE = true;
		productiveEffort = productiveEffort + n;
		//System.out.println("Contract " + thisTask + " +++");
	}


	public void increaseOverheadEffort(int n){
		increasedOE = true;
		overheadEffort = overheadEffort + n;
		//System.out.println("Contract " + thisTask + " ---");
	}

	public int getProductiveEffort(){
		return productiveEffort;
	}

	public int getEffort(){
		return (productiveEffort + overheadEffort);
	}

	public double getEfficiency(){
		if(getEffort() > 0.0){
			//System.out.println("+++");
			return ((double) productiveEffort / (double) getEffort());
		}
		else{
			//System.out.println("---");
			return 0.0;
		}
	}

	public double XgetEfficiency(){
		double efficiency;
		if(increasedPE || increasedOE){
			increasedPE = false;
			increasedOE = false;
			if(getEffort() > 0){
				efficiency = (double) ((double) productiveEffort / (double) getEffort());
			}
			else{
				efficiency = 0.0;
			}	
			if(efficiency > 0.0){
				myEfficiency = efficiency;
				nUpdates = nUpdates + 1;
				myAverageEfficiency = (myAverageEfficiency + myEfficiency);
			}
		}
		if(getEffort() > 0.0){
			return (productiveEffort / getEffort());
		}
		return 0.0;
	}

	//########### The following code creates and manages the Development Phase.
	private void XcreateDevelopmentPhase(){
		String name, deliverableName;
		TransformationMatrix m;
		name = transformation.getName();
		m = transformation.getTransformation();
		deliverableName = transformation.getPout().getName();
		//System.out.println("*********** CREATE DEVELOPMENT PHASE");
		currentBFIVersion = newBFIVersion;
		if(contractIsComplete){
			//System.out.println("CONTRACT " + thisTask + " was cretated COMPLETE");
		}
		//devPhase = new DevelopmentPhase(transformation, myBFI, contractIsComplete);
		//myDeliverables = devPhase.getDeliverables();
		//myTEOWList = devPhase.getTEOW();
	}

	public void XstartNewDevelopmentPhase(){
		XcreateDevelopmentPhase();
		inProgress = true;
		assigned = false;
	}

	public void XrestartNewDevelopmentPhase(){
		//devPhase = devPhaseList.get(0);
		//devPhase.restart();
		inProgress = true;
		assigned = false;
	}
	//#################

	//########### The following code is my attempt to remove the concept of Development Phase and manage Artifacts and TEOW directly
	private void createDeliverables(){
		Artifact artifact, deliverable;
		String aName;
		//System.out.println("@@@@@@@@@ " + thisTask + " Enter Create Deliverables");
		for (int a = 0; a<Pout.size(); a++){
			artifact = new Artifact(a, transformation);
			aName = artifact.getName();
			if (contractIsComplete){
				artifact.Complete();
			}
			myDeliverables.add(artifact);
			artifact.setX(0);
			artifact.setY(0);
		}
		if(! contractIsComplete){
			findDependencies();
		}
		//System.out.println("@@@@@@@@@ " + thisTask + " Created Deliverables and myDeliverables size = " + myDeliverables.size());
	}

	private Artifact getDependentArtifact(ProductVector p, int artifact){
		Artifact dartifact;
		dartifact = null;
		//System.out.println("@@@@@@@@@ " + thisTask + " getDependentArtifact myBFIsize = " + myBFI.size());
		for (int b=0; b<myBFI.size(); b++){
			//System.out.println("@@@@@@@@@ " + thisTask + " getDependentArtifact" + b);
			dartifact = myBFI.get(b);
			if (dartifact.isThatYou(p, artifact)){
				//Found Dependent Artifact
				//System.out.println("@@@@@@@@@ Found DependentArtifact");
				return dartifact;
			}
			else{
				//System.out.println("@@@@@@@@@ NOT ME!");
			}
		}
		//Dependent Artifact was not found.
		System.out.println("!!!!!!!!! NOT FOUND Dependent Artifact " + p.getName() + " " + artifact);
		return null;
	}

	private void findDependencies(){
		Artifact deliverable, bfi;
		Dependency dependency;
		TEOW teow;
		TransformationMatrix t;
		double c;
		int column; //This is the Product Artifact Number, or its position on the Product Vector.

		//System.out.println("@@@@@@@@@ " + thisTask + " findDependencies");
		bfi = null; //bfi is the dependent artifact.
		for (int d=0; d<myDeliverables.size(); d++){
			deliverable = myDeliverables.get(d);
			//System.out.println("Artifact Name = " + artifact.getName());
			t = deliverable.getTransformation();
			for (int row=0; row<t.nrows(); row++){
				column = deliverable.getArtifactNumber();
				c = t.getIdealValue(row, column);
				if (c != 0.0) {
					//System.out.println("%%%%%%%%%%%%%%%%%%%%% Artifact Name = " + deliverable.getName() + " row = " + row + " column = " + column + " c= " + c);
					bfi = getDependentArtifact(Pin, row);
					if (bfi != null){
						dependency = new Dependency(deliverable, bfi, c, row, column);
						deliverable.addDependency(dependency);
						deliverable.addBFI(bfi);
						//13Aug206 I will remove from here the creation of TEOW when the contract is created.
						//The creation of TEOW should be dynamic and as needed, which was my original idea.
						//teow = new TEOW(dependency);
						//deliverable.addTEOW(teow);
					}
					else {
						//Here is the error: BFI is NULL. Why?
						System.out.println("***** BFI is NULL **** Error to obtain Dependent Artifact for " + 
								deliverable.getName() + " artifact = " + deliverable.getArtifactNumber());
					}
				}
			}
		}
	}

	public void assignSeniorManager(Actor actor){
		seniorManager = actor;
	}

	public Actor getSeniorManager(){
		return seniorManager;
	}

	public void setIncrementSize(double increment){
		transformation.setIncrementSize(increment);
	}

	public double getIncrementSize(){
		return transformation.getIncrementSize();
	}

	public boolean error(){
		boolean result;
		result = false;
		for(int a=0; a<myDeliverables.size(); a++){
			if(myDeliverables.get(a).getError() > 0.0){
				return true;
			}
		}
		return result;
	}

	public double getError(){
		return transformation.deliverableError();
	}

	public double ExpectedError(){
		return transformation.expectedPoutError();
	}

	public double expectedEffectiveness(){
		return transformation.expectedEffectiveness();
	}

	public boolean deliverablesReady(){
		return deliverablesReady;
	}

	public boolean deliverablesDelivered(){
		return deliverablesDelivered;
	}


	public boolean isInProgress(){
		return transformation.isInProgress();
	}

	public void assigned(){
		assigned = true;
	}

	public boolean isAssigned(){
		return assigned;
	}

	public void baselineDeliverables(){
		//13Aug2010 ****** transformation.baselinePout();
		if(nextContract != null){
			//if(deliverablesReady){
			deliverablesReady = true;
			deliverablesDelivered = false;
			currentDeliverablesVersion = currentDeliverablesVersion +1;
			//}
		}
		else{
			currentDeliverablesVersion = currentDeliverablesVersion +1;
			deliverablesReady = true;
			deliverablesDelivered = true;
		}
	}

	public int currentDeliverablesVersion(){
		return currentDeliverablesVersion;
	}

	public void delivery(){
		if(nextContract != null){
			if(transformation.baselineChanged()){
				//System.out.println("CONTRACT DevPhase " + thisTask + " DELIVERED BFI TO NEXT CONTRACT");
				nextContract.BFIDelivery();
				deliverablesDelivered = true;
			}
			else{
				//System.out.println("CONTRACT DevPhase " + thisTask + " DELIVERED BFI ***** BASELINE DID NOT CHANGE!!!!!");
			}
		}
		else{
			deliverablesDelivered = true;
		}
	}

	public void baselineArtifact(int artifact){
		myDeliverables.get(artifact).baseline();
	}

	public void XcreateBaseline(int version){
		//transformation.baseline();
		for(int d=0; d<myDeliverables.size();d++){
			//myDeliverables.get(d).baseline();
			baselineArtifact(d);
		}
	}

	public int nIncrements(){
		return nIncrements;
	}

	public int currentIncrement(){
		return currentIncrement;
	}

	public void XreworkPin(){
		//currentBFIVersion is the same.
		transformation.reworkPin();
		deliverablesReady = false;
		deliverablesDelivered = false;
		//System.out.println("CONTRACT " + thisTask + " REWORK BFI Version = " + currentBFIVersion);
	}

	public void XacceptNewBFI(){
		//currentBFIVersion = newBFIVersion;
		started = true;
		currentBFIVersion = currentBFIVersion + 1;
		currentIncrement = currentIncrement +1;
		//13Aug2010 ****** transformation.acceptPin();
		deliverablesReady = false;
		deliverablesDelivered = false;
		//System.out.println("2 CONTRACT " + thisTask + " ACCEPTED BFI Version = " + currentBFIVersion);
	}

	public void BFIDelivery(){
		//deliveredBFI = true;
		newBFIVersion = newBFIVersion +1;
		//transformation.newPin();
		//System.out.println("1 CONTRACT " + thisTask + " BFI Delivery Version = " + newBFIVersion);
		if(!started){
			//acceptNewBFI();
		}
		//acceptNewBFI();
	}

	public boolean newBFI(){
		if(newBFIVersion > currentBFIVersion){
			return true;
		}
		else{
			return false;
		}
	}

	public void setFirstContract(Contract contract){
		firstContract = contract;
		transformation.setFirstTransformation(contract.getSOW());
	}

	public void setPreviousContract(Contract contract){
		previousContract = contract;
		if(transformation != null){
			if(previousContract != null){
				if(previousContract.getSOW() != null){
					transformation.setPreviousTransformation(previousContract.getSOW()); //there is a problem here.
				}
				else{
					//System.out.println("CONTRACT " + thisTask + " previousContract transformation is null for transformation.setPreviousTransformation ");
				}
			}
			else{
				//System.out.println("CONTRACT " + thisTask + " previousContract is null ");
			}
		}
		else{
			//System.out.println("CONTRACT " + thisTask + " transformation is null for transformation.setPreviousTransformation ");
		}
	}

	public Contract previousContract(){
		return previousContract;
	}

	public void setNextContract(Contract contract){
		nextContract = contract;
		if(transformation != null){
			if(transformation != null){
				if(nextContract != null){
					if(nextContract.getSOW() != null){
						transformation.setNextTransformation(nextContract.getSOW()); //there is a problem here.
					}
					else{
						//System.out.println("CONTRACT " + thisTask + " nextContract transformation is null for transformation.setNextTransformation ");
					}
				}
				else{
					//System.out.println("CONTRACT " + thisTask + " nextContract is null ");
				}
			}
		}
		else{
			//System.out.println("CONTRACT " + thisTask + " transformation is null for transformation.setNextTransformation ");
		}
	}

	public Contract nextContract(){
		return nextContract;
	}

	public void setCustomer(Organization org){
		customer = org;
	}

	public Organization getCustomer(){
		return customer;
	}

	public void setProvider(Organization org){
		provider = org;
	}

	public Organization getProvider(){
		return provider;
	}

	public Transformation getSOW(){
		return transformation;
	}

	public String TaskName(){
		return thisTask;
	}

	public String PreviousTaskName(){
		return previousTask;
	}

	public String nextTaskName(){
		return nextTask;
	}

	public void setNextTaskName(String name){
		nextTask = name;
		//if(devPhase != null){
		//	devPhase.setNextTaskName(name);
		//}
	}

	public ArrayList <Artifact> getDeliverables(){
		ArrayList <Artifact> emptyList = new ArrayList <Artifact>();
		//if(devPhase == null){
		//	//System.out.println("*********** DevPhase " + thisTask + " has EMPTY LIST OF DELIVERABLES");
		//	return emptyList;
		//}
		//return devPhase.getDeliverables();
		return myDeliverables;
	}

	public double getTaskError(){
		double taskError = 0.0;
		for(int a=0; a<myDeliverables.size(); a++){
			taskError = taskError + myDeliverables.get(a).getTaskError();
		}
		//taskError = taskError / myDeliverables.size();
		return taskError;
	}

	public ArrayList <Dependency> getDependencies(){
		ArrayList<Dependency> dependencyList;
		dependencyList = new ArrayList<Dependency>();
		for(int a=0; a<myDeliverables.size(); a++){
			dependencyList.addAll(myDeliverables.get(a).getDependencies());
		}
		return dependencyList;
	}

	public ArrayList <TEOW> getTEOW(){
		//ArrayList <TEOW> emptyList = new ArrayList <TEOW>();
		//if(devPhase == null){
		//	//System.out.println("*********** DevPhase " + thisTask + " has EMPTY LIST OF TEOW");
		//	return emptyList;
		//}
		//return devPhase.getTEOW();
		return myTEOWList;
	}

	//public boolean isComplete(){
	//	return devPhase.isComplete();
	//}

	public double getQuality(){
		return transformation.getQuality();
	}

	public double getEffectiveness(){
		return transformation.getPout().getEffectiveness();
	}

	public double getIdealEffectiveness(){
		return currentBFIVersion * transformation.getIncrementSize(); 
	}

	public void printEffectiveness(){
		//transformation.getPout().printEffectiveness();
	}

	public void XreworkCurrentBFI(){
		//System.out.println("**** " + thisTask + " REWORK to fix Errors ");
		inProgress = true;
		//devPhase.reworkCurrentBFI();
	}

	public void reworkNewBFI(){
		//System.out.println("****XXXX " + thisTask + " REWORK due to New BFI Version = " + currentBFIVersion);
		inProgress = true;
		//devPhase.reworkNewBFI();
	}

	public boolean baselined(){
		return deliverablesReady;
	}

	public boolean delivered(){
		return deliverablesDelivered;
	}

	public boolean DevelopmentPhaseComplete(){
		//if(devPhase == null){
		//	inProgress = false;
		//	assigned = false;
		//	deliverablesReady = false;
		//	return false;
		//}
		return false;
		//return devPhase.isComplete();
	}

	public int currentBFIVersion(){
		return currentBFIVersion;
	}

	public void printContractDetails(){
		//System.out.println("CONTRACT TASK = " + "contractIsComplete = " + contractIsComplete + " " + thisTask + " PREVIOUS TASK = " + previousTask + " NEXT TASK = " + nextTask);
	}

}
