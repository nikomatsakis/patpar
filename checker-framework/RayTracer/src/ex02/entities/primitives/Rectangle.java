package ex02.entities.primitives;

import java.util.List;

import ex02.Parser;
import ex02.Parser.ParseException;
import ex02.blas.MathUtils;
import ex02.entities.IEntity;
import ex02.entities.Ray;

public class Rectangle extends Primitive {
	
	private double[] p0, p1, p2, p3;
	private double[] normal = null;
	private double[] intersectionPoint = null;
	private double d;
	private double[] AB, AC;
	private double ABdotAB, ACdotAC;
	private double ABnorm;
	private double ACnorm;
	

	/**
	 * Default constructor.
	 */
	public Rectangle() {
	}
	
	/**
	 * Constructor.  Used only for defining rectangular components of a box. 
	 * @param p0
	 * @param p1
	 * @param p2
	 * @throws ParseException
	 */
	public Rectangle(double[] p0, double[] p1, double[] p2) throws ParseException {
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
		postInit(null);
	}


	/**
	 * First verifies that the given ray intersects with the 3D plane containing the rectangle 
	 * and if so, calculates the distance to the intersection point
	 */
	@Override
	public double intersect(Ray ray) {
		
		double distance = intersectWithPlane(ray);
		
		if(distance != Double.POSITIVE_INFINITY  &&  distance != Double.NEGATIVE_INFINITY)
		{
			return intersectBarycentric(ray, distance);			
		}
		
		// TODO: check end cases when ray is exactly on the disc's plane \ even inside the disc
		return Double.POSITIVE_INFINITY;
	}
	

	/**
	 * Check if the given ray intersects with the 3D plane containing the rectangle and returns 
	 * the intersection distance.
	 * @param ray
	 * @return
	 */
	private double intersectWithPlane(Ray ray) {
		
		// raySouce is called p0 in the lecture notes, it was rename to avoid conflicting names
		double[] raySource = ray.getPosition();
		double[] V = ray.getDirection();
		double distance = 0;

		if(MathUtils.dotProduct(V, normal) != 0)
		{
			// TODO: can optimize the numerator - should be preprocessed because it always calculates the same value 
			distance = (-(MathUtils.dotProduct(raySource, normal) + d)) / MathUtils.dotProduct(V, normal);
		}
		
		if(distance <= 0)
			return Double.POSITIVE_INFINITY;
		return distance;
	}


	/**
	 * Calculate the intersection distance of this rectangle with the given ray.
	 * Calculations are done with the same side technique, using cross products on vectors 
	 * between the rectangle vertices.
	 * 
	 * @param ray
	 * @param distance 
	 * @return
	 */
	private double intersectSameSide(Ray ray, double distance) {

		// Get the intersection point with the rectangle's plane
		ray.setMagnitude(distance);
		intersectionPoint = ray.getEndPoint();
		
		// 
		if(	sameSide(p0, p1, p2, intersectionPoint)  && 
			sameSide(p1, p3, p0, intersectionPoint)  &&
			sameSide(p2, p0, p3, intersectionPoint)  &&
			sameSide(p3, p2, p1, intersectionPoint) )
		{
			return distance;
		}
		
		return Double.POSITIVE_INFINITY;
	}

