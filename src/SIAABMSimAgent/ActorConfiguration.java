package SIAABMSimAgent;

import java.util.StringTokenizer;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.text.*;

import uchicago.src.sim.space.Object2DGrid;

import SIAABMSimAgent.ActorProfile;
import SIAABMSimAgent.ActorProfile.RoleType;
import SIAABMSimAgent.Actor;
import SIAABMSimAgent.CeoActor;
import SIAABMSimAgent.ManagerActor;
import SIAABMSimAgent.TeamLeaderActor;
import SIAABMSimAgent.TeamMemberActor;
import SIAABMSimAgent.ConsultantActor;
import SIAABMSimAgent.GenericActor;
import SIAABMSimConnection.Interaction;
import SIAABMSimConnection.Management;
import SIAABMSimConnection.Leadership;
import SIAABMSimConnection.Peer;
import SIAABMSimConnection.Task;
import SIAABMSimSocial.Organization;

import SIAABMSimUtils.coordinates;
import SIAABMSimUtils.probability;

public class ActorConfiguration {
	
	private int tpr; // tp number of rows.
	private int tpc; // tp number of columns.
	
	private int icr; // icr number of rows in the Interactions Matrix.
	private int icc; // cc number of columns in the Interactions Matrix.

	private String [][]apm; // ap is the Actor Population Matrix as read from the file.
	private ActorProfile [] app; // ap is the Actor Population Profiles.
	
	private int pr; //number of rows in the Probabilities Matrix.
	private int pc; //number of columns in the Probabilities Matrix (Aggressive, Doer, Constructive, Nice, Passive, Neutral).
	private int nStyles; //number of Behaviour Styles with probabilities assigned for each type of Situation.
	private int nSituations; //number of Situations with probabilities assigned for each Behaviour Style;
	private String [][]probabilitiesFile; //probabilities is the Behaviour Probability read from the file.
	private double [][]probabilities;
	
	private int nactors; // Number of Actors in the Team.
	private ArrayList <Actor> actorList;
	private Object2DGrid actorSpace;     //agentSpace is where Actors will be placed on.
	
	private String [][]icm; // icm is the Interaction Configuration Matrix as read from the file.
	//private Profile [] ipp; // ap is the Actor Population Profiles.
	
	private int ninteractions;
	private ArrayList <Interaction> interactionList;
	private Object2DGrid interactionSpace;
	
	private int ntasks;
	private ArrayList <Task> taskList;
	private Object2DGrid taskSpace;
	
	private coordinates actorCoord;
	private int xSpace, ySpace;
	private probability pD;
	
	private DecimalFormat df;
	
	public ActorConfiguration (String actorPopulationFileName, 
			                   String interactionsFileName, int xSize, int ySize) throws Throwable {
		
		
		System.out.println("***** ActorConfiguration");
		xSpace = xSize;
		ySpace = ySize;
		pD = pD.getInstance();
		
		df = df = new DecimalFormat("#.##");
		
		readActorPopulation(actorPopulationFileName);
		readInteractions(interactionsFileName);;
		
		//relocateActorsOnSpace();
		//placeCEOActorsOnSpace();
		placeActorsOnSpace();
		
		taskList = new ArrayList<Task>();
		taskSpace = new Object2DGrid(xSpace,ySpace);

	}
	
