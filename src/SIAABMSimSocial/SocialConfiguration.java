package SIAABMSimSocial;

//28Jan 2010: Fixed some problems on SocialConfiguration class related to the allocation of agents on space.

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.text.*;

import uchicago.src.sim.space.Object2DGrid;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.ActorProfile;
import SIAABMSimAgent.ActorProfile.RoleType;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.Peer;
import SIAABMSimConnection.RemotePeer;
import SIAABMSimConnection.Consultancy;
import SIAABMSimConnection.Task;
import SIAABMSimUtils.coordinates;
import SIAABMSimUtils.probability;
import SIAABMSimUtils.simSettings;

public class SocialConfiguration {

	private int or; // number of rows of organizations file.
	private int oc; //number of columns of organizations file.

	private String [][]om; //Organizations Matrix as read from the file.

	private int pr; //number of rows in the Probabilities Matrix.
	private int pc; //number of columns in the Probabilities Matrix (Aggressive, Doer, Constructive, Nice, Passive, Neutral).
	private int nStyles; //number of Behaviour Styles with probabilities assigned for each type of Situation.
	private int nSituations; //number of Situations with probabilities assigned for each Behaviour Style;
	private String [][]probabilitiesFile; //probabilities is the Behaviour Probability read from the file.
	private double [][]probabilities;

	private ArrayList <OrganizationProfile> OrgProfileList;
	private ArrayList <Organization> organizationList;

	private int nactors; // Number of Actors created by the Social Configuration.
	private ArrayList <Actor> actorList; // List of Actors created by the Social Configuration.
	private Object2DGrid actorSpace;     //agentSpace is where Actors will be placed on.

	private int ninteractions; // Number of Interactions created by the Social Configuration.
	private ArrayList <Interaction> interactionList; // List of Interactions created by the Social Configuration.
	private Object2DGrid interactionSpace; //interactionSpace is where Interactions will be placed on.

	private int ntasks;
	private ArrayList <Task> taskList;
	private ArrayList <Task> displayableTaskList;
	private Object2DGrid taskSpace;

	private coordinates actorCoord;
	private int xSpace, ySpace;

	private probability pD; //probabilityDistribution.
	private simSettings simS;

	private DecimalFormat df;

	private int activeActors = 0;
	private double activeMotivation = 0.0;
	private double activeAggressive = 0.0;
	private double activeConstructive = 0.0;
	private double activePassive = 0.0;
	private double activeExperience = 0.0;
	private double activeKnowledge = 0.0;

	private double motivation = 0.0;
	private double aggressive = 0.0;
	private double constructive = 0.0;
	private double passive = 0.0;
	private double experience = 0.0;
	private double knowledge = 0.0;

	private double requiredKnowledge = 0.0;
	private double availableKnowledge = 0.0;


	private double stepRequiredKnowledge = 0.0;
	private double stepAvailableKnowledge = 0.0;

	private double stepSpentEffort = 0.0;
	private double stepProductiveEffort = 0.0;

	private int productiveEffort = 0;
	private int spentEffort = 0;
	private double efficiency = 0.0;
	private double lastEfficiency = 0.0;
	private double effectiveness = 0.0;
	private double effectivenessError = 0.0;
	private double effectivenessDiscoveredError = 0.0;
	private int plannedEffort = 0;
	private int actualEffort = 0;
	private int plannedEffortLeft = 0;


	public SocialConfiguration (String organizationsFileName, int xSize, int ySize) throws Throwable {
		xSpace = xSize;
		ySpace = ySize;
		pD = pD.getInstance();
		simS = simS.getInstance();
		df = df = new DecimalFormat("#.##");
		OrgProfileList = new ArrayList<OrganizationProfile>();
		organizationList = new ArrayList<Organization>();

		actorList = new ArrayList<Actor>();
		actorSpace = new Object2DGrid(xSpace,ySpace);
		interactionList = new ArrayList<Interaction>();
		interactionSpace = new Object2DGrid(xSpace,ySpace);
		//System.out.println("***** " + organizationsFileName);
		readSocialConfiguration(organizationsFileName);
		//PrintSocialProfile();
		//System.out.println("CREATED ORGANIZATIONS ----");
		createOrganizations();
		createDistantInteractions();
		nactors = actorList.size();
		setCustomersAndProviders();
		//System.out.println("SET CUSTOMERS AND PROVIDERS");
		//System.out.println("WILL PLACE ACTORS ON SPACE");
		placeActorsOnSpace();
		//System.out.println("ACTORS PLACED ON SPACE");

		taskList = new ArrayList<Task>();
		displayableTaskList = new ArrayList<Task>();
		taskSpace = new Object2DGrid(xSpace,ySpace);
	}

