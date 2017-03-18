package SIAABMSimAgent;

import java.awt.Color;
import java.util.ArrayList;
import java.text.*;

//import SIAABMSimUtils.ChartColor;

import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.util.Random;
import SIAABMSimTask.ProductVector;
import SIAABMSimTask.ProductVector.ProductCategory;
import SIAABMSimTask.Transformation;
import SIAABMSimTask.TransformationMatrix;
import SIAABMSimTask.TEOW;
import SIAABMSimConnection.Dependency;
import SIAABMSimUtils.probability;
import SIAABMSimTask.Contract;

public class Artifact extends Agent{

	public enum ArtifactStatus {NotStarted, InProgress, Ready, Complete};

	private String name;
	private ProductVector Pin;
	private ProductVector Pout;
	private int PoutArtifact;
	private Transformation thisTransformation;
	private TransformationMatrix m;
	private ProductCategory category;
	private ArrayList <Dependency> dependencyList;
	private ArrayList <Artifact> fromArtifactList;
	private ArrayList <TEOW> TEOWList;
	
	private double [] inputBaseline;
	private double [] outputBaseline;
	
	private int availableBFIBaselineVersion;
	private int baselineVersion;
	private int inProgressVersion;
	
	private double idealValue;
	private double expectedValue;
	private double assignedValue;
	private double actualValue;
	private double baselineValue;
	private ArtifactStatus myStatus;
	
	private boolean pleaseReview;
	private boolean pleaseRework;
	
	private int productiveEffort = 0;
	private int overheadEffort = 0;
	
	

	private DecimalFormat df;

	public Artifact (int thisProductArtifact,
			Transformation transformation){

		df = new DecimalFormat("#.####");
		thisTransformation = transformation;
		Pin = thisTransformation.getPin();
		Pout = thisTransformation.getPout();
		name = Pout.getName();
		category = Pout.getProductCategory();
		PoutArtifact = thisProductArtifact; //This is the component of the ProductVector that represents the Artifact.
		m = transformation.getTransformation();
		idealValue = Pout.getIdeal()[thisProductArtifact];
		expectedValue = idealValue;
		//expectedValue = ExpectedPout.get()[PoutArtifact];
		assignedValue = idealValue;
		actualValue = 0.0;
		baselineValue = 0.0;
		myStatus = ArtifactStatus.NotStarted;
		myX = 0;
		myY = 0;
		fromArtifactList = new ArrayList<Artifact>();
		dependencyList = new ArrayList<Dependency>();
		TEOWList = new ArrayList<TEOW>();
		inputBaseline = new double [100]; // allows up to 100 baselines, i.e from 0 to 99.
		outputBaseline = new double [100]; // allows up to 100 baselines, i.e from 0 to 99.
		availableBFIBaselineVersion = 0;
		baselineVersion = 0;
		inProgressVersion = 0;
		pleaseReview = false;
		//printShortProfile();
		pD = pD.getInstance();
	}

	//--------------------------------------------
	//The following code was created on 13Aug2010
	//This code is specific to baseline the Need Artifacts
	public void baselineValue(double value){
		baselineVersion = baselineVersion + 1;
		outputBaseline[baselineVersion] = value;
	}

	public double getBaselineValue(int version){
		return outputBaseline[version];
	}

	public int getCurrentBaseline(){
		return baselineVersion;
	}
	
	public Contract getContract(){
		return Pout.getContract();
	}
	
	public void increaseProductiveEffort(int n){
		productiveEffort = productiveEffort + n;
		//System.out.println("***");
	}


	public void increaseOverheadEffort(int n){
		//overheadEffort = overheadEffort + n;
		//System.out.println("---");
	}
	
	public int getProductiveEffort(){
		return productiveEffort;
	}
	
	public int getTotalEffort(){
		return (productiveEffort + overheadEffort);
	}
	
	public int getEfficiency(){
		int totalEffort = getTotalEffort();
		if(totalEffort > 0){
			return (productiveEffort / totalEffort);
		}
		return 0;
	}
	
	public void baseline(){
		if(Pout != null){
			actualValue = Pout.get()[PoutArtifact];
			baselineValue = actualValue;
			baselineVersion = inProgressVersion;
			outputBaseline[baselineVersion] = baselineValue;
			thisTransformation.baselineDeliverableArtifact(this);
		}
		else{
			//System.out.println(name + " Pout is null");
		}
	}

