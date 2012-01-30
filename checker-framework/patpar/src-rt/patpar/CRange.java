package patpar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CRange extends Range {
	public final int min, max;
	
	public CRange(int min, int max) {
		super();
		this.min = min;
		this.max = max;
	}

	@Override
	boolean inBounds(int x) {
		return (x >= min) && (x < max); 
	}

	@Override
	List<CRange> divideC(int chunk, int times) {
		if (min == max)
			return Collections.emptyList();
		int inc = Math.max(max - min, 1) / times;
		inc = align(inc, chunk);
		List<CRange> result = new ArrayList<>(); 
		int c = min;
		while (c < max) {
			int ci = Math.min(c + inc, max);
			assert (ci == max || ((ci - c) % chunk == 0));
			result.add(new CRange(c, ci));
			c = ci;
		}
		return result;
	}

	private int align(int inc, int chunk) {
		return (inc + chunk - 1) / chunk * chunk;
	}

}
