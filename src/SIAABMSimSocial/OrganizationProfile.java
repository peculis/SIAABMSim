package SIAABMSimSocial;

import java.util.ArrayList;
import SIAABMSimAgent.Actor;
import SIAABMSimConnection.Interaction;

public class OrganizationProfile {

	private String myName;
	private double interactDensity;
	private ArrayList <Actor> myStaff;
	private ArrayList <Interaction> myStructure;
	private ArrayList <String> myTasks;
	private StaffProfile CEOProfile = null;
	private StaffProfile ManagersProfile = null;
	private StaffProfile ExpertsProfile = null;
	private StaffProfile TeamLeadersProfile = null;
	private StaffProfile TeamMembersProfile = null;
	private ArrayList<StaffProfile> ManagersProfileList;
	private ArrayList<StaffProfile> ExpertsProfileList;
	private ArrayList<StaffProfile> TeamLeadersProfileList;
	private ArrayList<StaffProfile> TeamMembersProfileList;

	public OrganizationProfile (String name, String interactionDensity){
		myStaff = new ArrayList<Actor>();
		myStructure = new ArrayList<Interaction>();
		myTasks = new ArrayList<String>();
		ManagersProfileList = new ArrayList<StaffProfile>();
		ExpertsProfileList = new ArrayList<StaffProfile>();
		TeamLeadersProfileList = new ArrayList<StaffProfile>();
		TeamMembersProfileList = new ArrayList<StaffProfile>();
		myName = name;
		interactDensity = Double.parseDouble(interactionDensity);
	}

	public void ceoProfile (String nStaff, String BehavMean, String BehavSD, String KnowMean, 
			                String KnowSD, String ExpMean, String ExpSD, String MotMean, String MotSD){
		CEOProfile = new StaffProfile("CEO", nStaff, BehavMean, BehavSD, 
				                      KnowMean, KnowSD, ExpMean, ExpSD, MotMean, MotSD);
	}

	public void ManagersProfile (String nStaff, String BehavMean, String BehavSD, 
			                     String KnowMean, String KnowSD, String ExpMean, String ExpSD, String MotMean, String MotSD){
		ManagersProfile = new StaffProfile("Managers", nStaff, BehavMean, BehavSD, 
				                           KnowMean, KnowSD, ExpMean, ExpSD, MotMean, MotSD);
		ManagersProfileList.add(ManagersProfile);
	}
	
	public void ExpertsProfile (String nStaff, String BehavMean, String BehavSD, 
			                    String KnowMean, String KnowSD, String ExpMean, String ExpSD, String MotMean, String MotSD){
		ExpertsProfile = new StaffProfile("Experts", nStaff, BehavMean, BehavSD, KnowMean, KnowSD, ExpMean, ExpSD, MotMean, MotSD);
		ExpertsProfileList.add(ExpertsProfile);
	}
	
	public void TeamLeadersProfile (String nStaff, String BehavMean, String BehavSD, 
			                        String KnowMean, String KnowSD, String ExpMean, String ExpSD, String MotMean, String MotSD){
		TeamLeadersProfile = new StaffProfile ("TeamLeaders", nStaff, BehavMean, BehavSD, KnowMean, KnowSD, ExpMean, ExpSD, MotMean, MotSD);
		TeamLeadersProfileList.add(TeamLeadersProfile);
	}
	
	public void TeamMembersProfile (String nStaff, String BehavMean, String BehavSD, 
			                        String KnowMean, String KnowSD, String ExpMean, String ExpSD, String MotMean, String MotSD){
		TeamMembersProfile = new StaffProfile ("TeamMembers", nStaff, BehavMean, BehavSD, KnowMean, KnowSD, ExpMean, ExpSD, MotMean, MotSD);
		TeamMembersProfileList.add(TeamMembersProfile);
		//System.out.println("TeamMembersProfile");
	}
	
	public String getName(){
		return myName;
	}
	
	public double interactionDensity(){
		return interactDensity;
	}
	
	public StaffProfile getCEO(){
		return CEOProfile;
	}
	
	public StaffProfile getManagers(){
		return ManagersProfile;
	}
	
	public StaffProfile getExperts(){
		return ExpertsProfile;
	}
	
	public StaffProfile getTeamLeaders(){
		return TeamLeadersProfile;
	}
	
	public StaffProfile getTeamMembers(){
		return TeamMembersProfile;
	}
	
	public ArrayList<StaffProfile> getManagersProfileList(){
		return ManagersProfileList;
	}
	
	public ArrayList<StaffProfile> getExpertsProfileList(){
		return ExpertsProfileList;
	}
	
	public ArrayList<StaffProfile> getTeamLeadersProfileList(){
		return TeamLeadersProfileList;
	}
	
	public ArrayList<StaffProfile> getTeamMembersProfileList(){
		return TeamMembersProfileList;
	}
	
	public void addTask (String task){
		myTasks.add(task);
	}
	
	public ArrayList<String> getTasks(){
		return myTasks;
	}
	
	public void printOrganizationProfile(){
		System.out.println("Organization =" + myName);
		System.out.print("My Tasks = ");
		for(int t=0; t<myTasks.size(); t++){
			System.out.print(myTasks.get(t));
			System.out.print(" ");
		}
		System.out.println();
		CEOProfile.print();
		ManagersProfile.print();
		ExpertsProfile.print();
		TeamLeadersProfile.print();
		TeamMembersProfile.print();
	}
	
}