	private void readSocialConfiguration(String fileName) throws Throwable{
		//The Organizations file is expected to be on a specific format.
		//ORG			ORGName	InteractionDensity Transformation1	Transformation2
		//CEO			1	BehavM	BehavSD	KnowM	KNowSD	ExpM	ExpSD	MotM	MotSD
		//Manager		N	BehavM	BehavSD	KnowM	KNowSD	ExpM	ExpSD	MotM	MotSD
		//TeamLeader	N	BehavM	BehavSD	KnowM	KNowSD	ExpM	ExpSD	MotM	MotSD
		//TeamMember	N	BehavM	BehavSD	KnowM	KNowSD	ExpM	ExpSD	MotM	MotSD
		//No checks are made to verify this.

		OrganizationProfile orgProfile;
		double interactDensity;
		orgProfile = new OrganizationProfile("Dummy", "0.0");
		BufferedReader inputStream = null;
		//System.out.println(fileName);
		int i = 0;
		int j = 0;
		int x, y;
		int tokenCount = 0;
		Actor actor;
		String l, n;
		String nStaff, BehavMean, BehavSD, KnowMean, KnowSD, ExpMean, ExpSD, MotM, MotSD;

		actorCoord = new coordinates();

		try {
			inputStream = new BufferedReader (new FileReader (fileName));
			inputStream.mark(2000000);
			while ((l = inputStream.readLine()) != null){
				StringTokenizer st = new StringTokenizer(l);
				tokenCount = st.countTokens();
				i++;
				j = tokenCount;
			}
			or = i;
			oc = j;
			om = new String [or][oc];
			//initialize om with empty string "".
			for (i=0; i<or; i++){
				for (j=0; j<oc; j++){
					om[i][j] = "";
				}
			}

			inputStream.reset();
			i = 0;
			while ((l = inputStream.readLine()) != null){
				//System.out.println("######  " + l);
				StringTokenizer st = new StringTokenizer(l);
				tokenCount = st.countTokens();
				j = 0;
				while (st.hasMoreTokens()) {
					n = st.nextToken(); 
					om [i][j] = n;
					j++;
				}
				i++;
			}
			//System.out.println();
		}
		catch (IOException e){
			System.out.println("^^^^^ Problems reading Organizations file " + fileName);
		}
		finally {
			if (inputStream != null){
				inputStream.close();
			}
		}

		//System.out.println("READING ORGANIZATION FILE");
		for(int r=0; r<or; r++){
			//System.out.println(om[r][0]);
			if(om[r][0].equals("ORG")){
				//System.out.println("NEW ORGANIZATION");
				orgProfile = new OrganizationProfile(om[r][1], om[r][2]);
				OrgProfileList.add(orgProfile);
				for(int c=3; c<oc; c++){
					if(! om[r][c].equals("")){
						orgProfile.addTask(om[r][c]);
					}
				}
			}
			else{
				nStaff= om[r][1];
				BehavMean= om[r][2];
				BehavSD= om[r][3];
				KnowMean= om[r][4];
				KnowSD = om[r][5];
				ExpMean= om[r][6];
				ExpSD= om[r][7];
				MotM = om[r][8];
				MotSD = om[r][9];
				if(om[r][0].equals("CEO")){
					orgProfile.ceoProfile(nStaff, BehavMean, BehavSD, KnowMean, KnowSD, ExpMean, ExpSD, MotM, MotSD);
				}
				if(om[r][0].equals("Manager")){
					orgProfile.ManagersProfile(nStaff, BehavMean, BehavSD, KnowMean, KnowSD, ExpMean, ExpSD, MotM, MotSD);
				}
				if(om[r][0].equals("Expert")){
					orgProfile.ExpertsProfile(nStaff, BehavMean, BehavSD, KnowMean, KnowSD, ExpMean, ExpSD, MotM, MotSD);
				}
				if(om[r][0].equals("TeamLeader")){
					orgProfile.TeamLeadersProfile(nStaff, BehavMean, BehavSD, KnowMean, KnowSD, ExpMean, ExpSD, MotM, MotSD);
				}
				if(om[r][0].equals("TeamMember")){
					orgProfile.TeamMembersProfile(nStaff, BehavMean, BehavSD, KnowMean, KnowSD, ExpMean, ExpSD, MotM, MotSD);
				}
			}
		}
		//System.out.println("READING ORGANIZATION FILE DONE");
	}


	private void createOrganizations(){
		OrganizationProfile orgProfile;
		coordinates orgCentre;
		Organization organization;
		ArrayList<Actor> actors;
		ArrayList<Interaction> interactions;
		//System.out.println("WILL CREATE ORGANIZATIONS");
		for(int o=0; o<OrgProfileList.size(); o++){
			orgProfile = OrgProfileList.get(o);
			organization = new Organization(orgProfile, xSpace, ySpace);
			organizationList.add(organization);
			actors = organization.getActors();
			//Inform all Actors the Orh=ganization they belong to.
			for(int a=0; a<actors.size(); a++){
				actors.get(a).setOrganization(organization);
			}
			interactions = organization.getInteractions();
			actorList.addAll(actors); // append Organization's actors to the actorList.
			interactionList.addAll(interactions); // append Organization's interactions to the interactionList.
			orgCentre = organization.getCentre();
		}
		//System.out.println("CREATED ORGANIZATIONS");
	}

