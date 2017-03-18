package SIAABMSimModel;

import SIAABMSimSocial.Organization;
import SIAABMSimSocial.OrganizationProfile;
import SIAABMSimSocial.SocialConfiguration;
import SIAABMSimAgent.ActorConfiguration;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.Artifact;
import SIAABMSimTask.TaskConfiguration;
import SIAABMSimTask.Contract;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.Task;
import SIAABMSimConnection.Dependency;
import SIAABMSimTask.TEOW;
import uchicago.src.sim.space.Object2DGrid;
//import uchicago.src.sim.util.Random;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.text.*;

import SIAABMSimUtils.polarBehaviour;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.simSettings;

public class SIAABMSimConfiguration {

	private SocialConfiguration socialConfiguration;
	private ActorConfiguration actorConfiguration;
	private TaskConfiguration productTaskConfiguration;

	private boolean SocialConfig;

	private ArrayList <Organization> organizationList;
	private ArrayList <Contract> contractList;
	private ArrayList <Actor> actorList;
	private ArrayList <Interaction> interactionList;

	private String actorsConfigFile [][];

	private int ActorSpaceX = 100; 
	private int ActorSpaceY = 100;
	private int ArtifactSpaceX = 100;
	private int ArtifactSpaceY = 100;
	private ArrayList<Artifact> artifactList;
	private ArrayList<Artifact> swArtifactList;
	private int nArtifacts;
	private ArrayList<Dependency> dependencyList;
	private int nDependencies;
	private ArrayList<TEOW> TEOWList;
	private int nTEOW;
	private int iPV;

	private probability probabilityDistribution;
	private simSettings simulationSettings;

	private double motivation = 0.0;
	private double aggressive = 0.0;
	private double constructive = 0.0;
	private double passive = 0.0;
	private double knowledge = 0.0;

	private double requiredKnowledge = 0.0;
	private double availableKnowledge = 0.0;

	private double experience = 0.0;
	private double efficiency = 0.0;
	private double actualEffectiveness = 0.0;
	private double baselineEffectiveness = 0.0;
	private double baselineSwEffectiveness = 0.0;
	private double effectivenessError = 0.0;
	private double effectivenessDiscoveredError = 0.0;
	private int plannedEffort = 0;
	private int actualEffort = 0;
	private int plannedEffortLeft = 0;
	
	private int totalDuration = 0;
	private int totalEffort = 0;
	private int totalProductiveEffort = 0;
	private double totalEfficiency = 0.0;

	private int nIncrements; //number of incremental development steps.
	private double reworkFactor; //percentage of effort allowed to rework

	private DecimalFormat df;


