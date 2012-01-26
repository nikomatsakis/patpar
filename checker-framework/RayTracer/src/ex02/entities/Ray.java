package ex02.entities;

import checkers.javari.quals.ReadOnly;
import ex02.blas.Vector3D;

// Pretty-name class for Vector3D, used to represent rays (position, direction, magnitude)
public class Ray extends Vector3D {
	public Ray(
			double /*@ReadOnly*/ [] position, 
			double /*@ReadOnly*/ [] direction,
			double magnitude) {
		super(position, direction, magnitude);
	}
}
