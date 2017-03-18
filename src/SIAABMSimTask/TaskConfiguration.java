package SIAABMSimTask;

//TaskConfiguration Class configures the Simulated Task in the context 
//of the ACTS (Agent, Cognitions, Task and Social environment) model. 
//The meaning of Task here is well beyond the Connection between 
//Actors and Artifacts through TEOW.

import SIAABMSimTask.ProductVector;
import SIAABMSimTask.ProductVector.ProductCategory;
import SIAABMSimTask.TEOW;
import SIAABMSimUtils.coordinates;
import SIAABMSimAgent.Artifact;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.simSettings;

import SIAABMSimConnection.Dependency;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import uchicago.src.sim.space.Object2DGrid;
//import uchicago.src.sim.util.Random;

public class TaskConfiguration {

	private int numberOfSteps;
	private int Np0 = 6;
	private double [] Vp0;
	//private int Np1 = 6;
	//private double [] Vp1 = {0.41, 0.28, 0.18, 0.07, 0.04, 0.02};

	public enum TaskPhase {T1, T2, T3, T4, T5,FINISHED};

	private TaskPhase Phase;

	private ProductVector P0;
	private TransformationMatrix T0;
	private String firstMatrix;

	private String [][]transformationsFile; // m is the actual matrix.
	private int mnr,mnc;

	private ProductVector products[];
	private int nProducts;
	private Transformation transformations [];

	private ArrayList <Contract> contractList;
	private ArrayList<Transformation> transformationList;

	private ArrayList <Artifact> artifactList;
	private int nartifacts;
	private Object2DGrid artifactSpace;     //artifactSpace is where Artifacts will be placed on.

	private int ndependencies;
	private ArrayList <Dependency> dependencyList; //List of Dependencies;
	private ArrayList <TEOW> TEOWList; //List of Terminal Elements Of Work (TEOW);
	private Object2DGrid dependencySpace;

	private coordinates artifactCoord;
	private int xSpace, ySpace;
	private simSettings simulationSettings;
	private probability pD;
	private int nIncrements; //number of incremental development steps.
	private double incrementSize;
	private double reworkFactor; //percentage of effort allowed to rework
	private boolean newSwDevBaseline;