	public SIAABMSimConfiguration(String fileDirectory, String simSettingsFile, String probabilitiesFile, String organisationsFile, 
			boolean keepSamePopulation){
		TEOW teow;

		simulationSettings = simSettings.getInstance();
		probabilityDistribution = probabilityDistribution.getInstance();
		if(keepSamePopulation){
			//System.out.println("Keep the same population");
			probabilityDistribution.initialise();
		}
		else{
			//System.out.println("Changed the population");
			probabilityDistribution.start();
		}
		df = df = new DecimalFormat("#.##");

		try{
			simulationSettings.readSimSettings(fileDirectory + "/Data/Input/" + simSettingsFile + ".txt");
			//simulationSettings.readSimSettings(fileDirectory + "/Data/Input/SimSettings.txt");
			//simulationSettings.printSimSettings();
		}
		catch (Throwable e){
			System.out.println("Problem reading SimSettings file.");
		}

		try{
			probabilityDistribution.readProbabilities(fileDirectory + "/Data/Input/Probabilities/" + probabilitiesFile + ".txt");
			//probabilityDistribution.readProbabilities(fileDirectory + "/Data/Input/Actors/Probabilities.txt");
			//probabilityDistribution.printProbabilities();
		}
		catch (Throwable e){
			System.out.println("Problem reading Probabilties file.");
		}

		//testDrawChance();
		SocialConfig = false;

		simulationSettings = simulationSettings.getInstance();
		nIncrements = simulationSettings.getNumberOfTaskIncrements();
		reworkFactor = simulationSettings.getReworkFactor();

		//For Testing only
		//nIncrements = 2;
		//reworkAllowed = 0.0;

		// This "try/catch" block reads the Organisations.txt file and creates a Social Configuration.
		// This block is intended to replace the next block that creates the Actor Configuration by reading the ActorPopulation.txt file.
		// The SocialConfiguration Class creates the Actors and their Interactions based on the Organization Profile in Organisations.txt.

		try {
			//System.out.println("Organisations file = " + fileDirectory + "/Data/Input/Actors/Organisations.txt");
			//socialConfiguration = new SocialConfiguration (fileDirectory + "/Data/Input/Actors/Organisations.txt",
			socialConfiguration = new SocialConfiguration (fileDirectory + "/Data/Input/Organisations/" + organisationsFile + ".txt",
					ActorSpaceX, ActorSpaceY);
			actorList = socialConfiguration.getActorList();
			//socialConfiguration.PrintSocialProfile();
		}
		catch (Throwable e){
			System.out.println("Problem reading Organisations file.");
		}

		//System.out.println("SO FAR SO GOOD");
		//socialConfiguration.PrintSocialProfile();

		SocialConfig = true; //Will never execute the block that follows

		if (! SocialConfig){
			// This block is required instead of the SocialConfiguration above.
			// This try/catch block creates the Actor Configuration by reading the ActorPopulation.txt file.
			// It should be used in place of the previous block that creates the Social Configuration.
			// It should be noticed that the ActorConfiguraton Class also creates the Interactions from the Interactions.txt file.
			//System.out.println("Will perform actorConfiguration.");

			try {
				actorConfiguration = new ActorConfiguration (fileDirectory + "/Data/Input/Actors/ActorPopulation.txt", 
						fileDirectory + "/Data/Input/Actors/Interactions.txt", ActorSpaceX, ActorSpaceY);
			}
			catch (Throwable e){
				System.out.println("Problem reading Actors or Interactions file.");
			}
			SocialConfig = false;
			//actorConfiguration.PrintTeamProfile();
			//actorConfiguration.PrintSocialProfile();
		}

		// This try/catch block creates the Task Configuration by reading the Transformations.txt file.
		// We have to pass the the fileDirectory name instead of fileName because several files (Transformations) 
		// will be read from that directory.

		try {
			productTaskConfiguration = new TaskConfiguration (fileDirectory, ArtifactSpaceX, ArtifactSpaceY);
		}
		catch (Throwable e){
			System.out.println("Problem reading Transformation files.");
		}

		contractList = productTaskConfiguration.getContractList();
		artifactList = productTaskConfiguration.getArtifactList(); 
		swArtifactList = contractList.get(contractList.size() - 1).getDeliverables();
		nArtifacts = artifactList.size();

		dependencyList = productTaskConfiguration.getDpendencyList();
		nDependencies = dependencyList.size();
		TEOWList = productTaskConfiguration.getTEOWList();
		nTEOW = TEOWList.size();
		for (int n = 0; n < nTEOW; n++){
			teow = TEOWList.get(n);
			iPV = iPV + teow.getIdealPV();
			//teow.printTEOW();
		}
		//The following "for" statement is only a test.
		for(int a=0; a<nArtifacts; a++){
			//if(artifactList.get(a).allDependenciesAreComplete()){
			//	//System.out.println("Dependency of " + artifactList.get(a).getName() + " is COMPLETE");
			//}
			//else{
			//System.out.println("Dependency of " + artifactList.get(a).getName() + " is NOT COMPLETE");
			//}
		}
		//The following "for" statement is only a test.
		for(int t=0; t<TEOWList.size(); t++){
			if(TEOWList.get(t).allDependenciesAreComplete()){
				//System.out.println("TEOW Dependency of " + TEOWList.get(t).getName() + " is COMPLETE");
			}
			else{
				//System.out.println("TEOW Dependency of " + TEOWList.get(t).getName() + " is NOT COMPLETE");
			}
		}
		///System.out.println("XXXXXXXXXXXXXXXXXXXXX");
		//productTaskConfiguration.printTaskProfile();
		if(socialConfiguration == null){
			System.out.println("&&&&&  " + SocialConfig);
		}
		organizationList = socialConfiguration.getOrganizationList();
		//System.out.println("VVVV socialConfiguration.getOrganizationList()");
		assignContractsToOrganisations();
		//System.out.println("No of Contracts = " + contractList.size());
		//System.out.println("No of Organisations = " + organizationList.size());
		//System.out.println("No of Artifacts = " + nArtifacts + " No of Dependencies " + nDependencies + " No of TEOW = " + nTEOW + " TOTAL Ideal PV = " + iPV);
		//System.out.println("*** CONTRACT DETAILS ***");
		for(int c=0; c<contractList.size();c++){
			//contractList.get(c).printContractDetails();
		}
		probabilityDistribution.reInitialise();
		//printActorProfile();
		//testPolarBehaviour();
		//testNormal();
	}

