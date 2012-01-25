package ex02.entities.lights;

import ex02.Parser;
import ex02.entities.IEntity;

public abstract class Light implements IEntity {	
	double[] position;
	double[] color = {1, 1, 1};
	
	public abstract double[] getAmountOfLight(double[] point);
	
	public abstract double[] getVectorToLight(double[] pointOfIntersection) throws Exception;
	
	public boolean parseParameter(String name, String[] args) throws Exception {
		boolean parsed = false;
				
		if ("color".equals(name)) { color = Parser.parseVector(args); parsed = true; }		
		
		return parsed;		
	}		
	
	public double[] getPosition() {
		return position;
	}	
	
	public void setPosition(double[] position) {
		this.position = position;
	} 		
	
	public double[] getColor() {
		return color;
	}
	
	public void setColor(double[] color) {
		this.color = color;
	}
}
