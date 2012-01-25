package patpar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class Task<T> {
	private final ForkJoinPool fjp;
	private final Task<?> parent;
	private final Closure<T> closure;
	private Future<T> future;
	private final List<Task<?>> children = new ArrayList<>();
	private boolean completed;
	
	public Task(ForkJoinPool fjp, Task<?> parent, Closure<T> closure) {
		super();
		this.fjp = fjp;
		this.parent = parent;
		this.closure = closure;
		completed = false;
	}
	
	<C> Task<C> fork(Closure<C> b) {
		Task<C> child = new Task<>(fjp, this, b);
		children.add(child);
		return child;
	}

	void enqueue() {
		this.future = fjp.submit(new RecursiveTask<T>() {
			private static final long serialVersionUID = 7420151722469283929L;
			@Override
			protected T compute() {
				final Task<?> t = PatPar.pushTask(Task.this);
				try {
					return closure.fork().join();
				} finally {
					completed = true;
					PatPar.popTask(t);
				}
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
	
	void sync() {
		for (Task<?> child : children) {
			child.enqueue();
		}

		for (Task<?> child : children) {
			child.join();
		}
	}		
	
	public T get() {
		if (this.future == null) {
			if (PatPar.getTask() == parent)
				throw new RuntimeException("get() before execute");
			return null;
		} else {
			try {
				return this.future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	T join() {
		return get();
	}

	boolean isChildOf(Task<?> o) {
		for (Task<?> p = parent; p != null; p = p.parent) {
			if (p == o)
				return true;
		}
		return false;
	}

	int guessHowManyTasksToMake() {
		return fjp.getParallelism() * 2;
	}
}
