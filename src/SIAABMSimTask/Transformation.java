package SIAABMSimTask;

import SIAABMSimTask.ProductVector.ProductCategory;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.simSettings;

import java.util.ArrayList;
import java.text.*;

import SIAABMSimAgent.Artifact;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.CeoActor;
import SIAABMSimAgent.SeniorManagerActor;
import SIAABMSimAgent.ActorProfile.RoleType;
import SIAABMSimConnection.Dependency;
//import SIAABMSimConnection.Task;

public class Transformation {

	private String Name;
	private boolean newPin;
	private ProductVector Pin; // Actual Input Product.
	private ProductVector Pout; // Actual Output Product.
	private TransformationMatrix T; // Transformation Matrix.
	private boolean inProgress;

	private Transformation nextTransformation;
	private Transformation previousTransformation;
	private Transformation firstTransformation;
	private Transformation lastTransformation;
	private Contract contract;

	private double KMean;
	private double KSDev;

	private simSettings simulationSettings;
	private probability pD;

	//This is a new design I introduced on 6 June 2010.
	//I moved the concept of Product Baseline from ProductVector to Transformation.

	private ArrayList <double []> PinBaselines;

	private double expEffectiveness;

	private int currentPinBaselineVersion;
	private int newPinBaselineVersion;
	private int currentPoutBaselineVersion;
	private int newPoutBaselineVersion;
	private double actualPinEffectiveness;

	private double incrementSize;
	private int numberOfIncrements;
	private double iterativeFactor;

	private double actualEffectiveness; //this is the effectiveness of the output product of this Transformation.
	private double actualTotalEffectiveness; //this is the Effectiveness of the final product, i.e. SwDev

	//These changes we introduced on 13Aug2010 to support Artifact baseline instead of Product baseline

	private ArrayList <Artifact> BFI;
	private ArrayList <Artifact> Deliverables;

	private boolean deliverableBaselineStatus [];

	private int deliveredInputArtifactBaselineVersion []; //Stores the delivered (or new) version of each input Artifact;
	private int currentInputArtifactBaselineVersion []; //Stores the current version of each input Artifact;
	private int previousInputArtifactBaselineVersion []; //Stores the previous version of each input Artifact;
	private int currentOutputArtifactBaselineVersion []; //Stores the current version of each output Artifact;
	private int previousOutputArtifactBaselineVersion []; //Stores the previous version of each output Artifact;

	//The following Product Vectors were already n the class before the change of 13Aug2010 and will still be used.

	private double newAppPinBaseline [];
	private double baselinePin []; //The baselineProduct is a copy of actualProduct when a baseline is defined
	private double previousBaselinePin []; 
	private double deltaPin []; // deltaBaseline = baseline - previousBaseline
	private double increment []; //the actual value of the current development
	private double lastIncrement []; //the last Increment is kept in case a rework is required.
	private double baselinePout [];
	private double previousBaselinePout [];
	private double actualPout [];
	private double expectedPout []; //expectedPout is the expected value of Pout based on Baseline Pin

	private boolean okToProceed;
	private boolean reworkIsNeeded;
	private boolean reworkInProgress;
	private int nRework;
	private boolean newPoutBaseline;

	private DecimalFormat df;

	public Transformation(Transformation t){

	}

