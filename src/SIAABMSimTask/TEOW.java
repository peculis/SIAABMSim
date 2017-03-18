package SIAABMSimTask;

import SIAABMSimAgent.Artifact;
import SIAABMSimConnection.Dependency;
import SIAABMSimAgent.Actor;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.simSettings;

import java.text.*;

//This class implements Terminal Element Of Work (TEOW) of Earned Value Management (EVM).

//07 May 2010: I did a clean-up on this class, removing some redundancies and unused variables.

//18-17 Nov 2009: I fixed the TEOW.progress(). The calculation of Actor's Useful Knowledge was not correct.
//k and beta represent the knowledge requred
//actorK and alpha represent the Actor's knowledge
//
//actorK = Math.sqrt(Math.pow(actorKin, 2) + Math.pow(actorKout, 2));
//alpha = Math.atan(kout / kin ); // actor's knowledge angle
//
//teta = beta - alpha;
//actorUsefulK = actorK * Math.cos(teta);
//
//va = percentageComplete * actorUsefulK/k * taskCoefficient; va is the actual task value


public class TEOW {

	public enum TEOWPhase { NOTSTARTED, EXECUTION, REVIEW, REWORK, FINISHED};

	private SIAABMSimConnection.Dependency dependency;
	private int iPV; //ideal Planned Value;
	
	//Earned Value Management - EVM
	private int PV; //allocated Planned Value - this is the same as BCWS (Budget Cost of Work Schedule);
	private int EV;  // Earned Value - this is the same as BCWP (Budget Cost of Work Performed);
	private double ev; //Calculated Earned Value;

	private double percentageComplete = 0.0; // progress
	//I will not use ACWP for simplification, assuming that ACWP = BCWP.

	private int ACWP; //Actual Cost of Work Performed: increases every time the Actor works on the task, whether EV progresses or not.
	private int thisACWP; //ACWP for the current task

	private int BAC; //Budget At Completion;
	private int thisBAC; //BAC for the current task

	private int EBAC; //Estimated Budget At Completion;
	private int thisEBAC; //EBAC for the current task

	private int ETC; // Estimate To Complete: ETC = EBAC - BCWP;
	private double SV;  //Schedule Variance: SV = EV - PV or SV = BCWP - BCWS;
	private double SPI; //Schedule Performance Index: SPI = EV/PV or SPI = BCWP/BCWS;

	private TEOWPhase phase = TEOWPhase.NOTSTARTED;

	private double taskCoefficient; // real (ideal) task value
	private double va = 0.0; // actual task value
	private double vError = taskCoefficient;
	private double revealedError = 0.0;
	private Artifact artifact;
	
	//These are the attributes from the former Task class. 
	//Several of these attributes can be obtained from the "dependency" and its Artifacts.I need to reorganize this.
	private TransformationMatrix transformation;
	private int id;
	private int r; // row
	private int c; // column

	private double vaz = 0.0; // previous actual task value;
	private ProductVector.ProductCategory pinCat; // Category of Pin.
	private ProductVector.ProductCategory poutCat; // Category of Pout;
	private double kin; // knowledge of input dimension
	private double kout; // knowledge of output dimension
	private double k; //required knowledge to perform the task
	private double beta; //required knowledge angle

	//These attributes should come from EVM.
	private double er = 100.0; // effort required
	private double ee = 100.0; // effort estimated
	private double ea = 1.0; // productive effort effort
	private double es = 1.0; //effort spent
	private double pz = 0.00; // previous sample

	private double incrementSize;

	private double effortFactor;

	private double pin = 1.0;
	private double pinz = 0.0;

	private DecimalFormat df;

	private probability p;
	private simSettings simS;

	private String name;
	private String sName;