	public TaskConfiguration (String fileDirectory, int xSize, int ySize) throws Throwable {

		//Read Transformations.txt file to obtain the filename for each Transformation.

		BufferedReader inputStream = null;
		int i = 0;
		int j = 0;
		int tokenCount = 0;
		String l, m, n;
		int nContracts;
		Contract contract;
		double	kMean, kSD;

		ArrayList<Artifact> BFI;
		Artifact a;

		simulationSettings = simulationSettings.getInstance();
		nIncrements = simulationSettings.getNumberOfTaskIncrements();
		reworkFactor = simulationSettings.getReworkFactor();

		incrementSize = 1.0 /nIncrements;
		reworkFactor = 0.0;

		//Dependency testDependency;

		//Random.createUniform();

		xSpace = xSize;
		ySpace = ySize;

		newSwDevBaseline = false;
		
		simulationSettings = simulationSettings.getInstance();
		nIncrements = simulationSettings.getNumberOfTaskIncrements();
		reworkFactor = simulationSettings.getReworkFactor();

		//System.out.println("TaskConfguration - Number of Task Increments = " + nIncrements);
		//System.out.println("TaskConfguration - Task Rework Allowed       = " + reworkFactor);

		pD = pD.getInstance();
		contractList = new ArrayList<Contract>();
		artifactList = new ArrayList<Artifact>();
		transformationList = new ArrayList<Transformation>();
		artifactSpace = new Object2DGrid(xSpace,ySpace);

		//BFI= null;

		BFI = new ArrayList<Artifact>();

		dependencyList = new ArrayList<Dependency>();
		dependencySpace = new Object2DGrid(xSpace,ySpace);

		TEOWList = new ArrayList<TEOW>();

		artifactCoord = new coordinates();

		try {
			inputStream = new BufferedReader (new FileReader (fileDirectory + 
			"/Data/Input/Transformations/Transformations.txt"));
			inputStream.mark(2000000);
			while ((l = inputStream.readLine()) != null){
				StringTokenizer st = new StringTokenizer(l);
				tokenCount = st.countTokens();
				i++;
				j = tokenCount;
			}
			mnr = i;
			mnc = j;
			transformationsFile = new String [mnr][mnc];
			inputStream.reset();
			i = 0;
			while ((l = inputStream.readLine()) != null){
				StringTokenizer st = new StringTokenizer(l);
				tokenCount = st.countTokens();
				j = 0;
				while (st.hasMoreTokens()) {
					n = st.nextToken(); 
					transformationsFile [i][j] = n;
					j++;
				}
				i++;
			}
		}
		catch (IOException e){
			//System.out.println("Problems reading Transformations file Transformations.txt");
		}
		finally {
			if (inputStream != null){
				inputStream.close();
				//System.out.println("Transformations.txt");
			}
		}

		//Print the Transformations.txt file

		//for(int nc=0; nc<nIncrements; nc++){

		for (int p=0; p<mnr; p++){
			for (int q=0; q<mnc; q++){
				//System.out.print(transformationsFile[p][q]);
				//System.out.print("\t");
			}
			//System.out.println(" ");
		}

		firstMatrix = transformationsFile[0][3];
		kMean = Double.parseDouble(transformationsFile[0][5]);
		kSD = Double.parseDouble(transformationsFile[0][6]);

		//System.out.println("FirstMatrix file " + firstMatrix);


		//System.out.println("Create SIA ABMSim Artifacts");
		numberOfSteps = 0;

		transformations = new Transformation [mnr];
		products = new ProductVector [mnr +1];
		nProducts = mnr;

		//artifacts [0] = new ProductVector(ProductCategory.Nil,Np0,Vp0);

		//System.out.println("Will read firstMatrix file.");

		try {
			T0 = new TransformationMatrix (fileDirectory + "/Data/Input/Transformations/" + firstMatrix);
			//T0.printDetails();
		}
		catch (Throwable e){
			System.out.println("Problem reading FirstMatrix file.");
		}

		//System.out.println("firstMatrix file has been read.");

		Np0 = T0.nrows();
		Vp0 = new double[Np0];

		for (int p=0; p<Np0; p++){
			Vp0[p] = 1.0;
		}
		//Here is where P0 is set to its initial value of Vp0 = {1,1,1,..1}
		//The dimension of P0 is the number of rows of the Transformation Matrix T0.
		//The first ProductVector P0 is created manually as {1,1,1,...1}
		//products[0] = P0;

		products[0] = new ProductVector(ProductCategory.Nil,Np0, Vp0);
		products[0].setName(transformationsFile[0][0]);
		//products[0].baseline();
		//products[0].printDetails();
		//products[0].printProduct();

		for (i=0; i<mnr; i++){
			String transformationMatrixName = transformationsFile[i][2];
			String fileName = fileDirectory + "/Data/Input/Transformations/" + transformationsFile[i][3];
			String artifactName = transformationsFile[i][4];
			kMean = Double.parseDouble(transformationsFile[i][5]);
			kSD = Double.parseDouble(transformationsFile[i][6]);

			ProductVector idealPin = products[i];
			ProductVector actualPin = products[i];
			ProductVector POut;
			ProductCategory category = ProductCategory.Nil;
			if (artifactName.equals("Appplication")){
				category = ProductCategory.Application;
			}
			if (artifactName.equals("Capabilities")){
				category = ProductCategory.Capabilities;
			}
			if (artifactName.equals("SysEng")){
				category = ProductCategory.SysEng;
			}
			if (artifactName.equals("SysArchitect")){
				category = ProductCategory.SysArchitect;
			}
			if (artifactName.equals("SoftEng")){
				category = ProductCategory.SoftEng;
			}
			if (artifactName.equals("SoftDev")){
				category = ProductCategory.SoftDev;
			}
			transformations [i] = new Transformation (transformationMatrixName, 
					fileName,
					idealPin,
					category,
					artifactName,
					kMean, kSD);
			transformationList.add(transformations[i]);
			//Following P0, the other ProductVectors, or Artifacts, are created by multiplying
			//the TransformationMatrix Ti by the input Product Pi-1 as following:
			// Pi+1 = Ti * Pi.

			POut = transformations[i].getPout();
			products[i+1] = POut;
			//products[i+1].printDetails();
			//products[i+1].printProduct();

			//The first Contract (Application or "The Need") is always complete to allow the other tasks to start.

			if(i == 0){
				contract = new Contract(transformations[i], null, true);	
			}
			else{
				contract = new Contract(transformations[i], BFI, false);
			}

			contract.setIncrementSize(incrementSize);
			//System.out.println("Created Contract for " + contract.TaskName());
			contractList.add(contract);
			//The BFI for the next Contract is this Contract's Deliverables
			BFI = contract.getDeliverables();
		}
		//}

		//Copy all lists (Artifacts, Dependencies and TEOW) from the Contracts to here.
		nContracts = contractList.size();
		for(int c=0; c<nContracts; c++){

			//Set previous and next contracts.
			contract = contractList.get(c);
			contract.setFirstContract(contractList.get(1));
			if(c == 0 && c < nContracts-1){
				contract.setPreviousContract(null);
				contract.setNextContract(contractList.get(c+1));
			}
			if(c > 0 && c < nContracts-1){
				contract.setPreviousContract(contractList.get(c-1));
				contract.setNextContract(contractList.get(c+1));
			}
			if(c > 0 && c == nContracts-1){
				contract.setPreviousContract(contractList.get(c-1));
				contract.setNextContract(null);
			}

			artifactList.addAll(contractList.get(c).getDeliverables());
			dependencyList.addAll(contractList.get(c).getDependencies());
			TEOWList.addAll(contractList.get(c).getTEOW());
		}
		for(int c=0; c<nContracts; c++){
			contract = contractList.get(c);
			//if(contract.DevelopmentPhaseComplete()){
			//	if(contract.nextContract() != null){
			//contract.nextContract().BFIDelivery();
			//	}
			//}
		}
		randomPositioningArtifacts();
		putArtifactOnSpace();
		clearTransformations();
		startDevelopmentProcess(); //13Aug2010
		//printArtifacts();
	}
	
	
	//Introduced on 13Aug2010
	//Inject newPin baseline into the Application Transformation.
	private void startDevelopmentProcess(){
		for(int p=0; p<nIncrements; p++){
			transformationList.get(1).newPin(); //Inject newPin baseline into the Application Transformation.
		}
		transformationList.get(1).OKtoProceed(0.0);
	}

