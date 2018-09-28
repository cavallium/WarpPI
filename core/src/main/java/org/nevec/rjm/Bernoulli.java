package org.nevec.rjm;

import java.math.BigInteger;
import java.util.Vector;

import it.cavallium.warppi.util.Error;

/**
 * Bernoulli numbers.
 *
 * @since 2006-06-25
 * @author Richard J. Mathar
 */
public class Bernoulli {
	/*
	 * The list of all Bernoulli numbers as a vector, n=0,2,4,....
	 */
	static Vector<Rational> a = new Vector<>();

	public Bernoulli() {
		if (Bernoulli.a.size() == 0) {
			Bernoulli.a.add(Rational.ONE);
			Bernoulli.a.add(new Rational(1, 6));
		}
	}

	/**
	 * Set a coefficient in the internal table.
	 *
	 * @param n
	 *            the zero-based index of the coefficient. n=0 for the constant
	 *            term.
	 * @param value
	 *            the new value of the coefficient.
	 */
	protected void set(final int n, final Rational value) {
		final int nindx = n / 2;
		if (nindx < Bernoulli.a.size()) {
			Bernoulli.a.set(nindx, value);
		} else {
			while (Bernoulli.a.size() < nindx) {
				Bernoulli.a.add(Rational.ZERO);
			}
			Bernoulli.a.add(value);
		}
	}

	/**
	 * The Bernoulli number at the index provided.
	 *
	 * @param n
	 *            the index, non-negative.
	 * @return the B_0=1 for n=0, B_1=-1/2 for n=1, B_2=1/6 for n=2 etc
	 * @throws Error
	 */
	public Rational at(final int n) throws Error {
		if (n == 1) {
			return new Rational(-1, 2);
		} else if (n % 2 != 0) {
			return Rational.ZERO;
		} else {
			final int nindx = n / 2;
			if (Bernoulli.a.size() <= nindx) {
				for (int i = 2 * Bernoulli.a.size(); i <= n; i += 2) {
					set(i, doubleSum(i));
				}
			}
			return Bernoulli.a.elementAt(nindx);
		}
	}

	/*
	 * Generate a new B_n by a standard double sum.
	 *
	 * @param n The index of the Bernoulli number.
	 *
	 * @return The Bernoulli number at n.
	 */
	private Rational doubleSum(final int n) throws Error {
		Rational resul = Rational.ZERO;
		for (int k = 0; k <= n; k++) {
			Rational jsum = Rational.ZERO;
			BigInteger bin = BigInteger.ONE;
			for (int j = 0; j <= k; j++) {
				final BigInteger jpown = new BigInteger("" + j).pow(n);
				if (j % 2 == 0) {
					jsum = jsum.add(bin.multiply(jpown));
				} else {
					jsum = jsum.subtract(bin.multiply(jpown));
				}

				/*
				 * update binomial(k,j) recursively
				 */
				bin = bin.multiply(new BigInteger("" + (k - j))).divide(new BigInteger("" + (j + 1)));
			}
			resul = resul.add(jsum.divide(new BigInteger("" + (k + 1))));
		}
		return resul;
	}

} /* Bernoulli */
