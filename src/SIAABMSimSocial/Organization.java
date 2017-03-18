package SIAABMSimSocial;

import java.util.ArrayList;
import java.util.Collections;
import java.text.*;

import uchicago.src.sim.util.Random;

import SIAABMSimAgent.ActorProfile;
import SIAABMSimAgent.ActorProfile.RoleType;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.CeoActor;
import SIAABMSimAgent.GhostCeoActor;
import SIAABMSimAgent.ConsultantActor;
import SIAABMSimAgent.ManagerActor;
import SIAABMSimAgent.GhostManagerActor;
import SIAABMSimAgent.TeamLeaderActor;
import SIAABMSimAgent.GhostTeamLeaderActor;
import SIAABMSimAgent.TeamMemberActor;
import SIAABMSimAgent.ExpertActor;
import SIAABMSimAgent.GenericActor;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.Management;
import SIAABMSimConnection.SeniorManagement;
import SIAABMSimConnection.Consultancy;
import SIAABMSimConnection.Leadership;
import SIAABMSimConnection.Subordination;
import SIAABMSimConnection.Peer;
import SIAABMSimConnection.Task;
import SIAABMSimTask.Contract;
import SIAABMSimTask.TEOW;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.coordinates;
import SIAABMSimUtils.polarBehaviour;
import SIAABMSimUtils.simSettings;;

public class Organization {

	protected OrganizationProfile myProfile;
	private Organization myCustomer; //previous Organization in the chain.
	private Organization myProvider; //next Organization in the chain.
	private coordinates orgCentre;
	private ArrayList <Contract> myContractList;
	private StaffProfile CEO;
	private StaffProfile Managers;
	private StaffProfile Experts;
	private StaffProfile TeamLeaders;
	private StaffProfile TeamMembers;
	private ArrayList<StaffProfile> ManagersProfileList;
	private ArrayList<StaffProfile> ExpertsProfileList;
	private ArrayList<StaffProfile> TeamLeadersProfileList;
	private ArrayList<StaffProfile> TeamMembersProfileList;
	private int nCEOs;
	private int nManagers;
	private int nExperts;
	private int nTeamLeaders;
	private int nTeamMembers;
	private int nTLsPerManager;
	private int nExpPerManager;
	private int nExpPerTeam;
	private int nMembersPerTeam;

	private int perceivedNumberOfActors;
	private int perceivedNumberOfActiveActors;

	private polarBehaviour perceivedBehaviour;
	private polarBehaviour perceivedActivedBehaviour;

	protected ArrayList <ActorProfile> actorProfileList;
	protected ArrayList <Actor> actorList;
	protected ArrayList <Interaction> interactionList;
	private ArrayList <TEOW> TEOWList;
	protected ArrayList <Task> taskList;
	protected ArrayList <Department> departmentList;

	protected ArrayList <Task> taskPool;

	private GhostCeoActor ghostCEO;
	private GhostManagerActor ghostManager;
	private GhostTeamLeaderActor ghostTL;
	private boolean initiate;


	private double performance = 0.0;
	private double mood = 0.0;

	private int xSpace, ySpace;
	private probability pD; //probabilityDistribution.
	private DecimalFormat df;
	private simSettings simS;

	private int activeActors = 0;
	private int productiveEffort = 0;
	private int spentEffort = 0;
	private double efficiency = 0.0;
	private double activeMotivation = 0.0;
	private double activeBehaviour = 0.0;
	private double activeAggressive = 0.0;
	private double activeConstructive = 0.0;
	private double activePassive = 0.0;
	private double activeExperience = 0.0;
	private double activeKnowledge = 0.0;

	private double requiredKnowledge = 0.0;
	private double availableKnowledge = 0.0;

	private double motivation = 0.0;
	private double behaviour = 0.0;
	private double aggressive = 0.0;
	private double constructive = 0.0;
	private double passive = 0.0;
	private double experience = 0.0;
	private double knowledge = 0.0;
	private double effectiveness = 0.0;
	private double effectivenessError = 0.0;
	private double effectivenessDiscoveredError = 0.0;
	private int plannedEffort = 0;
	private int actualEffort = 0;
	private int plannedEffortLeft = 0;

	private double minKEM = 0.0; //***13Jun2011