	public Transformation (String name, String file, 
			ProductVector inputProduct, ProductCategory outputProductCategory,
			String outputProduct, double knowledgeMean, double knowledgeSD){

		ProductCategory inputProductCategory = inputProduct.getProductCategory();
		int taskId;

		//Task t;
		Name = name;
		KMean = knowledgeMean;
		KSDev = knowledgeSD;
		inProgress = false;
		contract = null;

		nextTransformation = null;
		previousTransformation = null;
		firstTransformation = null;
		lastTransformation = null;

		incrementSize = 1.0; //The default value of 1.0 for incrementSize will produce one incremental phase.

		simulationSettings = simulationSettings.getInstance();
		pD = pD.getInstance();
		numberOfIncrements = simulationSettings.getNumberOfTaskIncrements();
		incrementSize = 1.0 / numberOfIncrements;

		//System.out.println("Transformation - Number of Task Increments = " + nIncrements);
		//System.out.println("Transformation - Task Rework Allowed       = " + reworkAllowed);

		df = new DecimalFormat("#.##");
		//System.out.println("Create Transformation Matrix " + name + " from " + file);
		try {
			T = new TransformationMatrix (file);
			T.setKMean(KMean);
			T.setKSD(KSDev);

			//System.out.println("Created Transformtion Matrix ********.");
			T.setName(name);
			//T.printDetails();
			//System.out.println("Printed Details ********.");
		}
		catch (Throwable e){
			System.out.println("Problem reading Matrix file.");
		}

		newPin = false;
		Pin = inputProduct;

		Pout = new ProductVector(outputProductCategory, T.ncolumns());
		Pout.setName(outputProduct);

		this.updateIdealPout();
		Pout.saveIdealProduct();

		initialiseInternalProductVectors();

		okToProceed = false;
		//okToProceed = true;
		newPoutBaseline = false;
		nRework = 1;
	}

	public void setContract(Contract c){
		contract = c;
	}

	public Contract getContract(){
		return contract;
	}

	public boolean isDefficient(){
		if( pOutEffectiveness() < 1.0 * expectedEffectiveness()){
			return true;
		}
		return false;
	}

	public void iterativeFeedbackLearning(){
		Actor actor;
		CeoActor ceo;
		SeniorManagerActor sm;
		if(contract != null){
			//System.out.println("Provider = " + contract.getProvider().name());

			actor = contract.getSeniorManager();
			if(actor != null){
				if(actor.myRole() == RoleType.CEO){
					ceo = (CeoActor) actor;
					ceo.iterativeFeedback(contract);
				}
				if(actor.myRole() == RoleType.SeniorManager){
					sm = (SeniorManagerActor) actor;
					sm.iterativeFeedback(contract);
				}
			}
			else{
				contract.getProvider().iterativeFeedback();
			}

		}
	}

	public void OKtoProceed(double totalEffectiveness){
		okToProceed = true;
		//System.out.println("Transformation " + Pout.getName() + " received Feedback");
		actualTotalEffectiveness = totalEffectiveness;
		if((currentPinBaselineVersion + 1) <= numberOfIncrements){
			/*
			System.out.println("Transformation " + Pout.getName() + 
					" expected Effectiveness = " + df.format(expectedEffectiveness()) +
					" Effectiveness = " + df.format(pOutEffectiveness()) +
					" OK to Proceed with Increment = " + (currentPinBaselineVersion + 1) +
					" Total Effectiveness = " + df.format(actualTotalEffectiveness));
			 */
			if( pOutEffectiveness() < 1.0 * expectedEffectiveness()){
				/*
				System.out.println("AAA REWORK is NEEDED for " + Pout.getName() +
						" expected Effectiveness = " + df.format(expectedEffectiveness()) +
						" Effectiveness = " + df.format(pOutEffectiveness()));
						//+ " Target Effectveness = " + df.format(100.0 * simulationSettings.getTargetEffectiveness()) + "%");
				 */
				if(simulationSettings.getReworkFactor() > 0.0){
					reworkIsNeeded = true;
				}
			}
			else{
				reworkIsNeeded = false;
			}
		}
		else{
			if( pOutEffectiveness() < 1.0 * expectedEffectiveness()){
				/*
					System.out.println("BBB REWORK is NEEDED for Transformation " + Pout.getName() +
							" expected Effectiveness = " + df.format(expectedEffectiveness()));
							//+ " Effectiveness = " + df.format(pOutEffectiveness()));
				 */
				if(simulationSettings.getReworkFactor() > 0.0){
					reworkIsNeeded = true;

				}
				else{
					reworkIsNeeded = false;
				}
			}
			else{
				reworkIsNeeded = false;
			}
			if(lastTransformation != null){
				if(pD.drawChanceFromUniform(simulationSettings.getIterativeFactor())){
					lastTransformation.OKtoProceed(1.0);
				}
			}
			//System.out.println("Transformation " + Name + " Effectiveness = " + df.format(pOutEffectiveness()) +
			//		" Planned Development is Complete and " +
			//		" Total Effectiveness = " + df.format(actualTotalEffectiveness));
		}
	}

