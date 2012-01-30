package checker.patpar;

import patpar.CRange;
import patpar.Closure1;
import patpar.IntArray;
import patpar.View;

public class DivideIntArray {
	
	public void foo(final IntArray arr) {
		arr.divideC(1, new Closure1<View<CRange,Integer>, Void>() {
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
