package ex02.entities;

import ex02.blas.Vector3D;

// Pretty-name class for Vector3D, used to represent rays (position, direction, magnitude)
public class Ray extends Vector3D {
	public Ray(double[] position, double[] direction, double magnitude) throws Exception {
		super(position, direction, magnitude);
	}
}
