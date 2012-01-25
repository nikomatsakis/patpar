package ex02.entities.primitives;

import java.util.List;

import Jama.Matrix;


import ex02.Parser;
import ex02.Parser.ParseException;
import ex02.blas.MathUtils;
import ex02.blas.RootFinder;
import ex02.entities.IEntity;
import ex02.entities.Ray;

public class Torus extends Primitive {

	private double[] center;
	private double centralRadius;
	private double tubeRadius;
	private double centralRadiusSquare;
	private double tubeRadiusSquare;
	private double[] normal;
	
	// quatric polynomial coefficients
	private double a4, a3, a2, a1, a0;
	private double alpha, beta, gamma;

	@Override
	public double[] getNormal(double[] point) {
		double[] normal = { 0, 0, 0};
		
		double innerComponent = MathUtils.sqr(point[0])  +  
							   MathUtils.sqr(point[1])  +  
							   MathUtils.sqr(point[2])  - tubeRadiusSquare - centralRadiusSquare;
		
		normal[0] = 4 * point[0] * innerComponent;
		normal[1] = 4 * point[1] * innerComponent;
		normal[2] = 4 * point[2] * innerComponent + (8 * centralRadiusSquare * MathUtils.sqr(point[2]));
		
		// Create the normal in matrix form
		Matrix normalMatrix = new Matrix(4,1);
		normalMatrix.set(0, 0, normal[0]);
		normalMatrix.set(1, 0, normal[1]);
		normalMatrix.set(2, 0, normal[2]);
		normalMatrix.set(3, 0, 1);
		
		// Create the translation matrix
		Matrix M = Matrix.identity(4, 4);
		M.set(0, 3, center[0]);
		M.set(1, 3, center[1]);
		M.set(2, 3, center[2]);

		// Translate the normal
		Matrix Mnormal = M.times(normalMatrix);

		// Extract it from the matrix form
		double[] translatedNormal = { Mnormal.get(0, 0) , Mnormal.get(1, 0) , Mnormal.get(2, 0) };
		
		MathUtils.normalize(translatedNormal);
		return translatedNormal;
	}

	@Override
	public double[] getTextureCoords(double[] point) {
		
		double[] referenceVector = { 1, 0, 0 };
		double[] pointOnRing = point.clone();
		MathUtils.addVectorAndMultiply(pointOnRing, MathUtils.oppositeVector(normal), tubeRadius);
		double[] vectorToRing = MathUtils.calcPointsDiff(center, pointOnRing);
		
		MathUtils.normalize(vectorToRing);
		
		double u = Math.acos(MathUtils.dotProduct(referenceVector, vectorToRing));
	    if(MathUtils.dotProduct(MathUtils.crossProduct(referenceVector, vectorToRing), normal) < 0)
	    {
	    	u = 2 * Math.PI - u;
	    }
	    
	    u /= (2 * Math.PI);
		
	    double[] fromRingToPoint = MathUtils.calcPointsDiff(pointOnRing, point);
		
		MathUtils.normalize(fromRingToPoint);

	    double v = Math.acos(MathUtils.dotProduct(referenceVector, fromRingToPoint));
//	    if(MathUtils.dotProduct(MathUtils.crossProduct(referenceVector, fromRingToPoint), normal) < 0)
//	    {
//	    	v = 2 * Math.PI - v;
//	    }
	    v /= (2 * Math.PI);

			
		return new double[] { u, v };
	}

	@Override
	public double intersect(Ray ray) {
		
		// Convert the ray position and direction to matrix style
		Matrix rayPosition = new Matrix(4,1);
		Matrix rayDirection = new Matrix(4,1);
		rayPosition.set(0, 0, ray.getPosition()[0]);
		rayPosition.set(1, 0, ray.getPosition()[1]);
		rayPosition.set(2, 0, ray.getPosition()[2]);
		rayPosition.set(3, 0, 1);
		rayDirection.set(0, 0, ray.getDirection()[0]);
		rayDirection.set(1, 0, ray.getDirection()[1]);
		rayDirection.set(2, 0, ray.getDirection()[2]);
		rayDirection.set(3, 0, 1);
		
		// Create the translation matrix
		Matrix M = Matrix.identity(4, 4);
		M.set(0, 3, -center[0]);
		M.set(1, 3, -center[1]);
		M.set(2, 3, -center[2]);

		// Translate the position and direction vectors
		Matrix MPosition = M.times(rayPosition);
		Matrix MDirection = M.times(rayDirection);
		
		// Extract them from the matrix form
		double[] translatedPosition = { MPosition.get(0, 0) , MPosition.get(1, 0) , MPosition.get(2, 0) };
		double[] translatedDirection = { MDirection.get(0, 0) , MDirection.get(1, 0) , MDirection.get(2, 0) };
		
		MathUtils.normalize(translatedDirection);
		
		// Reconstruct the ray after translation
		ray.setPosition(translatedPosition);
		ray.setDirection(translatedDirection);
		
		// Prepare parameters to work with for solving the polynomial
		double[] p = ray.getPosition();
		double[] d = ray.getDirection();
		alpha = MathUtils.dotProduct(d, d);
		beta = 2 * MathUtils.dotProduct(p, d);
		gamma = MathUtils.dotProduct(p, p) - tubeRadiusSquare - centralRadiusSquare;
		
		// Quatric polynomial coefficients
		a4 = MathUtils.sqr(alpha);
		a3 = 2 * alpha * beta;
		a2 = (MathUtils.sqr(beta))  +  (2 * alpha * gamma)  +  (4 * centralRadiusSquare * MathUtils.sqr(d[2]));
		a1 = (2 * beta * gamma)  +  (8 * centralRadiusSquare * p[2] * d[2]);
		a0 = MathUtils.sqr(gamma)  +  (4 * centralRadiusSquare * MathUtils.sqr(p[2]))  -  (4 * centralRadiusSquare * tubeRadiusSquare);
		
		// Solve quatric
		double[] coefficients = {a0, a1, a2, a3, a4};
		double[] roots = RootFinder.SolveQuartic(coefficients);
		
		if (roots == null || roots.length == 0)  return Double.POSITIVE_INFINITY;
		
		// Find the closest intersecting point
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < roots.length; i++) {
			if(roots[i] < min)
			{
				min = roots[i];
			}
		}
		
		return (min == Double.POSITIVE_INFINITY) ? Double.POSITIVE_INFINITY : min;
	}
	
	
	@Override
	public void postInit(List<IEntity> entities) throws ParseException {
		// TODO Auto-generated method stub
		super.postInit(entities);
		
		// normalize the normal vector
		MathUtils.normalize(normal);

		// Preprocess some stuff
		centralRadiusSquare = MathUtils.sqr(centralRadius);
		tubeRadiusSquare = MathUtils.sqr(tubeRadius);
		
	}
	

	@Override
	public void setParameter(String name, String[] args) throws Exception {
		if (surface.parseParameter(name, args)) return;
		if ("center".equals(name)) center = Parser.parseVector(args);						
		if ("central-radius".equals(name)) centralRadius = Double.parseDouble(args[0]);
		if ("tube-radius".equals(name)) tubeRadius = Double.parseDouble(args[0]);
		if ("normal".equals(name)) normal = Parser.parseVector(args);
	}

}