	private void clearDeliverableBaselineStatus (){
		for(int d=0; d<Pout.size(); d++){
			deliverableBaselineStatus [d] = false;
		}
	}

	private boolean allDeliverablesAreBaselined(){
		for(int d=0; d<Pout.size(); d++){
			if(!deliverableBaselineStatus [d]){
				return false;
			}
		}
		return true;
	}


	public void setIncrementSize(double increment){
		incrementSize = increment;
		//System.out.println("Transformation " + Name + " Increment Size = " + incrementSize);

	}

	public double getIncrementSize(){
		return incrementSize;
	}

	public void baselineDeliverableArtifact (Artifact a){
		deliverableBaselineStatus[a.getArtifactNumber()] = true;
		//System.out.println("Transformation " + Name + " received Deliverable baseline for Artifact = " + a.getName());

	}

	//Created on 6 June 2010 as part of the redesign
	private void initialiseInternalProductVectors(){

		//Create and initialize InputArtifactBaselineVersion
		//If the version number is zero, there is no baseline.
		deliveredInputArtifactBaselineVersion = new int [Pin.size()];
		currentInputArtifactBaselineVersion = new int [Pin.size()];
		previousInputArtifactBaselineVersion = new int [Pin.size()];
		for(int a=0; a<Pin.size(); a++){
			deliveredInputArtifactBaselineVersion [a] = 0;
			currentInputArtifactBaselineVersion [a] = 0;
			previousInputArtifactBaselineVersion [a] = 0;
		}

		//Create and initialize the status of Deliverables (Pout) baseline status.

		deliverableBaselineStatus = new boolean [Pout.size()];
		clearDeliverableBaselineStatus ();

		previousBaselinePin = new double [Pin.size()];
		baselinePin = new double [Pin.size()];
		deltaPin = new double [Pin.size()];
		PinBaselines = new ArrayList<double []>(); //I may not need this array of Pin baselines.

		//Create and initialize OutputArtifactBaselineVersion
		//If the version number is zero, there is no baseline.
		currentOutputArtifactBaselineVersion = new int [Pout.size()];
		previousOutputArtifactBaselineVersion = new int [Pout.size()];
		for(int a=0; a<Pout.size(); a++){
			currentOutputArtifactBaselineVersion [a] = 0;
			previousOutputArtifactBaselineVersion [a] = 0;
		}
		baselinePout = new double [Pout.size()];
		previousBaselinePout = new double [Pout.size()];
		increment = new double [Pout.size()];
		lastIncrement = new double [Pout.size()];
		actualPout = new double [Pout.size()];
		expectedPout = new double [Pout.size()];

		for(int i=0; i<Pin.size(); i++){
			previousBaselinePin[i] = 0.0;
			baselinePin[i] = 0.0;
			deltaPin[i] = 0.0;
		}
		PinBaselines.add(baselinePin);

		for(int i=0; i<Pout.size(); i++){
			baselinePout[i] = 0.0;
			previousBaselinePout[i] = 0.0;
			increment[i] = 0.0;
			lastIncrement[i] = 0.0;
			actualPout[i] = 0.0;
			expectedPout[i] = 0.0;
		}
		currentPinBaselineVersion = 0;
		newPinBaselineVersion = 0;
		currentPoutBaselineVersion = 0;
		newPoutBaselineVersion = 0;
		calculateExpectedPout();
	}

