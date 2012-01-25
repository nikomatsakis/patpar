package patpar;

import java.util.List;

abstract class Range {
	abstract boolean inBounds(int x);
	
	// Divides the range into approximately `times` subranges.
	// May be more or less depending on rounding and so forth.
	// Each subrange will be disjoint and non-empty.
	abstract List<CRange> divideC(int times);
}
