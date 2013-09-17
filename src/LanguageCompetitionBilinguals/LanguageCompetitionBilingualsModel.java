package LanguageCompetitionBilinguals;

import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import LanguageCompetitionBilinguals.Agent;

public class LanguageCompetitionBilingualsModel implements ContextBuilder<Object>{

	public Context<Object> build(Context<Object> context) {
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		int numAgents = (Integer)p.getValue("initialNumAgents");
		int height = (Integer)p.getValue("worldHeight");
		int width = (Integer)p.getValue("worldWidth");
		int percentX = (Integer)p.getValue("percentX");
		int percentY = (Integer)p.getValue("percentY");
		
		// Create a new 2D torroidal, single occupancy grid on which the agents will live.
		GridFactoryFinder.createGridFactory(null).createGrid("Grid", context, 
				new GridBuilderParameters<Object>(new WrapAroundBorders(), 
						new RandomGridAdder<Object>(), false, width, height));
		
		// Create the initial agents and add to the context.
		// percentage distribution
		if ((percentX > 0 && percentY > 0) && (percentX < 100 && percentY < 100)) {
			int[] agentGroups = new int[3];
			agentGroups[0] = (numAgents * percentX) / 100;
			agentGroups[1] = (numAgents * percentY) / 100;
			agentGroups[2] = numAgents - agentGroups[0] - agentGroups[1];
			// System.out.println("X:" + agentGroups[0]);
			// System.out.println("Y:" + agentGroups[1]);
			// System.out.println("XY:" + agentGroups[2]);

			for (int k = 0; k < agentGroups.length; k++){
				for(int i=0; i < agentGroups[k]; i++){
					System.out.println("k:" + k);
					System.out.println("X:" + agentGroups[k]);

					int agentType = k + 1;
					Agent agent = new Agent("Agent-"+i, agentType);
					context.add(agent);
				}
			}
		}
		// random distribution
		else {
			for (int i = 0; i < numAgents; i++){
				Agent agent = new Agent("Agent-"+i, 0);
				context.add(agent);
			}
		}
		
		return context;
	}
	
}