	//------------------------------------------------------------------
	//Following is the new code introduced with SIAABMSimV6 of 13Aug2010

	public void setBFI(ArrayList<Artifact> bfi){
		BFI = bfi;
	}

	public void setDeliverables(ArrayList<Artifact> deliverables){
		Deliverables = deliverables;
	}

	public void setFirstTransformation(Transformation transformation){
		//System.out.println(Name + " transformation.setPreviousTransformation ");
		firstTransformation = transformation;
		if(Pout.getName().equals("SoftDev")){
			firstTransformation.setLastTransformation(this);
		}
	}

	public void setLastTransformation(Transformation transformation){
		//System.out.println(Name + " transformation.setPreviousTransformation ");
		lastTransformation = transformation;
	}

	public Transformation getFirstTransformation(){
		return firstTransformation;
	}

	public void setPreviousTransformation(Transformation transformation){
		//System.out.println(Name + " transformation.setPreviousTransformation ");
		previousTransformation = transformation;
	}

	public Transformation getPreviousTransformation(){
		return previousTransformation;
	}

	public void setNextTransformation(Transformation transformation){
		//System.out.println(Name + " transformation.setNextTransformation ");
		nextTransformation = transformation;
	}

	public Transformation getNextTransformation(){
		return nextTransformation;
	}

	//----------------------------------------------------------------------

	private void calculateExpEffectiveness(){
		expEffectiveness = 0.0;
		for(int i=0; i<Pout.size(); i++){
			expEffectiveness = expEffectiveness + expectedPout[i];
		}
		//expEffectiveness = expEffectiveness / Pout.size();
	}

	public double expectedEffectiveness(){
		return expEffectiveness;
	}

	public boolean isInProgress(){
		return inProgress;
	}

	public void setInProgress(boolean value){
		inProgress = value;
	}

	public double deliverableError(){
		return Pout.error();
	}

	public double expectedPoutError(){
		double bPout, ePout, iPout;
		bPout = 0;
		ePout = 0;
		iPout = 0;
		for(int i=0; i<baselinePout.length; i++){
			bPout = bPout + baselinePout[i];
			ePout = ePout + expectedPout[i];
			iPout = iPout + Pout.getIdeal()[i];
		}
		//printExpectedPout();
		//printBaselinePout();
		//Pout.printIdealProduct();
		//System.out.println(Pout.getName() + " Expected    Error " + df.format(ePout - bPout));
		//System.out.println(Pout.getName() + " Deliverable Error " + df.format(iPout - bPout));
		return (ePout - bPout);
	}

	public String getName (){
		return Name;
	}

	public ProductVector getPin(){
		return Pin;
	}

	//public void baseline(){
	//	Pout.baseline();
	//}

	public void newPin(){
		double [] newPinBaseline;
		double v;
		newPinBaseline = new double [Pin.size()];

		newPinBaselineVersion = newPinBaselineVersion +1;
		//System.out.println(Pout.getName() + " newPinBaselineVersion = " + newPinBaselineVersion);
		actualPinEffectiveness = 0.0;
		if(Pout.getName().equals("Application")){
			v = incrementSize * newPinBaselineVersion;// 27 Jun 2010: Here is where the Need is split in a number of increments.
			if(v>= 1.0){
				v = 1.0;
			}
			//System.out.println(Pout.getName() + " newPinBaselineVersion = " + newPinBaselineVersion);
			//+ " baseline v = " + df.format(v));
			for(int i=0; i<Pin.size(); i++){
				newPinBaseline[i] = v;
				actualPinEffectiveness = actualPinEffectiveness + newPinBaseline[i];
			}
		}
		else{
			for(int i=0; i<Pin.size(); i++){
				newPinBaseline[i] = Pin.getActual()[i];
				actualPinEffectiveness = actualPinEffectiveness + newPinBaseline[i];
			}
		}
		PinBaselines.add(newPinBaseline);

		//System.out.println("Transformation RECEIVED New Pin for " + Pout.getName() + 
		//		" newPinBaselineVersion = " + newPinBaselineVersion + 
		//		" Actual Pin Effectiveness = " +  df.format(actualPinEffectiveness / 6.0));
	}