	/**
	 * We define the plane that contains the rectangle by the vectors b-a and c-a from the given parameters. 
	 * Let vector b-a a partition of the plane to two sides. We now check that the given intersection point 
	 * resides on the side of the plane which contains the rectangle.
	 * 
	 * @param a		The central vertex
	 * @param b		The left vertex
	 * @param c		The right vertex (for reference to the plane)
	 * @param intersectionPoint
	 * @return
	 */
	private boolean sameSide(double[] a, double[] b, double[] c, double[] intersectionPoint) {
		
		// Vector between two vertices of the rectangle 'a' and 'b'
		double[] vec1 = MathUtils.calcPointsDiff(a, b);
		
		// vector between reference vertice 'c' and original vertice 'a'
		double[] vec2 = MathUtils.calcPointsDiff(a, c);
		
		// vector between the point and 'a'
		double[] vec3 = MathUtils.calcPointsDiff(a, intersectionPoint);
		
		double[] crossProduct1 = MathUtils.crossProduct(vec1, vec2);
		double[] crossProduct2 = MathUtils.crossProduct(vec1, vec3);
		
		// If the dot product of the normals is positive then they point to the same direction
		// which means that 'point' is on the side of 'vec1' which is inside the rectangle. 
		if(MathUtils.dotProduct(crossProduct1, crossProduct2) > 0)
		{
			return true;
		}
		
		return false;
	}

	
	/**
	 * Calculate the intersection distance of this rectangle with the given ray.
	 * Calculations are done with barycentric: two of the rectangle's edges are taken as 
	 * spanning vectors of the plane and then the intersection point with the plane is located 
	 * by a linear combination of these vectors.  If the coordinates of both spanning vectors are 
	 * scalars x,y such that 0 < x,y < 1 , then the intersecting point is within the rectangle.
	 * between the rectangle vertices.
	 * 
	 * @param ray
	 * @param distance
	 * @return
	 */
	private double intersectBarycentric(Ray ray, double distance) {
		
		double[] v0, v1, v2;
		double dot00, dot01, dot02, dot11, dot12;
		double denominator, u, v;
				
		// Get the intersection point with the rectangle's plane
		ray.setMagnitude(distance);
		intersectionPoint = ray.getEndPoint();

		// Compute vectors        
		v0 = MathUtils.calcPointsDiff(p0, p2);
		v1 = MathUtils.calcPointsDiff(p0, p1);
		v2 = MathUtils.calcPointsDiff(p0, intersectionPoint);

		// Compute dot products
		dot00 = MathUtils.dotProduct(v0, v0);
		dot01 = MathUtils.dotProduct(v0, v1);
		dot02 = MathUtils.dotProduct(v0, v2);
		dot11 = MathUtils.dotProduct(v1, v1);
		dot12 = MathUtils.dotProduct(v1, v2);

		// Compute barycentric coordinates
		denominator = 1 / (dot00 * dot11 - dot01 * dot01);
		u = (dot11 * dot02 - dot01 * dot12) * denominator;
		v = (dot00 * dot12 - dot01 * dot02) * denominator;

		// Check if point is in rectangle
		if ((u > 0) && (v > 0) && (u < 1) && (v < 1)) {
			return distance;
		}
		
		return Double.POSITIVE_INFINITY;
	}


	

	public void setParameter(String name, String[] args) throws Exception {
		if (surface.parseParameter(name, args)) return;
		if ("p0".equals(name)) p0 = Parser.parseVector(args);						
		if ("p1".equals(name)) p1 = Parser.parseVector(args);						
		if ("p2".equals(name)) p2 = Parser.parseVector(args);						
	}


	@Override
	public double[] getNormal(double[] point) {
		return normal;
	}
	
	public double[] getNormal() {
		return normal;
	}


	public void postInit(List<IEntity> entities) throws ParseException {
		super.postInit(entities);
		
		if (MathUtils.arePointsCollinear(p0, p1, p2)) {
			throw new ParseException("The given points are collinear and cannot define a rectangle");
		}
		
		p3 = calcFourthPoint(p0, p1, p2);
		
		normal = MathUtils.crossProduct(MathUtils.calcPointsDiff(p0, p1), MathUtils.calcPointsDiff(p0, p2));
		MathUtils.normalize(normal);
		
		// Find d (the plane coefficient) by substituting x,y,z with a point which is on the plane. 
		// for example p0 of the rectangle;
		d = -(MathUtils.dotProduct(normal, p0)); 	
		
		// Preprocess some calculations to reduce load during rendering
		AB = MathUtils.calcPointsDiff(p0, p1);
		ABdotAB = MathUtils.dotProduct(AB, AB);
		AC = MathUtils.calcPointsDiff(p0, p2);
		ACdotAC = MathUtils.dotProduct(AC, AC);
		ABnorm = MathUtils.norm(AB);
		ACnorm = MathUtils.norm(AC);

	}


	/**
	 * Caclulates the fourth vertex of the rectangle
	 * @param p0
	 * @param p1
	 * @param p2
	 * @return
	 */
	private double[] calcFourthPoint(double[] p0, double[] p1, double[] p2) {
		double[] p3 = { p1[0] + (p2[0] - p0[0]) , p1[1] + (p2[1] - p0[1]) , p1[2] + (p2[2] - p0[2]) };
		return p3;
	}

	@Override
	public double[] getTextureCoords(double[] point) {
		
		double[] AP;
				
		// Calculate the projection of the intersection point onto the rectangle vectors
		AP = MathUtils.calcPointsDiff(p0, point);
		double q = 1 / MathUtils.norm(MathUtils.calcPointsDiff(p0, p1));
				
		double u = MathUtils.dotProduct(AB, AP) / ABdotAB;
		double v = MathUtils.dotProduct(AC, AP) / ACdotAC;
		
		u /= ABnorm * q;
		v /= ACnorm * q;
		
		return new double[] { u, v };
	}

}