	public void PrintSocialProfile(){
		//System.out.println();
		//System.out.println("WILL PRINT SOCIALPROFILE");
		//System.out.println("Social Profile - Profile Size = " + OrgProfileList.size());
		for (int org=0; org<OrgProfileList.size(); org++){
			OrgProfileList.get(org).printOrganizationProfile();
		}
	}

	public void PrintSocialProfileFile(){
		System.out.println();
		System.out.println("Social Profile File");
		for (int i=0; i<or; i++){
			for (int j=0; j< oc; j++){
				System.out.print(om[i][j]);
				System.out.print("\t");
			} 
			System.out.println(" ");
		}
	}

	private int numberOfOrganizations(){
		return organizationList.size();
	}

	private int numberOfCEOActors(){
		//int numberOfCEOs;
		//numberOfCEOs = organizationList.size();
		//System.out.println("NUMBER OF CEOS = " + numberOfCEOs);
		//return numberOfCEOs;
		int n;
		n = 0;
		for(int o=0; o<organizationList.size(); o++){
			n = n + organizationList.get(o).numberOfCEOs();
		}
		//System.out.println("NUMBER OF CEOs = " + n);
		return n;
	}

	private int numberOfManagers(){
		int n;
		n = 0;
		for(int o=0; o<organizationList.size(); o++){
			n = n + organizationList.get(o).numberOfManagers();
		}
		//System.out.println("NUMBER OF Managers = " + n);
		return n;
	}

	private int numberOfTeamLeaders(){
		int n;
		n = 0;
		for(int o=0; o<organizationList.size(); o++){
			n = n + organizationList.get(o).numberOfTeamLeaders();
		}
		//System.out.println("NUMBER OF TeamLeaders = " + n);
		return n;
	}

	//This procedure moves the actor to a near by position if the intended x, y is occupied.
	private void placeActor (Actor actor, int x, int y){
		int maxDelta;
		int newX, newY;
		maxDelta = 20;
		newX = x;
		newY = y;
		//System.out.println("##### placeActor 1");
		//if(actor == null){
		//	System.out.println("##### actor is NULL");
		//}
		for(int delta = 0; delta <maxDelta; delta++){
			newX = x;
			newY = y;
			if(!isCellOccupied(newX, newY)){
				delta = maxDelta;
			}
			else{
				newX = x + delta;
				if(!isCellOccupied(newX, newY)){
					delta = maxDelta;
				}
				else{
					newX = x - delta;
					if(!isCellOccupied(newX, newY)){
						delta = maxDelta;
					}
					else{
						newX = x;
						newY = y + delta;
						if(!isCellOccupied(newX, newY)){
							delta = maxDelta;
						}
						else{
							newX = x;
							newY = y - delta; 
							if(!isCellOccupied(newX, newY)){
								delta = maxDelta;
							}
							else{
								newX = x + delta;
								newY = y + delta;
								if(!isCellOccupied(newX, newY)){
									delta = maxDelta;
								}
								else{
									newX = x - delta;
									newY = y - delta;
									if(!isCellOccupied(newX, newY)){
										delta = maxDelta;
									}
									else{
										if(delta == maxDelta -1){
											System.out.println("############################### Cell is still occupied");
										}
									}
								}
							}
						}
					}
				}
			}
		}
		//System.out.println("##### placeActor 2");
		actor.setX(newX);
		actor.setY(newY);
		//System.out.println("##### placeActor 3");
	}

