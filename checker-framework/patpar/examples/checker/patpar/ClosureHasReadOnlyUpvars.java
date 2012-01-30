package checker.patpar;

import java.util.List;

import patpar.ParClosure;

public class ClosureHasReadOnlyUpvars {
	
	List<String> fld;

	public void foo(final List<String> lst) {
		new ParClosure<Void, Void>() {
			@Override
			protected Void compute(Void v) {
				fld = null; // ERROR a field of a ReadOnly object is not assignable
				fld.get(0);
				fld.add("hi"); // ERROR call to add(E) not allowed on the given receiver*
				lst.get(0); // OK.
				lst.set(0, "hi"); // ERROR call to set(int,E) not allowed on the given receiver*
				return null;
			}
		};
	}
	
}
