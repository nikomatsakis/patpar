package checker.patpar;

import patpar.CRange;
import patpar.ParClosure;
import patpar.IntArray;
import patpar.View;

public class DivideIntArray {
	
	public void foo(final IntArray arr) {
		arr.divideC(1, new ParClosure<View<CRange,Integer>, Void>() {
			@Override protected Void compute(View<CRange, Integer> view) {
				CRange range = view.range;
				for (int i = range.min; i < range.max; i++) {
					view.get(i);
					view.set(i, 0);
				}
				return null;
			}
		});
	}
	
}