	public Organization(OrganizationProfile organizationProfile, int xSize, int ySize){
		ActorProfile actorProfile;

		myProfile = organizationProfile;
		xSpace = xSize;
		ySpace = ySize;

		initiate = true;

		nCEOs = 0;
		nManagers = 0;
		nExperts = 0;
		nTeamLeaders = 0;
		nTeamMembers = 0;
		nTLsPerManager = 0;
		nExpPerManager = 0;
		nExpPerTeam = 0;
		nMembersPerTeam = 0;

		perceivedBehaviour = new polarBehaviour();
		perceivedActivedBehaviour = new polarBehaviour();

		orgCentre = new coordinates();
		orgCentre.setCartesian(xSize/2, ySize/2);
		pD = pD.getInstance();
		simS = simS.getInstance();
		minKEM = simS.getMinKEM();
		myContractList = new ArrayList<Contract>();
		actorProfileList = new ArrayList<ActorProfile>();
		actorList = new ArrayList<Actor>();
		interactionList = new ArrayList<Interaction>();
		departmentList = new ArrayList<Department>();
		taskList = new ArrayList<Task>();
		taskPool = new ArrayList<Task>();
		TEOWList = new ArrayList<TEOW>();
		df = df = new DecimalFormat("#.##");
		//System.out.println("WILL READ ACTORS PROFILE");
		CEO = myProfile.getCEO();
		Managers = myProfile.getManagers();
		Experts = myProfile.getExperts();
		TeamLeaders = myProfile.getTeamLeaders();
		TeamMembers = myProfile.getTeamMembers();
		ManagersProfileList = myProfile.getManagersProfileList();
		ExpertsProfileList = myProfile.getExpertsProfileList();
		TeamLeadersProfileList = myProfile.getTeamLeadersProfileList();
		TeamMembersProfileList = myProfile.getTeamMembersProfileList();
		if(CEO != null){
			nCEOs = CEO.getStaffNumber();
			//System.out.println("NUMBER OF CEOs = " + nCEOs);
			ghostCEO = null;
		}
		if(Managers != null){
			nManagers = Managers.getStaffNumber();
			ghostManager = null;
		}
		if(Experts != null){
			nExperts = Experts.getStaffNumber();
		}
		if(TeamLeaders != null){
			nTeamLeaders = TeamLeaders.getStaffNumber();
			ghostTL = null;
		}

		if(TeamMembers != null){
			nTeamMembers = TeamMembers.getStaffNumber();
		}
		if(nManagers != 0){
			nTLsPerManager =  nTeamLeaders / nManagers;
			nExpPerManager = nExperts /nManagers;
		}
		if(nTeamLeaders !=0 ){
			nMembersPerTeam = nTeamMembers / nTeamLeaders;
			nExpPerTeam = nExperts / nTeamLeaders;
		}
		//Random.createNormal(0.5, 0.5); //Create Normal Distribution.
		//System.out.println("WILL CREATE ACTORS PROFILE");
		createActorsProfile();
		//actorProfileList = (ArrayList<ActorProfile>) shuffle(actorProfileList);
		//System.out.println("All good here");
		//System.out.println("WILL CREATE ACTORS");
		createActors();
		//System.out.println("WILL SHUFFLE ACTORS");
		actorList = (ArrayList<Actor>) shuffle(actorList);
		//System.out.println("DONE SHUFFLE ACTORS");
		//for(int a=0; a<actorList.size(); a++){
		//	System.out.println(actorList.get(a).myRole() + " " + actorList.get(a).getName() );
		//}
		//System.out.println("WILL CREATE DEPARTMENTS");
		//createInteractions();
		createDepartments();
		//System.out.println("WILL CREATE INTERACTIONS");
		createAllInteractions(myProfile.interactionDensity());
		//System.out.println("CREATED ORGANIZATION ***");
	}
	
	public void setConstructive(){
		getCEO().setConstructive();
		for(int d=0; d<departmentList.size(); d++){
			departmentList.get(d).setConstructive();
		}
	}
	
	public void setAgressive(){
		getCEO().setAgressive();
		for(int d=0; d<departmentList.size(); d++){
			departmentList.get(d).setAgressive();
		}
	}
	
	public void setAggressiveManagement(){
		Actor actor;
		getCEO().setAgressive();
		for(int a=0; a<actorList.size(); a++){
			actor = actorList.get(a);
			if(actor.myRole() == RoleType.Manager){
				actor.setAgressive();
			}
		}
	}
	
	public void setConstructiveManagement(){
		Actor actor;
		getCEO().setConstructive();
		for(int a=0; a<actorList.size(); a++){
			actor = actorList.get(a);
			if(actor.myRole() == RoleType.Manager){
				actor.setConstructive();
			}
		}
	}

	private void createAllInteractions(double interactionDensity){
		Department department;
		for(int d=0; d<departmentList.size(); d++){
			department = departmentList.get(d);
			department.createInteractions(interactionDensity);
			interactionList.addAll(department.getInteractions());
		}
	}