	private void createDistantInteractions(){
		//Create Interaction between Actors of same Organization.
		//All TM in the same organization have a probability to interact.
		Interaction interaction, thisTLInteraction, nextTLInteraction, fromInteraction, toInteraction;
		ArrayList<Interaction> interactions, distantInteractions;
		Actor fromActor, toActor, fromTM, toTM;
		RoleType role;
		//System.out.println("################## ORGANIZATION Create Interactions within the Organization");
		distantInteractions = new ArrayList<Interaction>();
		for(int i=0; i<actorList.size();i++){
			fromActor= (Actor) actorList.get(i);
			for(int j=0; j<actorList.size();j++){
				toActor= (Actor) actorList.get(j);
				if(fromActor != toActor){
					if((fromActor.getBoss() == null) || (toActor.getBoss() == null)){
						//System.out.println("******* Boss is NUL");
					}
					else{
						if(fromActor.getOrganizationName() == toActor.getOrganizationName()){
							if(fromActor.myRole() == RoleType.Manager && toActor.myRole() == RoleType.Manager){
								if(pD.drawChanceFromUniform(simS.getRemoteInteractions())){
									//System.out.println("##### REMOTE Peer Manager to Manager");
									interaction = new RemotePeer(fromActor, toActor, "ON", 0.5);
									fromActor.addToInteraction(interaction);
									toActor.addFromInteraction(interaction);
									distantInteractions.add(interaction);
								}

							}
							else{
								if(fromActor.getBoss() != toActor.getBoss()){
									if(pD.drawChanceFromUniform(simS.getRemoteInteractions())){	
										if(toActor.myRole() == RoleType.Expert){
											interaction = new Consultancy(fromActor, toActor, "ON", 0.5);
										}
										else{
											interaction = new RemotePeer(fromActor, toActor, "ON", 0.5);
										}
										interaction = new RemotePeer(fromActor, toActor, "ON", 0.5);
										fromActor.addToInteraction(interaction);
										toActor.addFromInteraction(interaction);
										distantInteractions.add(interaction);
										//System.out.println("##### REMOTE Interaction from Actor " + fromActor.getName() + " " + fromActor.myRole() + 
										//		" to Actor " + toActor.getName() + " " + toActor.myRole() + " added to actorList");
									}
								}
							}
							//This is a special case to deal with Adaptive Organizations.
							//In this case interactions between actors will be RemoteInteractions.
							if(fromActor.getOrganization().isAdaptive()){
								if(toActor.myRole() == RoleType.Expert){
									//Always interact with an Expert.

									interaction = new Consultancy(fromActor, toActor, "ON", 0.5);
									fromActor.addToInteraction(interaction);
									toActor.addFromInteraction(interaction);
									distantInteractions.add(interaction);
									//System.out.println("##### REMOTE Interaction Adaptive ORGANIZATION from Actor " + fromActor.getName() + " " + fromActor.myRole() + 
									//		" to Actor " + toActor.getName() + " " + toActor.myRole() + " added to actorList");

								}
								else{
									if(pD.drawChanceFromUniform(simS.getRemoteInteractions())){
										interaction = new RemotePeer(fromActor, toActor, "ON", 0.5);
										fromActor.addToInteraction(interaction);
										toActor.addFromInteraction(interaction);
										distantInteractions.add(interaction);
										//System.out.println("##### REMOTE Interaction Adaptive ORGANIZATION from Actor " + fromActor.getName() + " " + fromActor.myRole() + 
										//		" to Actor " + toActor.getName() + " " + toActor.myRole() + " added to actorList");

									}
								}
							}
						}
						else{
							//This is the case or Actors from Different Organizations
							if(fromActor.myRole() == RoleType.Manager && toActor.myRole() == RoleType.Manager){
								if(pD.drawChanceFromUniform(simS.getRemoteInteractions())){
									//System.out.println("##### (2) REMOTE Peer Manager to Manager");
									interaction = new RemotePeer(fromActor, toActor, "ON", 0.5);
									fromActor.addToInteraction(interaction);
									toActor.addFromInteraction(interaction);
									distantInteractions.add(interaction);
								}
							}
							else{
								if(pD.drawChanceFromUniform(simS.getRemoteInteractions())){
									interaction = new RemotePeer(fromActor, toActor, "ON", 0.5);
									fromActor.addToInteraction(interaction);
									toActor.addFromInteraction(interaction);
									distantInteractions.add(interaction);
									//System.out.println("##### REMOTE Interaction from Actor " + fromActor.getName() + " " + fromActor.myRole() + 
									//		" to Actor " + toActor.getName() + " " + toActor.myRole() + " added to actorList");

								}
							}
						}
					}
				}
			}
		}
		interactionList.addAll(distantInteractions);
	}

	private void placeOrganizationsOnSpace(){
		int nOrganizations;
		int nCEOs;
		Organization org;
		coordinates orgCentre;
		double r, teta, tetaIncrement;
		int x, y;
		Actor actor;
		nCEOs = numberOfCEOActors();
		nOrganizations = numberOfOrganizations();
		//if((nCEOs == 1) || (nCEOs == 0)){
		if(nOrganizations == 1){
			r = 0.0;
			tetaIncrement = 2.0 * Math.PI;
		}
		else{
			r = xSpace/4.0;
			tetaIncrement = 2.0 * Math.PI / nOrganizations;
		}
		teta = Math.PI;
		orgCentre = new coordinates();
		//System.out.println("Number of Organizations = " + nOrganizations + " Number of CEO Actors = " + nCEOs);
		for(int o=0; o<organizationList.size(); o++){
			org = organizationList.get(o);
			orgCentre.setPolar(r, teta);
			x = (int)orgCentre.getX() + xSpace/2;
			y = (int)orgCentre.getY() + ySpace/2;
			org.setCentre(x, y);
			actor = org.getCEO();
			if(actor != null){
				placeActor (actor, x,y);
				addActorToSpace(actor);
			}
			teta = teta - tetaIncrement;
		}
	}