	public void importActors(String fileName){
		int i = 0;
		int j = 0;
		int ar = 0;
		int ac = 0;
		int tokenCount = 0;
		String l, n;
		String actorName;
		String orgName;
		String actorRole;
		double actorBehaviour;
		double actorExperience;
		double actorKnowledge;
		double actorMotivation;
		Actor actor;
		
		BufferedReader inputStream = null;
		System.out.println(fileName);

		try {
			inputStream = new BufferedReader (new FileReader (fileName));
			inputStream.mark(2000000);
			while ((l = inputStream.readLine()) != null){
				StringTokenizer st = new StringTokenizer(l);
				tokenCount = st.countTokens();
				i++;
				j = tokenCount;
			}
			ar = i;
			ac = j;
			//System.out.println("ar= " + ar + " ac= " + ac);
			actorsConfigFile = new String [ar][ac];
			//initialize om with empty string "".
			for (i=0; i<ar; i++){
				for (j=0; j<ac; j++){
					actorsConfigFile[i][j] = "";
				}
			}

			inputStream.reset();
			i = 0;
			while ((l = inputStream.readLine()) != null){
				//System.out.println(l);
				StringTokenizer st = new StringTokenizer(l);
				tokenCount = st.countTokens();
				j = 0;
				while (st.hasMoreTokens()) {
					n = st.nextToken(); 
					actorsConfigFile [i][j] = n;
					j++;
				}
				i++;
			}
			if (inputStream != null){
				//System.out.println("Finaly reading Actors file ");
				inputStream.close();
			}
			//System.out.println();
		}
		catch (IOException e){
			System.out.println("^^^^^ Problems reading Actors file " + fileName);
		}
		finally {
			
		}
		
		//System.out.println("WILL SET ACTOR'S ATTRIBUTES AS PER THE ACTORS FILE");
		
		for(int r=0; r<ar; r++){
				actorName= actorsConfigFile[r][0];
				orgName= actorsConfigFile[r][1];
				actorRole= actorsConfigFile[r][2];
				actorBehaviour= Double.parseDouble(actorsConfigFile[r][3]);
				actorKnowledge= Double.parseDouble(actorsConfigFile[r][4]);
				actorExperience = Double.parseDouble(actorsConfigFile[r][5]);
				actorMotivation= Double.parseDouble(actorsConfigFile[r][6]);
				for(int a=0; a<actorList.size(); a++){
					actor = actorList.get(a);
					if(actor.getName().equals(actorName)){
						actor.setAttributes(actorName, orgName, actorRole, actorBehaviour, 
								actorExperience, actorKnowledge, actorMotivation);
						//actor.printProfile();
						a = actorList.size();
					}
				}
		}
	}
	
	private void calculateContractDuration(){
		Contract thisContract;
		totalDuration = totalDuration + 1;
		probabilityDistribution.setTotalDuration(totalDuration);
		for(int c=1; c<contractList.size(); c++){
			thisContract = contractList.get(c);
			if(thisContract.isInProgress()){
				thisContract.incrementDuration();
			}
		}
	}
	
	public int getTotalDuration(){
		return totalDuration;
	}
	
	public String getContractDuration(){
		String result = "";
		Contract thisContract;
		//result = "" + totalDuration;
		for(int c=1; c<contractList.size(); c++){
			thisContract = contractList.get(c);
			if( c == 1){
				result = result + thisContract.getDuration();
			}
			else{
			result = result + "\t" + thisContract.getDuration();
			}
		}
		return result;
	}
	
	public String getContractEffictiveness(){
		String partial = "";
		String result = "";
		Contract thisContract;
		for(int c=1; c<contractList.size(); c++){
			thisContract = contractList.get(c);
			if(partial.equals("")){
				partial = partial + df.format(thisContract.getEffectiveness());
			}
			else{
				partial = partial + "\t" + df.format(thisContract.getEffectiveness());
			}
		}
		//result = "" + df.format(totalEfficiency / (contractList.size() -1)) + "\t" + partial;
		result = partial;
		return result;
	}
	
	
	public String getContractEfficiency(){
		String partial = "";
		String result = "";
		Contract thisContract;
		totalEfficiency = 0.0;
		for(int c=1; c<contractList.size(); c++){
			thisContract = contractList.get(c);
			totalEfficiency = totalEfficiency + thisContract.getEfficiency();
			if(partial.equals("")){
				partial = partial + df.format(thisContract.getEfficiency());
			}
			else{
				partial = partial + "\t" + df.format(thisContract.getEfficiency());
			}
		}
		//result = "" + df.format(totalEfficiency / (contractList.size() -1)) + "\t" + partial;
		result = partial;
		return result;
	}
	