	private ArrayList shuffle(ArrayList<Actor> list){
		ArrayList<Actor> local, result;
		Actor actor;
		int index;
		local = new ArrayList<Actor>();
		result = new ArrayList<Actor>();
		for(int o=0; o<list.size(); o++){
			local.add(list.get(o));
		}
		//System.out.println("shuffle list size = " + list.size());
		for(int a=0; a<local.size();){
			actor = local.get(a);
			if(actor.myRole() != RoleType.TeamMember){
				result.add(actor);
				local.remove(a);
			}
			else{
				a++;
			}
		}
		while(local.size() > 0){
			index = pD.getIntUniform(0, local.size()-1);
			result.add(local.get(index));
			local.remove(index);
			//System.out.println("shuffle index = " + index);
		}
		//System.out.println("end *****");
		return result;
	}


	private void createActorsProfile(){
		ActorProfile profile, actorProfile;
		String actorName;
		String orgName;
		StaffProfile manag;
		StaffProfile exp;
		StaffProfile teamL;
		StaffProfile teamM;
		int nManag;
		int nExp;
		int nTeamL;
		int nTeamM;
		int actorID;
		double BM, BSD, KM, KSD, EM, ESD, MM, MSD;
		double B, K, E, M;
		orgName = myProfile.getName();
		actorID = 0;

		//Create CEO.
		//System.out.println(orgName + " Create CEO Profile");
		actorID = actorID +1;
		actorName = orgName + "-" + String.valueOf(actorID);
		BM = CEO.getBehaviourMean();
		BSD = CEO.getBehviourSD();
		KM = CEO.getKnowMean();
		KSD = CEO.getKnowSD();
		EM = CEO.getExpMean();
		ESD = CEO.getExpSD();
		MM = CEO.getMotMean();
		MSD = CEO.getMotSD();
		B = pD.getNormalDistributionSample(BM, BSD);
		K = pD.getNormalDistributionSample(KM, KSD);
		E = pD.getNormalDistributionSample(EM, ESD);
		M = pD.getNormalDistributionSample(MM, MSD);
		if(K<minKEM){
			K = minKEM;
		}
		if(E<minKEM){
			E = minKEM;
		}
		if(M<minKEM){
			M = minKEM;
		}
		//Create CEO
		if(nCEOs == 1){
			profile = new ActorProfile(actorName, orgName, "CEO", B, E, K, M);
			actorProfileList.add(profile);
		}

		//Create Managers.
		//System.out.println(orgName + " Create Managers Profile");
		nManagers = 0;
		for(int p=0; p<ManagersProfileList.size(); p++){
			manag = ManagersProfileList.get(p);
			nManag = manag.getStaffNumber();
			nManagers = nManagers + nManag;
			for(int m=0; m<nManag; m++){
				actorID = actorID +1;
				actorName = orgName + "-" + String.valueOf(actorID);
				BM = manag.getBehaviourMean();
				BSD = manag.getBehviourSD();
				KM = manag.getKnowMean();
				KSD = manag.getKnowSD();
				EM = manag.getExpMean();
				ESD = manag.getExpSD();
				MM = manag.getMotMean();
				MSD = manag.getMotSD();
				B = pD.getNormalDistributionSample(BM, BSD);
				K = pD.getNormalDistributionSample(KM, KSD);
				E = pD.getNormalDistributionSample(EM, ESD);
				M = pD.getNormalDistributionSample(MM, MSD);
				if(K<minKEM){
					K = minKEM;
				}
				if(E<minKEM){
					E = minKEM;
				}
				if(M<minKEM){
					M = minKEM;
				}
				profile = new ActorProfile(actorName, orgName, "Manager", B, E, K, M);
				actorProfileList.add(profile);
			}
		}

		//Create Experts.
		//System.out.println(orgName + " Create Experts Profile");
		nExp = 0;
		for(int p=0; p<ExpertsProfileList.size(); p++){
			exp = ExpertsProfileList.get(p);
			nExp = exp.getStaffNumber();
			nExperts = nExperts + nExp;
			for(int e=0; e<nExp; e++){
				actorID = actorID +1;
				actorName = orgName + "-" + String.valueOf(actorID);
				BM = exp.getBehaviourMean();
				BSD = exp.getBehviourSD();
				KM = exp.getKnowMean();
				KSD = exp.getKnowSD();
				EM = exp.getExpMean();
				ESD = exp.getExpSD();
				MM = exp.getMotMean();
				MSD = exp.getMotSD();
				B = pD.getNormalDistributionSample(BM, BSD);
				K = pD.getNormalDistributionSample(KM, KSD);
				E = pD.getNormalDistributionSample(EM, ESD);
				M = pD.getNormalDistributionSample(MM, MSD);
				if(K<minKEM){
					K = minKEM;
				}
				if(E<minKEM){
					E = minKEM;
				}
				if(M<minKEM){
					M = minKEM;
				}
				profile = new ActorProfile(actorName, orgName, "Expert", B, E, K, M);
				actorProfileList.add(profile);
			}
			if(nManagers != 0){
				nExpPerManager = nExperts /nManagers;
			}
			else{
				nExpPerManager = 0;
			}
		}

		//Create TeamLeaders.
		nTeamLeaders = 0;
		for(int p=0; p<TeamLeadersProfileList.size(); p++){
			//System.out.println("111");
			teamL = TeamLeadersProfileList.get(p);
			nTeamL = teamL.getStaffNumber();
			nTeamLeaders = nTeamLeaders + nTeamL;
			//System.out.println("nTeamLeaders = " + nTeamLeaders);
			for(int tl=0; tl<nTeamL; tl++){
				//System.out.println("333");
				actorID = actorID +1;
				actorName = orgName + "-" + String.valueOf(actorID);
				BM = teamL.getBehaviourMean();
				BSD = teamL.getBehviourSD();
				KM = teamL.getKnowMean();
				KSD = teamL.getKnowSD();
				EM = teamL.getExpMean();
				ESD = teamL.getExpSD();
				MM = teamL.getMotMean();
				MSD = teamL.getMotSD();
				B = pD.getNormalDistributionSample(BM, BSD);
				K = pD.getNormalDistributionSample(KM, KSD);
				E = pD.getNormalDistributionSample(EM, ESD);
				M = pD.getNormalDistributionSample(MM, MSD);
				if(K<minKEM){
					K = minKEM;
				}
				if(E<minKEM){
					E = minKEM;
				}
				if(M<minKEM){
					M = minKEM;
				}
				profile = new ActorProfile(actorName, orgName, "TeamLeader", B, E, K, M);
				actorProfileList.add(profile);
			}
			if(nManagers != 0){
				nTLsPerManager =  nTeamLeaders / nManagers;
			}
			else{
				nTLsPerManager = 0;
			}
		}

		//Create TeamMembers.
		//System.out.println(orgName + " Create TeamMembers Profile");

		nTeamMembers = 0;
		for(int p=0; p<TeamMembersProfileList.size(); p++){
			teamM = TeamMembersProfileList.get(p);
			nTeamM = teamM.getStaffNumber();
			nTeamMembers = nTeamMembers + nTeamM;
			for(int tm=0; tm<nTeamM; tm++){
				actorID = actorID +1;
				actorName = orgName + "-" + String.valueOf(actorID);
				BM = teamM.getBehaviourMean();
				BSD = teamM.getBehviourSD();
				KM = teamM.getKnowMean();
				KSD = teamM.getKnowSD();
				EM = teamM.getExpMean();
				ESD = teamM.getExpSD();
				MM = teamM.getMotMean();
				MSD = teamM.getMotSD();
				B = pD.getNormalDistributionSample(BM, BSD);
				K = pD.getNormalDistributionSample(KM, KSD);
				E = pD.getNormalDistributionSample(EM, ESD);
				M = pD.getNormalDistributionSample(MM, MSD);
				if(K<minKEM){
					K = minKEM;
				}
				if(E<minKEM){
					E = minKEM;
				}
				if(M<minKEM){
					M = minKEM;
				}
				profile = new ActorProfile(actorName, orgName, "TeamMember", B, E, K, M);
				actorProfileList.add(profile);
			}
			if(nTeamLeaders !=0 ){
				nMembersPerTeam = nTeamMembers / nTeamLeaders;
			}

			//actorProfileList = (ArrayList<ActorProfile>) shuffle(actorProfileList);
			//System.out.println("All good here");

		}
	}

