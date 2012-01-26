package ex02.blas;

import checkers.javari.quals.ReadOnly;

// Vector class for vectors having a position, direction and magnitude properties
public class Vector3D {
	double[] position;
	double[] direction;	
	double magnitude;
	
	public Vector3D() {
		
	}
	
	// Constructor
	public Vector3D(
			double /*@ReadOnly*/ [] position, 
			double /*@ReadOnly*/ [] direction,
			double magnitude) {
		assert position.length == 3;
		assert direction.length == 3;
		this.position = position.clone();
		this.direction = direction.clone();
		this.magnitude = magnitude;
	}
	
	// Returns the norm of the difference between this vector's position point and another point
	public double normPointDiff(double[] p2) {
		double[] p1 = this.position;		
		
		return Math.sqrt(MathUtils.sqrDiff(p1[0], p2[0]) + MathUtils.sqrDiff(p1[1], p2[1]) + MathUtils.sqrDiff(p1[2], p2[2]));
	}
		
	// Normalizes the vector
	public void normalize() {
		double norm  = MathUtils.norm(direction);
		
		direction[0] = direction[0] / norm;
		direction[1] = direction[1] / norm;
		direction[2] = direction[2] / norm;
		
		magnitude = 1;	
	}
	
	// Returns the dot product of the current vector's direction with the other vector's direction
	public double dotProduct(Vector3D otherVec) {
		return (this.direction[0] * otherVec.direction[0] + 
				this.direction[1] * otherVec.direction[1] +
				this.direction[2] * otherVec.direction[2]);
	}
	
		
	// Reflects a vector around a normal. assumes the normal vector is normalized
	public void reflectAround(double[] normal) {
		if (magnitude != 1) normalize();
		
		double dotProduct = MathUtils.dotProduct(direction, normal);
		
		direction[0] = -direction[0] + 2 * normal[0] * dotProduct;
		direction[1] = -direction[1] + 2 * normal[1] * dotProduct;
		direction[2] = -direction[2] + 2 * normal[2] * dotProduct;
	}
	
	
	// Returns the end of the vector as a point in 3D space 
	public double[] getEndPoint() {
		double[] endPoint = { position[0] + magnitude * direction[0], 
							 position[1] + magnitude * direction[1], 
							 position[2] + magnitude * direction[2] };
		
		return endPoint;
	}
	
	public double[] getPosition() {
		return position;
	}

	public void setPosition(double[] position) {
		this.position = position;
	}

	public double[] getDirection() {
		return direction;
	}

	public void setDirection(double[] direction) {
		this.direction = direction;
	}

	public double getMagnitude() {
		return magnitude;
	}

	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}		
}