	private void printArtifacts(){
		for(int a=0; a<artifactList.size(); a++){
			//artifactList.get(a).printShortProfile();
		}
	}

	private void clearTransformations(){
		for(int t=1; t<transformationList.size(); t++){
			//transformationList.get(t).updateIdealPout();
			transformationList.get(t).getTransformation().clearActual();
			transformationList.get(t).updateIdealPout();
		}
	}
	
	public double pOutEffectiveness(){
		Transformation t;
		t = transformations[5];
		return t.pOutEffectiveness();
	}
	
	public boolean newSwDevBaseline(){
		if(transformations[6].newPoutBaseline()){
			return true;
		}
		return false;
	}
	
	public boolean isInProgress(){
		for(int t=0; t<transformationList.size();t++){
			if(transformationList.get(t).isInProgress()){
				return true;
			}
		}
		return false;
	}

	public void printTaskProfile(){
		Artifact artifact;
		int n, ipv;
		String name, newname;
		n = 0;
		ipv = 0;
		name = "";
		newname = "";
		for (int a=0; a<nartifacts; a++){
			artifact = artifactList.get(a);
			newname = artifact.getName();
			if (! newname.equals(name) ){
				if (! name.equals("")){
					//System.out.println("Product = " + name + " No of Artifacts = " 
					//		+ n + " Ideal PV = " + ipv);
				}
				name = newname;
				n = 1;
				ipv =  artifact.getIdealPV();
			}
			else{
				n = n +1;
				ipv = ipv + artifact.getIdealPV();
			}
		}
		//System.out.println("Product = " + name + " No of Artifacts = " + n + " Ideal PV = " + ipv);
	}


	private int numberOfProducts(){
		return nProducts;
	}

