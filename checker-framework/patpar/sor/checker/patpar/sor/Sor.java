package checker.patpar.sor;

/*
 * Copyright (C) 2000 by ETHZ/INF/CS
 * All rights reserved
 * 
 * @version $Id: Sor.java 2094 2003-01-30 09:41:18Z praun $
 * @author Florian Schneider
 */

import java.util.Date;

import patpar.Array;
import patpar.CRange;
import patpar.Closure;
import patpar.Closure1;
import patpar.FloatArray;
import patpar.ObjArray;
import patpar.PatPar;
import patpar.View;
import checkers.javari.quals.ReadOnly;

public class Sor {

	public final static int N = 500; // Cols range from 1:N (we allocate N+1)
	public final static int M = 500; // Rows range from 1:M (we allocate M+2)
	public static int iterations = 100;
	
	public static float get(Array<Float> arr, int r, int c) {
		return arr.get(c + (r * (N + 1)));
	}

	public static void set(Array<Float> arr, int r, int c, float v) {
		arr.set(c + (r * (N + 1)), v);
	}

	public static void moveUnoffsetRow(
			Array<Float> to,
			@ReadOnly Array<Float> from, int row) {
		if (row < 1 || row > M)
			return;
		
		for (int col = 0; col < N; col++) {
			float v = (get(from, row - 1, col) + get(from, row + 1, col)
					+ get(from, row, col) + get(from, row, col + 1))
					/ (float) 4.0;
			set(to, row, col, v);
		}
	}

	public static void moveOffsetRow(
			Array<Float> to,
			@ReadOnly Array<Float> from, int row) {
		if (row < 1 || row > M)
			return;
		
		for (int col = 1; col <= N; col++) {
			float v = (get(from, row - 1, col) + get(from, row + 1, col)
					+ get(from, row, col) + get(from, row, col - 1))
					/ (float) 4.0;
			set(to, row, col, v);
		}
	}
	
	public static void blackFromRed(Array<Float> black,
			@ReadOnly Array<Float> red, int fromIdx, int toIdx) {
		int fromRow = convRow(fromIdx);
		int toRow = convRow(toIdx);
		int row = fromRow;

		if ((row % 2) == 1) // first row (fromRow) odd
			moveUnoffsetRow(black, red, row++);

		for (; row < toRow - 1; row += 2) {
			moveOffsetRow(black, red, row); // even row
			moveUnoffsetRow(black, red, row + 1); // odd row
		}

		if ((toRow % 2) == 1) // final row (toRow-1) even
			moveOffsetRow(black, red, toRow - 1);
	}

	private static int convRow(int idx) {
		assert (idx % (N+1)) == 0;
		return idx / (N+1);
	}

	public static void redFromBlack(Array<Float> red,
			@ReadOnly Array<Float> black, int fromIdx, int toIdx) {
		int fromRow = convRow(fromIdx);
		int toRow = convRow(toIdx);
		int row = fromRow;

		if ((row % 2) == 1) // first row (fromRow) odd
			moveOffsetRow(red, black, row++);

		for (; row < toRow - 1; row += 2) {
			moveUnoffsetRow(red, black, row); // even row
			moveOffsetRow(red, black, row + 1); // odd row
		}

		if ((toRow % 2) == 1) // final row (toRow-1) even
			moveUnoffsetRow(red, black, toRow - 1);
	}

	public static void main1(String[] args) {

		final FloatArray black = new FloatArray((M+2) * (N+1));
		final FloatArray red = new FloatArray((M+2) * (N+1));

		boolean nop = false;

		try {
			if (args[0].equals("--nop"))
				nop = true;
			else {
				iterations = Integer.parseInt(args[0]);
			}
		} catch (Exception e) {
			System.out
					.println("usage: java Sor <iterations>");
			System.out.println("    or java Sor --nop");
			System.exit(-1);
		}

		// initialize arrays
		int first_row = 1;
		int last_row = M;

		/*
		 * Initialize the top edge.
		 */
		for (int j = 0; j <= N; j++) {
			set(red, 0, j, (float) 1.0);
			set(black, 0, j, (float) 1.0);
		}

		for (int i = first_row; i <= last_row; i++) {
			/*
			 * Initialize the left and right edges.
			 */
			if ((i & 1) != 0) {
				set(red, i, 0, (float) 1.0);
				set(black, i, N, (float) 1.0);
			} else {
				set(black, i, 0, (float) 1.0);
				set(red, i, N, (float) 1.0);
			}
		}

		/*
		 * Initialize the bottom edge.
		 */
		for (int j = 0; j <= N; j++) {
			set(red, M+1, j, (float) 1.0);
			set(black, M+1, j, (float) 1.0);
		}

		// start computation
		System.gc();
		long a = new Date().getTime();
		
		if (!nop) {
			for (int i = 0; i < iterations; i += 1) {
				PatPar.finish(new Runnable() {
					public void run() {
						black.divideC(N+1, new Closure1<View<CRange,Float>, Void>() {
							protected Void compute(
									View<CRange, Float> black) {
								blackFromRed(black, red, black.range.min, black.range.max);
								return null;
							}
						});
					}
				});
				
				PatPar.finish(new Runnable() {
					public void run() {
						red.divideC(N+1, new Closure1<View<CRange,Float>, Void>() {
							protected Void compute(
									View<CRange, Float> red) {
								redFromBlack(red, black, red.range.min, red.range.max);
								return null;
							}
						});
					}
				});
			}
		}
		
		long b = new Date().getTime();

		System.out.println("SorPatPar-" + "\t" + Long.toString(b - a));

		// print out results
		float red_sum = 0, black_sum = 0;
		for (int r = 0; r < M + 2; r++)
			for (int c = 0; c < N + 1; c++) {
				red_sum += get(red, r, c);
				black_sum += get(black, r, c);
			}
		System.out.println("Exiting. red_sum = " + red_sum + ", black_sum = "
				+ black_sum);
	}

	private static ObjArray<FloatArray> makeArray(int rows, int cols) {
		ObjArray<FloatArray> result = new ObjArray<>(rows);
		for (int r = 0; r < rows; r++)
			result.set(r, new FloatArray(cols));
		return result;
	}

	public static void print(String s) {
		System.out.println(Thread.currentThread().getName() + ":" + s);
	}
	
	public static void main(final String args[]) {
		PatPar.root(new Closure<Void>() {
			protected Void compute() {
				main1(args);
				return null;
			}
		});
	}

}