	private void readActorPopulation(String fileName) throws Throwable{
		
		//The Team Description File is expected to have one and only one Team Leader.
		//The Team Leader must be the first entry in the file.
		//No checks are made to verify this.
		
		BufferedReader inputStream = null;
		System.out.println(fileName);
		int i = 0;
		int j = 0;
		int x, y;
		int tokenCount = 0;
		//coordinates agentCoord;
		//int agent = 0;
		Organization org;
		Actor actor;
		String l, n;
		
		actorList = new ArrayList<Actor>();
		actorSpace = new Object2DGrid(xSpace,ySpace);
		actorCoord = new coordinates();
		
		org = null;
		
		try {
			inputStream = new BufferedReader (new FileReader (fileName));
			inputStream.mark(2000000);
		    while ((l = inputStream.readLine()) != null){
		    	StringTokenizer st = new StringTokenizer(l);
		    	tokenCount = st.countTokens();
		    	i++;
		    	j = tokenCount;
		    }
		    tpr = i;
		    tpc = j;
		    apm = new String [tpr][tpc];
		    
		    inputStream.reset();
		    i = 0;
		    while ((l = inputStream.readLine()) != null){
		    	//System.out.println("######  " + l);
				StringTokenizer st = new StringTokenizer(l);
				tokenCount = st.countTokens();
				j = 0;
			    while (st.hasMoreTokens()) {
		          n = st.nextToken(); 
		          //x = Double.parseDouble(n);
		          apm [i][j] = n;
		          j++;
		        }
			    i++;
			}
		    System.out.println();
		}
		catch (IOException e){
			System.out.println("Problems reading Actor file " + fileName);
		}
		finally {
			if (inputStream != null){
				inputStream.close();
			}
		}
		
		nactors = tpr;
	    app = new ActorProfile [nactors];
	    String t1, t2, t3, rl, st, sp, ex;
	    double c, p, a, m, v;
	    double exp, know, behav;
	    //Random.createNormal(50, 50);
	    for (int ag = 0; ag < nactors; ag++){
	    	t1 = apm[ag][0];
	    	t2 = apm[ag][1];
	    	t3 = apm[ag][2];
	    	rl = apm[ag][3];
	    	st = apm[ag][4];
	    	sp = apm[ag][5];
	    	ex = apm[ag][6];
            c = Double.parseDouble(apm[ag][7]);
            a = Double.parseDouble(apm[ag][8]);
            p = Double.parseDouble(apm[ag][9]);
            m = Double.parseDouble(apm[ag][10]);
            v = Double.parseDouble(apm[ag][11]);
            //the following instantiation includes Behaviour from the file.
            //app[ag] = new ActorProfile(t1,t2,t3,rl,st,sp,ex,c,a,p,m, v);
            exp = 0.9;
            know = 0.95;
            //behav = Random.normal.nextInt()/100.0;
            behav = pD.getDoubleUniform(0.0, 1.0);
            
            //app[ag] = new ActorProfile(t1,t2,t3,rl,st, sp, exp, know, behav, m);
            app[ag] = new ActorProfile(t1,t2,rl,1.0,1.0,1.0,1.0);
            actorCoord.setPolar(xSpace/2, pD.getDoubleUniform(0.0, 2.0*Math.PI));
            //x = xSpace/2 + (int) actorCoord.getX();
            //y = ySpace/2 + (int) actorCoord.getY();
            //x = Random.uniform.nextIntFromTo(0, xSpace-1);
            x = pD.getIntUniform(0, xSpace-1);
            //y = Random.uniform.nextIntFromTo(0, ySpace-1);
            y = (int) ( (double)(ySpace) * 0.98);
            //x = Random.normal.nextInt();
            //System.out.println("Actor " + t1 + " X = " + x + " Y = " + y);
            
            actor = null;
            
            switch (app[ag].role()){
            case CEO:{
            	actor = new CeoActor(app[ag], org, x, y);
            	break;
            }
            case Manager:{
            	actor = new ManagerActor(app[ag], org, x, y);
            	break;
            }
            case TeamLeader:{
            	actor = new TeamLeaderActor(app[ag], org, x, y);
            	break;
            }
            case TeamMember:{
            	actor = new TeamMemberActor(app[ag], org, x, y);
            	break;
            }
            case Consultant:{
            	actor = new ConsultantActor(app[ag], org, x, y);
            	break;
            }
            case Generic:{
            	actor = new GenericActor(app[ag], org, x, y);
            	break;
            }
            }
            
            if (actor != null){
            	addActorToSpace(actor);
            	actorList.add(actor);
                System.out.println("***** Actor " + actor.getName() + " role = " + actor.myRole() + " added to actorList and to actorSpace on " + x + " , " + y);
            }
            else {
            	System.out.println("**************** Actor is NULL");
            }
	    }
	}
	
