package SIAABMSimModel;

//SIAABMSim is an experiment of simulating the social environment of Software-Intensive 
//Acquisitions using the ACTS theory and the agent based simulation toolkit RePast 3.1. 
//This simulation is part of the PhD by Research program at the SEEC of the University of South Australia.
//
//Coded by Ricardo Peculis.
//Initial: 18 July 2009. (SIAABMSim1)

//8 August 2011: SIAABMSimV8
//Final stable version for submission.

//25 July 2011: SIAABMSimV7
//Stable version with all intended feature.

//25 September 2010: SIAABMSimV7
//Started a new project from a stable version: SIAABMSimV6.

//25 September 2010: SIAABMSimV6
//Stable version. Task and Development Models are complete.

//18 August 2010: SIAABMSimV6
//Changed the way to check whether the simulation is in progress: it is now checked in the Transformation class.
//Fixed total effectiveness printed at the end of the simulation.

//17 August 2010: SIAABMSimV5
//Stable version. The simulation is working with all configurations except 
//Adaptive Organization (yet to be fixed).
//The simulation is working with the Engineering task only. Review and Rework are yet to be included.



import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.SimModel;
import uchicago.src.sim.engine.SimpleModel;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.analysis.DataRecorder;

import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;

import net.quies.math.plot.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Timer;
import java.math.*;
import javax.swing.*;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.StringTokenizer;

import SIAABMSimUtils.plot;
import SIAABMSimUtils.ChartColor;
import SIAABMSimUtils.simSettings;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.simSettings;



public class SIAABMSimModel extends SimpleModel{

	//private ActsAcquisitionTask task;
	private Schedule schedule;
	private OpenSequenceGraph PerformanceGraph;
	private OpenSequenceGraph EffortGraph;
	private OpenSequenceGraph BehaviourGraph;

	//private plot BehaviourPlot;

	private double duration;
	private double averageEfficiency = 0.0;
	private double integratedEfficiency = 0.0;
	private int efficiencyCount = 0;
	private double actualEffectiveness;
	private double baselineEffectiveness;
	private double baselineSwEffectiveness;

	private DataRecorder Behaviour;
	private DataRecorder Performance;

	private Object2DDisplay displayActors;
	private Object2DDisplay displayArtifacts;
	private Object2DDisplay displayDependencies;
	private Object2DDisplay displayInteractions;
	private Object2DDisplay displayTasks;

	private DisplaySurface displaySurface;

	private plot behaviourPlot;
	private plot performancePlot;

	private String fileDirectory = ".";
	private String outputFile;
	//private String actorsFile;

	private SIAABMSimConfiguration simConfiguration;

	private boolean firstStep = true;
	private static SIAABMSimModel thisModel;

	private simSettings simS;
	private probability pD;

	private int finish = 0;
	private boolean finished = false;
	private boolean reachedTarget = false;

	private DecimalFormat df;
	private DecimalFormat df4;
	private DecimalFormat intf;
	
	private simSettings simulationSettings;

	//Parameters
	private int multiRun = 1;
	private String simID = "0001";
	private String organisationsFile = "Organisations";
	private String initialActorsFileName = "InitialActors";
	private String finalActorsFileName = "FinalActors";
	private String actorsFileName = "Actors";
	private String actorsFile = "";
	private String simSettingsFile = "SimSettings";
	private String probabilitiesFile = "Probabilities";
	private boolean keepPopulation = false;
	private boolean exportActors = false;
	private boolean importActors = false;
	
	private ArrayList <String> as = new ArrayList<String>();
	private ArrayList <String> ps = new ArrayList<String>();
	private ArrayList <String> cs = new ArrayList<String>();
	
	private double [] a;
	private double [] p;
	private double [] c;
	
	private double aMean, aSD;
	private double pMean, pSD;
	private double cMean, cSD;
	
