package patpar;

public class IntArray extends Array<Integer> {
	private int[] data;
	
	public IntArray(int size) {
		this.data = new int[size];
		updateOwner();
	}

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
	
	public IntArray map(ParClosure<Integer,Integer> cl) {
		IntArray result = new IntArray(data.length);
		mapInto(new IntArray(data.length), cl);
		return result;
	}

	/** Returns a copy of the internal array */
	public int[] toArray() {
		checkAccess(false);
		int[] copy = new int[data.length];
		System.arraycopy(data, 0, copy, 0, data.length);
		return copy;
	}
}
