package checker.patpar;

import patpar.Closure;

public class ClosureWithFields extends Closure<Void> {
	
	int field; // ERROR Closure objects cannot have fields

	@Override
	protected Void compute() {
		return null;
	}

}