	private double totalTargetEffectiveness = 0.9;

	
	public SIAABMSimModel (){
		name = "SIAABMSimV8";
	}

	
	public String [] getInitParam(){
		String [] params = {"KeepPopulation", "MultiRun","MultiRunID", "SimSettingsFile", "ProbabilitiesFile", 
				"ActorsFile", "OrganisationsFile", "ExportActors", "ImportActors"};
		return params;
	}
	
	/*
	public String [] getInitParam(){
		String [] params = {"KeepPopulation", "MultiRunID", "SimSettingsFile", "ProbabilitiesFile", 
				"ActorsFile", "OrganisationsFile", "ExportActors", "ImportActors"};
		return params;
	}
	*/

	public void setMultiRun(int n){
		multiRun = n;
	}

	public int getMultiRun(){
		return multiRun;
	}

	public void setMultiRunID(String sID){
		simID = sID;
	}

	public String getMultiRunID(){
		return simID;
	}

	public void setKeepPopulation(String kP){
		if(kP.equals("YES")){
			keepPopulation = true;
		}
		else{
			keepPopulation = false;
		}
	}

	public String getKeepPopulation(){
		if(keepPopulation){
			return "YES";
		}
		return "NO";
	}

	public void setSimSettingsFile(String sSF){
		simSettingsFile = sSF;
	}

	public String getSimSettingsFile(){
		return simSettingsFile;
	}

	public void setProbabilitiesFile(String pF){
		probabilitiesFile = pF;
	}

	public String getProbabilitiesFile(){
		return probabilitiesFile;
	}

	public void setActorsFile(String fileName){
		actorsFileName = fileName;
	}

	public String getActorsFile(){
		return actorsFileName;
	}

	public void setOrganisationsFile(String fileName){
		organisationsFile = fileName;
	}

	public String getOrganisationsFile(){
		return organisationsFile;
	}

	public void setExportActors(String eA){
		if(eA.equals("YES")){
			exportActors = true;
		}
		else{
			exportActors = false;
		}
	}

	public String getExportActors(){
		if(exportActors){
			return "YES";
		}
		return "NO";
	}

	public void setImportActors(String iE){
		if(iE.equals("YES")){
			importActors = true;
		}
		else{
			importActors = false;
		}
	}

	public String getImportActors(){
		if(importActors){
			return "YES";
		}
		return "NO";
	}

	public void setup(){
		super.setup();
		df = new DecimalFormat("#.##");
		df4 = new DecimalFormat("#.####");
		intf = new DecimalFormat("#");
		if(displaySurface != null){
			displaySurface.dispose();
		}

		displaySurface = null;
		//pD = new probability();

		//Creates the (Window) Display Surface that displays Actors, Artifacts, Dependencies, Interactions and Tasks.

		displaySurface = new DisplaySurface(this, "SIA ABM Sim Window");

		registerDisplaySurface("SIA ABM Sim Window", displaySurface);
	}

