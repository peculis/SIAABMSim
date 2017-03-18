package SIAABMSimSocial;

public class StaffProfile {
	
	private String Staff;
	private int n; //numner of staff;
	private double BM; //Behaviour Mean.
	private double BSD;//Behaviour Standard Deviation.
	private double KM; //Knowledge Mean.
	private double KSD; //Standard Deviation.
	private double EM; //Experience Mean.
	private double ESD; //ExperienceStandard Deviation.
	private double MM; //Motivation Mean;
	private double MSD; //Motivation Standard Deviation;

	public StaffProfile (String StaffType, String nStaff, String BehavMean, String BehavSD, 
			             String KnowMean, String KnowSD, String ExpMean, String ExpSD, String MotMean, String MotSD){
		Staff = StaffType;
		n = Integer.parseInt(nStaff);
		//System.out.println(Staff + " nStaff = " + n);
		BM = Double.parseDouble(BehavMean);
		BSD = Double.parseDouble(BehavSD);
		KM = Double.parseDouble(KnowMean);
		KSD = Double.parseDouble(KnowSD);
		EM = Double.parseDouble(ExpMean);
		ESD = Double.parseDouble(ExpSD); 
		MM = Double.parseDouble(MotMean);
		MSD = Double.parseDouble(MotSD); 
	}
	
	public String getStaffType(){
		return Staff;
	}
	
	public int getStaffNumber(){
		return n;
	}
	
	public double getBehaviourMean(){
		return BM;
	}
	
	public double getBehviourSD(){
		return BSD;
	}
	
	public double getKnowMean(){
		return KM;
	}
	
	public double getKnowSD(){
		return KSD;
	}
	
	public double getExpMean(){
		return EM;
	}
	
	public double getExpSD(){
		return ESD;
	}
	
	public double getMotMean(){
		return MM;
	}
	
	public double getMotSD(){
		return MSD;
	}
	
	public void print(){
		System.out.println(n + " " + Staff + " BM = " + BM + " BSD = " + BSD + 
				           " KM = " + KM + " KSD = " + KSD + 
				           " EM = " + EM + " ESD = " + ESD + 
				           " MM = " + MM + " MSD = " + MSD);
	}
}