	public String getContractEffort(){
		String partial = "";
		String result = "";
		Contract thisContract;
		totalEffort = 0;
		for(int c=1; c<contractList.size(); c++){
			thisContract = contractList.get(c);
			totalEffort = totalEffort + thisContract.getEffort();
			if(partial.equals("")){
				partial = partial + thisContract.getEffort();
			}
			else{
				partial = partial + "\t" + thisContract.getEffort();
			}
		}
		//result = "" + totalEffort + "\t" + partial;
		result = partial;
		return result;
	}
	
	public String getContractProductiveEffort(){
		String partial = "";
		String result = "";
		Contract thisContract;
		totalProductiveEffort = 0;
		for(int c=1; c<contractList.size(); c++){
			thisContract = contractList.get(c);
			totalProductiveEffort = totalProductiveEffort + thisContract.getProductiveEffort();
			if(partial.equals("")){
				partial = partial + thisContract.getProductiveEffort();
			}
			else{
				partial = partial + "\t" + thisContract.getProductiveEffort();
			}
		}
		//result = "" + totalProductiveEffort + "\t" + partial;
		result = partial;
		return result;
	}
	
	public int getActorProductiveEffort(){
		int actorPE;
		actorPE = 0;
		for(int a=0; a< actorList.size(); a++){
			actorPE = actorPE + actorList.get(a).getProductiveEffort();
		}
		return actorPE;
	}
	
	public int getActorSpentEffort(){
		int actorPE;
		actorPE = 0;
		for(int a=0; a< actorList.size(); a++){
			actorPE = actorPE + actorList.get(a).getSpentEffort();
		}
		return actorPE;
	}

	public String getActorProfile(){
		String profile = "";
		for(int a=0; a<actorList.size(); a++){
			profile = profile + actorList.get(a).getProfile();
		}
		return profile;
	}

	public void printActorProfile(){
		for(int a=0; a<actorList.size(); a++){
			//System.out.print(actorList.get(a).getProfile());
		}
	}

	private void testDrawChance(){
		int True, False;
		System.out.println("Test DrawChance");
		for(int p = 0; p<=100; p++){
			True = 0;
			False = 0;
			for(int t=0; t<10000; t++){
				if(probabilityDistribution.drawChanceFromUniform((double) (p / 100.0))){
					True++;
				}
				else{
					False++;
				}
			}
			//System.out.println("Probability = " + (int) (p) + " True = " + True + " False = " + False);
		}

	}

	private void testNormal(){
		double x;
		//Create a Normal Distribution of mean m and standard deviation v: Random.createNormal(m, v);;
		//Random.createNormal(30, 25);
		System.out.println("Test Normal Distribution");
		for (int n=0; n<100; n++){
			//Get next sample (int) from the Normal Distribution.
			x = 0.0; //included just to eliminate the error by removing next line.
			//x = Random.normal.nextInt();
			//make the 0.0 <= x <= 1.0
			if (x < 0.0) x = x + 100.0;
			if (x > 100.0) x = x - 100.0;
			x = x / 100.0;
			System.out.println(n+1 + " x = " + x);
		}
	}


	private void testPolarBehaviour(){
		polarBehaviour behaviour;
		behaviour = new polarBehaviour();
		for (double b=0.0; b<1.01; b=b+0.01){
			behaviour.setBehaviour(b);
			behaviour.printBehaviour();
		}
	}

