package patpar;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class PatPar {
	private static final ThreadLocal<Task<?>> state = new ThreadLocal<>();
	
	static Task<?> pushTask(Task<?> t) {
		Task<?> old = state.get();
		state.set(t);
		return old;
	}
	
	static void popTask(Task<?> t) {
		state.set(t);
	}
	
	static Task<?> getTask() {
		return state.get();
	}
	
	public static <T> T root(final Closure<T> b) {
		final ForkJoinPool fjp = new ForkJoinPool();
		try {
			return fjp.submit(new RecursiveTask<T>() {
				private static final long serialVersionUID = 6904038984846007980L;
				@Override
				protected T compute() {
					Task<T> root = new Task<>(fjp, null, b);
					root.enqueue();
					return root.join();
				}
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> Task<T> fork(Closure<T> b) {
		Task<?> parent = getTask();
		return parent.fork(b);
	}
	
	public static void sync() {
		Task<?> parent = getTask();
		parent.sync();
	}
}
