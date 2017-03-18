package SIAABMSimUtils;

import java.text.DecimalFormat;

import SIAABMSimUtils.probability;
import SIAABMSimUtils.simSettings;

public class polarBehaviour {

	private double b; //behaviour;
	private double a; //aggressive;
	private double p; //passive;
	private double c; //constructive;
	private BehaviourStyle behaviourStyle;
	private int behaviourIndex;
	private double threePI;
	int A, C, P, B;

	private probability pD;
	private simSettings simS;
	private DecimalFormat df;

	public enum BehaviourStyle {Aggressive, Doer, Constructive, Nice, Passive, Neutral};

	private static double maxA = 0.0;
	private static double maxC = 0.34;
	private static double maxP = 0.67;
	
	private static double Aggressive = 0.0;
	private static double Doer = 0.165;
	private static double Constructive = 0.33;
	private static double Nice = 0.495;
	private static double Passive = 0.66;
	private static double Neutral = 0.83;
	

	private double bIncrement = 0.0;
	private double cIncrement = 0.0;
	private double pIncrement = 0.0;
	private double aIncrement = 0.0;


	public polarBehaviour(){
		pD = pD.getInstance();
		simS = simS.getInstance();
		df = df = new DecimalFormat("#.##");
		bIncrement = simS.getBehaviourChangeRate();
		cIncrement = simS.getConstructiveChangeRate();
		pIncrement = simS.getPassiveChangeRate();
		aIncrement = simS.getAggressiveChangeRate();
		//System.out.println("**** polarBehavour cRate = " + simS.getConstructiveChangeRate() + 
		//		" pRate = " + simS.getPassiveChangeRate() + 
		//		" aRate = " + simS.getAggressiveChangeRate());
		b = 0.0;
		threePI =  3*Math.PI;
		calculateBehaviour();
	}

	public BehaviourStyle getBehaviourStyle(){
		return behaviourStyle;
	}
	
	public int getBehaviourIndex(){
		return behaviourIndex;
	}

	public polarBehaviour(double behaviour){
		pD = pD.getInstance();
		simS = simS.getInstance();
		df = df = new DecimalFormat("#.##");
		b = behaviour;
		threePI =  3*Math.PI;
		calculateBehaviour();
		bIncrement = simS.getBehaviourChangeRate();
		cIncrement = simS.getConstructiveChangeRate();
		pIncrement = simS.getPassiveChangeRate();
		aIncrement = simS.getAggressiveChangeRate();
		//System.out.println("**** polarBehavour cRate = " + simS.getConstructiveChangeRate() + 
		//		" pRate = " + simS.getPassiveChangeRate() + 
		//		" aRate = " + simS.getAggressiveChangeRate());
	}

	private void calculateBehaviourStyle(){
		if((b >= 0.0 && b < 0.066) || (b >= 0.934)){
			behaviourStyle = BehaviourStyle.Aggressive;
			behaviourIndex = 0;
		}
		if (b >= 0.066 && b < 0.264){
			behaviourStyle = BehaviourStyle.Doer;
			behaviourIndex = 1;
		}
		if (b >= 0.264 && b < 0.462){
			behaviourStyle = BehaviourStyle.Constructive;
			behaviourIndex = 2;
		}
		if (b >= 0.462 && b < 0.594){
			behaviourStyle = BehaviourStyle.Nice;
			behaviourIndex = 3;
		}
		if (b >= 0.594 && b < 0.792){
			behaviourStyle = BehaviourStyle.Passive;
			behaviourIndex = 4;
		}
		if (b >= 0.792 && b < 0.934){
			behaviourStyle = BehaviourStyle.Neutral;
			behaviourIndex = 5;
		}
	}

	public boolean isAgressive(){
		if((behaviourStyle == BehaviourStyle.Aggressive) ||
				(behaviourStyle == BehaviourStyle.Doer)){
			return true;
		}
		else{
			return false;
		}
	}

	public boolean isConstructive(){
		if((behaviourStyle == BehaviourStyle.Constructive) ||
				(behaviourStyle == BehaviourStyle.Nice)){
			return true;
		}
		else{
			return false;
		}
	}

	public boolean isPassive(){
		if((behaviourStyle == BehaviourStyle.Passive) ||
				(behaviourStyle == BehaviourStyle.Neutral)){
			return true;
		}
		else{
			return false;
		}
	}

	public void increaseAggressive(double n){
		//System.out.println("+++ AAAAA increaseAggressive " + df.format(n));
		if(b > maxA && b < 0.5){
			b = b - n * aIncrement;;
			if(b< 0.0){
				b = maxA;
			}
		}
		if(b>= 0.5){
			b = b + n * aIncrement;
			if(b>= 1.0){
				b = maxA;
			}
		}
		calculateBehaviour();
	}

	public void increaseConstructive(double n){
		//System.out.println("CCCC increaseConstructive " + df.format(n));
		if(b < maxC ){
			b = b + n * cIncrement;;
		}
		if(b> maxC){
			b = b - n * cIncrement;
		}
		calculateBehaviour();
	}

	public void increasePassive(double n){
		//System.out.println("--- PPPPP increasePassive " + df.format(n));
		if(b< maxP){
			b = b + n * pIncrement;
		}
		if(b> maxP){
			b = b - n * pIncrement;
		}
	}

	public void setAgressive(){
		setBehaviour(Aggressive);
	}
	
	public void setDoer(){
		setBehaviour(Doer);
	}

	public void setConstructive(){
		setBehaviour(Constructive);
	}
	
	public void setNice(){
		setBehaviour(Nice);
	}

	public void setPassive(){
		setBehaviour(Passive);
	}
	
	public void setNeutral(){
		setBehaviour(Neutral);
	}

	private void calculateBehaviour(){
		if ((b >= 0.0) && (b < 0.33)){
			p = 0.0;
			a = 0.5 * (1.0 + Math.cos(b * threePI));
			c = 0.5 * (1.0 - Math.cos(b * threePI));
		}
		if ((b >= 0.33) && (b < 0.66)){
			a = 0.0;
			p = 0.5 * (1.0 + Math.cos(b * threePI));
			c = 0.5 * (1.0 - Math.cos(b * threePI));
		}
		if ((b >= 0.66) && (b <= 1.0)){
			c = 0.0;
			p = 0.5 * (1.0 + Math.cos(b * threePI));
			a = 0.5 * (1.0 - Math.cos(b * threePI));
		}
		A = (int)(100.0 * a);
		C = (int)(100.0 * c);
		P = (int)(100.0 * p);
		B = (int)(100.0 * b);
		calculateBehaviourStyle();
	}

	public void printBehaviour(){
		System.out.println("Behaviour = " + B + " Aggressive = " + A + " Constructive = " + C + " Passive = " + P);
	}

	public void setBehaviour(double behaviour){
		b = behaviour;
		//System.out.println(" ***** Behaviour = " + b);
		calculateBehaviour();
	}

	public double getBehaviour(){
		return b;
	}

	public double getAggressive(){
		return a;
	}

	public double getPassive(){
		return p;
	}

	public double getConstructive(){
		return c;
	}

}
