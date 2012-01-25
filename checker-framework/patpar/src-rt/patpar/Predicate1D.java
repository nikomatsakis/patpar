package patpar;

public interface Predicate1D {
	enum Result {
		ILLEGAL,
		NOT_IN_VIEW,
		LEGAL
	}
	public Result test(int x);
}