	private void assignContractsToOrganisations(){
		Organization org, customerOrg, providerOrg;
		ArrayList<String> orgTaskList;
		ArrayList<Contract> customerContractList;
		String orgTask;
		String contractName;
		String previousPhaseName;
		Contract contract;
		//System.out.println("Assign Contract" );
		//orgTaskList = new ArrayList<String>();
		for(int o=0; o<organizationList.size(); o++){
			org = organizationList.get(o);
			orgTaskList = org.getContracts();
			for(int t=0; t<orgTaskList.size(); t++){
				orgTask = orgTaskList.get(t);
				for(int c=0; c<contractList.size(); c++){
					contract = contractList.get(c);
					//Need to find what is the Customer Organization.
					contractName = contract.TaskName();
					previousPhaseName = contract.PreviousTaskName();
					if(c<contractList.size()-1){
						contract.setNextTaskName(contractList.get(c+1).TaskName());
					}
					if(c==contractList.size()-1){
						contract.setNextTaskName("nil");
					}
					if(previousPhaseName.equals("")){
						contract.setCustomer(null);
					}
					else{
						for(int oc=0; oc<organizationList.size(); oc++){
							customerContractList = organizationList.get(oc).getContractList();
							for(int cc=0; cc<customerContractList.size(); cc++){
								if(customerContractList.get(cc).TaskName().equals(previousPhaseName)){
									contract.setCustomer(organizationList.get(oc));
									//System.out.println("********* " + organizationList.get(oc).name() + " is customer of " + org.name());
									cc = customerContractList.size();
								}
							}
						}
					}

					//System.out.println("Offered Contract " + contract.name() + " to " + org.name() + " orgTask " + orgTask);
					if((orgTask.equals("All")) || (contract.TaskName().equals(orgTask))){
						org.assignContract(contract);
						contract.setProvider(org);
						//System.out.println("Contract " + contract.TaskName() + " assinged to " + org.name() + "-" + orgTask);
						if(contract.getCustomer() != null){
							//System.out.println("********* " + contract.getCustomer().name() + " is customer of " + contract.getProvider().name());
						}
					}
				}
			}
		}

	}

	public ArrayList <Actor> getActors(){
		if (SocialConfig){
			return socialConfiguration.getActorList();
		}
		else{
			return actorConfiguration.getActorList();
		}
	}

	public Object2DGrid getActorSpace(){
		//System.out.println("getActorSpace()");
		if (socialConfiguration == null){
			System.out.println("#### socialConfiguration is NULL!");
		}
		if (SocialConfig){
			if (socialConfiguration.getActorSpace() == null){
				System.out.println("#### ActorSpace is NULL!");
			}
			return socialConfiguration.getActorSpace();
		}
		else {
			return actorConfiguration.getActorSpace();
		}
	}

	public ArrayList <Interaction> getInteractions(){
		if (SocialConfig){
			return socialConfiguration.getInteractionList();
		}
		else {
			return actorConfiguration.getInteractionList();
		}
	}

	public Object2DGrid getInteractionSpace(){
		if (SocialConfig){
			if (socialConfiguration.getInteractionSpace() == null){
				System.out.println("#### InteractionSpace is NULL!");
			}
			return socialConfiguration.getInteractionSpace();
		}
		else {
			return actorConfiguration.getInteractionSpace();
		}
	}

	public ArrayList <Task> getTasks(){
		if (SocialConfig){
			return socialConfiguration.getTaskList();
		}
		else {
			return actorConfiguration.getTaskList();
		}
	}

	public ArrayList <Task> getDisplayableTasks(){
		if (SocialConfig){
			return socialConfiguration.getDisplayableTaskList();
		}
		else {
			return actorConfiguration.getTaskList();
		}
	}

	public Object2DGrid getTaskSpace(){
		if (SocialConfig){
			return socialConfiguration.getTaskSpace();
		}
		else {
			return actorConfiguration.getTaskSpace();
		}
	}

	public ArrayList <Artifact> getArtifacts(){
		return productTaskConfiguration.getArtifactList();
	}

	public Object2DGrid getArtifactSpace(){
		return productTaskConfiguration.getArtifactSpace();
	}

	public ArrayList <Dependency> getDependencies(){
		return productTaskConfiguration.getDpendencyList();
	}

	public Object2DGrid getDependencySpace(){
		return productTaskConfiguration.getDependencySpace();
	}

	//08 May 2010: The ArtifactLst now contains the "Nill" product which is used to create the "Application"
	//For that reason the meaningful Artifacts start in 5 (should be 6, but I don't know why it does not work with 6).
	//There are now 6 meaningful products: from Application to SoftDev.
	//The change above applies to both getEffectiveness() and getEffectivenessError().

	public double getActualEffectiveness(){
		Artifact artifact;
		double actualValue;
		actualValue = 0;
		
		for(int a=6; a<artifactList.size(); a++){
			artifact = artifactList.get(a);
			actualValue = actualValue + artifact.getActualValue();
		}
		//System.out.println("Get Effectiveness: ArtifactList size = " + artifactList.size());
		actualEffectiveness = (actualValue)/6.0;
		
		//actualEffectiveness = contractList.get(6).getEffectiveness();
		//System.out.println("Get Effectiveness = " + effectiveness);
		return actualEffectiveness;
	}
	
	
	public double getFinalEffectiveness(){
		int n = contractList.size() - 1;
		return (contractList.get(n).getEffectiveness());
	}
	