	private void readInteractions(String fileName) throws Throwable{
		
		Interaction interaction;
		
		BufferedReader inputStream2 = null;
		System.out.println(fileName);
		int i = 0;
		int j = 0;
		int tc = 0;
		String l, n;
		Actor fromActor, toActor;
		
		interactionList = new ArrayList<Interaction>();
		interactionSpace = new Object2DGrid(xSpace,ySpace);
		
		try {
			inputStream2 = new BufferedReader (new FileReader (fileName));
			inputStream2.mark(2000000);
		    while ((l = inputStream2.readLine()) != null){
		    	//System.out.println("######" + l);
		    	StringTokenizer sti = new StringTokenizer(l);
		    	tc = sti.countTokens();
		    	i++;
		    	j = tc;
		    	//System.out.println("icr = " + i + " tc = " + tc);
		    }
		    icr = i;
		    icc = j;
		    icm = new String [icr][icc];
		    
		    //System.out.println("icr = " + icr + " icc = " + icc);
		    
		    inputStream2.reset();
		    i = 0;
		    while ((l = inputStream2.readLine()) != null){
				StringTokenizer sti = new StringTokenizer(l);
				tc = sti.countTokens();
				j = 0;
			    while (sti.hasMoreTokens()) {
		          n = sti.nextToken(); 
		          //x = Double.parseDouble(n);
		          icm [i][j] = n;
		          j++;
		        }
			    i++;
			}
		    System.out.println();
		}
		catch (IOException e){
			System.out.println("Problems reading Interactions file " + fileName);
		}
		finally {
			if (inputStream2 != null){
				inputStream2.close();
			}
		}
		
		ninteractions = icr;
	    //app = new ActorProfile [nactors];
	    String actor1, actor2, type, state, quality;
	    double q;
	    for (int it = 0; it < ninteractions; it++){
	    	actor1 = icm[it][0];
	    	actor2 = icm[it][1];
	    	type = icm[it][2];
	    	state = icm[it][3];
	    	quality = icm[it][4];
            q = Double.parseDouble(icm[it][4]);
            fromActor = getActor(actor1);
            toActor = getActor(actor2);
            if ((fromActor != null) && (toActor != null)){
            	//set a default Interaction;
            	interaction = null;
            	if(type == "Boss"){
            		interaction = new Management(fromActor, toActor, state, q);
            	}
            	if(type == "Leader"){
            		interaction = new Leadership(fromActor, toActor, state, q);
            	}
            	if(type == "Peer"){
            		interaction = new Peer(fromActor, toActor, "ON", q);
            	}
            	//addInteractionToSpace(interaction);
            	interactionList.add(interaction);
            	fromActor.addToInteraction(interaction);
            	toActor.addFromInteraction(interaction);
            	System.out.println("ACTOR CONFIGURATION Interaction from Actor " + fromActor.getName() + " role = " + fromActor.myRole() + 
            			           " to Actor " + toActor.getName() + " role = " + toActor.myRole() + "added to actorList");
            }
            else {
            	fromActor = null;
                toActor = null;
                interaction = null;
                System.out.println("**************** Interaction is NULL");
            }
	    }
	}
	
	private Actor getActor(String name){
		Actor actor;
		actor = null;
		for (int ag = 0; ag<nactors; ag++){
			actor = actorList.get(ag);
			if (actor.getName().equals(name)){
				ag = nactors +1;
			}
			else {
				actor = null;
			}
		}
		return actor;
	}
	
	private int numberOfCEOActors(){
		int numberOfCEOs;
		numberOfCEOs = 0;
		 for (int ag = 0; ag < nactors; ag++){
			 if (apm[ag][3].equals("CEO")){
				 numberOfCEOs++;
			 }
		 }
		return numberOfCEOs;
	}
	
	//This procedure moves the actor to a near by position if the intended x, y is occupied.
	private void placeActor (Actor actor, int x, int y){
		if (isCellOccupied(x, y)){
        	x = x+2;
        	if (isCellOccupied(x, y)){
        		y = y+2;
        		if (isCellOccupied(x, y)){
        			x = x-2;
        		}
        		if (isCellOccupied(x, y)){
        			y = y-2;
        			if (isCellOccupied(x, y)){
        				System.out.println("#################Cell is still occupied");
            		}
        		}
        	}
        	
        }
		actor.setX(x);
		actor.setY(y);
	}
	
