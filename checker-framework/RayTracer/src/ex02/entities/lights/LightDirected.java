package ex02.entities.lights;

import java.util.List;

import ex02.Parser;
import ex02.Parser.ParseException;
import ex02.blas.MathUtils;
import ex02.entities.IEntity;

public class LightDirected extends Light {
	public double[] position;
	public double[] direction;		
	public double[] oppositeDirection;
	
	public LightDirected() {
		super();				
	}
	
	public void setParameter(String name, String[] args) throws Exception {
		if (parseParameter(name, args)) return; // parse common parameters using the superclass
		
		if ("direction".equals(name)) {
			direction = Parser.parseVector(args);
			MathUtils.normalize(direction);
			
			oppositeDirection = MathUtils.oppositeVector(direction);
		}
	}
	
	public double[] getAmountOfLight(double[] point) {					
		return color; // constant light, regardless of distance to target
	}
	
	@Override	
	public double[] getPosition() {		 		
		return position;
	}

	public void postInit(List<IEntity> entities) throws ParseException {
		position = new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };		
	}

	@Override
	public double[] getVectorToLight(double[] pointOfIntersection) throws Exception {
		return oppositeDirection;
	}

}
