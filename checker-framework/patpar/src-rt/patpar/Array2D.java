package patpar;

public class Array2D<T> extends Array<T> {
	
	private final Array<T> base;
	private final int width;
	
	public Array2D(Array<T> base, int width) {
		super();
		this.base = base;
		this.width = width;
	}
	
	public T get(int x, int y) {
		return get(x + y * width);
	}

	public void set(int x, int y, T val) {
		set(x + y * width, val);
	}

	@Override
	T getUnchecked(int x) {
		return base.getUnchecked(x);
	}

	@Override
	void setUnchecked(int x, T v) {
		base.setUnchecked(x, v);
	}

	@Override
	Range range() {
		return base.range();
	}

}