	public TEOW (Dependency d){
		p = probability.getInstance();
		simS = simSettings.getInstance();
		df = new DecimalFormat("#.####");
		dependency = d;
		//state = TEOWState.NOTALLOCATED;
		phase = TEOWPhase.NOTSTARTED;
		r = dependency.getRow();
		c = dependency.getColumn();
		taskCoefficient = d.getDependencyCoefficient();
		transformation = d.getToArtifact().getTransformation();
		artifact = d.getToArtifact();
		name = artifact.getName();
		sName = artifact.getShortName();

		effortFactor = 1.0;

		incrementSize = 1.0;

		incrementSize = artifact.incrementSize();

		//System.out.println("TEOW " + artifact.getName() + " Increment Size = " + incrementSize);


		//Calculate iPV from the dependency.
		//iPV is obtained from the Coefficient of the Transformation Matrix;
		va = 0;
		vError = taskCoefficient - va;
		//System.out.println(sName);
		
		if(sName.equals("Application")){
			//System.out.println("App");
			iPV = (int) ((taskCoefficient * 2700.0) * incrementSize);
			
			//iPV = (int) ((1000.0) * incrementSize);
		}
		if(sName.equals("Capabilities")){
			//System.out.println("Cap");
			iPV = (int) ((taskCoefficient * 2700.0) * incrementSize);
			//iPV = (int) ((1000.0) * incrementSize);
		}
		if(sName.equals("SysEng")){
			//System.out.println("SysE");
			iPV = (int) ((taskCoefficient * 1500.0) * incrementSize);
		}
		if(sName.equals("SysArchitect")){
			//System.out.println("SysA");
			iPV = (int) ((taskCoefficient * 4200.0) * incrementSize);
		}
		if(sName.equals("SoftEng")){
			//System.out.println("SwE");
			iPV = (int) ((taskCoefficient * 750.0) * incrementSize);
		}
		
		if(sName.equals("SoftDev")){
			//System.out.println("SwD");
			iPV = (int) ((taskCoefficient * 1100.0) * incrementSize);
		}

		//This should never happen because taskCoeeficient is never 0 (by the definition of task).
		if (iPV < 50){
			iPV = 50;
		}
		//Required Knowledge to perform the task.
		kin = d.getKin(); //required input knowledge
		if(kin < 0.01){
			kin = 0.01;
		}
		kout = d.getKout(); // required output knowledge
		if(kout < 0.01){
			kout = 0.01;
		}
		
		k = Math.sqrt(Math.pow(kin, 2) + Math.pow(kout, 2)) / Math.sqrt(2.0); // required knowledge amplitude
		beta = Math.atan(kout / kin ); // required knowledge angle

		BAC = 0;
		EBAC = 0;
		ACWP = 0;

		thisBAC = 0;
		thisEBAC = 0;
		thisACWP = 0;


		EV = 0;
		ev = EV;
		ACWP = 0;
		percentageComplete = 0;
		ETC = EBAC - (int) EV;;
		SV = EV - PV;

		if(PV < 1){
			SPI = EV;
		}
		else{
			SPI = EV / PV;
		}
	}

	private void calculateEV(Actor actor){
		if(effortFactor == 0){
			System.out.println("EffortFactor = 0.0");
		}
		ev = ev + 10.0 * simS.getTaskProgressRate() * effortFactor * 1.2 * actor.workEfficiency();
		EV = (int) ev;
	}

	public double getPerformance(){
		double p;
		if( ev == 0.0 || thisACWP == 0 || thisBAC == 0){
			return 1.0;
		}
		p = percentageComplete / ((double) thisACWP / (double) thisBAC);
		return p;
	}

	public void effortFactor(double factor){
		effortFactor = factor;
		if(effortFactor == 0){
			System.out.println("Set lmitEffort = 0.0");
		}
		//iPV = (int) ((double) iPV * limitEffort);
	}

	public double requiredKnowledge(){
		//System.out.println("******* Required Knowledge = " + df.format(k));
		return k;
	}

	//Set and Get EVM parameters

	public int getIdealEffort(){
		return iPV;
	}

	public int getPlannedEffort(){
		return BAC;
	}

	public int getActualEffort(){
		return ACWP;
	}

	public void setPV(int pv){
		PV = pv;
	}

	public int getPV(){
		return PV;
	}


	public void setEV(int earnedV){
		EV = earnedV;
	}

	public int getEV(){
		return EV;
	}

