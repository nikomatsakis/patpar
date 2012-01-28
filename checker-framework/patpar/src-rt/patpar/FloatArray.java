package patpar;



public final class FloatArray extends Array<Float> {
	private float[] data;
	
	public FloatArray(int size) {
		data = new float[size];
		updateOwner();
	}

	@Override
	Float getUnchecked(int x) {
		return data[x];
	}

	@Override
	void setUnchecked(int x, Float v) {
		data[x] = v;
	}

	@Override
	Range range() {
		return new CRange(0, data.length);
	}
}
