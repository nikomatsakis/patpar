package patpar;

public class View1D<R extends Range,T> extends Array<T> {
	private final Array<T> array;
	public final R range;
	
	View1D(Array<T> array, R range) {
		super();
		this.array = array;
		this.range = range;
	}
	
	@Override
	T getUnchecked(int x) {
		if (!range.inBounds(x))
			throw new PatParException("Index " + x + " out of range for view.");
		return array.getUnchecked(x);
	}

	@Override
	void setUnchecked(int x, T v) {
		if (!range.inBounds(x))
			throw new PatParException("Index " + x + " out of range for view.");
		array.setUnchecked(x, v);
	}

	@Override
	Range range() {
		return range;
	}
}
