package LanguageCompetitionBilinguals;

import java.awt.Color;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

public class AgentStyle2D extends DefaultStyleOGL2D {

	@Override
	public Color getColor(Object o) {
		Agent agent = (Agent)o;
		if (agent.getType() == 1)	
			return Color.RED;
		else if ((agent.getType() == 2))
			return Color.BLUE;
		else if ((agent.getType() == 3))
			return Color.GREEN;
		return null;
	}
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    if (spatial == null) {
	      //spatial = shapeFactory.createCircle(4, 16);
	      spatial = shapeFactory.createRectangle(8, 8);

	    }
	    return spatial;
	  }
}
