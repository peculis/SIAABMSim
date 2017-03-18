package SIAABMSimUtils;

//Coded by Ricardo Peculis.
//Initial: 26 April 2010.
//This class contains Simulation attributes defined by SimSettings file

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.text.*;

import uchicago.src.sim.util.Random;

public class simSettings {

	private static final simSettings INSTANCE = new simSettings();

	private DecimalFormat df;

	private int sr; //number of rows in the SimSettings File.
	private int sc; //number of columns in the SimSettings File.
	private String [][]simSettingsFile; //Simulation Settings read from the file.
	
	//CoopLearningRate

	private	int	display = 0;
	private int devProcess = 0;
	private int nTaskIncrements = 1;
	private double targetEffectiveness = 1.0;
	private double targetEffort = -1.0;
	private double targetDuration = -1.0;
	private	double effortFactor = 0.0;
	private double reworkEffortFactor = 0.0;
	private double reviewFactor = 0.0;
	private double reworkFactor = 0.0;
	private double taskProgressRate = 0.1;
	private double cooperationFactor = 1.0;
	private double learningRate = 0.1;
	private double forgettingRate = 0.1;
	private double experienceRate = 0.1;
	private double behaviourRate = 0.1;
	private	double constructiveRate = 0.1;
	private double passiveRate = 0.1;
	private double aggressiveRate = 0.1;
	private double motivationRate = 0.1;
	private double remoteInteractions = 0.1;
	private double interactionFactor = 0.0;
	private double learningFactor = 1.0;
	private double iterativeLearningFactor = 0.0;
	private double iterativeFactor = 0.0;
	private double minKEM = 0.0;
	private double flipBehaviour = 0.0;
	private double explicitReward = 0.5;
	private double implicitReward = 0.5;
	private	int delayPraise = 0;
	private int delayReprimand = 0;
	private double praiseThreshold = 1.0;
	private double reprimandThreshold = 1.0;
	private double copySeniorBehaviour = 0.0;

	private simSettings(){
		df = new DecimalFormat("#.###");
	}

	public double getExplicitReward(){
		return explicitReward;
	}
	
	public double getImplicitReward(){
		return implicitReward;
	}
	
	public int getDelayPraise(){
		return delayPraise;
	}
	
	public int getDelayReprimand(){
		return delayReprimand;
	}
	
	public double getPraiseThreshold(){
		return praiseThreshold;
	}
	
	public double getReprimandThreshold(){
		return reprimandThreshold;
	}
	
	public static simSettings getInstance() {
		return INSTANCE;
	}

	public int getNumberOfTaskIncrements(){
		return nTaskIncrements;
	}
	
	public double getTargetEffectiveness(){
		return targetEffectiveness;
	}
	
	public double getTargetEffort(){
		return targetEffort;
	}
	
	public double getTargetDuration(){
		return targetDuration;
	}
	
	public double getIterativeFactor(){
		return iterativeFactor;
	}

	public double getTaskProgressRate(){
		return taskProgressRate;
	}
	
	public double getEffortFactor(){
		return effortFactor;
	}
	
	public double getReworkEffortFactor(){
		return reworkEffortFactor;
	}
	
	public double getReviewFactor(){
		return reviewFactor;
	}

	public double getReworkFactor(){
		return reworkFactor;
	}
	
	public double getMinKEM(){
		return minKEM;
	}
	
	public double getCooperationFactor(){
		return cooperationFactor;
	}

	public double getLearningRate(){
		return learningRate;
	}
	
	public double getForgettingRate(){
		return forgettingRate;
	}

	public double getExperienceChangeRate(){
		return experienceRate;
	}

	public double getBehaviourChangeRate(){
		return behaviourRate;
	}
	
	public double getConstructiveChangeRate(){
		return constructiveRate;
	}
	
	public double getPassiveChangeRate(){
		return passiveRate;
	}
	
	public double getAggressiveChangeRate(){
		return aggressiveRate;
	}
	
	public double getMotivationChangeRate(){
		return motivationRate;
	}
	
	public double getRemoteInteractions(){
		return remoteInteractions;
	}
	
	public double getInteractionFactor(){
		return interactionFactor;
	}
	
	public double getLearningFactor(){
		return learningFactor;
	}
	
	public double getFlipBehaviourProbability(){
		return flipBehaviour;
	}
	
	public double getIterativeLearningFactor(){
		return iterativeLearningFactor;
	}
	
	public double getCopySeniorBehaviour(){
		return copySeniorBehaviour;
	}
	
	public boolean isIterative(){
		if(devProcess == 1){
			return true;
		}
		return false;
	}
	
	public String getLifeCycle(){
		if(devProcess == 1){
			return "Evolutionary";
		}
		else{
			return "Waterfall";
		}
		//if(getNumberOfTaskIncrements() == 0){
		//	return "Waterfall";
		//}
		//return "Incremental";
	}
	
