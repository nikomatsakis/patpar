package patpar;

import java.util.List;

abstract class Array<T> {
	private Task<?> owner;
	private boolean divided;

	Array() {
		owner = PatPar.getTask();
		divided = false;
	}
	
	protected void checkAccess(boolean isWrite) /*@ReadOnly*/ {
		Task<?> o = owner.update();
		if (o != owner) {
			// Old owner may have died.  In that case, we are now 
			// owned by the parent (which is the one accessing us).
			//
			// Note that these may be racy writes.  Another thread 
			// could might simultaneously be performing the same
			// computations.  It's ok because the results will be
			// the same for both of them, and because there is 
			// no need for a happens-before relation to be established
			// between this reader and later readers that might see
			// these writes.
			owner = o;       // technically violates @ReadOnly but it's ok.
			divided = false; // technically violates @ReadOnly but it's ok.
		}
		
		Task<?> c = PatPar.getTask();
		if (o == c) { // The owner can always access.
			return;
		}
		
		// Not the owner.  Must be a child of the owner.
		assert c.isChildOf(o);
		
		// If we were divided, children can only access through the view:
		if (divided) {
			throw new PatParException("Cannot access divided array except through view");
		}
		
		// If we were not divided, children should have only @ReadOnly pointers, which
		// would prevent them from writing to us.  But just in case, we do a check here.
		if (isWrite)
			throw new PatParException("Cannot write to array from child task");
	}
	
	// Note: package local
	abstract T getUnchecked(int x);
	
	// Note: package local
	abstract void setUnchecked(int x, T v);

	public final T get(int x) /*@ReadOnly*/ {
		checkAccess(false);
		return getUnchecked(x);
	}
	
	public final void set(int x, T v) {
		checkAccess(true);
		setUnchecked(x, v);
	}
	
	abstract Range range();
	
	final <R extends Range> void divide(
			final Closure1<View1D<R, T>> cl,
			List<? extends R> ranges) {
		checkAccess(true);
		divided = true;
		for (R range : ranges) {
			final View1D<R, T> view = new View1D<>(this, range);
			PatPar.fork(new Closure<Void>() {
				@Override
				protected Void compute() {
					cl.compute(view);
					return null;
				}
			});
		}
		PatPar.sync();
		divided = false;
	}
	
	public final void divideC(final Closure1<View1D<CRange, T>> cl) {
		Task<?> task = PatPar.getTask();
		int par = task.guessHowManyTasksToMake();
		List<CRange> ranges = range().divideC(par);
		divide(cl, ranges);
	}
}