	private void calculateExpectedPout(){
		//System.out.println("#### " + Pout.getName() + " calculateExpected()");
		multiply(T, baselinePin, expectedPout);
		calculateExpEffectiveness();
		//printExpectedPout();
		//System.out.println("#### " + Pout.getName() + " Expected Effectiveness = " + expEffectiveness);
		//printBaselinePout();
		//Pout.printIdealProduct();
	}

	public void reworkPin(){
		//System.out.println("REWORK Pin for " + Pout.getName());	
		for(int d=0; d<Deliverables.size();d++){
			Deliverables.get(d).pleaseRework();
		}
		reworkInProgress = true;
		reworkIsNeeded = false;
	}

	private void pleaseReview(){
		for(int d=0; d<Deliverables.size();d++){
			Deliverables.get(d).pleaseReview();
		}
	}

	private void doItAgain(){
		for(int d=0; d<Deliverables.size();d++){
			Deliverables.get(d).newBFIisAvailable();
		}
	}

	private void acceptPin(){
		double baselinePinEffectiveness;
		baselinePinEffectiveness = 0.0;
		if(newPinBaselineVersion > currentPinBaselineVersion){
			currentPinBaselineVersion = currentPinBaselineVersion + 1;
			for(int i=0; i<Pin.size(); i++){
				previousBaselinePin[i] = baselinePin[i];
				baselinePin[i] = PinBaselines.get(currentPinBaselineVersion)[i];
				baselinePinEffectiveness = baselinePinEffectiveness + baselinePin[i];
				deltaPin[i] = (baselinePin[i] - previousBaselinePin[i]);
			}
			calculateExpectedPout();
			if(Pout.getName().equals("Application")){
				baselinePinEffectiveness = baselinePinEffectiveness / 6.0;
				/*
				System.out.println("Transformation ACCEPTED New Pin for " + Pout.getName() +
						" currentPinBaselineVersion = " + currentPinBaselineVersion +
						" Baseline Pin Effectiveness = " +  df.format(baselinePinEffectiveness) + 
						" Expected Pout Effectiveness = " +  df.format(expectedEffectiveness()));
				 */
			}
			//System.out.println("Transformation ACCEPTED New Pin for " + Pout.getName() +
			//		" currentPinBaselineVersion = " + currentPinBaselineVersion +
			//		" availablePinBaselineVersion = " + newPinBaselineVersion + 
			//	    " Baseline Pin Effectiveness = " +  df.format(baselinePinEffectiveness) + 
			//		" Expected Pout Effectiveness = " +  df.format(expectedEffectiveness()));
			//printDeltaPin();
			for(int d=0; d<Deliverables.size();d++){
				Deliverables.get(d).newBFIisAvailable();
			}
		}
		else{
			//System.out.println("#### " + Pout.getName() + " acceptPin(): There is no new Pin to be accepted");
		}
	}

	public boolean isPinBaselined(){
		if(currentPinBaselineVersion > 0){
			return true;
		}
		return false;
	}

	public boolean isPoutBaselined(){
		if(currentPoutBaselineVersion > 0){
			return true;
		}
		return false;
	}

	public void Xrework(){
		//restore last increment
		for(int i=0; i<increment.length; i++){
			increment[i] = lastIncrement[i]; //Restore last Increment to allow rework
		}
	}

	public boolean baselineChanged(){
		double variance;
		variance = 0.0;
		for(int i=0; i<baselinePout.length; i++){
			variance = variance + baselinePout[i] - previousBaselinePout[i];
		}
		if(variance < 0.01){
			return false;
		}
		return true;
	}