	// builds the display
	private void buildDisplay() {

		//There is only one Display Surface that displays Actors, Artifacts, Dependencies, Interactions and Tasks.

		//Create Artifacts Display
		//System.out.println("#### Create displayArtifacts");
		displayArtifacts = new Object2DDisplay(simConfiguration.getArtifactSpace());
		displayArtifacts.setObjectList(simConfiguration.getArtifacts());
		displaySurface.addDisplayable(displayArtifacts, "Artefacts");

		//Creates Dependencies Display
		//System.out.println("#### Create displayDependencies");
		
		//The following three lines will be commented out to reduce the graphic output - 5 Apr 2011
		
		//displayDependencies = new Object2DDisplay(simConfiguration.getDependencySpace());
		//displayDependencies.setObjectList(simConfiguration.getDependencies());
		//displaySurface.addDisplayable(displayDependencies, "Dependencies");

		//Created Actors Display
		//System.out.println("#### Create displayActors");
		displayActors = new Object2DDisplay(simConfiguration.getActorSpace());
		displayActors.setObjectList(simConfiguration.getActors());
		displaySurface.addDisplayableProbeable(displayActors, "Actors"); //Actors are now Probeable on Display Surface

		//Creates Interactions Display
		//System.out.println("#### Create displayInteractions");
		displayInteractions = new Object2DDisplay(simConfiguration.getInteractionSpace());
		displayInteractions.setObjectList(simConfiguration.getInteractions());
		displaySurface.addDisplayable(displayInteractions, "Interactions");

		//Creates Tasks Display
		//System.out.println("#### Create displayTasks");
		displayTasks = new Object2DDisplay(simConfiguration.getTaskSpace());
		displayTasks.setObjectList(simConfiguration.getDisplayableTasks());
		displaySurface.addDisplayable(displayTasks, "Tasks");


		// creates a sequence graph to plot the size of the population vs. tick count.
		//System.out.println("#### Create Performance and Effectiveness Graphs");
		PerformanceGraph = new OpenSequenceGraph("Performance and Effectiveness vs. Time", this);
		EffortGraph = new OpenSequenceGraph("Effort vs. Time", this);
		this.registerMediaProducer("Mouse Graph", PerformanceGraph);
		this.registerMediaProducer("Mouse Graph", EffortGraph);

		PerformanceGraph.setYRange(-1.5, 1.5);
		EffortGraph.setYRange(-1.5, 2.5);

		PerformanceGraph.addSequence("Effectiveness", new Sequence() {
			public double getSValue() {
				return simConfiguration.getActualEffectiveness();
			}});

		PerformanceGraph.addSequence("Error", new Sequence() {
			public double getSValue() {
				return simConfiguration.getEffectivenessError();
			}});

		PerformanceGraph.addSequence("Discovered Error", new Sequence() {
			public double getSValue() {
				return simConfiguration.getDiscoveredError();
			}});

		EffortGraph.addSequence("Work to be Done", new Sequence() {
			public double getSValue() {
				return 0.0;
				//return simConfiguration.getPlannedEffortLeft();
			}});

		EffortGraph.addSequence("Work Done", new Sequence() {
			public double getSValue() {
				return simConfiguration.getActualEffort();
			}});


		//Create BeahaviourGraphic.

		BehaviourGraph = new OpenSequenceGraph("Behaviour vs. Time", this);
		this.registerMediaProducer("Mouse Graph", BehaviourGraph);

		BehaviourGraph.setAxisTitles("Time", "Behaviour");
		//BehaviourGraph.setXRange(0, 20);
		BehaviourGraph.setYRange(-1.5, 1.5);

		BehaviourGraph.addSequence("Aggressive", new Sequence() {
			public double getSValue() {
				return simConfiguration.getAggressive();
			}});

		BehaviourGraph.addSequence("Constructive", new Sequence() {
			public double getSValue() {
				return simConfiguration.getConstructive();
			}});

		BehaviourGraph.addSequence("Passive", new Sequence() {
			public double getSValue() {
				return simConfiguration.getPassive();
			}});

		BehaviourGraph.addSequence("Motivation", new Sequence() {
			public double getSValue() {
				return simConfiguration.getMotivation();
			}});


		//Create Behaviour Plot.
		behaviourPlot = new plot("Behaviour", 0, 0);
		behaviourPlot.addFunction("Aggressive", ChartColor.DARK_RED);
		behaviourPlot.addFunction("Passive", ChartColor.VERY_DARK_GREEN);
		behaviourPlot.addFunction("Constructive", ChartColor.DARK_BLUE);
		behaviourPlot.addFunction("Motivation", ChartColor.VERY_DARK_MAGENTA);
		behaviourPlot.addFunction("", ChartColor.WHITE); //this is a "padding" to resolve the "bouncing problem" with the list of labels.

		//Create Performance Plot.
		performancePlot = new plot("Performance", 0, 425);
		performancePlot.addFunction("Efficiency", ChartColor.VERY_DARK_GREEN);
		performancePlot.addFunction("EngEffectiv", ChartColor.DARK_RED);
		performancePlot.addFunction("SwEffectiv", ChartColor.DARK_BLUE);
		performancePlot.addFunction("KnowledgeGap", ChartColor.VERY_DARK_MAGENTA);
		//performancePlot.addFunction("RequiredK", ChartColor.ORANGE);
		//performancePlot.addFunction("Experience", ChartColor.VERY_DARK_YELLOW);
		performancePlot.addFunction("", ChartColor.WHITE); //this is a "padding" to resolve the "bouncing problem" with the list of labels.

		//Define Snapshot and Movie files fro the Display Surface
		displaySurface.setSnapshotFileName(fileDirectory + "/Data/Output/SIAABMSimSurface");
		//displaySurface.setMovieName(fileDirectory + "/Data/Output/SIAABMSimSurface", DisplaySurface.QUICK_TIME);
	}

