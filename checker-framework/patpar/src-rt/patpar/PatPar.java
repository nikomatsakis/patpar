package patpar;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class PatPar {
	public static <T> T root(final Closure<T> b) {
		final ForkJoinPool fjp = new ForkJoinPool();
		try {
			return fjp.submit(new RecursiveTask<T>() {
				private static final long serialVersionUID = 6904038984846007980L;
				@Override
				protected T compute() {
					Task<T> root = new Task<>(null, b);
					root.enqueue(fjp);
					return root.getFuture();
				}
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> Task<T> fork(Closure<T> b) {
		Finish f = Finish.current();
		if (f == null)
			throw new PatParException("Use root() method for the root task");
		
		return f.fork(b);
	}
	
	public static void finish(final Runnable r) {
		Finish f = Finish.current();
		if (f == null)
			throw new PatParException("Use root() method for the root task");
		
		new Finish(f).run(new FinishBody<Void>() {
			@Override public Void run() {
				r.run();
				return null;
			}
		});
	}
}
