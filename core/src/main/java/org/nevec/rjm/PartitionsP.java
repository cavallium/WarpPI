package org.nevec.rjm;

import java.math.BigInteger;
import java.util.Vector;

/**
 * Number of partitions.
 *
 * @since 2008-10-15
 * @author Richard J. Mathar
 */
public class PartitionsP {
	/**
	 * The list of all partitions as a vector.
	 */
	static protected Vector<BigInteger> a = new Vector<>();

	/**
	 * The maximum integer covered by the high end of the list.
	 */
	static protected BigInteger nMax = new BigInteger("-1");

	/**
	 * Default constructor initializing a list of partitions up to 7.
	 */
	public PartitionsP() {
		if (PartitionsP.a.size() == 0) {
			PartitionsP.a.add(new BigInteger("" + 1));
			PartitionsP.a.add(new BigInteger("" + 1));
			PartitionsP.a.add(new BigInteger("" + 2));
			PartitionsP.a.add(new BigInteger("" + 3));
			PartitionsP.a.add(new BigInteger("" + 5));
			PartitionsP.a.add(new BigInteger("" + 7));
		}
		PartitionsP.nMax = new BigInteger("" + (PartitionsP.a.size() - 1));
	} /* ctor */

	/**
	 * return the number of partitions of i
	 *
	 * @param i
	 *            the zero-based index into the list of partitions
	 * @return the ith partition number. This is 1 if i=0 or 1, 2 if i=2 and so
	 *         forth.
	 */
	public BigInteger at(final int i) {
		/*
		 * If the current list is too small, increase in intervals
		 * of 3 until the list has at least i elements.
		 */
		while (i > PartitionsP.nMax.intValue()) {
			growto(PartitionsP.nMax.add(new BigInteger("" + 3)));
		}
		return PartitionsP.a.elementAt(i);
	} /* at */

	/**
	 * extend the list of known partitions up to n
	 *
	 * @param n
	 *            the maximum integer hashed after the call.
	 */
	private void growto(final BigInteger n) {
		while (PartitionsP.a.size() <= n.intValue()) {
			BigInteger per = new BigInteger("0");
			final BigInteger cursiz = new BigInteger("" + PartitionsP.a.size());
			for (int k = 0; k < PartitionsP.a.size(); k++) {
				final BigInteger tmp = PartitionsP.a.elementAt(k).multiply(BigIntegerMath.sigma(PartitionsP.a.size() - k));
				per = per.add(tmp);
			}
			PartitionsP.a.add(per.divide(cursiz));
		}
		PartitionsP.nMax = new BigInteger("" + (PartitionsP.a.size() - 1));
	} /* growto */

	/**
	 * Test program.
	 * It takes one integer argument n and prints P(n).<br>
	 * java -cp . org.nevec.rjm.PartitionsP n<br>
	 *
	 * @since 2008-10-15
	 */
	public static void main(final String[] args) throws Exception {
		final PartitionsP a = new PartitionsP();
		final int n = new Integer(args[0]).intValue();
		System.out.println("P(" + n + ")=" + a.at(n));
	}
}
