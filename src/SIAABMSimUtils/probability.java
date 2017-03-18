package SIAABMSimUtils;

//Coded by Ricardo Peculis.
//Initial: 06 Sep 2009.
//This class includes the methods to handle Probability Distributions
//and Probabilistic Events.

//23 Sep 2009: Implemented "singleton" design pattern.
//probability class implements a design pattern "singleton" which allows
//a single instance to be used by any other class/object in the program.

import uchicago.src.sim.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Date;
import java.text.*;

import SIAABMSimAgent.CeoActor;
import SIAABMSimUtils.polarBehaviour;
import SIAABMSimUtils.polarBehaviour.BehaviourStyle;
import SIAABMSimUtils.simSettings;

public class probability {

	private static final probability INSTANCE = new probability();

	private int pr; //number of rows in the Probabilities Matrix.
	private int pc; //number of columns in the Probabilities Matrix (Aggressive, Doer, Constructive, Nice, Passive, Neutral).
	private int nStyles; //number of Behaviour Styles with probabilities assigned for each type of Situation.
	private int nSituations; //number of Situations with probabilities assigned for each Behaviour Style;
	private String [][]probabilitiesFile; //probabilities is the Behaviour Probability read from the file.
	private double [][]probabilities;
	//private String [] styles;
	private String [] situations;
	private polarBehaviour.BehaviourStyle [] styles = {
			polarBehaviour.BehaviourStyle.Aggressive,
			polarBehaviour.BehaviourStyle.Doer,
			polarBehaviour.BehaviourStyle.Constructive,
			polarBehaviour.BehaviourStyle.Nice,
			polarBehaviour.BehaviourStyle.Passive,
			polarBehaviour.BehaviourStyle.Neutral};

	private DecimalFormat df;

	private Date date;
	private long now;
	private int totalDuration;


	private probability(){
		//System.out.println("Probability");
		//Random.createUniform(); //Create Uniform Distribution;
		//Random.createNormal(0.5, 0.5); //Create Normal Distribution.
		df = df = new DecimalFormat("#.##");
	}
	public void start(){
		date = new Date();
		now = date.getTime();
		Random.setSeed(now);
		Random.createUniform(); //Create Uniform Distribution;
		Random.createNormal(0.5, 0.5); //Create Normal Distribution.
	}

	public void initialise(){
		Random.setSeed(1L);
		Random.createUniform(); //Create Uniform Distribution;
		Random.createNormal(0.5, 0.5); //Create Normal Distribution.
	}

	public void reInitialise(){
		date = new Date();
		now = date.getTime();
		Random.setSeed(now);
		Random.createUniform(); //Create Uniform Distribution;
		Random.createNormal(0.5, 0.5); //Create Normal Distribution.
	}


	public static probability getInstance() {
		return INSTANCE;
	}


	public void readProbabilities(String fileName) throws Throwable{
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
			pr = i;
			pc = j;
			probabilitiesFile = new String [pr][pc];

			//System.out.println("pr = " + pr + " pc = " + pc);

			inputStream2.reset();
			i = 0;
			while ((l = inputStream2.readLine()) != null){
				StringTokenizer sti = new StringTokenizer(l);
				tc = sti.countTokens();
				j = 0;
				while (sti.hasMoreTokens()) {
					n = sti.nextToken(); 
					probabilitiesFile [i][j] = n;
					j++;
				}
				i++;
			}
			//System.out.println();
		}
		catch (IOException e){
			System.out.println("Problems reading Probabilities file " + fileName);
		}
		finally {
			if (inputStream2 != null){
				inputStream2.close();
			}
		}