	private void baselineReworkPout(){ 
		double newPout[];
		newPout = new double [Pout.size()];
		updateIncrement();
		for(int i=0; i<Pout.size(); i++){
			//newPout[i] = baselinePout[i] + simulationSettings.getTaskReworkAllowed() * increment[i];
			newPout[i] = baselinePout[i] + increment[i];
			if(newPout[i] > expectedPout[i]){
				//System.out.println("*********");
				newPout[i] = expectedPout[i];
			}
			previousBaselinePout[i] = baselinePout[i]; //save previous baseline to be able to check whether the baseline changed.
			baselinePout[i] = newPout[i];
			lastIncrement [i] = increment [i];
			increment[i] = 0.0;
		}
		Pout.set(baselinePout);
		currentPoutBaselineVersion = currentPoutBaselineVersion +1;
		//printBaselineEffectiveness();
	}



	private void baselinePout(){ 
		double newPout[];
		newPout = new double [Pout.size()];
		updateIncrement();
		for(int i=0; i<Pout.size(); i++){
			newPout[i] = baselinePout[i] + increment[i];
			if(newPout[i] > expectedPout[i]){
				//System.out.println("---------");
				newPout[i] = expectedPout[i];
			}
			previousBaselinePout[i] = baselinePout[i]; //save previous baseline to be able to check whether the baseline changed.
			baselinePout[i] = newPout[i];
			lastIncrement [i] = increment [i];
			increment[i] = 0.0;
		}
		Pout.set(baselinePout);
		currentPoutBaselineVersion = currentPoutBaselineVersion +1;
		//printBaselineEffectiveness();
	}

	public double pOutEffectiveness(){
		double poutE;
		poutE = 0.0;
		for (int i=0; i<Pout.size(); i++){
			poutE = poutE + baselinePout[i];
		}
		actualEffectiveness = poutE;
		return poutE;
	}

	public double getQuality(){
		if(isPoutBaselined()){
			return (1.0 - (1.0 - (pOutEffectiveness() / expectedEffectiveness())));
		}
		//Quality is assumed to be maximum before the product is baselined.
		return 1.0;
	}

	public void printBaselineEffectiveness(){
		double poutE, pinE;
		pinE=0.0;
		poutE = 0.0;
		for (int i=0; i<Pin.size(); i++){
			pinE = pinE + baselinePin[i];
		}
		if(Pin.getName().equals("Need")){
			pinE = pinE / 6.0;
		}
		for (int i=0; i<Pout.size(); i++){
			poutE = poutE + baselinePout[i];
		}
		//System.out.println("CurrentPinBaseline   = " + currentPinBaselineVersion + " for " + Pin.getName() + " Effectiveness = " + df.format(pinE));
		//System.out.println("BaselinePout version = " + currentPoutBaselineVersion + " for " + Pout.getName() + " Effectiveness = " + df.format(poutE));
	}


	public boolean newBFI(){
		if(newPinBaselineVersion > currentPinBaselineVersion){
			return true;
		}
		return false;
	}

	public ProductVector getPout(){
		return Pout;
	}

	public TransformationMatrix getTransformation(){
		return T;
	}

	public void setPin(ProductVector pin){

	}

	public double [] getActualPout(){
		return actualPout;
	}

	public double [] getBaselinePout(){
		return baselinePout;
	}

	private void printDeltaPin(){
		//System.out.print("DeltaPin " + Pin.getName() + " version = " + currentPinBaselineVersion + "  ");
		for (int i=0; i<Pin.size(); i++){
			//System.out.print(df.format(deltaPin[i]) + "   ");
		}
		//System.out.println();
	}

	private void printExpectedPout(){
		//System.out.print("ExpectedPout " + Pout.getName() + " ");
		for (int i=0; i<Pout.size(); i++){
			//System.out.print(df.format(expectedPout[i]) + "   ");
		}
		//System.out.println();
	}