	public boolean baselined(){
		if(baselineVersion > inProgressVersion - 1){
			return true;
		}
		return false;
	}
	
	public void pleaseRework(){
		pleaseRework = true;
	}
	
	public boolean toBeReworked(){
		if(pleaseRework){
			pleaseRework = false;
			return true;
		}
		return false;
	}
	
	public void pleaseReview(){
		pleaseReview = true;
	}
	
	public boolean toBeReviewed(){
		if(pleaseReview){
			pleaseReview = false;
			return true;
		}
		return false;
	}


	public boolean BFIAvailable(){
		if(availableBFIBaselineVersion > inProgressVersion){
			return true;
		}
		else{
			return false;
		}
	}

	public void acceptNewBFIBaseline(){
		if(availableBFIBaselineVersion > inProgressVersion){
			if(baselineVersion == inProgressVersion){
				//System.out.println(getName() + " accepted new BFI");
				//createTEOW(); //Create TEOWs to process what is need for the new BFI;
				inProgressVersion = inProgressVersion + 1;
				myStatus = ArtifactStatus.InProgress;
			}
		}
	}

	public void Xbaseline(){ // I need to include the baseline number when baselining the Artifact
		if(Pout != null){
			//baselineVersion = baselineVersion + 1;
			//inProgressVersion = inProgressVersion + 1;
			actualValue = Pout.get()[PoutArtifact];
			baselineValue = actualValue;
			baselineVersion = inProgressVersion;
			outputBaseline[baselineVersion] = baselineValue;
		}
		else{
			//System.out.println(name + " Pout is null");
		}
	}

	public int baselineVersion(){
		return baselineVersion;
	}

	public int inProgressVersion(){
		return inProgressVersion;
	}


	public double incrementSize(){
		return thisTransformation.getIncrementSize();
	}

	public int productArtifact(){
		return PoutArtifact;
	}

	public ArtifactStatus getStatus(){
		return myStatus;
	}

	public void setStatus(ArtifactStatus s){
		myStatus = s;
	}

	public void Complete(){
		myStatus = ArtifactStatus.Complete;
	}