	private void clusterOrganization(Organization org){
		String myOrganization;
		coordinates orgCentre, coordManager;
		double r, rManager, teta, tetaIncrement, managersArc;
		int nManagers, nCEOs, nOrganizations;
		int x, y, xCentre, yCentre;
		Actor ceo, actor;
		Interaction interaction;
		ArrayList <Actor> actors;
		ArrayList <Actor> managers;
		ArrayList <Interaction> interactions;
		actors = new ArrayList<Actor>();
		managers = new ArrayList<Actor>();
		interactions = new ArrayList <Interaction>();
		orgCentre = new coordinates();
		coordManager = new coordinates();
		myOrganization = org.name();
		orgCentre = org.getCentre();
		nOrganizations = numberOfOrganizations();
		nCEOs = numberOfCEOActors();
		//if((nCEOs == 1) || (nCEOs == 0)){
		if(nOrganizations == 1){
			r = xSpace/4.0;
			rManager = r * 0.4;
		}
		else{
			r = xSpace/8.0;
			rManager = r * 0.4;
		}
		ceo = org.getCEO();
		actors = org.actorList;
		for(int a=0; a<actors.size(); a++){
			actor = actors.get(a);
			if (actor.myRole() == RoleType.Manager){
				managers.add(actor);
			}
		}

		//Place Managers of this Organization on the Space

		nManagers = managers.size();

		if(nManagers > 0){
			teta = 2.0 * Math.PI;
			tetaIncrement = 2.0 * Math.PI / nManagers;
			for (int m=0; m<nManagers; m++){
				actor = managers.get(m);
				coordManager.setPolar(rManager,  teta);
				x = (int) (orgCentre.getX() + coordManager.getX());
				y = (int) (orgCentre.getY() + coordManager.getY()); 
				placeActor (actor, x,y);
				addActorToSpace(actor);
				teta = teta - tetaIncrement;
				clusterGroup(org, actor);//actor is a Manager.
			}
		}
		else{
			clusterGroup(org, null);//there are no Managers
		}
	}

	private void clusterGroup (Organization org, Actor manager){
		String myOrganization;
		coordinates orgCentre, coordManager, coordLeader;
		double r, rLeader, teta, tetaIncrement, managerTeta;
		double deltaX, deltaY;
		int  nLeaders, nCEOs, nOrganizations;
		int x, y, xManager, yManager, xCentre, yCentre;
		double direction, separation;
		Actor actor;
		Interaction interaction;
		ArrayList <Actor> actors;
		ArrayList <Actor> leaders;
		ArrayList <Interaction> interactions;
		interactions = new ArrayList <Interaction>();
		orgCentre = new coordinates();
		coordManager = new coordinates();
		coordLeader = new coordinates();
		actors = new ArrayList<Actor>();
		leaders = new ArrayList<Actor>();

		//System.out.println("@@@@@ CLUSTER GROUP ");
		myOrganization = org.name();
		orgCentre = org.getCentre();
		xCentre = (int) orgCentre.getX();
		yCentre = (int) orgCentre.getY();
		nOrganizations = numberOfOrganizations();
		nCEOs = numberOfCEOActors();
		//if((nCEOs == 1) || (nCEOs == 0)){
		if(nOrganizations == 1){
			r = xSpace/4.0;
			rLeader = r * 0.4;
		}
		else{
			r = xSpace/8.0;
			rLeader = r * 0.4;
		}

		if(manager != null){
			xManager = manager.getX();
			yManager = manager.getY();
			coordManager.setCartesian(xManager, yManager);
			deltaX = xManager - xCentre;
			deltaY = yManager - yCentre;
			managerTeta = Math.atan(deltaY/deltaX) - Math.PI/2 ;

			//Relocate Leaders to be close to their Manager.
			interactions = manager.ToInteractions();
			//System.out.println("@@@@@ Interactions = " + interactions.size());
			for (int i=0; i<interactions.size(); i++){
				interaction = interactions.get(i);
				actor = (Actor) interaction.getToAgent();
				if (actor.getOrganizationName().equals(myOrganization)){
					if (actor.myRole() == RoleType.TeamLeader){
						leaders.add(actor);
						//System.out.println("@@@@@ " + actor.getOrganization() + " " + actor.myRole() + " ID= " + actor.getName());
					}
					else{
						//System.out.println("Actor " + actor.getName() + "NOT TeamLeader");
					}
				}
				else{
					//System.out.println("NOT from same Organization");
				}
			}
		}
		else{
			xManager = (int) org.getCentre().getX();
			yManager = (int) org.getCentre().getY();
			coordManager.setCartesian(xManager, yManager);
			deltaX = xManager - xCentre;
			deltaY = yManager -yCentre;
			if(deltaX != 0.0){
				managerTeta = Math.atan(deltaY/deltaX) - Math.PI/2 ;
			}
			else{
				managerTeta = Math.PI;
			}
			actors = org.actorList;
			for(int a=0; a<actors.size(); a++){
				actor = actors.get(a);
				if (actor.myRole() == RoleType.TeamLeader){
					leaders.add(actor);
				}
			}
		}
		//Place TeamLeaders of this Organization on the Space;
		nLeaders = leaders.size();
		tetaIncrement = 0.5;;
		//teta = managerTeta - 0.5;
		if ((deltaY > 0.0) & (deltaX > 0.0)){
			managerTeta = managerTeta + Math.PI/4;
		}
		if ((deltaY > 0.0) & (deltaX < 0.0)){
			managerTeta = managerTeta + Math.PI/4;
		}
		if ((deltaY < 0.0) & (deltaX < 0.0)){
			managerTeta = managerTeta - Math.PI/4;
		}
		if ((deltaY < 0.0) & (deltaX > 0.0)){
			managerTeta = managerTeta - 3.0 * Math.PI/4;
		}
		if (deltaX == 0.0){
			managerTeta = managerTeta + 0.0;
		}
		if (deltaY == 0.0){
			if (deltaX > 0.0){
				managerTeta = managerTeta + Math.PI;
			}
			else{
				managerTeta = managerTeta + 0.0;	
			}
		}
		teta = managerTeta;
		direction = -1.0;
		separation = 0.0;
		//tetaIncrement = arc / nLeaders;
		if(nLeaders > 0){
			for (int l=0; l<nLeaders; l++){
				actor = leaders.get(l);
				coordLeader.setPolar(rLeader, teta + direction*separation);
				//x = (int) (coordCEO.getX() + coordLeader.getX());
				//y = (int) (coordCEO.getY() + coordLeader.getY()); 
				x = (int) (xManager + coordLeader.getX());
				y = (int) (yManager + coordLeader.getY()); 
				placeActor (actor, x,y);
				addActorToSpace(actor);
				direction = direction * (-1.0);
				if(direction > 0){
					separation = separation + tetaIncrement;
				}
				//teta = teta + tetaIncrement;
				clusterTeam(org, actor);//actor is a TeamLeader.
			}
		}
		else{
			clusterTeam(org, null);//there are no TeamLeaders.
		}
	}

