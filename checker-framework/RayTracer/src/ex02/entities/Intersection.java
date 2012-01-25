package ex02.entities;

import ex02.entities.primitives.Primitive;

// Helper class representing an intersection
public class Intersection {
	private Primitive primitive;
	private double distance;
	
	public Intersection(double distance, Primitive primitive) {
		this.primitive = primitive;
		this.distance = distance;
	}

	public Primitive getPrimitive() {
		return primitive;
	}

	public void setPrimitive(Primitive primitive) {
		this.primitive = primitive;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}	
}