	public boolean isReady(){
		if(myStatus == ArtifactStatus.Ready){
			//if(thisTransformation.isPinBaselined() || name.equals("Need")){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void newBFIisAvailable(){
		availableBFIBaselineVersion = availableBFIBaselineVersion + 1;
		//System.out.println("New BFI version " + availableBFIBaselineVersion + " is available for Artifact = " + getName());
	}


	public boolean isComplete(){
		if(thisTransformation.isPinBaselined()){ //03 Aug 2010: this test may not be correct.
			//I may have to introduce Artifact baseline.
			return true;
		}
		return false;
	}

	public double getIdealValue(){
		return idealValue;
	}

	public void setAssignedValue(double v){
		assignedValue = v;
	}

	public double getAssignedlValue(){
		return assignedValue;
	}

	public void setActualValue(double v){
		actualValue = v;
	}

	public double getActualValue(){
		//return actualValue;
		return thisTransformation.getActualPout()[PoutArtifact];
	}


	public double getBaselinedValue(){
		//return actualValue;
		return thisTransformation.getBaselinePout()[PoutArtifact];
	}

	public double getError(){
		double error;
		//error = 1.0 - (baselineValue / idealValue);
		error = 1.0 - (thisTransformation.getActualPout()[PoutArtifact] / idealValue);
		return error;
	}

	public double getTaskError(){
		double taskError = 0.0;
		for(int t=0; t<TEOWList.size(); t++){
			taskError = taskError + TEOWList.get(t).getError();
		}
		taskError = taskError / TEOWList.size();
		return taskError;
	}

	public double getExpectedError(){
		return expectedValue - actualValue;
	}


	public int getIdealPV(){
		int idealpv;
		idealpv = 0;
		for (int t=0; t<TEOWList.size(); t++){
			idealpv = idealpv + TEOWList.get(t).getIdealPV();
		}
		return idealpv;
	}

	private boolean isVersionReady(int version){
		if(baselineVersion >= version){
			return true;
		}
		return false;
	}

	public boolean isBFIVersionReady(int version){
		return isVersionReady(version);
	}

	public boolean isDelverableVersionReady(int version){
		return isVersionReady(version);
	}

	private double getValuetForVersion(int version){
		if ((version < 100) && (baselineVersion >= version)){
			return outputBaseline[version];
		}
		else{
			return 0.0;
		}
	}

	public double getBFIforVersion(int version){
		return getValuetForVersion(version);
	}

	public double getDelverableForVersion(int version){
		return getValuetForVersion(version);
	}


	//Dependencies: add and get Dependencies

	public void addDependency(Dependency d){
		dependencyList.add(d);
	}

	public ArrayList<Dependency> getDependencies(){
		return dependencyList;
	}

	//BFIs: add and get BFIs (fromArtifacts)

	public void addBFI(Artifact a){
		boolean alreadyInTheList;
		alreadyInTheList = false;
		for(int b=0; b<fromArtifactList.size(); b++){
			if(fromArtifactList.get(b) == a){
				alreadyInTheList = true;
				b = fromArtifactList.size();
				//System.out.println(" *********** " + name + " BFI ARTIFACT IS ALREADY IN THE LIST OF BFIs");
			}
		}
		if(! alreadyInTheList){
			fromArtifactList.add(a);
			//System.out.println(" ############# " + this.getName() + " BFI ARTIFACT "+ a.getName() + " ADDED TO THE LIST OF BFIs");
		}
	}

	public ArrayList<Artifact> getBFIList(){
		return fromArtifactList;
	}


	public boolean isInProgress(){
		if(inProgressVersion > baselineVersion){
			return true;
		}
		return false;
	}

	public String getShortName (){
		return name;
	}

	public String getName (){
		return name.concat("-" + String.valueOf(PoutArtifact));
	}

	public ProductCategory getCategory (){
		return category;
	}

	public int getArtifactNumber(){
		return PoutArtifact;
	}

	public TransformationMatrix getTransformation(){
		return m;
	}

	public ProductVector getDependentProduct(){
		return Pin;
	}

	public ProductVector getOutputProduct(){
		return Pout;
	}

	public boolean isThatYou(ProductVector p, int artifact){
		boolean result;
		//System.out.println("isThatYou ?");
		if ((p == Pout) & (artifact == PoutArtifact)){
			result = true;
			//System.out.println("*********************** Found Dependent Artifact " + name + " " + artifact);
		}
		else {
			result = false;
			//System.out.println("########################## NOT FOUND Dependent Artifact " + name + " " + artifact);
		}
		return result;
	}

	public void setX(int x){
		myX = x;
	}

	public void setY(int y){
		myY = y;
	}

	public int getX(){
		return myX;
	}

	public int getY(){
		return myY;
	}

	public void printShortProfile(){
		//System.out.println("Artifact " + getName() + " state = " + myStatus + " IdealValue = " + df.format(idealValue) + 
		//		" ActualValue = " + df.format(actualValue) + " Category = " + getCategory());
	}

	public void draw(SimGraphics G){
		java.awt.Color myColor;
		int r, g, b;
		double error;
		myColor= Color.BLACK;
		if(! name.equals("Need")){
			if(myStatus != ArtifactStatus.NotStarted){
				//printShortProfile();
				error = getError();
				if(error > 0.8){
					myColor = Color.RED;
				}
				if(error <= 0.8 && error > 0.6){
					myColor = Color.MAGENTA;
				}
				if (error <= 0.6 && error > 0.4){
					myColor = Color.BLUE;
				}
				if(error <= 0.4 && error > 0.15){
					myColor = Color.YELLOW;
				}
				if(error <= 0.15 && error > 0.05){
					myColor = Color.GREEN;
				}
				if(error <= 0.05){
					myColor = Color.WHITE;
				}
			}
			//myColor = new Color (r,g,b);
			if(name.equals("Need")){
				G.drawHollowFastRect(Color.YELLOW);
			}
			else{
				G.drawHollowFastRect(Color.WHITE);
			}
			//G.drawHollowFastRect(Color.WHITE);
			G.drawFastCircle(myColor);
		}
	}

	public void step() {
		
	}

}
