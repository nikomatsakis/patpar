package ex02.entities.lights;

import java.util.List;

import ex02.Parser;
import ex02.Parser.ParseException;
import ex02.blas.MathUtils;
import ex02.entities.IEntity;

// Note: LightArea instances are only alive during parse-time. As soon as a LightArea has been fully parsed,
// it replaces itself with a grid of point-lights and ceases to exist.
public class LightArea extends Light {
	double[] p0, p1, p2;	
	double[] attenuation = {1, 0, 0};
	double[] color = {1, 1, 1};
	int gridWidth = 1;
	
	public void setParameter(String name, String[] args) throws Exception {
		if ("p0".equals(name)) p0 = Parser.parseVector(args);	
		if ("p1".equals(name)) p1 = Parser.parseVector(args);
		if ("p2".equals(name)) p2 = Parser.parseVector(args);
		if ("grid-width".equals(name)) gridWidth = Integer.parseInt(args[0]);		
		if ("attenuation".equals(name)) attenuation = Parser.parseVector(args);
		if ("color".equals(name)) color = Parser.parseVector(args);		
	}
	
	// This method will be never called (see class notes).
	public double[] getAmountOfLight(double[] point) {
		return null;
	}

	public void postInit(List<IEntity> entities) throws ParseException {
		// Replace the LightArea with a grid of point-light objects.
		double[] effectiveAttenuation = MathUtils.multiplyScalar(attenuation, MathUtils.sqr(gridWidth));
		
		double[] p1Offset = MathUtils.calcPointsDiff(p0, p1);
		double[] p2Offset = MathUtils.calcPointsDiff(p0, p2);
		
		MathUtils.multiplyVectorByScalar(p1Offset, 1F / gridWidth);
		MathUtils.multiplyVectorByScalar(p2Offset, 1F / gridWidth);				
		
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridWidth; j++) {
				
				double[] pos = { p0[0] + i * p1Offset[0] + j * p2Offset[0],
								p0[1] + i * p1Offset[1] + j * p2Offset[1],
								p0[2] + i * p1Offset[2] + j * p2Offset[2] };
								
				LightPoint lightPoint = new LightPoint(pos, effectiveAttenuation, color);
				entities.add(lightPoint);								
			}
		}
				
		
		// Look for our instance in the list and remove it
		for (int i = 0; i < entities.size(); i++) {
			if (entities.get(i) == this) {
				entities.remove(i);
			}
		}
		
	}

	@Override
	public double[] getVectorToLight(double[] pointOfIntersection) throws Exception {
		return null;
	}
	
}
