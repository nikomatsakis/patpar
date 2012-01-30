package checker.patpar;

import java.util.ArrayList;
import java.util.List;

import patpar.Closure;
import patpar.PatPar;
import checkers.javari.quals.ReadOnly;

public class ClosureWithInheritedList {

	class Data {
		int f;
	}

	public void main(final List<Data> parentList) {
		PatPar.fork(new Closure<Void>() {
			@Override
			protected Void compute() {
				parentList.set(0, null);    // ERROR call to set(int,E) not allowed on the given receiver*
				
				@ReadOnly List<Data> l = parentList;
				
				@SuppressWarnings("unused")
				Data d = parentList.get(0); // ERROR incompatible types*found   : @ReadOnly Data*required: @Mutable Data
				parentList.get(0).f = 2;    // ERROR a field of a ReadOnly object is not assignable

				Data childData = new Data();
				childData.f = 2;
				List<Data> childList = new ArrayList<Data>();
				childList.add(childData);
				return null;
			}
		});
	}

}