	public double getTotalEffectiveness(){
		double contractEffect;
		Contract contract;
		contractEffect = 0.0;
		for(int c=1; c < (contractList.size() -1); c++){
			contract = contractList.get(c);
			contractEffect = contractEffect + contract.getEffectiveness();
		}
		return contractEffect/5.0;
		//return contractEffect / (contractList.size() -2);
	}

	public double getBaselineEffectiveness(){
		Artifact artifact;
		double baselineValue;
		baselineValue = 0;
		//for(int a=6; a<artifactList.size()-(26+52); a++){
		//for(int a=6; a<artifactList.size(); a++){
		for(int a=6; a<artifactList.size()-(52); a++){
			artifact = artifactList.get(a);
			baselineValue = baselineValue + artifact.getBaselinedValue();
		}
		//baselineEffectiveness = (baselineValue) / 4.0;
		//baselineEffectiveness = (baselineValue) / 6.0;
		baselineEffectiveness = (baselineValue) / 6.0;
		return baselineEffectiveness;

	}

	public double getBaselineSwEffectiveness(){
		if(productTaskConfiguration.newSwDevBaseline()){
			//baselineSwEffectiveness = getBaselineEffectiveness();
			baselineSwEffectiveness = getTotalEffectiveness();
		}	
		return baselineSwEffectiveness;
	}

	public double getEffectivenessError(){
		Artifact artifact;
		double error;
		error = 0;
		for(int a=6; a<artifactList.size(); a++){
			artifact = artifactList.get(a);
			error = error + artifact.getError();
		}
		effectivenessError = error / 6.0;
		//System.out.println("Get Effectiveness Error = " + effectivenessError);
		return effectivenessError;
	}

	public double getDiscoveredError(){
		return effectivenessDiscoveredError;
	}

	public double getIdealEffort(){
		double effort;
		effort = 0.0;
		for(int a=0; a<actorList.size(); a++){
			effort = effort + actorList.get(a).getIdealEffort();
		}
		return effort;
	}

	public double getPlannedEffort(){
		double effort;
		effort = 0.0;
		for(int a=0; a<actorList.size(); a++){
			effort = effort + actorList.get(a).getPlannedEffort();
		}
		return effort;
	}

	public int getActualEffort(){
		int effort;
		effort = 0;
		for(int a=0; a<actorList.size(); a++){
			effort = effort + actorList.get(a).myActualEffort();
		}
		return effort;
	}
	
	public int getNumberOfOrganisations(){
		return organizationList.size();
	}
	
	public String getLifeCycle(){
		return simulationSettings.getLifeCycle();
	}
	
	public int getNumberOfIncrements(){
		return simulationSettings.getNumberOfTaskIncrements();
	}
	

	public double getMotivation(){
		return motivation;
	}

	public double getConstructive(){
		return constructive;
	}

	public double getPassive(){
		return passive;
	}

	public double getAggressive(){
		return aggressive;
	}

	public double getEfficiency(){
		double ef, pe, se;
		pe = getActorProductiveEffort() ;
		se = getActorSpentEffort();
		if(se > 0){
			ef = pe / se;
		}
		else{
			ef = 0.0;
		}
		return ef;
	}

	public double getKnowledge(){
		return knowledge;
	}

	public double getRequiredKnowledge(){
		return requiredKnowledge;
	}

	public double getAvailableKnowledge(){
		return availableKnowledge;
	}

	public double getExperience(){
		return experience;
	}

	public boolean isInProgress(){
		return productTaskConfiguration.isInProgress();
	}

	public void step() {
		if (SocialConfig){
			socialConfiguration.step();
		}
		else {
			actorConfiguration.step();
		}
		calculateContractDuration();
		productTaskConfiguration.step();
		motivation = socialConfiguration.getMotivation();
		aggressive = socialConfiguration.getAggressive();
		constructive = socialConfiguration.getConstructive();
		passive = socialConfiguration.getPassive();
		knowledge = socialConfiguration.getKnowledge();

		requiredKnowledge = socialConfiguration.getRequiredKnowledge();
		availableKnowledge = socialConfiguration.getAvailableKnowledge();

		//System.out.println("**** RequiredKnowledge = " + df.format(requiredKnowledge) + " AvailableKnowledge = " + df.format(availableKnowledge));


		experience = socialConfiguration.getExperience();
		efficiency = socialConfiguration.getEfficiency();
	}

}