	public boolean graphicDisplay(){
		if(display == 1){
			return true;
		}
		return false;
	}


	public void readSimSettings(String fileName) throws Throwable{
		BufferedReader inputStream2 = null;
		//System.out.println(fileName);
		int i = 0;
		int j = 0;
		int tc = 0;
		String l, n;

		try {
			inputStream2 = new BufferedReader (new FileReader (fileName));
			inputStream2.mark(2000000);
			while ((l = inputStream2.readLine()) != null){
				//System.out.println(l);
				StringTokenizer sti = new StringTokenizer(l);
				tc = sti.countTokens();
				i++;
				j = tc;
				//System.out.println("pr = " + i + " pc = " + tc);
			}
			sr = i;
			sc = j;
			simSettingsFile = new String [sr][sc];

			//System.out.println("pr = " + pr + " pc = " + pc);

			inputStream2.reset();
			i = 0;
			while ((l = inputStream2.readLine()) != null){
				StringTokenizer sti = new StringTokenizer(l);
				tc = sti.countTokens();
				j = 0;
				while (sti.hasMoreTokens()) {
					n = sti.nextToken(); 
					simSettingsFile [i][j] = n;
					j++;
				}
				i++;
			}
			//System.out.println();
		}
		catch (IOException e){
			System.out.println("Problems reading SimSettings file " + fileName);
		}
		finally {
			if (inputStream2 != null){
				inputStream2.close();
			}
		}
		for (int s = 0; s < sr; s++){
			if(simSettingsFile[s][0].equals("GraphicDisplay")){
				if(simSettingsFile[s][1].equals("Y")){
					display = 1;
				}
				if(simSettingsFile[s][1].equals("N")){
					display = 0;
				}
			}
			if(simSettingsFile[s][0].equals("Development")){
				if(simSettingsFile[s][1].equals("W")){
					devProcess = 0;
				}
				if(simSettingsFile[s][1].equals("S")){
					devProcess = 0;
				}
				if(simSettingsFile[s][1].equals("E")){
					devProcess = 1;
				}
				if(simSettingsFile[s][1].equals("I")){
					devProcess = 1;
				}
				if(devProcess == 0){
					//System.out.print("Waterfall");
				}
				if(devProcess == 1){
					//System.out.print("Iterative");
				}
			}
			if(simSettingsFile[s][0].equals("nTaskIncrements")){
				nTaskIncrements = Integer.parseInt(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("TargetEffectiveness")){
				targetEffectiveness = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("TargetEffort")){
				targetEffort = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("TargetDuration")){
				targetDuration = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("EffortFactor")){
				effortFactor = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("ReworkEffortFactor")){
				reworkEffortFactor = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("IterativeFactor")){
				iterativeFactor = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("ReviewFactor")){
				reviewFactor = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("ReworkFactor")){
				reworkFactor = Double.parseDouble(simSettingsFile[s][1]);
				reworkFactor = 0.5 * reworkFactor;
			}
			if(simSettingsFile[s][0].equals("TaskProgressRate")){
				taskProgressRate = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("MinKEM")){
				minKEM = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("CooperationFactor")){
				cooperationFactor = Double.parseDouble(simSettingsFile[s][1]);
			}
			//SimSettings accepts CooperationFactor or CollaborationFactor labels.
			//The name of the variable is the same: cooperationFactor.
			if(simSettingsFile[s][0].equals("CollaborationFactor")){
				cooperationFactor = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("LearningRate")){
				learningRate = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("ForgettingRate")){
				forgettingRate = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("ExperienceRate")){
				experienceRate = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("BehaviourRate")){
				behaviourRate = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("ConstructiveRate")){
				constructiveRate = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("PassiveRate")){
				passiveRate = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("AggressiveRate")){
			aggressiveRate = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("MotivationRate")){
				motivationRate = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("RemoteInteractions")){
				remoteInteractions = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("RemoteInteractions")){
				remoteInteractions = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("InteractionFactor")){
				interactionFactor = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("LearningFactor")){
				learningFactor = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("IterativeLearningFactor")){
				iterativeLearningFactor = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("FlipBehaviour")){
				flipBehaviour = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("ExplicitReward")){
				explicitReward = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("ImplicitReward")){
				implicitReward = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("DelayPraise")){
				delayPraise = Integer.parseInt(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("DelayReprimand")){
				delayReprimand = Integer.parseInt(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("PraiseThreshold")){
				praiseThreshold = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("ReprimandThreshold")){
				reprimandThreshold = Double.parseDouble(simSettingsFile[s][1]);
			}
			if(simSettingsFile[s][0].equals("CopySeniorBehaviour")){
				copySeniorBehaviour = Double.parseDouble(simSettingsFile[s][1]);
			}
		}
	}

	public void printSimSettings(){
		System.out.println("*** Print SimSettings ***");
		for (int s = 0; s < sr; s++){
			System.out.print(simSettingsFile[s][0] + "   ");
			if(simSettingsFile[s][0].equals("GraphicDisplay")){
				if(display == 0){
					System.out.print("NO");
				}
				if(display == 1){
					System.out.print("YES");
				}
			}
			//if(simSettingsFile[s][0].equals("Development")){
				if(devProcess == 0){
					System.out.print("Waterfall");
				}
				if(devProcess == 1){
					System.out.print("Iterative");
				}
			//}
			if(simSettingsFile[s][0].equals("nTaskIncrements")){
				System.out.print(nTaskIncrements);
			}
			if(simSettingsFile[s][0].equals("IterativeFactor")){
				System.out.print(iterativeFactor);
			}
			if(simSettingsFile[s][0].equals("TargetEffort")){
				System.out.print(targetEffort);
			}
			if(simSettingsFile[s][0].equals("TargetDuration")){
				System.out.print(targetDuration);
			}
			if(simSettingsFile[s][0].equals("TargetEffectiveness")){
				System.out.print(targetEffectiveness);
			}
			if(simSettingsFile[s][0].equals("EffortFactor")){
				System.out.print(df.format(effortFactor));
			}
			if(simSettingsFile[s][0].equals("ReworkEffortFactor")){
				System.out.print(df.format(reworkEffortFactor));
			}
			if(simSettingsFile[s][0].equals("ReviewFactor")){
				System.out.print(df.format(reviewFactor));
			}
			if(simSettingsFile[s][0].equals("ReworkFactor")){
				System.out.print(df.format(reworkFactor));
			}
			if(simSettingsFile[s][0].equals("TaskProgressRate")){
				System.out.print(df.format(taskProgressRate));
			}
			if(simSettingsFile[s][0].equals("CooperationFactor")){
				System.out.print(df.format(cooperationFactor));
			}
			//SimSettings accepts CooperationFactor or CollaborationFactor labels.
			//The name of the variable is the same: cooperationFactor.
			if(simSettingsFile[s][0].equals("CollaborationFactor")){
				System.out.print(df.format(cooperationFactor));
			}
			if(simSettingsFile[s][0].equals("LearningRate")){
				System.out.print(df.format(learningRate));
			}
			if(simSettingsFile[s][0].equals("ForgettingRate")){
				System.out.print(df.format(forgettingRate));
			}
			if(simSettingsFile[s][0].equals("ExperienceRate")){
				System.out.print(df.format(experienceRate));
			}
			if(simSettingsFile[s][0].equals("BehaviourRate")){
				System.out.print(df.format(behaviourRate));
			}
			if(simSettingsFile[s][0].equals("ConstructiveRate")){
				System.out.print(df.format(constructiveRate));
			}
			if(simSettingsFile[s][0].equals("PassiveRate")){
				System.out.print(df.format(passiveRate));
			}
			if(simSettingsFile[s][0].equals("AggressiveRate")){
				System.out.print(df.format(aggressiveRate));
			}
			if(simSettingsFile[s][0].equals("MotivationRate")){
				System.out.print(df.format(motivationRate));
			}
			if(simSettingsFile[s][0].equals("RemoteInteractions")){
				System.out.print(df.format(remoteInteractions));
			}
			if(simSettingsFile[s][0].equals("InteractionFactor")){
				System.out.print(df.format(interactionFactor));
			}
			if(simSettingsFile[s][0].equals("LearningFactor")){
				System.out.print(df.format(learningFactor));
			}
			if(simSettingsFile[s][0].equals("FlipBehaviour")){
				System.out.print(df.format(flipBehaviour));
			}
			if(simSettingsFile[s][0].equals("ExplicitReward")){
				System.out.print(df.format(explicitReward));
			}
			if(simSettingsFile[s][0].equals("ImplicitReward")){
				System.out.print(df.format(implicitReward));
			}
			if(simSettingsFile[s][0].equals("DelayPraise")){
				System.out.print(delayPraise);
			}
			if(simSettingsFile[s][0].equals("DelayReprimand")){
				System.out.print(delayReprimand);
			}
			if(simSettingsFile[s][0].equals("PraiseThreshold")){
				System.out.print(df.format(praiseThreshold));
			}
			if(simSettingsFile[s][0].equals("ReprimandThreshold")){
				System.out.print(df.format(reprimandThreshold));
			}
			if(simSettingsFile[s][0].equals("ReprimandThreshold")){
				System.out.print(df.format(reprimandThreshold));
			}
			if(simSettingsFile[s][0].equals("CopySeniorBehaviour")){
				System.out.print(df.format(copySeniorBehaviour));
			}
			System.out.println();
		}
		System.out.println();
	}
}
