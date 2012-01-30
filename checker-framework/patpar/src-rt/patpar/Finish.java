package patpar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

class Finish {
	private static ThreadLocal<Finish> finish = new ThreadLocal<>();
	
	public final ForkJoinPool fjp;
	private final Task<?> parent;
	private final List<Task<?>> children = new ArrayList<>();
	private final List<Array<?>> divided = new ArrayList<>();
	
	public Finish(ForkJoinPool fjp, Task<?> parent) {
		this.parent = parent;
		this.fjp = fjp;
	}
	
	public Finish(Finish from) {
		this(from.fjp, from.parent);
	}
	
	static Finish current() {
		return finish.get();
	}
	
	static Task<?> currentTask() {
		Finish f = current();
		if (f == null)
			return null;
		return f.parent;
	}

	<C> Task<C> fork(ParTask<C> b) {
		Task<C> child = new Task<>(parent, b);
		children.add(child);
		return child;
	}
	
	void addDividedArray(Array<?> a) {
		this.divided.add(a);
	}
	
	<R> R run(FinishBody<R> body) {
		final Finish f = finish.get();
		finish.set(this);
		
		try {
			R result = body.run();
			
			for (Task<?> child : children) {
				child.enqueue(fjp);
			}

			for (Task<?> child : children) {
				child.getFuture();
			}
			
			return result;
		} finally {
			for (Array<?> arr : divided) 
				arr.undivide();
			
			finish.set(f);
		}
	}		
	
	int guessHowManyTasksToMake() {
		return fjp.getParallelism() * 2;
	}
}