		//The Probabilities Matrix has the following format (the values are an example only):
		/*
		Header	Aggressive	Doer	Constructive	Nice	Passive	Neutral
		Start	0.10	0.20	0.50	0.20	0.10	0.05
		Respond	0.20	0.30	0.30	0.50	0.10	0.05
		AskHelp	0.10	0.15	0.20	0.30	0.10	0.05
		OfferHelp	0.01	0.05	0.50	0.20	0.05	0.0
		AcceptHelp	0.30	0.40	0.80	0.30	0.20	0.10
		Help	0.01	0.05	0.20	0.50	0.20	0.10
		Praise	0.01	0.05	0.30	0.20	0.05	0.0
		Reprimand	0.50	0.30	0.05	0.01	0.0	0.0
		Learning	0.10	0.20	0.30	0.10	0.05	0.01
		Forgetting	0.05	0.10	0.15	0.20	0.25	0.30
		Motivated	0.10	0.15	0.20	0.15	0.10	0.15
		Demotivated	0.20	0.25	0.40	0.25	0.10	0.15
		Working	0.90	0.98	0.95	0.90	0.80	0.85
		Overtime	0.70	0.50	0.30	0.10	0.05	0.10
		Aggressive	1.0	0.50	0.25	0.15	0.05	0.0
		Constructive	0.20	0.30	1.0	0.30	0.10	0.0
		Passive	0.0	0.10	0.20	0.30	1.0	0.50
		BAggressive	1.0	0.40	0.30	0.10	0.05	0.01
		BConstructive	0.10	0.20	1.0	0.30	0.10	0.0
		BPassive	0.0	0.10	0.30	0.20	1.0	0.15
		MngAggressive	1.0	0.40	0.30	0.10	0.05	0.01
		MngConstructive	0.10	0.20	1.0	0.30	0.10	0.0
		MngPassive	0.0	0.10	0.30	0.20	1.0	0.15
		TLAggressive	1.0	0.40	0.30	0.10	0.05	0.01
		TLConstructive	0.10	0.20	1.0	0.30	0.10	0.0
		TLPassive	0.0	0.10	0.30	0.20	1.0	0.15
		minWorkEffort	0.90	0.80	0.75	0.70	0.60	0.50
		maxWorkEffort	1.00	1.00	1.00	1.00	1.00	1.00
		minOvertimeEffort	0.30	0.20	0.10	0.00	0.00	0.00
		maxOvertimeEffort	0.60	0.50	0.30	0.10	0.05	0.00
		minLearnEffort	0.00	0.00	0.00	0.10	0.05	0.00
		maxLearnEffort	0.20	0.25	0.40	0.15	0.10	0.00
		minAskHelpEffort	0.00	0.00	0.10	0.05	0.00	0.00
		maxAskHelpEffort	0.10	0.15	0.30	0.10	0.05	0.00
		minHelpEffort	0.00	0.00	0.10	0.05	0.00	0.00
		maxHelpEffort	0.10	0.15	0.30	0.20	0.10	0.00
		QualityMotiv	0.2		0.3		0.7		0.6		0.55	0.5
		CostSchedMotiv	0.8		0.7		0.3		0.4		0.35	0.5
		ExplicitReward	0.7		0.6		0.3		0.4		0.55	0.5
		ImplicitReward	0.3		0.4		0.7		0.6		0.35	0.5
		 */

		nStyles = pc-1;
		nSituations = pr-1;
		probabilities = new double [nSituations][nStyles];
		//styles = new String [nStyles];
		situations = new String [nSituations];