	// builds the model
	public void buildModel() {
		Random.createUniform();
		//System.out.println("Create SIA ABM Simulation Configuration");
		outputFile = fileDirectory + "/Data/Output/SimResults-" + simID + ".txt";
		actorsFile = fileDirectory + "/Data/Input/Actors/" + actorsFileName + ".txt";
		initialActorsFileName = fileDirectory + "/Data/Output/InitialActors-" + simID + ".txt";
		finalActorsFileName =  fileDirectory + "/Data/Output/FinalActors-" + simID + ".txt";
		simConfiguration = new SIAABMSimConfiguration(fileDirectory, simSettingsFile, probabilitiesFile, organisationsFile, keepPopulation);
		simulationSettings = simulationSettings.getInstance();
		if(exportActors){
			exportActorsProfile(simConfiguration.getActorProfile());
			exportInitialActorsProfile(simConfiguration.getActorProfile());
		}
		if(importActors){
			simConfiguration.importActors(actorsFile);
		}
		//System.out.print(simConfiguration.getActorProfile());
		buildSchedule();
		duration = 0;
		simS = simS.getInstance();
		if(simS.graphicDisplay()){
			//System.out.println();
			//System.out.println("YYYYYYYYYYYYYYYYYYYYYYYY Will present GraphicDisplays");
			//System.out.println();
			buildDisplay();
			displaySurface.display();
			//Take one Snapshot after the Model is built and before the Simulation starts to show the Initial Conditions.
			displaySurface.takeSnapshot();
		}
		else{
			//System.out.println();
			//System.out.println("NNNNNNNNNNNNNNNNNNNNNNNN WILL NOT present GraphicDisplays");
			//System.out.println();
		}
		//buildDisplay();
		//PerformanceGraph.display();
		//EffortGraph.display();
		//BehaviourGraph.display();
		//duration = 0;
		//displaySurface.display();

		//Take one Snapshot after the Model is built and before the Simulation starts to show the Initial Conditions.
		//displaySurface.takeSnapshot();
	}


	private void updateBehaviourPlot(){
		double n, a, p, c, m;
		n = simConfiguration.getTotalDuration();
		//n = duration * 2;
		a = getAggressive();
		p = getPassive();
		c = getConstructive();
		m = getMotivation();
		if(simS.graphicDisplay()){
			behaviourPlot.updateFunction("Aggressive", n, a);
			behaviourPlot.updateFunction("Passive", n, p);
			behaviourPlot.updateFunction("Constructive", n, c);
			behaviourPlot.updateFunction("Motivation", n, m);
		}
	}

	private void updatePerformancePlot(){
		double n, t, e, k, rK, aK, nK, x, bt, bst;
		n = simConfiguration.getTotalDuration();
		//n = duration * 2;
		t = getActualEffectiveness();
		bt = getBaselineEffectiveness();
		bt = simConfiguration.getTotalEffectiveness();
		bst = getBaselineSwEffectiveness();
		//bst = simConfiguration.getFinalEffectiveness();
		e = getEfficiency();
		k = getKnowledge();
		rK = getRequiredKnowledge();
		aK = getAvailableKnowledge();
		nK = getNetKnowledge();
		x = getExperience();
		if(simS.graphicDisplay()){
			performancePlot.updateFunction("Efficiency", n, e);
			performancePlot.updateFunction("EngEffectiv", n, bt);
			performancePlot.updateFunction("SwEffectiv", n, bst);
			//performancePlot.updateFunction("RequiredK", n, rK);
			performancePlot.updateFunction("KnowledgeGap", n, nK);
			//performancePlot.updateFunction("Experience", n, x);
		}
	}