	private void placeCEOActorsOnSpace(){
		int nCEOs;
		coordinates coordCEO;
		double r, teta, tetaIncrement;
		Actor actor;
		nCEOs = numberOfCEOActors();
		r = xSpace/3.2;
		//r = 20.0;
		tetaIncrement = 2.0 * Math.PI / nCEOs;
		teta = Math.PI;
		coordCEO = new coordinates();
		System.out.println("Number of CEO Actors = " + nCEOs);
		for (int ag = 0; ag<nactors; ag++){
			actor = actorList.get(ag);
			if( actor.myRole() == RoleType.CEO){
				coordCEO.setPolar(r, teta);
				//System.out.println("CEO " + actor.getOrganization() + " Polar r = " + coordCEO.getR() + " teta = " + coordCEO.getTeta());
				//System.out.println("CEO " + actor.getOrganization() + " Carte x = " + coordCEO.getX() + "    y = " + coordCEO.getY());
				actorSpace.putObjectAt(actor.getX(), actor.getY(), null);
				actor.setX((int)coordCEO.getX() + xSpace/2);
				actor.setY((int)coordCEO.getY() + ySpace/2);
				addActorToSpace(actor);
				//actorSpace.putObjectAt(actor.getX(), actor.getY(), actor);
				teta = teta - tetaIncrement;
				
			}
		}
	}
	
	private void clusterOrganization(Actor ceo){
		String myOrganization;
		coordinates coordCEO, coordManager;
		double r, rManager, teta, tetaIncrement, managersArc;
		int nManagers;
		int x, y, xCEO, yCEO;
		Actor actor;
		Interaction interaction;
		ArrayList <Actor> managers;
		ArrayList <Interaction> interactions;
		managers = new ArrayList<Actor>();
		interactions = new ArrayList <Interaction>();
		coordCEO = new coordinates();
		coordManager = new coordinates();
		myOrganization = ceo.getOrganizationName();
		xCEO = ceo.getX();
		yCEO = ceo.getY();
		coordCEO.setCartesian(xCEO, yCEO);
		r = xSpace/10.0;
		rManager = r * 0.4;
		
		//Relocate Managers to be close to their CEO.
		interactions = ceo.ToInteractions();
		for (int i=0; i<interactions.size(); i++){
			interaction = interactions.get(i);
			actor = (Actor) interaction.getToAgent();
			if (actor.getOrganizationName().equals(myOrganization)){
				if (actor.myRole() == RoleType.Manager){
					managers.add(actor);
				}
			}
		}
		
		//Place Managers of this Organization on the Space;
		nManagers = managers.size();
		tetaIncrement = 2.0 * Math.PI / nManagers;
		teta = Math.PI;
		if (nManagers <= 2){
			managersArc = teta;
		}
		else{
			managersArc = tetaIncrement;
		}
		for (int m=0; m<nManagers; m++){
			actor = managers.get(m);
			coordManager.setPolar(rManager, teta);
			x = (int) (coordCEO.getX() + coordManager.getX());
			y = (int) (coordCEO.getY() + coordManager.getY()); 
			placeActor (actor, x,y);
			addActorToSpace(actor);
			teta = teta - tetaIncrement;
			clusterGroup(actor, ceo, managersArc);//actor is a Manager.
		}
		
	}
	
	private void clusterGroup (Actor manager, Actor ceo, double arc){
		String myOrganization;
		coordinates coordCEO, coordManager, coordLeader;
		double r, rLeader, teta, tetaIncrement, managerTeta;
		double deltaX, deltaY;
		int  nLeaders;
		int x, y, xManager, yManager, xCEO, yCEO;
		Actor actor;
		Interaction interaction;
		ArrayList <Actor> leaders;
		ArrayList <Interaction> interactions;
		interactions = new ArrayList <Interaction>();
		coordCEO = new coordinates();
		coordManager = new coordinates();
		coordLeader = new coordinates();
		leaders = new ArrayList<Actor>();
		
		myOrganization = manager.getOrganizationName();
		xCEO = ceo.getX();
		yCEO = ceo.getY();
		coordCEO.setCartesian(xCEO, yCEO);
		xManager = manager.getX();
		yManager = manager.getY();
		coordManager.setCartesian(xManager, yManager);
		deltaX = xManager - xCEO;
		deltaY = yManager -yCEO;
		managerTeta = Math.atan(deltaY/deltaX) -  Math.PI/2.0 ;
		//System.out.println(manager.getOrganization() + " " + manager.getRole() + " ID= " 
		//		+ manager.getName() + " managerTeta = " + df.format(managerTeta)+ " " + df.format(coordManager.getTeta()));
		//coordManager.setCartesian(xManager, yManager);
		//managerTeta = coordManager.getTeta();
		r = xSpace/10.0;
		rLeader = r * 1.0;
		
		//Relocate Leaders to be close to their Manager.
		interactions = manager.ToInteractions();
		System.out.println("@@@@@ Interactions = " + interactions.size());
		for (int i=0; i<interactions.size(); i++){
			interaction = interactions.get(i);
			actor = (Actor) interaction.getToAgent();
			if (actor.getOrganizationName().equals(myOrganization)){
				if (actor.myRole() == RoleType.TeamLeader){
					leaders.add(actor);
					//System.out.println("@@@@@ " + actor.getOrganization() + " " + actor.getRole() + " ID= " + actor.getName());
				}
				else{
					//System.out.println("Actor " + actor.getName() + "NOT TeamLeader");
				}
			}
			else{
				//System.out.println("NOT from same Organization");
			}
		}
		//Place TeamLeaders of this Organization on the Space;
		nLeaders = leaders.size();
		tetaIncrement = 0.8;;
		teta = managerTeta - 0.5;
		//tetaIncrement = arc / nLeaders;
		for (int l=0; l<nLeaders; l++){
			actor = leaders.get(l);
			coordLeader.setPolar(rLeader, teta);
			x = (int) (coordCEO.getX() + coordLeader.getX());
			y = (int) (coordCEO.getY() + coordLeader.getY()); 
			placeActor (actor, x,y);
			addActorToSpace(actor);
			teta = teta + tetaIncrement;
			clusterTeam(actor, ceo, arc);//actor is a TeamLeader.
		}
	}
	