	//Set a random coordinates to all Artifacts.
	private void randomPositioningArtifacts(){
		int x, y;
		Artifact artifact;
		//System.out.println("randomPositioningArtifacts()");
		for (int a = 0; a<artifactList.size(); a++){
			artifact = artifactList.get(a);
			x = pD.getIntUniform(0, xSpace-1);
			y =  pD.getIntUniform(0, ySpace-1);

			if (isCellOccupied(x, y)){
				x = x+1;
				if (isCellOccupied(x, y)){
					y = y+1;
					if (isCellOccupied(x, y)){
						x = x-2;
					}
					if (isCellOccupied(x, y)){
						y = y-2;
					}
				}
			}
			artifact.setX(x);
			artifact.setY(y);
			addArtifactToSpace(artifact);
		}
	}

	private void putArtifactOnSpace(){
		int nProducts;
		//int nArtifacts;
		int xP, yP;
		int x, y;
		coordinates coordProduct;
		coordinates coordArtifact;
		double rP, tetaP, tetaIncrement;
		//System.out.println("putArtifactOnSpace()");
		String name = "";
		Artifact artifact;
		coordProduct = new coordinates();
		coordArtifact = new coordinates();
		nProducts = numberOfProducts();
		//System.out.println("Number of Products = " + nProducts);
		rP = xSpace/3.5;
		tetaIncrement = 2.0 * Math.PI / nProducts;
		tetaP = - 3* Math.PI/4;
		//tetaP = Math.PI/2;
		//System.out.println("Artifact name = " + artifact.getName());
		xP =0;
		yP =0;
		name = "";
		coordProduct.setPolar(rP, tetaP);
		for (int art = 6; art<artifactList.size(); art++){ //The "Need" will not be place on Artifact Space
			artifact = artifactList.get(art);
			if (! artifact.getShortName().equals(name)){
				name = artifact.getShortName();
				//System.out.println("Changed Artifact name = " + name);
				tetaP = tetaP - tetaIncrement;
				coordProduct.setPolar(rP, tetaP);
				xP = (int)coordProduct.getX();
				yP = (int)coordProduct.getY();
			}
			//System.out.println("Artifact name = " + artifact.getName());
			coordArtifact.setPolar(18.0, pD.getDoubleUniform(0.0, 2.0*Math.PI));
			x = (int) (50 + xP + coordArtifact.getX());
			y = (int) (50 + yP + coordArtifact.getY());
			//System.out.println("putArtifactOnSpace() " + artifact.getName() + " x = " + x + " y = " + y);
			if (isCellOccupied(x, y)){
				x = x+1;
				if (isCellOccupied(x, y)){
					y = y+1;
					if (isCellOccupied(x, y)){
						x = x-2;
					}
					if (isCellOccupied(x, y)){
						y = y-2;
					}
				} 	
			}
			artifact.setX(x);
			artifact.setY(y);
			//addArtifactToSpace(artifact);
		}
	}

	public boolean isCellOccupied(int x, int y){
		if(artifactSpace.getObjectAt(x,y)==null) {
			//System.out.println("isCellOccupied = No");
			return false;
		}
		else {
			//System.out.println("isCellOccupied = Yes");
			return true;
		}
	}

	public void addArtifactToSpace (Artifact artifact){
		int x, y;
		x = artifact.getX();
		y = artifact.getY();
		if (isCellOccupied(x,y)){
			//System.out.println("********* There is more than one Artifact on " + x + " , " + y);
		}
		else {
			//System.out.println("Artifact " + artifact.getName() + " added to agentSpace on " + x + " , " + y);
		}

	}

	public ArrayList <Contract> getContractList(){
		return contractList;
	}

	public ArrayList <Artifact> getArtifactList(){
		return artifactList;
	}

	public Object2DGrid getArtifactSpace(){
		return artifactSpace;
	}

	public ArrayList <Dependency> getDpendencyList(){
		return dependencyList;
	}

	public ArrayList <TEOW> getTEOWList(){
		return TEOWList;
	}

	public Object2DGrid getDependencySpace(){
		return dependencySpace;
	}

	public void step() {
		int nTransformations, nArtifacts;
		nTransformations = transformationList.size();
		for(int t=1; t< nTransformations; t++){
			transformationList.get(t).updatePout();
		}
		nArtifacts = artifactList.size();
		for (int a=0; a<nArtifacts; a++){
			artifactList.get(a).step();
		}
	}

}
