package checker.patpar;

import java.util.List;

import checkers.javari.quals.ReadOnly;

public class RoList {
	public void foo(@ReadOnly List<String> lst) {
		lst.get(0);
		lst.set(0, null); // ERROR call to set(int,E) not allowed on the given receiver*
	}
}