	private void createActors(){
		int x, y;
		String orgName;
		Actor actor;
		ActorProfile profile, actorProfile;
		//System.out.println("ORGANIZATION Create Actors");
		orgName = myProfile.getName();

		for (int p=0; p<actorProfileList.size(); p++){
			//x = Random.uniform.nextIntFromTo(0, xSpace-1);
			//y = Random.uniform.nextIntFromTo(0, ySpace-1);
			profile = actorProfileList.get(p);
			x = pD.getIntUniform(0, xSpace-1);
			y = pD.getIntUniform(0, ySpace-1);
			actor = null;

			switch (profile.role()){
			case CEO:{
				actor = new CeoActor(profile, this, x, y);
				actor.setBoss((Actor) null);
				break;
			}
			case Manager:{
				actor = new ManagerActor(profile, this, x, y);
				break;
			}
			case TeamLeader:{
				actor = new TeamLeaderActor(profile, this, x, y);
				break;
			}
			case Expert:{
				actor = new ExpertActor(profile, this, x, y);
				break;
			}
			case TeamMember:{
				actor = new TeamMemberActor(profile, this, x, y);
				break;
			}
			case Consultant:{
				actor = new ConsultantActor(profile, this, x, y);
				break;
			}
			case Generic:{
				actor = new GenericActor(profile, this, x, y);
				break;
			}
			}
			if (actor != null){
				actorList.add(actor);
				//addActorToSpace(actor);
				/*
				System.out.println(orgName + " Create Actor " + actor.getName() + " role = " + actor.myRole() + 
						" B = " + df.format(actor.getBehaviour()) +
						" E = " + df.format(actor.getExperience()) + 
						" K = " + df.format(actor.getKnowledge())+
						" M = " + df.format(actor.getMotivation()) +
						" added to actorSpace on " + x + " , " + y);
				 */
			}
			else {
				//System.out.println("*** " + orgName + " Actor " + profile.role() + " is NULL");
			}
		}
	}

