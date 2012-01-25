package ex02.entities.primitives;

import java.util.List;

import ex02.Parser;
import ex02.Parser.ParseException;
import ex02.blas.MathUtils;
import ex02.entities.IEntity;
import ex02.entities.Ray;

public class Box extends Primitive {

	private double[] p0, p1, p2, p3;
	private Rectangle[] rectangles = new Rectangle[6];
	private Rectangle currentIntersectingRectangle = null;
	private int intersectingRectangleIndex = 0;

	
	/**
	 * Get the normal of the rectangle which is currently intersected.
	 */
	@Override
	public double[] getNormal(double[] point) throws Exception {
		return currentIntersectingRectangle.getNormal(point);
	}

	
	@Override
	public double intersect(Ray ray) {
		
		// Start off with infinite distance and no intersecting primitive
		double minDistance = Double.POSITIVE_INFINITY;
		
		for (int i = 0; i < rectangles.length; i++)
		{
			double t = rectangles[i].intersect(ray);

			// If we found a closer intersecting rectangle, keep a reference to and it
			if (t < minDistance)
			{
				minDistance = t;
				currentIntersectingRectangle = rectangles[i];
				intersectingRectangleIndex = i;
			}
		}

		return minDistance;
	}
	
	public void postInit(List<IEntity> entities) throws ParseException {
		super.postInit(entities);
		
		double[] p0_p1 = MathUtils.calcPointsDiff(p0, p1);
		double[] p0_p3 = MathUtils.calcPointsDiff(p0, p3);
		
		// Assume this is a cube just for the documentation
		rectangles[0] = new Rectangle(p0, p1, p2);		// Front facing rectangle
		rectangles[1] = new Rectangle(p0, p2, p3);		// Left facing rectangle
		rectangles[2] = new Rectangle(p0, p3, p1);		// Bottom facing rectangle
		
		rectangles[3] = new Rectangle(p1, MathUtils.addPoints(p3, p0_p1), MathUtils.addPoints(p2, p0_p1));		// Right facing rectangle
		rectangles[4] = new Rectangle(p2, MathUtils.addPoints(p2, p0_p1), MathUtils.addPoints(p2, p0_p3));		// Top facing rectangle
		rectangles[5] = new Rectangle(p3, MathUtils.addPoints(p2, p0_p3), MathUtils.addPoints(p3, p0_p1));		// Back facing rectangle
	}
	
	public void setParameter(String name, String[] args) throws Exception {
		if (surface.parseParameter(name, args)) return;
		if ("p0".equals(name)) p0 = Parser.parseVector(args);						
		if ("p1".equals(name)) p1 = Parser.parseVector(args);						
		if ("p2".equals(name)) p2 = Parser.parseVector(args);						
		if ("p3".equals(name)) p3 = Parser.parseVector(args);						
	}


	@Override
	public double[] getTextureCoords(double[] point) {
		return rectangles[intersectingRectangleIndex].getTextureCoords(point);
	}

}
