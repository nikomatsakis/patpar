package checker.patpar;

import patpar.CRange;
import patpar.ParClosure;
import patpar.ObjArray;
import patpar.View;

public class DivideObjArrray {
	
	class Foo {
		int fld;
	}

	public void foo(final ObjArray<Foo> arr) {
		arr.divideC(1, new ParClosure<View<CRange,Foo>, Void>() {
			@Override protected Void compute(View<CRange, Foo> view) {
				view.get(0).fld = 10; // ERROR a field of a ReadOnly object is not assignable
				return null;
			}
		});
	}
	
}
