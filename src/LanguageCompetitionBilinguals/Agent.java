package LanguageCompetitionBilinguals;

import java.util.ArrayList;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.MooreQuery;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import LanguageCompetitionBilinguals.Transmission;

public class Agent {
	private int numberOfAgentTypes = 3;
	private int type;
	private int oldType;
	private String id;
	private int speakingX;
	private int speakingY;
	private double densityX;
	private double densityY;
	private int neighborCount;
	public int speakersX;
	public int speakersY;
	public int speakersXY;

	public Agent(String id, int agentType) {
		if (agentType == 0) {
			this.id = id;
			// The agent is randomly a type from 1 to numberOfAgentTypes
			// Language mapping goes as: Language X = 1, Language Y = 2, Bilinguals speaking XY = 3
			// TODO: implement ratio distribution of types with random positioning
			this.type = RandomHelper.nextIntFromTo(1, numberOfAgentTypes);
		}
		else {
			this.id = id;
			this.type = agentType;
		}
	}
	
	// Schedule the step method for agents.
	@ScheduledMethod(start=0, interval=1)
	public void step() {	
		// initialize group variables
		speakingX = 0;      // number of neighbors speaking X
		speakingY = 0;		// number of neighbors speaking Y
		densityX = 0;
		densityY = 0;
		neighborCount = 0;	// number of neighbors
		
		// initialize transmission probabilities variables
		// common to vertical and horizontal models
		double pXtoX = 0;
		double pYtoY = 0;
		double pXYtoY = 0;  
		// vertical model specific
		double pXYtoX = 0;
		double pXYtoXY = 0;
		// horizontal model specific
		double pXtoXY = 0; 
		double pYtoXY = 0;
		
		// an array to store the transmission objects
		// each transmission has fields: targetLanguage and probability 
		ArrayList<Transmission> transmissions = new ArrayList<Transmission>();
		
		// get model parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
		double statusX = (Double)p.getValue("statusX");
		double statusY = 1 - statusX;
		double volatility = (Double)p.getValue("volatility");
		double cXYtoX = (Double)p.getValue("cXYtoX");
		double cXYtoY = (Double)p.getValue("cXYtoY");
		double cXtoXY = (Double)p.getValue("cXtoXY");
		double cYtoXY = (Double)p.getValue("cYtoXY");
		int neighborhoodType = (Integer)p.getValue("neighborhoodType");
		int totalAgents = (Integer)p.getValue("initialNumAgents");
		
		// compute probability to adopt Vertical transmission model based on global mortality rate 8.37/1000
		// true adopts vertical model, false adopts horizontal model
		boolean verticalModel = (RandomHelper.nextIntFromTo(0, 118) == 0);
		
		// initialize reporting variables
		speakersX = this.getSpeakersX();
		speakersY = this.getSpeakersY();
		speakersXY = this.getSpeakersXY();
		
		// Moore neighborhood
		if (neighborhoodType == 1) {
			// Get the context in which the agent is residing
			Context<Agent> context = (Context)ContextUtils.getContext(this);
	
			// Get the grid from the context
			Grid<Agent> grid = (Grid)context.getProjection("Grid");
	
			// Query Moore neighbors in grid
			// TODO: document Von Neumann vs Moore neighborhoods effects on model
			MooreQuery<Agent> query = new MooreQuery<Agent>(grid, this);
				
			// Check Moore neighbors and sum speaker types
			for (Agent agent : query.query()){
				if (agent.getType() == 1) {
					speakingX++;
				}
				if (agent.getType() == 2) {
					speakingY++;
				}
				neighborCount++;
			}
			
			// compute local densities
			if (neighborCount > 0) {
				densityX = (double) speakingX / neighborCount;
				densityY = (double) speakingY / neighborCount;
			}

		}
		
		// total population based neighborhood, similar to fully connected network
		if (neighborhoodType == 2)  {
			densityX = (double) speakersX / totalAgents;
			densityY = (double) speakersY / totalAgents;
		}


		/* Transmission probabilities for VModel:
		* pXtoX
		* pYtoY
		* pXYtoX
		* pXYtoY 
		* pXYtoXY
		* Transmission probabilities for HModel
		* pXtoX = 0;
		* pXtoXY = 0;
		* pYtoY = 0;
		* pYtoXY = 0;
		* pXYtoXY = 1;
		* compute possible transmissions for each agent type based on spoken language and transmission model
		* 
		*/
		double xPowA = Math.pow(densityX, volatility);
		double yPowA = Math.pow(densityY, volatility);
		
		switch (this.getType()) {
			// agent speaks X
			case 1: 
				if (verticalModel) {
					pXtoX = 1;
					transmissions.add(new Transmission(1, pXtoX));
				}
				else {
					pXtoXY = cXtoXY * statusY * yPowA;
					pXtoX = 1 - pXtoXY;
					transmissions.add(new Transmission(3, pXtoXY));
					transmissions.add(new Transmission(1, pXtoX));
				}
				break;
			// agent speaks Y
			case 2:
				if (verticalModel) {
					pYtoY = 1;
					transmissions.add(new Transmission(2, pYtoY));
				}
				else {					
					pYtoXY = cYtoXY * statusX * xPowA;
					pYtoY = 1 - pYtoXY;					
					transmissions.add(new Transmission(3, pYtoXY));
					transmissions.add(new Transmission(2, pYtoY));
				}
				break;
			// agent speaks XY
			case 3:
				if (verticalModel) {
					pXYtoX = cXYtoX * statusX * xPowA;
					pXYtoY = cXYtoY * statusY * yPowA;
					pXYtoXY = 1 - pXYtoX - pXYtoY;
					transmissions.add(new Transmission(1, pXYtoX));
					transmissions.add(new Transmission(2, pXYtoY));
					transmissions.add(new Transmission(3, pXYtoXY));
				}
				else {
					pXYtoXY = 1;
					transmissions.add(new Transmission(3, pXYtoXY));
				}
				break;
		}
		
		
		// before operating transmission archive existing type for debugging
		this.setOldType(this.getType());
		
		// get the transmission to be operated based on roulette wheel selection if needed
		if (transmissions.size() == 1) {
			// execute transmission directly, there is no stream to weight
			this.setType(transmissions.get(0).getTargetLanguage());
		}
		else {
			// extract transmission via roulette procedure
			int candidateLanguage = Agent.selectRouletteWheel(transmissions);
			if (candidateLanguage != 99) {
				this.setType(candidateLanguage);
			}
		}
	}
	