	public boolean isAdaptive(){
		if(nCEOs == 0 && nManagers == 0 && nTeamLeaders == 0){
			return true;
		}
		else {
			return false;
		}
	}

	private void createDepartments(){
		String orgName;
		Department department;
		Team team;
		Interaction interaction, thisTLInteraction, nextTLInteraction, fromInteraction, toInteraction;
		ArrayList<Interaction> interactions, distantInteractions;
		Actor fromActor, toActor, fromTM, toTM;
		RoleType role;
		int nAllocations, nTLAllocations, nExpAllocations, nTMAllocations, nGroups, nTeams;
		int nExpertsInTheTeam;
		orgName = myProfile.getName();
		//System.out.println("ORGANIZATION Create Interactions");
		//Get all the Actors in the Organization.
		nGroups = 0;
		nTeams = 0;

		for(int fa=0; fa<actorList.size(); fa++){
			fromActor = actorList.get(fa);
			role = fromActor.myRole();
			switch (role){
			case CEO:{
				//System.out.println("ORGANIZATION " + orgName + " found CEO");
				fromActor.allocated(true);
				for(int ta=fa; ta<actorList.size(); ta++){
					toActor = actorList.get(ta);
					if(toActor.myRole() == RoleType.Manager){
						//System.out.println("ORGANIZATION " + orgName + " found Manager for CEO");
						department = new Department(this, toActor);
						departmentList.add(department);
						toActor.allocated(true);
						//System.out.println("ORGANIZATION Interaction from Actor " + fromActor.getName() + " " + fromActor.myRole() + 
						//		" to Actor " + toActor.getName() + " " + toActor.myRole() + " added to actorList");
					}
				}
				break;
			}
			case Manager:{
				//System.out.println("ORGANIZATION " + orgName + " found Manager");

				//Allocate TLs to Manager.
				nGroups = nGroups +1;
				if (nGroups == 1){
					nTLAllocations = nTLsPerManager + nTeamLeaders % nManagers;
				}
				else {
					nTLAllocations = nTLsPerManager;
				}
				for(int ma=fa; ma<actorList.size(); ma++){
					toActor = actorList.get(ma);
					if(toActor.myRole() == RoleType.TeamLeader){
						if(!toActor.isAllocated()){
							//System.out.println("ORGANIZATION " + orgName + " found TeamLeader for Manager");
							toActor.allocated(true);
							nTLAllocations = nTLAllocations -1;
							team = new Team(fromActor.getDepartment(), toActor);
							fromActor.getDepartment().addTeam(team);
							//System.out.println("ORGANIZATION Interaction from Actor " + fromActor.getName() + " " + fromActor.myRole() + 
							//		" to Actor " + toActor.getName() + " " + toActor.myRole() + " added to actorList");
						}
						if(nTLAllocations == 0){
							//Stop searching for more TLs.
							ma = actorList.size();
						}
					}
				}
				break;
			}
			case TeamLeader:{
				//System.out.println("ORGANIZATION " + orgName + " found TeamLeader");
				//Allocate TeamMeber to TL.
				nTeams = nTeams +1;
				if (nTeams == 1){
					nTMAllocations = nMembersPerTeam + nTeamMembers % nTeamLeaders;
				}
				else {
					nTMAllocations = nMembersPerTeam;
				}
				nExpertsInTheTeam = 0;
				for(int tl=1; tl<actorList.size(); tl++){
					toActor = actorList.get(tl);
					if(toActor.myRole() == RoleType.Expert){
						if((!toActor.isAllocated()) & (nExpertsInTheTeam < (nExpPerTeam + 1))){
							//System.out.println("ORGANIZATION " + orgName + " found Expert for TL");
							toActor.allocated(true);
							nExpertsInTheTeam = nExpertsInTheTeam + 1;
							fromActor.getTeam().addExpert(toActor);
							//System.out.println("ORGANIZATION Interaction from Actor " + fromActor.getName() + " " + fromActor.myRole() + 
							//		" to Actor " + toActor.getName() + " " + toActor.myRole() + " added to actorList");
						}
					}
					if(toActor.myRole() == RoleType.TeamMember){
						if(!toActor.isAllocated()){
							//System.out.println("ORGANIZATION " + orgName + " found TeamMember for TL");
							toActor.allocated(true);
							nTMAllocations = nTMAllocations -1;
							//toActor.setBoss(fromActor);
							fromActor.getTeam().addTeamMember(toActor);
							//System.out.println("ORGANIZATION Interaction from Actor " + fromActor.getName() + " " + fromActor.myRole() + 
							//		" to Actor " + toActor.getName() + " " + toActor.myRole() + " added to actorList");
						}
						if(nTMAllocations == 0){
							//Stop searching for more TMs.
							tl = actorList.size();
						}
					}
				}
				break;
			}
			case Expert:{
				//if (isAdaptive()){
				for(int ex=0; ex<actorList.size(); ex++){
					toActor = actorList.get(ex);
					if(toActor != null){
						if(fromActor != toActor){
							if(pD.drawChanceFromUniform(0.0)){
								interaction = new Consultancy(fromActor, toActor, "ON", 0.5);
								fromActor.addToInteraction(interaction);
								toActor.addFromInteraction(interaction);
								interactionList.add(interaction);
							}
						}
					}
				}
				//}
				//System.out.println("ORGANIZATION " + orgName + " found Expert");
				break;
			}
			case TeamMember:{
				//if (isAdaptive()){
				for(int tm=0; tm<actorList.size(); tm++){
					toActor = actorList.get(tm);
					if(toActor != null){
						if(fromActor != toActor){
							if(pD.drawChanceFromUniform(0.00)){
								interaction = new Peer(fromActor, toActor, "ON", 0.5);
								fromActor.addToInteraction(interaction);
								toActor.addFromInteraction(interaction);
								interactionList.add(interaction);
							}
						}
					}
				}
				//}
				//System.out.println("ORGANIZATION " + orgName + " found TeamMember");
				break;
			}
			case Consultant:{
				if (isAdaptive()){

				}
				//System.out.println("ORGANIZATION " + orgName + " found Consultant");
				break;
			}
			case Generic:{
				//System.out.println("ORGANIZATION " + orgName + " found Generic");
				break;
			}
			}
		}
	}
	
