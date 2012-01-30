package checker.patpar;

import patpar.ParTask;

public class ClosureWithFields extends ParTask<Void> {
	
	int field; // ERROR Closure objects cannot have fields

	@Override
	protected Void compute() {
		return null;
	}

}