	private void clusterTeam (Organization org, Actor leader){
		String myOrganization;
		coordinates orgCentre, coordLeader, coordMember;
		double r, rMember, teta, tetaIncrement, leaderTeta;
		double deltaX, deltaY;
		int nOrganizations;
		int  nMembers;
		int x, y, xLeader, yLeader, xCentre, yCentre;
		double direction, separation;
		Actor actor;
		Interaction interaction;
		ArrayList <Actor> actors;
		ArrayList <Actor> members;
		ArrayList <Interaction> interactions;
		interactions = new ArrayList <Interaction>();
		orgCentre = new coordinates();
		coordLeader = new coordinates();
		coordMember = new coordinates();
		actors = new ArrayList<Actor>();
		members = new ArrayList<Actor>();

		//System.out.println("@@@@@ CLUSTER TEAM ");
		myOrganization = org.name();
		orgCentre = org.getCentre();
		xCentre = (int) orgCentre.getX();
		yCentre = (int) orgCentre.getY();

		nOrganizations = numberOfOrganizations();

		//System.out.println("orgCentre x = " + xCentre + " y = " + yCentre);

		if(leader != null){
			xLeader = leader.getX();
			yLeader = leader.getY();
			//System.out.println("leaderCentre x = " + xLeader + " y = " + yLeader);
			coordLeader.setCartesian(xLeader, yLeader);
			deltaX = xLeader - xCentre;
			deltaY = yLeader -yCentre;

			r = xSpace/8.0;
			rMember = r * 0.8;

			//Relocate Leaders to be close to their Manager.
			interactions = leader.ToInteractions();
			//System.out.println("@@@@@ Interactions = " + interactions.size());
			for (int i=0; i<interactions.size(); i++){
				interaction = interactions.get(i);
				actor = (Actor) interaction.getToAgent();
				if (actor.getOrganizationName().equals(myOrganization)){
					members.add(actor);
				}
			}
			//System.out.println("@@@@@ CLUSTER TEAM 1");
		}
		else{
			xLeader = (int) org.getCentre().getX();
			yLeader = (int) org.getCentre().getY();
			coordLeader.setCartesian(xLeader, yLeader);
			if(org.hierarchic()){
				r = xSpace/(2 * nOrganizations);
			}
			else{
				r = xSpace/(4 * nOrganizations);
			}
			rMember = r * 0.5;
			deltaX = xLeader - xCentre;
			actors = org.actorList;
			for(int a=0; a<actors.size(); a++){
				actor = actors.get(a);
				if ((actor.myRole() == RoleType.TeamMember) || (actor.myRole() == RoleType.Expert)){
					members.add(actor);
				}
			}
		}
		deltaY = yLeader -yCentre;
		if(deltaX != 0){
			leaderTeta = Math.atan(deltaY/deltaX) - Math.PI/2 ;
		}
		else{
			leaderTeta = Math.PI;
		}
		//System.out.println("@@@@@ CLUSTER TEAM 2");
		if ((deltaY > 0.0) & (deltaX > 0.0)){
			leaderTeta = leaderTeta + Math.PI/4;
		}
		if ((deltaY > 0.0) & (deltaX < 0.0)){
			leaderTeta = leaderTeta +  Math.PI/2;
		}
		if ((deltaY < 0.0) & (deltaX < 0.0)){
			leaderTeta = leaderTeta - Math.PI/4;
		}
		if (deltaX == 0.0){
			if (deltaY > 0.0){
				leaderTeta = leaderTeta + Math.PI;
			}
			else{
				leaderTeta = leaderTeta + 0.0;	
			}
		}
		if (deltaY == 0.0){
			if (deltaX > 0.0){
				leaderTeta = leaderTeta + Math.PI;
			}
			else{
				leaderTeta = leaderTeta + 0.0;	
			}
		}

		//Place TeamMembers of this Organization on the Space;
		nMembers = members.size();
		tetaIncrement = 0.3;;
		//teta = leaderTeta - 0.6;
		teta = leaderTeta;
		//tetaIncrement = arc / nLeaders;
		direction = -1.0;
		separation = 0.0;
		//System.out.println("@@@@@ CLUSTER TEAM 3");
		for (int l=0; l<nMembers; l++){
			//System.out.println("@@@@@ CLUSTER TEAM 4");
			actor = members.get(l);
			if(actor == null){
				//System.out.println("@@@@@ CLUSTER TEAM 5");
			}
			if ((actor.myRole() == RoleType.TeamMember) || (actor.myRole() == RoleType.Expert)){

				coordMember.setPolar(rMember, teta + direction*separation);
				x = (int) (coordLeader.getX() + coordMember.getX());
				y = (int) (coordLeader.getY() + coordMember.getY()); 
				//System.out.println("@@@@@ CLUSTER TEAM 6");
				placeActor (actor, x,y);
				//System.out.println("@@@@@ CLUSTER TEAM 7");
				addActorToSpace(actor);
				//System.out.println("@@@@@ CLUSTER TEAM 8");
				direction = direction * (-1.0);
				if(direction > 0){
					separation = separation + tetaIncrement;
				}
			}
		}
		//System.out.println("@@@@@ CLUSTER TEAM DONE");
	}