	public double getPercentageComplete(){
		return percentageComplete;
	}

	public void setETC(int etc){
		ETC = etc;
	}

	public int getETC(){
		return ETC;
	}

	public void setBAC(int bac){
		BAC = bac;
	}

	public int getBAC(){
		return BAC;
	}

	public int getEBAC(){
		return EBAC;
	}

	public int getACWP(){
		return ACWP;
	}

	public double getSV(){
		return SV;
	}

	public double getSPI(){
		return SPI;
	}

	public double getError(){
		return (1 - va/taskCoefficient);
	}

	public double quality(){
		double q;
		q = va/(percentageComplete * taskCoefficient);
		if(q > 1.0){
			q = 1.0;
		}
		return q;
	}

	public double getACWPBACRatio(){
		return (double) thisACWP / (double) thisBAC;
	}

	public double getRevealedError(){
		return revealedError;
	}

	public void sendForExecution (){
		phase = TEOWPhase.EXECUTION;
	}


	public void sendForReview (){
		phase = TEOWPhase.REVIEW;
	}

	public void sendForRework (){
		phase = TEOWPhase.REWORK;
	}

	public void setDone(){
		transformation.setActualValue(va, r, c);
		phase = TEOWPhase.FINISHED;
		//System.out.println(getName() + " taskCoefficient " + df.format(taskCoefficient) + " va = " + df.format(va) + " percentageComplete = " + df.format(percentageComplete) + 
		//		" actorUsefulK/k = " + " vError = " + df.format(vError));
	}

	public boolean isDone(){
		if(phase == TEOWPhase.FINISHED){
			return true;
		}
		else{
			return false;
		}
	}

	public void isReady(){
		BAC = BAC + thisBAC;
		ACWP = ACWP + thisACWP;
		//System.out.println("TEOW = " + getName() + " " + phase + " thisACWP = " + thisACWP + " is Ready");
	}

	public Dependency getDependency(){
		return dependency;
	}

	public int getIdealPV(){
		return iPV;
	}


	public String getName(){
		return name;
	}


	public TEOWPhase getPhase(){
		return phase;
	}


	public boolean XnewBFI(){
		return artifact.BFIAvailable();
	}

	public boolean isBFIVersionReady(int BFIVersion){
		Artifact a;
		a = (Artifact) dependency.getFromAgent();
		return a.isBFIVersionReady(BFIVersion);
	}


	public boolean allDependenciesAreComplete(){
		if(dependency.isDependencyComplete()){
			return true;
		}
		return false;
	}

	public void printTEOW(){
		System.out.println("Artifact = " + artifact.getCategory() +
				" TaskCoefficient = " + taskCoefficient +
				" Kin = " + df.format(kin) +
				" Kout = " + df.format(kout) +
				" IdealPV = " + iPV +
				" all Dependencies Complete = " + allDependenciesAreComplete());
	}

	public void estimate (Actor actor){

		//"estimate" takes into consideration the Actor's attributes to estimate the task (TEOW).
		//"estimate" calculates BAC and sets the initial values for all EVM parameters.

		double motivation, experience, knowledge, behaviour;

		motivation = actor.getMotivation();
		experience = actor.getExperience();
		knowledge = actor.getKnowledge();
		behaviour = actor.getBehaviour();

		//iPV was calculated from the dependency at instantiation.
		//iPV is obtained from the Coefficient of the Transformation Matrix;

		if(iPV < 10){
			iPV = 10;
			//System.out.println("*** estimate iPV = " + iPV);
		}

		thisBAC = (int) (effortFactor * (double) iPV);
		//thisBAC = iPV;
		thisEBAC = thisBAC;
		ETC = thisEBAC;
		EV = 0;
		ev = EV;
		PV = thisEBAC;
		thisACWP = 0;
	}