		for (int s = 1; s < nSituations+1; s++){
			situations [s-1] = probabilitiesFile[s][0];
			for(int p = 1; p< nStyles+1; p++){
				//styles [p-1] = probabilitiesFile[0][p];
				probabilities[s-1][p-1] = Double.parseDouble(probabilitiesFile[s][p]);
			}
		}
		//printProbabilities();
	}
	
	public void setTotalDuration(int d){
		totalDuration = d;
	}
	
	public int totalDuration(){
		return totalDuration;
	}

	public void printProbabilities(){
		System.out.println("*** Print Probabilities ***");
		System.out.print("         ");
		for(int p = 0; p< nStyles; p++){
			System.out.print(styles[p]+ "   ");
		}
		System.out.println();
		for (int s = 0; s < nSituations; s++){
			System.out.print(situations[s] + "  ");
			for(int p = 0; p< nStyles; p++){
				System.out.print(df.format(probabilities[s][p]) + "   ");
			}
			System.out.println();
		}
	}

	public double getEffort(String event, polarBehaviour.BehaviourStyle behaviourStyle){
		double minEffort, maxEffort, effort;
		int situation, style;
		String minEvent;
		minEvent = "min" + event;
		effort = 1.0;
		situation = 0;
		style = 0;
		for(int s=0; s<nSituations; s++){
			if(minEvent.equals(situations[s])){
				situation = s;
				s = nSituations;
			}
		}
		switch (behaviourStyle){
		case Aggressive:{
			style = 0;
			break;
		}
		case Doer:{
			style = 1;
			break;
		}
		case Constructive:{
			style = 2;
			break;
		}
		case Nice:{
			style = 3;
			break;
		}
		case Passive:{
			style = 4;
			break;
		}
		case Neutral:{
			style = 5;
			break;
		}
		}
		minEffort = probabilities[situation][style];
		maxEffort = probabilities[situation + 1][style];
		effort = getDoubleUniform(minEffort, maxEffort);
		//System.out.println("### s = " + situation + 
		//		" minEffort = " + df.format(minEffort) +
		//		" maxEffort = " + df.format(maxEffort) +
		//		" Effort = " + df.format(effort));
		return effort;
	}

	public boolean getChanceOfEvent(String event, polarBehaviour.BehaviourStyle behaviourStyle){
		boolean result;
		int situation, style;
		double eventProbability;
		situation = 0;
		style = 0;
		result = false;
		for(int s=0; s<nSituations; s++){
			if(event.equals(situations[s])){
				situation = s;
				s = nSituations;
			}
		}
		switch (behaviourStyle){
		case Aggressive:{
			style = 0;
			break;
		}
		case Doer:{
			style = 1;
			break;
		}
		case Constructive:{
			style = 2;
			break;
		}
		case Nice:{
			style = 3;
			break;
		}
		case Passive:{
			style = 4;
			break;
		}
		case Neutral:{
			style = 5;
			break;
		}
		}
		eventProbability = probabilities[situation][style];
		result = drawChanceFromUniform(eventProbability);
		return result;
	}
	
	public boolean getChanceOfEvent(String event, double factor, polarBehaviour.BehaviourStyle behaviourStyle){
		boolean result;
		int situation, style;
		double eventProbability;
		situation = 0;
		style = 0;
		result = false;
		for(int s=0; s<nSituations; s++){
			if(event.equals(situations[s])){
				situation = s;
				s = nSituations;
			}
		}
		switch (behaviourStyle){
		case Aggressive:{
			style = 0;
			break;
		}
		case Doer:{
			style = 1;
			break;
		}
		case Constructive:{
			style = 2;
			break;
		}
		case Nice:{
			style = 3;
			break;
		}
		case Passive:{
			style = 4;
			break;
		}
		case Neutral:{
			style = 5;
			break;
		}
		}
		eventProbability = probabilities[situation][style];
		result = drawChanceFromUniform(eventProbability * factor);
		return result;
	}

	public double getInteractionProbability(polarBehaviour.BehaviourStyle behaviourStyle){
		double probability;
		probability = 0.0;
		switch (behaviourStyle){
		case Aggressive:{
			probability = 0.4;
			break;
		}
		case Doer:{
			probability = 0.6;
			break;
		}
		case Constructive:{
			probability = 0.8;
			break;
		}
		case Nice:{
			probability = 0.5;
			break;
		}
		case Passive:{
			probability = 0.2;
			break;
		}
		case Neutral:{
			probability = 0.3;
			break;
		}
		}
		return probability;
	}

	//drawChanceFromUniform returns an Event TRUE or FALSE in accordance with the given probability.
	public boolean drawChanceFromUniform(double probability){
		int n;
		int drawn;
		boolean result;
		result = false;
		n = (int) (10000.0 * probability);
		drawn = Random.uniform.nextIntFromTo(1, 10000);
		//if(probability > 0){
			if (drawn <= n){
				result = true;
			}
		//}
		return result;
	}

	//getNormalDistributionSample returns a sample between 0.0 and 1.0
	//from a Normal Distribution given a Mean and StandardDeviation.
	public double getNormalDistributionSample(double Mean, double StandardDeviation){
		double sample;
		sample = 0.5;
		//sample = Random.normal.nextDouble(0.5, 0.5);
		sample = Random.normal.nextDouble(Mean, StandardDeviation);
		if(sample < 0.0){
			sample = 0.0;
		}
		if(sample > 1.0){
			sample = 1.0;
		}
		return sample;
	}

	public int getIntUniform(int a, int b){
		return Random.uniform.nextIntFromTo(a, b);
	}

	public double getDoubleUniform(double a, double b){
		return Random.uniform.nextDoubleFromTo(a, b);
	}
}