	private void clusterTeam (Actor leader, Actor ceo, double arc){
		String myOrganization;
		coordinates coordCEO, coordLeader, coordMember;
		double r, rMember, teta, tetaIncrement, leaderTeta;
		double deltaX, deltaY;
		int  nMembers;
		int x, y, xLeader, yLeader, xCEO, yCEO;
		Actor actor;
		Interaction interaction;
		ArrayList <Actor> members;
		ArrayList <Interaction> interactions;
		interactions = new ArrayList <Interaction>();
		coordCEO = new coordinates();
		coordLeader = new coordinates();
		coordMember = new coordinates();
		members = new ArrayList<Actor>();
		
		myOrganization = leader.getOrganizationName();
		xCEO = ceo.getX();
		yCEO = ceo.getY();
		coordCEO.setCartesian(xCEO, yCEO);
		xLeader = leader.getX();
		yLeader = leader.getY();
		coordLeader.setCartesian(xLeader, yLeader);
		deltaX = xLeader - xCEO;
		deltaY = yLeader -yCEO;
		if (deltaY > 0.0){
			deltaY = 0.0 - deltaY;
		}
		leaderTeta = Math.atan(deltaY/deltaX) + Math.PI;
		//System.out.println(leader.getOrganization() + " " + leader.getRole() + " ID= " 
		//		+ leader.getName() + " managerTeta = " + df.format(leaderTeta) + " " + df.format(coordLeader.getTeta()));
		//coordManager.setCartesian(xManager, yManager);
		//managerTeta = coordManager.getTeta();
		r = xSpace/10.0;
		rMember = r * 0.8;
		
		//Relocate Leaders to be close to their Manager.
		interactions = leader.ToInteractions();
		System.out.println("@@@@@ Interactions = " + interactions.size());
		for (int i=0; i<interactions.size(); i++){
			interaction = interactions.get(i);
			actor = (Actor) interaction.getToAgent();
			if (actor.getOrganizationName().equals(myOrganization)){
					members.add(actor);
			}
		}
		//Place TeamMembers of this Organization on the Space;
		nMembers = members.size();
		tetaIncrement = 0.3;;
		teta = leaderTeta - 0.6;
		//tetaIncrement = arc / nLeaders;
		for (int l=0; l<nMembers; l++){
			actor = members.get(l);
			coordMember.setPolar(rMember, teta);
			x = (int) (coordLeader.getX() + coordMember.getX());
			y = (int) (coordLeader.getY() + coordMember.getY()); 
			placeActor (actor, x,y);
			addActorToSpace(actor);
			teta = teta + tetaIncrement;
		}
	}
	
	//This is the NEW procedure to relocate Actors on Space
	private void placeActorsOnSpace(){
		Actor actor;
		placeCEOActorsOnSpace();
		for (int ag = 0; ag<nactors; ag++){
			actor = actorList.get(ag);
			if ( actor.myRole() == RoleType.CEO){
				clusterOrganization(actor);
			}
		}
	}
	