	static int selectRandomWeightedArrayList(ArrayList<Transmission> transmissions) {
		// selectedLanguage is initially set to an non-operation/skip value
	    int selectedLanguage = 99;
	    double total = transmissions.get(0).getProbability();
	    for (int i = 1; i < transmissions.size(); i++) {
	        total += transmissions.get(i).getProbability();            
	        if (RandomHelper.nextDouble() <= (transmissions.get(i).getProbability() / total)) {
	        	selectedLanguage = transmissions.get(i).getTargetLanguage();
	    	    return selectedLanguage;        
	        }
	    }
	    return selectedLanguage;        
	}
	
	static int selectRouletteWheel(ArrayList<Transmission> transmissions) {
	    int selectedLanguage = 99;
	    double roulette = RandomHelper.nextDoubleFromTo(0, 1);
		//System.out.println("Roulette: " + roulette);
	    double currentProbability = 0;
	    for (int i = 0; i < transmissions.size(); i++) {
	        currentProbability += transmissions.get(i).getProbability();            
	        if (roulette <= currentProbability) {
	        	selectedLanguage = transmissions.get(i).getTargetLanguage();
	    	    return selectedLanguage;        
	        }
	    }
	    return selectedLanguage;        
	}
	
	private int countSpeakers(int type) {
		@SuppressWarnings("unchecked")
		final Iterable<Agent> agents = RunState.getInstance().getMasterContext()
				.getObjects(Agent.class);
		int speakers = 0;
			for (final Agent agent : agents) {
				if (agent.getType() == type) {
					speakers++;
				}
			}
		return speakers;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public String getId() {
		return id;
	}
	
	public int getSpeakersX() {
		int speakersX = this.countSpeakers(1);
		return speakersX;
	}
	
	public int getSpeakersY() {
		int speakersX = this.countSpeakers(2);
		return speakersX;
	}
	
	public int getSpeakersXY() {
		int speakersX = this.countSpeakers(3);
		return speakersX;
	}

	public int getOldType() {
		return oldType;
	}

	public void setOldType(int oldType) {
		this.oldType = oldType;
	}
	
}