	private void printBaselinePout(){
		//System.out.print("BaselinePout " + Pout.getName() + " ");
		for (int i=0; i<Pout.size(); i++){
			//System.out.print(df.format(baselinePout[i]) + "   ");
		}
		//System.out.println();
	}

	private void updateIncrement(){
		//System.out.println("New updateIncrement() from Transformation");
		int mnr = T.nrows();
		int mnc = T.ncolumns();
		double mv [][];

		mv = T.get();

		if (deltaPin.length != mnr){
			//System.out.println("Error updateIncrement() deltaLenth != mnr");
			return;
		}
		if (increment.length != mnc) {
			//System.out.println("Error updateIncrement() incrementLength != mnc");
			return;
		}

		for (int j=0; j<mnc; j++){
			increment[j] = 0.0;
			for (int i=0; i< mnr; i++){
				increment[j] = increment[j] + deltaPin[i]* mv[i][j];
			}
		}
		for(int a=0; a<actualPout.length; a++){
			//actualPout[a] = Pout.get()[a] + increment[a];
			actualPout[a] = baselinePout[a] + increment[a];
			if(actualPout[a] > Pout.getIdeal()[a]){
				actualPout[a] = Pout.getIdeal()[a];
			}
		}
	}


	public void multiply (TransformationMatrix m, double vin[], double vout[]){
		//System.out.println("Begin Vector.multiply (Matrix, Vector)");
		int mnr = m.nrows();
		int mnc = m.ncolumns();
		double mo [][] = new double [mnr][mnc];
		mo = m.getOriginal();
		if (vin.length != mnr){
			//System.out.println("Error Vector.multiply (Matrix, vin, vout) vin != mnr");
			return;
		}
		if (vout.length != mnc) {
			//System.out.println("Error Vector.multiply (Matrix, vin, vout) vout != mnc");
			return;
		}
		//System.out.println("Loop Vector.multiply (Matrix, Vector)");
		for (int j=0; j<mnc; j++){
			vout[j] = 0.0;
			for (int i=0; i< mnr; i++){
				vout[j] = vout[j] + vin[i]* mo[i][j];
			}
		}
	}


	public void updateIdealPout(){
		//System.out.println("New updatePout() from Transformation");
		Pin.multiply(T, Pout);
	}

	public boolean newPoutBaseline(){
		if(newPoutBaseline){
			newPoutBaseline = false;
			return true;
		}
		return false;
	}