	//This is the OLD procedure to relocate Actors on Space
	private void relocateActorsOnSpace(){
		coordinates coordCEO, coordActor;
		double r, teta;
		int x, y;
		String organization;
		Actor actor;
		placeCEOActorsOnSpace();
		//r = xSpace/4.0;
		coordCEO = new coordinates();
		coordActor = new coordinates();
		for (int ag = 0; ag<nactors; ag++){
			actor = actorList.get(ag);
			if (actor.myRole().equals(RoleType.CEO)){
				coordCEO.setCartesian(actor.getX(), actor.getY());
				organization = actor.getOrganizationName();
				//System.out.println("CEO " + organization + " X = " + coordCEO.getX() + " Y = " + coordCEO.getY());
			}
			else{
				r = xSpace/8.0;
				if (actor.myRole().equals(RoleType.Manager)){
					r = r*0.3;
				}
				if (actor.myRole().equals(RoleType.TeamLeader)){
					r = r*0.6;
				}
				coordActor.setPolar(r, pD.getDoubleUniform(0.0, 2.0*Math.PI));
				actorSpace.putObjectAt(actor.getX(), actor.getY(), null);
				//System.out.println("Agent X = " + coordAgent.getX() + " Agent Y = " + coordAgent.getY());
				x = (int) (coordCEO.getX() + coordActor.getX());
				y = (int) (coordCEO.getY() + coordActor.getY()); 
				placeActor (actor, x,y);
				addActorToSpace(actor);
			}
		}
		for (int ag = 0; ag<nactors; ag++){
			for (int agb = 0; agb<nactors; agb++){
				if (ag != agb){
					if (actorList.get(ag).getX() == actorList.get(agb).getX()){
						if (actorList.get(ag).getY() == actorList.get(agb).getY()){
							System.out.println("************ Actor " + actorList.get(ag).getName() + 
									" and Actor " + actorList.get(agb).getName() + " are in the same cell");
							}
					}
				}
			}
		}
	}
	
	public int NumberOfActors(){
		return nactors;
	}
	
	public ActorProfile getTeamMember(int teamMember){
		return app[teamMember];
	}
	
	public ActorProfile getTeamLeader(){
		return app[0];
	}
	
	public ArrayList <Actor> getActorList(){
		return actorList;
	}
	
	public Object2DGrid getActorSpace(){
		return actorSpace;
	}
	
	public ArrayList <Interaction> getInteractionList(){
		return interactionList;
	}
	
	public Object2DGrid getInteractionSpace(){
		return interactionSpace;
	}
	
	public ArrayList <Task> getTaskList(){
		return taskList;
	}
	
	public Object2DGrid getTaskSpace(){
		return taskSpace;
	}
	
	public boolean isCellOccupied(int x, int y){
		if(actorSpace.getObjectAt(x,y)==null) {
			//System.out.println("isCellOccupied = No");
			return false;
		}
		else {
			//System.out.println("isCellOccupied = Yes");
			return true;
		}
	}

	public void addActorToSpace (Actor actor){
		int x, y;
		x = actor.getX();
		y = actor.getY();
		if (isCellOccupied(x,y)){
			System.out.println("********* There is more than one Actor on " + x + " , " + y);
		}
		else {
			actorSpace.putObjectAt(x, y, actor);
			System.out.println("Actor " + actor.getName() + " role = " + actor.myRole() + " added to agentSpace on " + x + " , " + y);
		}
		
	}
	
	public void PrintTeamProfile(){
		System.out.println();
		System.out.println("Number of Actors in the Population = " + tpr);
	   	for (int i=0; i<tpr; i++){
	    	for (int j=0; j< tpc; j++){
	    		System.out.print(apm[i][j]);
	    		System.out.print("\t");
	    	} 
	    	System.out.println(" ");
		}
	}
	
	public void PrintSocialProfile(){
		System.out.println();
		System.out.println("Number of Interactions = " + icr);
	   	for (int i=0; i<icr; i++){
	    	for (int j=0; j< icc; j++){
	    		//System.out.print(icm[i][j]);
	    		//System.out.print("\t");
	    	} 
	    	//System.out.println(" ");
		}
	}
	
	public void step() {
		int nActors;
		nActors = actorList.size();
		for (int a=0; a<nActors; a++){
			actorList.get(a).step();
		}
	}

}
