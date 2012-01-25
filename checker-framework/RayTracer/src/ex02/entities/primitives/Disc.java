package ex02.entities.primitives;

import java.util.List;

import ex02.Parser;
import ex02.Parser.ParseException;
import ex02.blas.MathUtils;
import ex02.entities.IEntity;
import ex02.entities.Ray;

public class Disc extends Primitive {

	private double[] center = null;
	private double[] normal = null;
	private double radius;
	private double[] intersectionPoint = null;
	private double d;
	private double[] referenceVector = new double[3];
	double[] pivotVector;	
	
	/**
	 * First verifies that the given ray intersects with the 3D plane containing the disc 
	 * and if so, calculates the distance to the intersection point
	 */
	@Override
	public double intersect(Ray ray) {
		
		double distance = intersectWithPlane(ray);
		
		if(distance != Double.POSITIVE_INFINITY  &&  distance > 0)
		{
			return intersectWithinRadius(ray, distance);			
		}
		
		// TODO: check end cases when ray is exactly on the disc's plane \ even inside the disc
		return Double.POSITIVE_INFINITY;
	}
	
	

	/**
	 * Check if the given ray intersects with the 3D plane containing the disc and returns 
	 * the intersection distance.
	 * @param ray
	 * @return
	 */
	private double intersectWithPlane(Ray ray) {
		
		// raySouce is called p0 in the lecture notes, it was rename to avoid conflicting names
		double[] raySource = ray.getPosition();
		double[] V = ray.getDirection();
		double distance = Double.POSITIVE_INFINITY;				

		if(MathUtils.dotProduct(V, normal) != 0)
		{
			distance = (-(MathUtils.dotProduct(raySource, normal) + d)) / MathUtils.dotProduct(V, normal);
		}
		
		// TODO: deal with the case that the Disc is exactly parallel to the direction of sight
		if(distance <= 0)
			return Double.POSITIVE_INFINITY;
		return distance;
	}

	
	
	/**
	 * 
	 * 
	 * @param ray
	 * @param distance
	 * @return
	 */
	private double intersectWithinRadius(Ray ray, double distance) {
		
		// Get the intersection point with the rectangle's plane
		ray.setMagnitude(distance);
		intersectionPoint  = ray.getEndPoint();
		ray.setMagnitude(1);
		
		// Caclulate the distance between from the intersection point on the plane to the center of the disc
		double distanceFromCenter = MathUtils.norm( MathUtils.calcPointsDiff(center, intersectionPoint) );

		
		// If this distance in less than the radius length, we've intersected the disc
		if (distanceFromCenter <= radius) {
			return distance;
		}

		return Double.POSITIVE_INFINITY;
	}


	
	public void setParameter(String name, String[] args) throws Exception {
		if (surface.parseParameter(name, args)) return;
		if ("center".equals(name)) center = Parser.parseVector(args);						
		if ("normal".equals(name)) {
			normal = Parser.parseVector(args);
			MathUtils.normalize(normal);
		}
		if ("radius".equals(name)) radius = Double.parseDouble(args[0]);						
	}

	@Override
	public double[] getNormal(double[] point) {
		return normal;
	}

	public void postInit(List<IEntity> entities) throws ParseException {
		super.postInit(entities);
		
		// normalize the normal vector
		MathUtils.normalize(normal);
		
		// Find d (the plane coefficient) by substituting x,y,z with a point which is on the plane. 
		// for example the center of the disc;
		d = -(MathUtils.dotProduct(normal, center));
		
		// Use a reference vector to get the pivot vector on the disc's plane which we will work with.
		// Also normalize the vectors.
		initializeReferenceVector();
		pivotVector = MathUtils.crossProduct(normal, referenceVector);
		
		MathUtils.normalize(pivotVector);			
	}
	
	
	private void initializeReferenceVector() {
		
		// Choose an arbitrary vector as a reference vector - say the x basis vector
		referenceVector[0] = 1F;
		referenceVector[1] = 0F;
		referenceVector[2] = 0F;
		
		// Check if the reference vector is linearly dependent with the direction vector
		if(normal[1] == 0 && normal[2] == 0)
		{
			// Change the reference vector to something else - say the y basis vector
			referenceVector[0] = 0F;
			referenceVector[1] = 1F;
			referenceVector[2] = 0F;
		}
	}


	@Override
	public double[] getTextureCoords(double[] point) {		
		
		// Get the vector from the center to the intersection point
		double[] centerToPoint = MathUtils.calcPointsDiff(center, point);						
		
		// parameterize u:
		double u = MathUtils.norm(centerToPoint) / radius;		
		
		MathUtils.normalize(centerToPoint);
		
		// parameterize v:
		double q = MathUtils.dotProduct(pivotVector, centerToPoint);
		if (Math.abs(q) > 1) q = 1 * Math.signum(q);
		
		double v = Math.acos(q);		

		//  Check which half of the circle we're at.  If we're in the second, v will be negative, make it positive
		if(MathUtils.dotProduct(MathUtils.crossProduct(pivotVector, centerToPoint), normal) < 0)
		{
			v = (2 * Math.PI) - v;
		}
		v /= (2 * Math.PI);					
				
		return new double[] { u, v };
	}

}