	public void iterativeFeedback(){
		//System.out.println(name() + " received Iterative FeedBack");
		Actor actor;
		double feedbackLearning;
		feedbackLearning = simS.getIterativeLearningFactor();
		for(int a=0; a<actorList.size(); a++){
			if(pD.drawChanceFromUniform(2.0 * feedbackLearning)){
			actor = actorList.get(a);
			actor.increaseExperience(0.10);
			actor.increaseKnowledge(0.10);
			}
		}
	}

	public String name(){
		return myProfile.getName();
	}

	public ArrayList<String> getContracts(){
		return myProfile.getTasks();
	}

	public ArrayList<Task> getTasks(){
		return taskList;
	}

	public ArrayList<Task> getTaskPool(){
		return taskPool;
	}

	public void addTaskToTaskPool(Task task){
		taskPool.add(task);
	}

	public void setCustomer(Organization org){
		myCustomer = org;
	}

	public Organization getCustomer(){
		return myCustomer;
	}

	public void setProvider(Organization org){
		myProvider= org;
	}

	public Organization getProvider(){
		return myProvider;
	}

	public void assignContract(Contract contract){
		myContractList.add(contract);
		TEOWList.addAll(contract.getTEOW());
	}

	public ArrayList <Contract> getContractList(){
		return myContractList;
	}

	public ArrayList <TEOW> getTEOWList(){
		return TEOWList;
	}

	public ArrayList<Actor> getActors(){
		return actorList;
	}

	public ArrayList<Interaction> getInteractions(){
		return interactionList;
	}

	public double getEffectiveness(){
		return effectiveness;
	}

	public boolean isAggressive(){
		return perceivedBehaviour.isAgressive();
	}

	public boolean isPassive(){
		return perceivedBehaviour.isPassive();
	}

	public boolean isConstructive(){
		return perceivedBehaviour.isConstructive();
	}

	public double getEffectivenessError(){
		return effectivenessError;
	}

	public double getDiscoveredError(){
		return effectivenessDiscoveredError;
	}

	public double getPlannedEffort(){
		return (double) plannedEffort;
	}

	public double getActualEffort(){
		return (double) actualEffort;
	}

	public double getPlannedEffortLeft(){
		return (double) plannedEffortLeft;
	}

	public int getActiveActors(){
		return activeActors;
	}

	public double getActiveMotivation(){
		return activeMotivation;
	}

	public double getActiveConstructive(){
		return activeConstructive;
	}

	public double getActivePassive(){
		return activePassive;
	}


	public double getActiveAggressive(){
		return activeAggressive;
	}

	public double getActiveExperience(){
		return activeExperience;
	}

