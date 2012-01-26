package checker.patpar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import patpar.Closure;
import patpar.PatPar;
import patpar.Task;

public class HelloWorld {
	
	int sharedField;
	Map<String, String> sharedMap;
	
	public HelloWorld() {
		sharedMap = new HashMap<String, String>();
	}
	
	class TranslateString extends Closure<String>
	{
		public final String string;
		
		public TranslateString(String s) {
			this.string = s;
		}
		
		@Override
		protected String compute() {
			return sharedMap.get(string);
		}
	}
	
	public List<String> translate(final List<String> list) throws ExecutionException {
		return PatPar.root(new Closure<List<String>>() {
			@Override
			protected List<String> compute() {
				List<Task<String>> futures = new ArrayList<>();
				for (String s : list) {
					futures.add(PatPar.fork(new TranslateString(s)));
				}
				PatPar.sync();
				List<String> result = new ArrayList<>();
				for (Task<String> f : futures) {
					result.add(f.get());
				}
				return result;
			}
		});
	}

	public static void main(String args[]) {
	}
	
}