	//This is the NEW procedure to relocate Actors on Space
	private void placeActorsOnSpace(){
		Actor actor;
		Organization org;
		placeOrganizationsOnSpace();
		for(int o=0; o<organizationList.size(); o++){
			org = organizationList.get(o);
			clusterOrganization(org);
		}
		//for (int ag = 0; ag<nactors; ag++){
		//	actor = actorList.get(ag);
		//	if ( actor.myRole() == RoleType.CEO){
		//		clusterOrganization(actor);
		//	}
		//}
	}

	public boolean isCellOccupied(int x, int y){
		//System.out.println("##### isCellOccupied 1");
		//if(actorSpace == null){
		//	System.out.println("##### actorSpace is NULL");
		//}
		//System.out.println("x = " + x + " y = " + y);
		if(actorSpace.getObjectAt(x,y)==null) {
			//System.out.println("isCellOccupied = No");
			return false;
		}
		else {
			//System.out.println("isCellOccupied = Yes");
			return true;
		}
	}

	private void addActorToSpace (Actor actor){
		int x, y;
		x = actor.getX();
		y = actor.getY();
		if (isCellOccupied(x,y)){
			//System.out.println("********* There is more than one Actor on " + x + " , " + y);
		}
		else {
			actorSpace.putObjectAt(x, y, actor);
			//System.out.println("Actor " + actor.getName() + " role = " + actor.myRole() + " added to agentSpace on " + x + " , " + y);
		}

	}

	private void setCustomersAndProviders(){
		Organization thisOrg, prevOrg, nextOrg;
		for(int o=0; o<NumberOfOrganizations(); o++){
			thisOrg = organizationList.get(o);
			prevOrg = null;
			nextOrg = null;
			if(o!=0){
				prevOrg = organizationList.get(o-1);
			}
			if(o<NumberOfOrganizations()-1){
				nextOrg = organizationList.get(o+1);
			}
			thisOrg.setCustomer(prevOrg);
			thisOrg.setProvider(nextOrg);
		}
	}

	public int NumberOfOrganizations(){
		return organizationList.size();
	}

	public ArrayList <Organization> getOrganizationList(){
		//System.out.println("*********$$$$$$$$$$$$$");
		return organizationList;
	}

	public int NumberOfActors(){
		return nactors;
	}

	public ArrayList <Actor> getActorList(){
		return actorList;
	}

	public Object2DGrid getActorSpace(){
		return actorSpace;
	}

	public ArrayList <Interaction> getInteractionList(){
		//if(interactionList == null){
		//	System.out.println("interactionList is NULL ###################");
		//}
		return interactionList;
	}