	public void estimateReview(Actor actor){

		double motivation, experience, knowledge, behaviour;

		motivation = actor.getMotivation();
		experience = actor.getExperience();
		knowledge = actor.getKnowledge();
		behaviour = actor.getBehaviour();

		if(iPV < 10){
			iPV = 10;
			//System.out.println("*** estimate Review iPV = " + iPV);
		}

		thisBAC = (int) (effortFactor * (double) iPV / 4);
		//thisBAC = iPV;
		thisEBAC = thisBAC;
		ETC = thisEBAC;
		EV = 0;
		ev = EV;
		PV = thisEBAC;
		thisACWP = 0;
	}

	public void estimateRework(Actor actor){

		double motivation, experience, knowledge, behaviour;

		motivation = actor.getMotivation();
		experience = actor.getExperience();
		knowledge = actor.getKnowledge();
		behaviour = actor.getBehaviour();

		//System.out.println("********* Error = " + df.format(vError));

		if(iPV < 10){
			iPV = 10;
			//System.out.println("*** estimate Rework iPV = " + iPV);
		}

		thisBAC = (int) (effortFactor * (double) iPV / 2);
		//thisBAC = iPV;
		thisEBAC = thisBAC;
		ETC = thisEBAC;
		EV = 0;
		ev = EV;
		PV = thisEBAC;
		thisACWP = 0;

	}

	public void estimateETC (Actor actor){

		//"estimateETC" takes into consideration the Actor's attributes to estimate the ETC.

		double motivation, experience, knowledge, behaviour;

		motivation = actor.getMotivation();
		experience = actor.getExperience();
		knowledge = actor.getKnowledge();
		behaviour = actor.getBehaviour();
	}

	public double actorUsefulKnowledgeRatio(Actor actor){
		return actorUsefulKnowledge(actor) / k;
	}

	private double actorUsefulKnowledge(Actor actor){
		double actorExperience, actorKnowledge;
		double actorUsefulK;
		double actorKin, actorKout, actorK, teta;
		double alpha;

		actorExperience = actor.getExperience();
		actorKnowledge = actor.getKnowledge();

		actorKin = actorKnowledge * actorExperience;
		actorKout = actorKnowledge;

		//Limit Agent's useful knowledge.
		if (actorKin > kin) {
			actorKin = kin;
		}
		if (actorKout > kout) {
			actorKout = kout;
		}
		actorK = Math.sqrt(Math.pow(actorKin, 2) + Math.pow(actorKout, 2));
		alpha = Math.atan(kout / kin ); // actor's knowledge angle
		teta = beta - alpha;
		actorUsefulK = actorK * (Math.cos(teta) /  Math.sqrt(2.0));
		actorUsefulK = (actorK /  Math.sqrt(2.0)) * actor.qualityFactor();

		return actorUsefulK;
	}

	public boolean canActorDoIt(Actor actor){
		double actorUsefulK;

		actorUsefulK = actorUsefulKnowledge(actor);

		if(actorUsefulK/k < 1.0){
			//System.out.println("THE ACTOR CANNOT DO THE TASK DUE TO LACK OF KNOWLEDGE");
			return false;
		}
		else{
			return true;
		}
	}

	public double availableKnowledge(Actor actor){		
		return actorUsefulKnowledge(actor);
	}

	public void wasteEffort(){
		thisACWP = thisACWP + 1;
	}