	public double getActiveKnowledge(){
		return activeKnowledge;
	}


	public double getMotivation(){
		return motivation;
	}

	/*
	public double getConstructive(){
		return perceivedBehaviour.getConstructive();
	}


	public double getPassive(){
		return perceivedBehaviour.getPassive();
	}

	public double getAggressive(){
		return perceivedBehaviour.getAggressive();
	}
	 */

	public double getConstructive(){
		return constructive;
	}

	public double getPassive(){
		return passive;
	}

	public double getAggressive(){
		return aggressive;
	}

	public double getExperience(){
		return experience;
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

	public double getPerformance(){
		return performance;
	}

	public double socialMood(){
		return mood;
	}

	public int getNumberOfActors(){
		return actorList.size();
	}

	public int numberOfCEOs(){
		return nCEOs;
	}

	public Actor getCEO(){
		Actor actor;
		actor = null;
		for(int a=0; a<actorList.size(); a++){
			actor = actorList.get(a);
			if(actor.myRole() == RoleType.CEO){
				return actor;
			}
			else{
				actor = null;
			}
		}
		return actor;
	}

	public coordinates getCentre(){
		Actor ceo;
		ceo = getCEO();
		if(ceo != null){
			orgCentre.setCartesian(ceo.getX(), ceo.getY());
		}
		return orgCentre;

	}

	public void setCentre(int x, int y){
		Actor ceo;
		ceo = getCEO();
		if(ceo != null){
			ceo.setX(x);
			ceo.setY(y);
		}
		orgCentre.setCartesian(x, y);
	}

	public int numberOfManagers(){
		return nManagers;
	}

	public int numberOfTeamLeaders(){
		return nTeamLeaders;
	}

	public int numberOfTeamMembers(){
		return nTeamMembers;
	}

	public boolean hierarchic(){
		if((numberOfCEOs() + numberOfManagers() + numberOfTeamLeaders()) == 0){
			return false;
		}
		else{
			return true;
		}
	}

	public int getProductiveEffort(){
		return productiveEffort;
	}

	public int getSpentEffort(){
		return spentEffort;
	}

	public double getEfficiency(){
		return efficiency;
	}

	public void clearEffort(){
		Actor actor;
		for(int a=0; a<actorList.size(); a++){
			actor = actorList.get(a);
			//actor.clearEffort();
		}
	}

	public void updateEffort(){
		Actor actor;
		productiveEffort = 0;
		spentEffort = 0;
		for(int a=0; a<actorList.size(); a++){
			actor = actorList.get(a);
			//if(actor.isActive()){
			actor.updateEffort();
			productiveEffort = productiveEffort + actor.getProductiveEffort();
			spentEffort = spentEffort + actor.getSpentEffort();
			//}
		}
		if(spentEffort >0){
			efficiency = (double) productiveEffort / (double) spentEffort;
		}
		else{
			//System.out.println("000000000000000");
			efficiency = 0.0;
		}
	}

	public double getQuality(){
		double q;
		int s;
		q = 0.0;
		s = myContractList.size();
		if(s > 0){
			for(int c=0; c<s; c++){
				q = q + myContractList.get(c).getQuality();
			}
			q =  (q / s);
		}
		else{
			q = 0.0;
		}
		//System.out.println(myProfile.getName() + " Quality = " + df.format(q));
		return q;
	}

	public void step() {
		ActorProfile actorProfile;
		Actor actor;
		int nActors;
		// pActors this is the number of Actors weighted in accordance with their roles
		// Ceo = 50, Manager = 20; TL = 10, Exp = 10 and TM = 1;
		int pActors;
		int wActor; // this is the weight of the actor n accordance with its role;
		String orgName;
		double mot = 0.0;
		double behav = 0.0;
		double agg = 0.0;
		double con = 0.0;
		double pas = 0.0;
		double exp = 0.0;
		double kno = 0.0;
		double rkno = 0.0;
		double akno = 0.0;
		double cEx = 0.0;
		double cKn = 0.0;


		if(isAdaptive()){
			orgName = myProfile.getName();
			if(initiate){
				initiate = false;
				//Create Ghost CEO
				actorProfile = new ActorProfile("ghostCEO", orgName, "CEO", 0.33, 1.0, 1.0, 1.0);
				ghostCEO = new GhostCeoActor(actorProfile, this, 0, 0);
				//Create Ghost Manager
				actorProfile = new ActorProfile("ghostManager", orgName, "Manager", 0.33, 1.0, 1.0, 1.0);
				ghostManager = new GhostManagerActor(actorProfile, this, 0, 0);
				//Create Ghost TeamLeader
				actorProfile = new ActorProfile("ghostTL", orgName, "TeamLeader", 0.33, 1.0, 1.0, 1.0);
				ghostTL = new GhostTeamLeaderActor(actorProfile, this, 0, 0);

				ghostCEO.assignManager(ghostManager);
				ghostManager.assignTeamLeader(ghostTL);
				ghostTL.assignManager(ghostManager);
			}
		}

		if(ghostCEO != null){
			//System.out.println("Ghost CEO step");
			ghostCEO.step();
		}

		if(ghostManager != null){
			//System.out.println("Ghost Manager step");
			ghostManager.step();
		}

		if(ghostTL != null){
			//System.out.println("Ghost TL step");
			ghostTL.step();
		}

		perceivedNumberOfActors = 0;
		perceivedNumberOfActiveActors = 0;

		activeActors = 0;
		activeMotivation = 0.0;
		activeAggressive = 0.0;
		activeConstructive = 0.0;
		activePassive = 0.0;
		activeExperience = 0.0;
		activeKnowledge = 0.0;

		motivation = 0.0;
		aggressive = 0.0;
		constructive = 0.0;
		passive = 0.0;
		experience = 0.0;
		knowledge = 0.0;
		requiredKnowledge = 0.0;
		availableKnowledge = 0.0;

		//System.out.println(myProfile.getName() + " step()");
		nActors = actorList.size();
		pActors = 0;
		taskList.clear();

		//productiveEffort = 0;
		//spentEffort = 0;

		for (int a=0; a<nActors; a++){
			actor = actorList.get(a);
			perceivedNumberOfActors = perceivedNumberOfActors + actor.roleWeight();
			actor.step();
			taskList.addAll(actor.myTasks());

			behav = behav + actor.roleWeight() * actor.getBehaviour();

			mot = mot + actor.getMotivation();
			agg = agg + actor.getAggressive();
			con = con + actor.getConstructive();
			pas = pas + actor.getPassive();
			exp = exp + actor.getExperience();
			kno = kno + actor.getKnowledge();
			if(actor.isActive()){
				activeActors = activeActors +1;
				perceivedNumberOfActiveActors = perceivedNumberOfActiveActors + actor.roleWeight();
				activeMotivation = activeMotivation + actor.getMotivation();
				activeAggressive = activeAggressive + actor.getAggressive();
				activeConstructive = activeConstructive + actor.getConstructive();
				activePassive = activePassive + actor.getPassive();
				activeExperience = activeExperience + actor.getExperience();
				activeKnowledge = activeKnowledge + actor.getKnowledge();

				activeBehaviour = activeBehaviour + actor.roleWeight() * actor.getBehaviour();

				rkno = rkno + actor.requiredKnowledge();
				akno = akno + actor.availableKnowledge();
			}
		}

		behav = behav / perceivedNumberOfActors;
		//perceivedBehaviour.setBehaviour(behav);

		activeBehaviour = activeBehaviour / perceivedNumberOfActiveActors;
		//perceivedActivedBehaviour.setBehaviour(activeBehaviour);

		motivation = mot / nActors;
		behaviour = behav / nActors;
		aggressive = agg / nActors;
		constructive = con / nActors;
		passive = pas / nActors;
		experience = exp / nActors;
		knowledge = kno / nActors;
		requiredKnowledge = rkno / activeActors;
		availableKnowledge = akno / activeActors;

		activeMotivation = activeMotivation / activeActors;
		activeBehaviour = activeBehaviour / activeActors;
		activeAggressive = activeAggressive / activeActors;
		activeConstructive = activeConstructive / activeActors;
		activePassive = activePassive/ activeActors;
		activeExperience = activeExperience / activeActors;
		activeKnowledge = activeKnowledge / activeActors;

		//perceivedBehaviour.setBehaviour(behav);

		updateEffort();

		//Calculate CollectiveExperience and CollectveKnowledge

		for(int d=0; d<departmentList.size(); d++){
			cEx = cEx + departmentList.get(d).getCollectiveExperience();
			cKn = cKn + departmentList.get(d).getCollectiveKnowledge();
		}

		if(departmentList.size() > 0){
			cEx = cEx / departmentList.size();
			cKn = cKn / departmentList.size();
		}

		//System.out.println(("Org = " + myProfile.getName() + " ColectiveExperience = " + df.format(cEx) + " ColectiveKnowledge = " + df.format(cKn)));

		//Calculate Organization Mood as a function of the Organization's Behaviour, Performance and Motivation.
		
		mood = ((0.4 * constructive)) + (0.3 * getQuality())+ (0.3 * motivation);

		//System.out.println(myProfile.getName() + " Social Mood = " + df.format(socialMood()));
		
		for(int c=0; c<myContractList.size(); c++){
			//System.out.println(name() + " " + df.format(myContractList.get(c).getEfficiency()));
			myContractList.get(c).getEfficiency();
		}
	}
}
