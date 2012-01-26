package patpar;

import java.util.List;

abstract class Array<T> {
	private Task<?> owner;
	private boolean divided;

	Array() {
	}
	
	void updateOwner() {
		owner = Finish.currentTask();
	}
	
	protected void checkAccess(boolean isWrite) /*@ReadOnly*/ {
		if (divided) {
			throw new PatParException("Cannot access divided array except through view");
		}
		
		// Strictly speaking, these checks are not needed.
		// Type system ought to enforce it.
		
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
		}
		
		Task<?> c = Finish.currentTask();
		if (o == c) { // The owner can always access.
			return;
		}
		
		// Not the owner.  Must be a child of the owner.
		assert c.isChildOf(o);
		
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
			final Finish fin,
			final Closure1<View<R, T>, Void> cl,
			final List<? extends R> ranges) {
		checkAccess(true);
		divided = true;
		fin.addDividedArray(this);
		for (R range : ranges) {
			final View<R, T> view = new View<>(this, range);
			PatPar.fork(new Closure<Void>() {
				@Override
				protected Void compute() {
					view.updateOwner();
					cl.compute(view);
					return null;
				}
			});
		}
	}
	
	public final void divideC(final Closure1<View<CRange, T>, Void> cl) {
		Finish fin = Finish.current();
		int par = fin.guessHowManyTasksToMake();
		List<CRange> ranges = range().divideC(par);
		divide(fin, cl, ranges);
	}
	
	<U> void mapInto(
			final Array<U> newArray, 
			final Closure1<T, U> cl) {
		newArray.divideC(new Closure1<View<CRange,U>, Void>() {
			@Override
			protected Void compute(View<CRange, U> view) {
				CRange range = view.range;
				for (int i = range.min; i < range.max; i++) {
					U u = cl.compute(Array.this.get(i));
					view.set(i, u);
				}
				return null;
			}
		});
	}

	void undivide() {
		divided = false;
	}
}