	public double getTaskEffectiveness(){
		return simConfiguration.getActualEffectiveness();
	}

	public double getTaskEfficiency(){
		return simConfiguration.getEfficiency();
	}

	public double getTaskEffort(){
		//System.out.println("Task Effort = " + task.getTaskEffort());
		return (double) simConfiguration.getPlannedEffort();
	}

	public double getTaskSpentEffort(){
		//System.out.println("Task Spent Effort = " + task.getTaskSpentEffort());
		return (double) simConfiguration.getActualEffort();
	}

	public double getMotivation(){
		return simConfiguration.getMotivation();
	}

	public double getConstructive(){
		double constructive;
		String s;
		constructive = simConfiguration.getConstructive();
		s = df.format(constructive);
		cs.add(s);
		return constructive;
	}

	public double getPassive(){
		double passive;	
		String s;
		passive = simConfiguration.getPassive();
		s = df.format(passive);
		ps.add(s);
		return passive;
	}

	public double getAggressive(){
		double aggressive;
		String s;
		aggressive = simConfiguration.getAggressive();
		s = df.format(aggressive);
		as.add(s);
		return aggressive;
	}

	public double getEfficiency(){
		if(thisModel.getTickCount() == 0){
			return 0.0;
		}
		else{
			return simConfiguration.getEfficiency();
		}
	}

	public double getKnowledge(){
		return simConfiguration.getKnowledge();
	}

	public double getRequiredKnowledge(){
		return simConfiguration.getRequiredKnowledge();
	}

	public double getNetKnowledge(){
		return (simConfiguration.getAvailableKnowledge() - simConfiguration.getRequiredKnowledge());
	}

	public double getAvailableKnowledge(){
		return simConfiguration.getAvailableKnowledge();
	}

	public double getExperience(){
		return simConfiguration.getExperience();
	}

	public double getActualEffectiveness(){
		actualEffectiveness = simConfiguration.getActualEffectiveness();
		return actualEffectiveness;
	}

	public double getBaselineEffectiveness(){
		baselineEffectiveness = simConfiguration.getBaselineEffectiveness();
		return baselineEffectiveness;
	}

	public double getBaselineSwEffectiveness(){
		baselineSwEffectiveness = simConfiguration.getBaselineSwEffectiveness();
		return baselineSwEffectiveness;
	}

	public double getEffictivenessError(){
		return simConfiguration.getEffectivenessError();
	}
	
	
	private double calculateSD(double [] array, double mean){
		double sd = 0.0;
		double [] variance;
		variance = new double[array.length];
		for(int i=0; i<array.length;i++ ){
			variance[i] = array[i] - mean;
			variance[i] = variance[i] * variance[i];
			sd = sd + variance[i];
		}
		sd = sd / (variance.length - 1);
		sd = Math.sqrt(sd);
		return sd;
	}
	
	
	private void calculateACP(){
		double am, cm, pm;
		a = new double[as.size()];
		c = new double[ps.size()];
		p = new double[cs.size()];
		am = 0.0;
		cm = 0.0;
		pm = 0.0;
		for(int i=0; i<a.length; i++){
			a[i] = Double.parseDouble(as.get(i));
			am = am + a[i];
		}
		for(int i=0; i<c.length; i++){
			c[i] = Double.parseDouble(cs.get(i));
			cm = cm + c[i];
		}
		for(int i=0; i<p.length; i++){
			p[i] = Double.parseDouble(ps.get(i));
			pm = pm + p[i];
		}
		aMean = am / a.length;
		cMean = cm / c.length;
		pMean = pm / p.length;
		aSD = calculateSD(a, aMean);
		cSD = calculateSD(c, cMean);
		pSD = calculateSD(p, pMean);
	}