	//public void progress (double agentMotivation, double agentExperience, double agentKnowledge){
	public void progress (Actor actor){

		//"progress" takes into consideration the Actor's attributes to progress the task (TEOW).
		//"progress" calculates the actual task value (va) and the current values for all EVM parameters.

		double actorMotivation, actorExperience, actorKnowledge;
		double actorUsefulK;
		double actorKin, actorKout, actorK, teta;
		double alpha;
		double actorPerf; // performance;

		double pc;

		actorMotivation = actor.getMotivation();
		actorUsefulK = actorUsefulKnowledge(actor);
		actorPerf = 0.28 + 0.9 * actorMotivation; // The Actor's performance can be > 100%
		if(actorPerf < 0.1){
			actorPerf = 0.1;
		}
		
		thisACWP = thisACWP + 10;

		switch (phase){
		case NOTSTARTED:{
			//Do nothing.
			//System.out.println("TEOW phase = NOTSTARTED");
			break;
		}
		case EXECUTION:{
			//System.out.println("TEOW phase = EXECUTION");
			//BCWS increases with time when the task is in progress: see step().
			
			calculateEV(actor);

			//Progress is a function of the Actor's performance and EV.

			thisEBAC = (int) ((double)thisBAC / actorPerf); //EBAC increases when the Actor's Performance decreases.

			if(thisEBAC == 0){
				//System.out.println("EBAC is zero in TEOW EXECUTION");
				thisEBAC = thisBAC;
			}

			ETC = thisEBAC - (int) EV;
			SV = EV - (double) PV;
			if(PV < 1){
				SPI = EV;
			}
			else{
				SPI = EV / PV;
			}

			percentageComplete = (double) ((double) EV / (double) thisEBAC);


			if (percentageComplete >= 0.99){
				percentageComplete =  1.0;
				//System.out.println("Percentage Complete = 1.0");
			}

			if( k == 0.0){
				System.out.println("k is zero in TEOW EXECUTION");
			}

			va = (percentageComplete * actorUsefulK/k) * taskCoefficient;

			//System.out.println(getName() + " taskCoefficient " + df.format(taskCoefficient) + " va = " + df.format(va) + " percentageComplete = " + df.format(percentageComplete) + 
			//	" actorUsefulK/k = " + df.format(actorUsefulK/k)  + " vError = " + df.format(vError));

			if (va  >= taskCoefficient){
				va = taskCoefficient;
				//System.out.println("????????????????????? VA is TOO BIG");
			}

			//vError = (1 - va/taskCoefficient);

			vError = taskCoefficient - va;

			break;
		}
		case REVIEW:{

			//System.out.println("------------- TEOW REVIEW");
			//BCWS increases with time when the task is in progress: see step().

			//EV = EV + 1; //EV increases every time the task is updated. However, the task may not progress as much. 
			calculateEV(actor);

			//Progress is a function of the Actor's performance and EV.

			thisEBAC = (int) ((double)thisBAC / actorPerf); //EBAC increases when the Actor's Performance decreases.

			if(thisEBAC == 0){
				//System.out.println("EBAC is zero in TEOW EXECUTION");
				thisEBAC = thisBAC;
			}

			ETC = thisEBAC - (int) EV;
			SV = EV - (double) PV;
			if(PV < 1){
				SPI = EV;
			}
			else{
				SPI = EV / PV;
			}

			percentageComplete = (double) ((double) EV / (double) thisEBAC);

			vError = taskCoefficient - va;


			if (percentageComplete >= 0.99){
				percentageComplete = 1.0;
			}


			revealedError = percentageComplete * actorUsefulK/k * vError;

			if(revealedError == 0.0){
				//System.out.println("revealedError = 0.0");
			}
			else{
				//System.out.println("********* " + df.format(getRevealedError()));
			}

			break;
		}
		case REWORK:{

			//BCWS increases with time when the task is in progress: see step().

			calculateEV(actor);

			//Progress is a function of the Actor's performance and EV.

			thisEBAC = (int) ((double)thisBAC / actorPerf); //EBAC increases when the Actor's Performance decreases.

			if(thisEBAC == 0){
				//System.out.println("EBAC is zero in TEOW EXECUTION");
				thisEBAC = thisBAC;
			}

			ETC = thisEBAC - (int) EV;
			SV = EV - (double) PV;
			if(PV < 1){
				SPI = EV;
			}
			else{
				SPI = EV / PV;
			}

			percentageComplete = (double) ((double) EV / (double) thisEBAC);

			if (percentageComplete >= 0.99){
				percentageComplete = 1.0;
			}

			if(revealedError > 0.0){
				//System.out.println("TEOW Revealed Error = " + df.format((percentageComplete));
			}

			va = va + (percentageComplete * actorUsefulK/k) * 0.3 * revealedError;


			if (va > taskCoefficient){
				va = taskCoefficient;
			}

			vError = taskCoefficient - va;

			transformation.setActualValue(va, r, c);

			break;
		}
		case FINISHED:{
			//Do nothing.
			break;
		}
		}
	}

	public void step(){

	}

}
