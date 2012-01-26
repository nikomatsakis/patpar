package patpar;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

import checkers.javari.quals.ReadOnly;

public class Task<T> {
	private final Closure<T> closure;
	private final Task<?> parent;
	private Future<T> future;
	private boolean completed = false;
	
	public Task(Task<?> parent, Closure<T> closure) {
		super();
		this.parent = parent;
		this.closure = closure;
	}
	
	void enqueue(final ForkJoinPool fjp) {
		this.future = fjp.submit(new RecursiveTask<T>() {
			@Override protected T compute() {
				return new Finish(fjp, Task.this).run(new FinishBody<T>() {
					@Override public T run() {
						try {
							return closure.compute();
						} finally {
							completed = true;
						}
					}
				});
			}
		});
	}
	
	Task<?> update() {
		Task<?> t = this;
		while (t.completed) {
			t = t.parent;
		}
		return t;
	}

	/** 
	 * Return result of this task. 
	 * 
	 * Can only be invoked from the parent or from some ancestor of the parent. */ 
	public T get() {
		// FIXME-- this check should not be necessary, because Task method is
		// not readonly.  Have to fix up a bug or two in the type checker (for example,
		// making sure to convert type parameters to @ReadOnly in views and so forth)
		// to make this really work.
		if (Finish.currentTask() == parent) {
			return getFromParent();
		}
		throw new PatParException("cannot call get() except from parent");
	}

	/** 
	 * Return result of this task, joining as needed.
	 * 
	 * Can be invoked safely from children. */ 
	public @ReadOnly T join() /*@ReadOnly*/ {
		Task<?> task = Finish.currentTask();
		if (task == parent)
			return getFromParent();
		return getFuture();
	}

	private T getFromParent() {
		if (completed) {
			return getFuture();
		}
		throw new PatParException("cannot call get() until task is finished");
	}
	
	T getFuture() {
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	boolean isChildOf(Task<?> o) {
		for (Task<?> p = parent; p != null; p = p.parent) {
			if (p == o)
				return true;
		}
		return false;
	}
}
