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
	List<CRange> divideC(int times) {
		if (min == max)
			return Collections.emptyList();
		int inc = Math.max(max - min, 1) / times;
		List<CRange> result = new ArrayList<>(); 
		int c = min;
		while (c < max) {
			int ci = c + inc;
			result.add(new CRange(c, ci));
			c = ci;
		}
		return result;
	}

}
