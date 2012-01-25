package patpar;



public final class DoubleArray extends Array<Double> {
	private double[] data;
	
	public DoubleArray(int size) {
		data = new double[size];
		updateOwner();
	}

	@Override
	Double getUnchecked(int x) {
		return data[x];
	}

	@Override
	void setUnchecked(int x, Double v) {
		data[x] = v;
	}

	@Override
	Range range() {
		return new CRange(0, data.length);
	}
}
