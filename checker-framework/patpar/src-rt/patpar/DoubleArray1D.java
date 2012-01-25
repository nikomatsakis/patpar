package patpar;



public final class DoubleArray1D extends Array1D<Double> {
	private double[] data;
	
	public DoubleArray1D(int size) {
		data = new double[size];
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