	public void writeResults(){
		calculateACP();
		try{
			FileWriter fstream = new FileWriter(outputFile, true);
			BufferedWriter out = new BufferedWriter(fstream);
			
			out.append(
					//intf.format(simConfiguration.getTotalDuration()) + "\t" + 
					(simConfiguration.getTotalDuration()) + "\t" + 
					df.format(simConfiguration.getActorSpentEffort()) + "\t" +
					df.format(simConfiguration.getTotalEffectiveness()) + "\t" + 
					df.format(simConfiguration.getEfficiency()) + "\t" +
					df.format(simConfiguration.getActorProductiveEffort()) + "\t" +
					df.format(aMean) + "\t" +
					df.format(cMean) + "\t" +
					df.format(pMean) + "\t" +
					df.format(aSD) + "\t" +
					df.format(cSD) + "\t" +
					df.format(pSD) + "\t" +
					
					//The following results are Strings
					//(simConfiguration.getContractProductiveEffort()) + "\t" +
					//(simConfiguration.getContractEffort()) + 
					"\n");
			
			out.close();
		}
		catch(Exception e){
			System.out.println("writeResultsExecption");
		}
	}

	public void exportActorsProfile(String profile){
		try{
			FileWriter fstream = new FileWriter(actorsFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.append(profile);
			out.close();
		}
		catch(Exception e){

		}
	}
	
	public void exportInitialActorsProfile(String profile){
		try{
			FileWriter fstream = new FileWriter(initialActorsFileName);
			BufferedWriter out = new BufferedWriter(fstream);
			out.append(profile);
			out.close();
		}
		catch(Exception e){

		}
	}
	
	public void exportFinalActorsProfile(String profile){
		try{
			FileWriter fstream = new FileWriter(finalActorsFileName);
			BufferedWriter out = new BufferedWriter(fstream);
			out.append(profile);
			out.close();
		}
		catch(Exception e){

		}
	}

	public void preStep() {
		//Plot testPlot;
		if(firstStep){
			if(simS.graphicDisplay()){
				thisModel.pause();
			}
			firstStep = false;
		}
	}

	public void step() {
		simConfiguration.step();
		duration = thisModel.getTickCount();
		//System.out.println(duration + " " + simConfiguration.getTotalDuration());
		updateBehaviourPlot();
		updatePerformancePlot();
	}

	public void postStep() {
		//21 Nov 2010: this call to displaySurface.repaint() was causing the exceptions while displaying tasks.
		//displaySurface.repaint();
		if(simS.graphicDisplay()){
			displaySurface.updateDisplay();
			behaviourPlot.refresh();
			performancePlot.refresh();
		}
		//duration = thisModel.getTickCount();
		efficiencyCount = efficiencyCount + 1;
		integratedEfficiency = integratedEfficiency + simConfiguration.getEfficiency();
		averageEfficiency = integratedEfficiency / efficiencyCount;
		//averageEfficiency = (averageEfficiency + simConfiguration.getEfficiency()) / 2.0;
		if(simS.getTargetEffort() > 0 && simS.getTargetEffort() < simConfiguration.getActorSpentEffort()){
			reachedTarget = true;
			//System.out.println("*** Reached Target Effort");
		}
		if((simS.getTargetDuration() > 0 && simS.getTargetDuration() < simConfiguration.getTotalDuration())){
			reachedTarget = true;
			//System.out.println("*** Reached Target Duration");
		}
		if(simConfiguration.getTotalEffectiveness() > simulationSettings.getTargetEffectiveness()){
			reachedTarget = true;
			//System.out.println("*** Reached Target Effectiveness");
		}
		//if(simConfiguration.isInProgress() && !reachedTarget){
		//	finish = 0;
		//	finished = false;
		//}
		//else{
		if(!simConfiguration.isInProgress() || reachedTarget){
		//if(!simConfiguration.isInProgress()){
			finish = finish + 1;
			if( finish > 1 && !finished){
				if(simS.graphicDisplay()){
					System.out.println("************** SIMULATION RESULTS **************");
					System.out.println("SimSettings File        =   " + simSettingsFile);
					System.out.println("Probabilities File      =   " + probabilitiesFile);
					System.out.println("Organisations File      =   " + organisationsFile);
					if(importActors){
					System.out.println("Actors File             =   " + actorsFile);
					}
					System.out.println("Number of Organisations =   " + simConfiguration.getNumberOfOrganisations());
					System.out.println("Developlemnt LyfeCycle  = " + simConfiguration.getLifeCycle());
					System.out.println("Number of Increments    =   " + simConfiguration.getNumberOfIncrements());
					//System.out.println("EFFECTIVENESS     = " + df.format(simConfiguration.getActualEffectiveness()));
					System.out.println("EFFECTIVENESS     = " + df.format(simConfiguration.getTotalEffectiveness()));
					System.out.println("DURATION           = " + simConfiguration.getTotalDuration());
					System.out.println("EFFICIENCY = " + df.format(simConfiguration.getEfficiency()));
					System.out.println("PRODUCTIVE EFFORT = " + df.format(simConfiguration.getActorProductiveEffort()));
					System.out.println("SPENT EFFORT = " + df.format(simConfiguration.getActorSpentEffort()));
					System.out.println("Contracts                 " + "\t" +"Applic" + "\t" + "Capabi" + "\t" + "SysEng" + "\t" + "SysArc" + "\t" + "SwEng" + "\t" + "SwDev");
					System.out.println("Contracts EFFECTIVENESS = " + "\t" + simConfiguration.getContractEffictiveness());
					System.out.println("Contracts EFFICIENCY = " + "\t" + simConfiguration.getContractEfficiency());
					System.out.println("Contracts DURATION =   " + "\t" + simConfiguration.getContractDuration());
					System.out.println("Contracts PRODEFFORT = " + "\t" + simConfiguration.getContractProductiveEffort());
					System.out.println("Contracts EFFORT =     " + "\t" + simConfiguration.getContractEffort());
					//System.out.println("AVERAGE EFFICIENCY = " + df.format(averageEfficiency));
					//System.out.println("IDEAL  EFFORT/COST  = " + df.format(simConfiguration.getIdealEffort()));
					//System.out.println("PLANNED  EFFORT/COST  = " + df.format(simConfiguration.getPlannedEffort()));
					System.out.println("************ SIMULATION HAS FINISHED ***********");
				}
				else{
					//5 April 2011.
					// added the code below just as a temporary feature to display results when the Display is OFF.
					//Need to remove this and to uncomment the writeResults() to to Multi-Run.
					/*
					System.out.println(simConfiguration.getTotalDuration() + "\t" + 
					df.format(simConfiguration.getActorSpentEffort()) + "\t" +
					df.format(simConfiguration.getActualEffectiveness()) + "\t" + 
					df.format(simConfiguration.getEfficiency()));
					*/
					
					writeResults();
				}
				if(exportActors){
					exportFinalActorsProfile(simConfiguration.getActorProfile());
				}
				//thisModel.pause();
				finished = true;
				reachedTarget = false;
				thisModel.stop();
			}
			if(finish > 2){
				thisModel.stop();
				finish = 0;
				finished = false;
				reachedTarget = false;
			}
		}
		else{
			finish = 0;
			finished = false;
			reachedTarget = false;
		}

		//EffortGraph.step(); 
		//PerformanceGraph.step();
		//BehaviourGraph.step();
	}

	public static void main(String[] args) throws Throwable {
		uchicago.src.sim.engine.SimInit init =
			new uchicago.src.sim.engine.SimInit();
		System.out.println("RePast3 - SIAABMSimV8.");
		SIAABMSimModel model = new SIAABMSimModel();
		thisModel = model;
		//System.out.println("Init SIARepast3Model.");
		init.loadModel((SimModel) model, null, false);
	}

}

