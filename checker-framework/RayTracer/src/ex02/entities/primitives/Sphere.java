package ex02.entities.primitives;

import java.util.List;

import ex02.Parser;
import ex02.Parser.ParseException;
import ex02.blas.MathUtils;
import ex02.entities.IEntity;
import ex02.entities.Ray;

public class Sphere extends Primitive {

	private double[] center = new double[3];
	private double radius;
	
	
	@Override
	public double intersect(Ray ray) {
						
		return intersectGeometric(ray);
	}
		
	/**
	 * Calculate the intersection distance of this sphere with the given ray.
	 * Calculations are done in a geometric method, using pythagorean calculations. 
	 * Some references claim that this method may work faster than the algebraic method. 
	 * 
	 * @param ray
	 * @return
	 */
	private double intersectGeometric(Ray ray) {
		
		// Note that locals are named according to the equations in the lecture notes.
		double[] L = MathUtils.calcPointsDiff(ray.getPosition(), center);
		double[] V = ray.getDirection();
		
		double tCA = MathUtils.dotProduct(L, V);
		
		if(tCA < 0) {
			// In this case the camera is inside the sphere or the sphere center lies
			// behind the ray, which means we have no intersection
			return Double.POSITIVE_INFINITY;
		}
		
		double LSquare = MathUtils.dotProduct(L, L);
		
		double dSquare =  LSquare - MathUtils.sqr(tCA);
		double radiusSquare = MathUtils.sqr(radius);

		if(dSquare > radiusSquare) {
			// In this case the ray misses the sphere
			return Double.POSITIVE_INFINITY;
		}
		
		double tHC = Math.sqrt(radiusSquare - dSquare);

		// We now check where the ray originated:
		// Gur: CHECK. LSquare == MathUtils.dotProduct(L, L), can't be smaller
		if(MathUtils.dotProduct(L, L) < LSquare)
		{
			// The ray originated in the sphere - the intersection is with the exit point
			return tCA + tHC;
		}
		else
		{
			// The ray originated ouside the sphere - the intersection is with the entrance point
			return tCA - tHC;
		}
	}


	/**
	 * Calculate the intersection distance of this sphere with the given ray.
	 * Calculations are done in an algebraic method.
	 *  
	 * @param ray
	 * @return
	 */
	private double intersectAlgebraic(Ray ray) {


		// Quadratic equation coefficients
		double a,b,c;

		
		// Note that locals are named according to the equations in the lecture notes.
		double[] v = ray.getDirection();
		double[] p0 = ray.getPosition();
		double[] O = center;
		double[] p0_O = MathUtils.calcPointsDiff(p0, O);
		
		a = 1;
		b = 2 * MathUtils.dotProduct(v, p0_O);
		c = MathUtils.dotProduct(p0_O, p0_O)  -  MathUtils.sqr(radius);
		
		// Solve equation for at^2 + bt + c = 0
		double[] roots = MathUtils.solveQuadraticEquation(a, b, c);
		
		double distance;
		if(roots[0] > 0 && roots[1] > 0)
		{
			distance = Math.min(roots[0], roots[1]);
		}
		else if (roots[0] <= 0 && roots[1] <= 0)
		{
			distance = Double.POSITIVE_INFINITY;
		}
		else
		{
			distance = Math.max(roots[0], roots[1]);
		}
		return distance;
	}

	public void setParameter(String name, String[] args) throws Exception {
		if (surface.parseParameter(name, args)) return;
		if ("center".equals(name)) center = Parser.parseVector(args);						
		if("radius".equals(name)) radius = Double.parseDouble(args[0]);
	}

	@Override
	public double[] getNormal(double[] point) {
		double[] normal = MathUtils.calcPointsDiff(center, point);		
		MathUtils.normalize(normal);
		
		return normal;
	}

	@Override
	public void postInit(List<IEntity> entities) throws ParseException {
		super.postInit(entities);		
	}	

	@Override
	public double[] getTextureCoords(double[] point) {
		double[] rp = MathUtils.calcPointsDiff(center, point);
		
        double v = rp[2] / radius;
        
        if (Math.abs(v) > 1) v -= 1 * Math.signum(v);
        v = Math.acos(v);
        
        double u = rp[0] / (radius * Math.sin(v));
        
        if (Math.abs(u) > 1) u = Math.signum(u);
        u = Math.acos(u);               
        
        if (rp[1] < 0)
            u = -u;
        if (rp[2] < 0)
            v = v + Math.PI;
        
        if (Double.isNaN(u)) {
        	int a = 0; a++;
        }
        
        u = (u / (2 * Math.PI));
        v = (v / Math.PI);
        
        if (u > 1) u -= 1;
        if (u < 0) u += 1;
        
        if (v > 1) v -= 1;
        if (v < 0) v += 1;
        
        return new double[] {u , v };						
	}
}
