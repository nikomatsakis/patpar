package patpar;

public class IntArray1D extends Array1D<Integer> {
	private int[] data;

	@Override
	Integer getUnchecked(int x) {
		return data[x];
	}

	@Override
	void setUnchecked(int x, Integer v) {
		data[x] = v;
	}

	@Override
	Range range() {
		return new CRange(0, data.length);
	}
}
