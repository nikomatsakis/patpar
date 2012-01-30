package patpar;

public class ObjArray<T> extends Array<T> {
	private T[] data;
	
	@SuppressWarnings("unchecked")
	public ObjArray(int size) {
		data = (T[]) new Object[size];
		updateOwner();
	}
	
	@Override
	T getUnchecked(int x) {
		return data[x];
	}

	@Override
	void setUnchecked(int x, T v) {
		data[x] = v;
	}

	@Override
	Range range() {
		return new CRange(0, data.length);
	}
	
	public <U> ObjArray<U> map(ParClosure<T,U> cl) {
		ObjArray<U> result = new ObjArray<>(data.length);
		mapInto(result, cl);
		return result;
	}
}
