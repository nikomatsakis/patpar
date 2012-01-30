package patpar;

public abstract class ParTask<T> extends ParClosure<Void, T> {
	protected final T compute(Void _) {
		return compute();
	}

	protected abstract T compute();
}
