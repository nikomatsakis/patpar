package patpar;

import java.util.List;

abstract class Range {
	abstract boolean inBounds(int x);
	
	// Divides the range into approximately `times` subranges.
	// Each subrange will be a multiple of chunk in size, except
	// possibly the last one.  Precise number of subranges
	// will vary. Each subrange will be disjoint and non-empty.
	abstract List<CRange> divideC(int chunk, int times);
}