	public Object2DGrid getInteractionSpace(){
		return interactionSpace;
	}

	public ArrayList <Task> getTaskList(){
		return taskList;
	}

	public ArrayList <Task> getDisplayableTaskList(){
		return displayableTaskList;
	}

	public Object2DGrid getTaskSpace(){
		return taskSpace;
	}

	public double getEffectiveness(){
		return effectiveness;
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

	public double getMotivation(){
		return motivation;
		//return activeMotivation;
	}

	public double getConstructive(){
		return constructive;
		//return activeConstructive;
	}

	public double getPassive(){
		return passive;
		//return activePassive;
	}

	public double getAggressive(){
		//System.out.println("Agressive = " + aggressive);
		return aggressive;
		//return activeAggressive;
	}

	public double getKnowledge(){
		return knowledge;
		//return activeKnowledge;
	}

	public double getRequiredKnowledge(){
		return requiredKnowledge;
	}

	public double getAvailableKnowledge(){
		return availableKnowledge;
	}

	public double getExperience(){
		return experience;
		//return activeExperience;
	}

	public double getEfficiency(){
		return efficiency;
	}



	public void step() {
		Organization org;
		Actor actor;
		int nOrg;
		//Actor actor;
		//int nActors;
		double mot = 0.0;
		double agg = 0.0;
		double con = 0.0;
		double pas = 0.0;
		double exp = 0.0;
		double kno = 0.0;

		activeActors = 0;
		activeMotivation = 0.0;
		activeAggressive = 0.0;
		activeConstructive = 0.0;
		activePassive = 0.0;
		activeExperience = 0.0;
		activeKnowledge = 0.0;

		stepRequiredKnowledge = 0.0;
		stepAvailableKnowledge = 0.0;

		stepSpentEffort = 0.0;
		stepProductiveEffort = 0.0;

		//actorList.clear(); // will update the actorList.
		//interactionList.clear(); // will update the interactionList.
		//System.out.println("SocialConfiguration.step()");
		nOrg = organizationList.size();
		//newTaskList.clear();
		taskList.clear();

		for(int a=0; a<actorList.size(); a++){
			actor = actorList.get(a);
			//actor.clearEffort();
			//if(actor.isActive()){

			stepSpentEffort = stepSpentEffort + (double) actor.getSpentEffort();
			stepProductiveEffort = stepProductiveEffort + actor.getProductiveEffort();

			if(actor.requiredKnowledge() > 0.0){
				activeActors = activeActors +1;
				activeMotivation = activeMotivation + actor.getMotivation();
				activeAggressive = activeAggressive + actor.getAggressive();
				activeConstructive = activeConstructive + actor.getConstructive();
				activePassive = activePassive + actor.getPassive();
				activeExperience = activeExperience + actor.getExperience();
				activeKnowledge = activeKnowledge + actor.getKnowledge();

				stepRequiredKnowledge = stepRequiredKnowledge + actor.requiredKnowledge();
				stepAvailableKnowledge = stepAvailableKnowledge + actor.availableKnowledge();
			}
			if(stepRequiredKnowledge > 0.0){
				requiredKnowledge = stepRequiredKnowledge;
				availableKnowledge = stepAvailableKnowledge;
				//System.out.println("stepRequiredKnowledge > 0");
			}
			//}
		}

		spentEffort = spentEffort + (int) stepSpentEffort;
		productiveEffort = productiveEffort + (int) stepProductiveEffort;

		for(int o=0; o<nOrg; o++){
			org = organizationList.get(o);
			//org.clearEffort();
			org.step();
			taskList.addAll(org.getTasks());
			mot = mot + org.getMotivation();
			agg = agg + org.getAggressive();
			con = con + org.getConstructive();
			pas = pas + org.getPassive();
			exp = exp + org.getExperience();
			kno = kno + org.getKnowledge();
		}

		motivation = mot / nOrg;
		aggressive = agg / nOrg;
		constructive = con / nOrg;
		passive = pas / nOrg;
		experience = exp / nOrg;
		knowledge = kno / nOrg;

		if(activeActors >0){
			activeMotivation = activeMotivation / activeActors;
			activeAggressive = activeAggressive / activeActors;
			activeConstructive = activeConstructive / activeActors;
			activePassive = activePassive/ activeActors;
			activeExperience = activeExperience / activeActors;
			activeKnowledge = activeKnowledge / activeActors;

			requiredKnowledge = requiredKnowledge /activeActors;
			availableKnowledge = availableKnowledge /activeActors;

			//System.out.println("RequiredKnowledge = " + df.format(requiredKnowledge) + " AvailableKnowledge = " + df.format(availableKnowledge));

			if(spentEffort > 0.0){
				efficiency = (double) productiveEffort / (double) spentEffort;
			}
			else{
				efficiency = 0.0;
			}
		}
		displayableTaskList.clear();
		displayableTaskList.addAll(taskList);
	}

}