	public void updatePout(){
		//System.out.println("$$$$$$$$$$$$$$$$$$$$$$ UpdatePout ");
		//updateIncrement();
		if(inProgress){
			updateIncrement();
			//contract.incrementDuration();
		}
		else{
			if(newPinBaselineVersion > currentPinBaselineVersion){
				if(Pout.getName().equals("Application")){ 
					if(simulationSettings.isIterative()){
						if(okToProceed){
							if(reworkIsNeeded){
								reworkPin();
								//reworkIsNeeded = false;
								//nRework = nRework +1;	
								inProgress = true;
							}
							else{
								acceptPin();  //Accept new BFI (Pin) if t is available and it is OK to proceed.
								//nRework = 0;
								//reworkIsNeeded = false;
								inProgress = true;
								okToProceed = false;
							}
						}
					}
					else{
						if(reworkIsNeeded){
							reworkPin();
							//nRework = nRework + 1;
							//reworkIsNeeded = false;
							inProgress = true;
						}
						else{
							acceptPin();
							//nRework = 0;
							//reworkIsNeeded = false;
							inProgress = true;
							okToProceed = false;
						}
					}
				}
				else{
					if(reworkIsNeeded){
						reworkPin();
						//nRework = nRework + 1;
						//reworkIsNeeded = false;
						inProgress = true;
					}
					else{
						acceptPin();
						//nRework = 0;
						//reworkIsNeeded = false;
						inProgress = true;
						okToProceed = false;
					}
				}

			}
			else{
				if(Pout.getName().equals("Application")){ 
					if(currentPinBaselineVersion == numberOfIncrements){
						if(reworkIsNeeded){
							reworkPin();
							inProgress = true;
						}
					}

				}
			}
		}

		if(allDeliverablesAreBaselined()){
			//System.out.println("All Deliverables are Baselined for " + Pout.getName());
			if(reworkInProgress){
				baselineReworkPout();   //Baseline its own Deliverables
				reworkInProgress = false;
				/*
				System.out.println("REWORK is COMPLETE for " + Pout.getName() +
						" expected Effectiveness = " + df.format(expectedEffectiveness()) +
						" Effectiveness = " + df.format(pOutEffectiveness()) +
						" ACHIEVED Target Effectiveness = " + df.format(100.0 * pOutEffectiveness() / expectedEffectiveness()) + "%");
				 */
				clearDeliverableBaselineStatus();
				if(nextTransformation != null){
					nextTransformation.newPin();     //Inform the next Transformation that a new BFI is available.
				}
				inProgress = false;
			}
			else{
				baselinePout();                  //Baseline its own Deliverables
				clearDeliverableBaselineStatus();
				if(nextTransformation != null){
					nextTransformation.newPin();     //Inform the next Transformation that a new BFI is available.
					if(!simulationSettings.isIterative()){
						if(Pout.getName().equals("Application")){ 
							if( pOutEffectiveness() < 1.0 * expectedEffectiveness()){
								/*
								System.out.println("CCC REWORK is NEEDED for Transformation " + Pout.getName() +
										" expected Effectiveness = " + df.format(expectedEffectiveness()) +
										" Effectiveness = " + df.format(pOutEffectiveness()));
								//+ " Target Effectveness = " + df.format(100.0 * simulationSettings.getTargetEffectiveness()) + "%");
								 */
								if(simulationSettings.getReworkFactor() > 0.0){
									//reworkIsNeeded = true;
								}
							}
							else{
								reworkIsNeeded = false;
							}
						}
						else{
							//This is new code: 7 Dec 2010
							if( pOutEffectiveness() < 1.0 * expectedEffectiveness()){
								if(simulationSettings.getReworkFactor() > 0.0){
									//reworkIsNeeded = true;
								}
							}
							else{
								reworkIsNeeded = false;
							}
						}
					}
					else{
						//This is new code: 7 Dec 2010
						if(previousTransformation != null){	
							if((Pout.getName().equals("Application") || Pout.getName().equals("Capabilities"))){
								//if( pOutEffectiveness() < simulationSettings.getTargetEffectiveness() * expectedEffectiveness()){
								if( pOutEffectiveness() < 1.0 * expectedEffectiveness()){
									if(pD.drawChanceFromUniform(simulationSettings.getIterativeFactor())){
										//previousTransformation.OKtoProceed(pOutEffectiveness());
									}
								}
							}
							else{
								//System.out.println("Transformation " + Pout.getName() + " will send OK to proceed to " + previousTransformation.Pout.getName());
								if(this.isDefficient() && previousTransformation.isDefficient()){
									if(pD.drawChanceFromUniform(simulationSettings.getIterativeFactor())){
										if(previousTransformation != null){
											previousTransformation.OKtoProceed(pOutEffectiveness());
											previousTransformation.iterativeFeedbackLearning();
										}
									}
								}
							}
						}

					}
				}
				else{
					//System.out.println("Transformation " + Pout.getName() + " will send OK to proceed to " + previousTransformation.Pout.getName());
					if(this.isDefficient() && previousTransformation.isDefficient()){
						if(pD.drawChanceFromUniform(simulationSettings.getIterativeFactor())){
							if(previousTransformation != null){
								previousTransformation.OKtoProceed(pOutEffectiveness());
								previousTransformation.iterativeFeedbackLearning();
							}
						}
					}
					firstTransformation.OKtoProceed(pOutEffectiveness());
					newPoutBaseline = true;
				}
				inProgress = false;
				//}
			}
		}
	}

}
