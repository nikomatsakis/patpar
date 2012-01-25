package ex02.entities.primitives;

import java.util.List;

import ex02.Parser;
import ex02.Parser.ParseException;
import ex02.blas.MathUtils;
import ex02.blas.Vector3D;
import ex02.entities.IEntity;
import ex02.entities.Ray;
import ex02.entities.Surface;

public class Cylinder extends Primitive {

	
	private double[] start = null;
	private double[] end = null;
	private double[] direction = null;
	private double length;
	private double radius;
	
	// Fields for calculation optimizations
	private double radiusSquare;
	private double[] AB;
	private double ABdotAB;
	private double[] referenceVector;
	private double[] pivotVector;
	

	/*
	 * Note that some calculations are performed in the postInit method for optimization.
	 * (non-Javadoc)
	 * @see ex02.entities.primitives.Primitive#getNormal(double[])
	 */
	@Override
	public double[] getNormal(double[] point) throws Exception {
		
		// Formulas according to http://answers.yahoo.com/question/index?qid=20080218071458AAYz1s1
		double[] AP, center;
		
		// Calculate the projection of the intersection point onto the direction vector of the cylinder
		AP = MathUtils.calcPointsDiff(start, point);
		double t = MathUtils.dotProduct(AB, AP) / ABdotAB;
		center = start.clone();
		MathUtils.addVectorAndMultiply(center, AB, t);

		// Calculate the vector from the intersection point to its projection onto the direction of the cylinder.
		double[] normal = MathUtils.calcPointsDiff(center, point);
		MathUtils.normalize(normal);
		
		return normal;
	}

	
	/*
	 * Note that some calculations are performed in the postInit method for optimization.
	 * (non-Javadoc)
	 * @see ex02.entities.primitives.Primitive#intersect(ex02.entities.Ray)
	 */
	@Override
	public double intersect(Ray ray) {
		
		double[] AO, AOxAB, VxAB;	// Vectors to work with
		double a, b, c;		// Quadratic equation coefficients
		
		AO = MathUtils.calcPointsDiff(start, ray.getPosition());
		AOxAB = MathUtils.crossProduct(AO, direction);
		VxAB = MathUtils.crossProduct(ray.getDirection(), direction);
		
		
		a = MathUtils.dotProduct(VxAB, VxAB);
		b = 2 * MathUtils.dotProduct(VxAB, AOxAB);
		c = MathUtils.dotProduct(AOxAB, AOxAB) - radiusSquare;
		
		// Solve equation for at^2 + bt + c = 0
		double[] roots = MathUtils.solveQuadraticEquation(a, b, c);
		double distance;

		if(roots[0] == Double.POSITIVE_INFINITY)
		{
			distance = Double.POSITIVE_INFINITY;
		}
		else if(roots[0] <= 0 && roots[1] <=0)
		{
			distance = Double.POSITIVE_INFINITY;
		}
		// We need to choose the closest intersection point which is within the cylinder length
		else if(roots[0] >= 0 && roots[1] >= 0)
		{
			if(isPointOnCylinder(roots[0], ray))
			{
				if(isPointOnCylinder(roots[1], ray))
				{
					distance = Math.min(roots[0], roots[1]);
				}
				else
				{
					distance = roots[0];
				}
			}
			else if(isPointOnCylinder(roots[1], ray))
			{
				distance = roots[1];
			}
			else
			{
				distance = Double.POSITIVE_INFINITY;
			}
		}
		else
		{
			distance = Math.max(roots[0], roots[1]);
		}
		
		return distance;
	}
	

	private boolean isPointOnCylinder(double root, Ray ray) {

		// Formulas according to http://answers.yahoo.com/question/index?qid=20080218071458AAYz1s1
		double[] AP;
//		double[] intersectPoint = ray.getPosition();
//		MathUtils.addVectorAndMultiply(intersectPoint, ray.getDirection(), root);
		
		ray.setMagnitude(root);
		
		// Calculate the projection of the intersection point onto the direction vector of the cylinder
		AP = MathUtils.calcPointsDiff(start, ray.getEndPoint());
		double t = MathUtils.dotProduct(direction, AP);
		
		if(t > length || t < 0)
			return false;
		
		ray.setMagnitude(1);
		return true;
	}


	public void postInit(List<IEntity> entities) throws ParseException {
		super.postInit(entities);
		
		initializeReferenceVector();
		MathUtils.normalize(direction);
		pivotVector = MathUtils.crossProduct(direction, referenceVector);
		MathUtils.normalize(pivotVector);
		
		// The new end point determines the new direction, we just have to normalize it
		end = new double[3];
		end[0] = start[0] + (direction[0] * length);
		end[1] = start[1] + (direction[1] * length);
		end[2] = start[2] + (direction[2] * length);
		


		// Optimization:  Perform calculations for later use
		radiusSquare = radius * radius;
		AB = MathUtils.calcPointsDiff(start, end);
		ABdotAB = MathUtils.dotProduct(AB, AB);		

	}

	private void initializeReferenceVector() throws ParseException {
		
		// Choose an arbitrary vector as a reference vector - say the x basis vector
		referenceVector = new double[] { 1F, 0F, 0F };
		
		if (MathUtils.dotProduct(referenceVector, direction) > 0.9)
			referenceVector = new double[] { 0F, 1F, 0F };
			
		MathUtils.normalize(referenceVector);
	}


	public void setParameter(String name, String[] args) throws Exception {
		if (surface.parseParameter(name, args)) return;
		if ("start".equals(name)) start = Parser.parseVector(args);						
		if ("direction".equals(name)) direction = Parser.parseVector(args);						
		if ("length".equals(name)) length = Double.parseDouble(args[0]);						
		if ("radius".equals(name)) radius = Double.parseDouble(args[0]);						
	}
	
	
	public double[] getTextureCoords(double[] point) {
		try {			
			double pointStartDiff = MathUtils.norm(MathUtils.calcPointsDiff(point, start)); 
			double dist = Math.sqrt(Math.abs(MathUtils.sqr(pointStartDiff) - MathUtils.sqr(radius)));
	
			Vector3D startToCenter = new Vector3D(start, direction, dist);
			double[] pointToCenter = MathUtils.calcPointsDiff(point, startToCenter.getEndPoint());
			MathUtils.normalize(pointToCenter);
	
			double u = dist / length;
			double q = MathUtils.dotProduct(pointToCenter, referenceVector);
			if (Math.abs(q) > 1) q = 1 * Math.signum(q);
			
			double v = Math.acos(q);
			double[] orthoToPointToCenter = MathUtils.crossProduct(pointToCenter, referenceVector);
			MathUtils.normalize(orthoToPointToCenter);
			
			if (MathUtils.dotProduct(orthoToPointToCenter, direction) < 0) {
				v = (2 * Math.PI) - v;
			}
			
			v = v / (2 * Math.PI);
			
			return new double[] { u, v };		
		}
		catch (Exception e) {
			e.printStackTrace();
			return new double[] { 0, 0 };
		}
	}	
}
