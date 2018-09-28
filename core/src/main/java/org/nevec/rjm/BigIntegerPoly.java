package org.nevec.rjm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.Vector;

import it.cavallium.warppi.util.Error;

/**
 * Polynomial with integer coefficients.
 * Alternatively to be interpreted as a sequence which has the polynomial as an
 * (approximate)
 * generating function.
 *
 * @since 2010-08-27
 * @author Richard J. Mathar
 */
public class BigIntegerPoly implements Cloneable {
	/**
	 * The list of all coefficients, starting with a0, then a1, as in
	 * poly=a0+a1*x+a2*x^2+a3*x^3+...
	 */
	Vector<BigInteger> a;

	/**
	 * Default ctor.
	 * Creates the polynomial p(x)=0.
	 */
	public BigIntegerPoly() {
		a = new Vector<>();
	}

	/**
	 * Ctor with a comma-separated list as the list of coefficients.
	 *
	 * @param L
	 *            the string of the form a0,a1,a2,a3 with the coefficients
	 */
	public BigIntegerPoly(final String L) throws NumberFormatException {
		a = new Vector<>();
		final Scanner sc = new Scanner(L);
		sc.useDelimiter(",");
		while (sc.hasNextBigInteger()) {
			a.add(sc.nextBigInteger());
		}
		simplify();
		sc.close();
	} /* ctor */

	/**
	 * Ctor with a list of coefficients.
	 *
	 * @param c
	 *            The coefficients a0, a1, a2 etc in a0+a1*x+a2*x^2+...
	 */
	@SuppressWarnings("unchecked")
	public BigIntegerPoly(final Vector<BigInteger> c) {
		a = (Vector<BigInteger>) c.clone();
		simplify();
	} /* ctor */

	/**
	 * Ctor with a list of coefficients.
	 *
	 * @param c
	 *            The coefficients a0, a1, a2 etc in a0+a1*x+a2*x^2+...
	 */
	public BigIntegerPoly(final BigInteger[] c) {
		for (final BigInteger element : c) {
			a.add(element.add(BigInteger.ZERO));
		}
		simplify();
	} /* ctor */

	/**
	 * Create a copy of this.
	 *
	 * @since 2010-08-27
	 */
	@Override
	public BigIntegerPoly clone() {
		return new BigIntegerPoly(a);
	} /* clone */

	/**
	 * Translate into a RatPoly copy.
	 *
	 * @since 2012-03-02
	 */
	public RatPoly toRatPoly() {
		final RatPoly bd = new RatPoly();
		for (int i = 0; i < a.size(); i++) {
			bd.set(i, a.elementAt(i));
		}
		return bd;
	} /* toRatPoly */

	/**
	 * Retrieve a polynomial coefficient.
	 *
	 * @param n
	 *            the zero-based index of the coefficient. n=0 for the constant
	 *            term.
	 * @return the polynomial coefficient in front of x^n.
	 */
	public BigInteger at(final int n) {
		if (n < a.size()) {
			return a.elementAt(n);
		} else {
			return BigInteger.ZERO;
		}
	} /* at */

	/**
	 * Evaluate at some integer argument.
	 *
	 * @param x
	 *            The abscissa point of the evaluation
	 * @return The polynomial value.
	 * @since 2010-08-27
	 * @author Richard J. Mathar
	 */
	public BigInteger valueOf(final BigInteger x) {
		if (a.size() == 0) {
			return BigInteger.ZERO;
		}
		BigInteger res = a.lastElement();
		/*
		 * Heron casted form
		 */
		for (int i = a.size() - 2; i >= 0; i--) {
			res = res.multiply(x).add(a.elementAt(i));
		}
		return res;
	} /* valueOf */

	/**
	 * Horner scheme to find the function value at the argument x
	 *
	 * @param x
	 *            The argument x.
	 * @return Value of the polynomial at x.
	 * @since 2008-11-13
	 */
	public BigInteger valueOf(final int x) {
		return valueOf(new BigInteger("" + x));
	} /* valueOf */

	/**
	 * Set a polynomial coefficient.
	 *
	 * @param n
	 *            the zero-based index of the coefficient. n=0 for the constant
	 *            term.
	 *            If the polynomial has not yet the degree to need this
	 *            coefficient,
	 *            the intermediate coefficients are set to zero.
	 * @param value
	 *            the new value of the coefficient.
	 */
	public void set(final int n, final BigInteger value) {
		if (n < a.size()) {
			a.set(n, value);
		} else {
			/*
			 * fill intermediate powers with coefficients of zero
			 */
			while (a.size() < n) {
				a.add(BigInteger.ZERO);
			}
			a.add(value);
		}
	} /* set */

	/**
	 * Set a polynomial coefficient.
	 *
	 * @param n
	 *            the zero-based index of the coefficient. n=0 for the constant
	 *            term.
	 *            If the polynomial has not yet the degree to need this
	 *            coefficient,
	 *            the intermediate coefficients are implicitly set to zero.
	 * @param value
	 *            the new value of the coefficient.
	 */
	public void set(final int n, final int value) {
		final BigInteger val2 = new BigInteger("" + value);
		set(n, val2);
	} /* set */

	/**
	 * Count of coefficients.
	 *
	 * @return the number of polynomial coefficients.
	 *         Differs from the polynomial degree by one.
	 */
	public int size() {
		return a.size();
	} /* size */

	/**
	 * Polynomial degree.
	 *
	 * @return the polynomial degree.
	 */
	public int degree() {
		return a.size() - 1;
	} /* degree */

	/**
	 * Polynomial lower degree.
	 *
	 * @return power of the smallest non-zero coefficient.
	 *         If the polynomial is identical to 0, 0 is returned.
	 */
	public int ldegree() {
		for (int n = 0; n < a.size(); n++) {
			if (a.elementAt(n).compareTo(BigInteger.ZERO) != 0) {
				return n;
			}
		}
		return 0;
	} /* ldegree */

	/**
	 * Multiply by a constant factor.
	 *
	 * @param val
	 *            the factor
	 * @return the product of this with the factor.
	 *         All coefficients of this have been multiplied individually by the
	 *         factor.
	 * @since 2010-08-27
	 */
	public BigIntegerPoly multiply(final BigInteger val) {
		final BigIntegerPoly resul = new BigIntegerPoly();
		if (val.compareTo(BigInteger.ZERO) != 0) {
			for (int n = 0; n < a.size(); n++) {
				resul.set(n, a.elementAt(n).multiply(val));
			}
		}
		return resul;
	} /* multiply */

	/**
	 * Multiply by another polynomial
	 *
	 * @param val
	 *            the other polynomial
	 * @return the product of this with the other polynomial
	 */
	public BigIntegerPoly multiply(final BigIntegerPoly val) {
		final BigIntegerPoly resul = new BigIntegerPoly();
		/*
		 * the degree of the result is the sum of the two degrees.
		 */
		final int nmax = degree() + val.degree();
		for (int n = 0; n <= nmax; n++) {
			BigInteger coef = BigInteger.ZERO;
			for (int nleft = 0; nleft <= n; nleft++) {
				coef = coef.add(at(nleft).multiply(val.at(n - nleft)));
			}
			resul.set(n, coef);
		}
		resul.simplify();
		return resul;
	} /* multiply */

	/**
	 * Raise to a positive power.
	 *
	 * @param n
	 *            the exponent of the power
	 * @return the n-th power of this.
	 */
	public BigIntegerPoly pow(final int n) throws ArithmeticException {
		BigIntegerPoly resul = new BigIntegerPoly("1");
		if (n < 0) {
			throw new ArithmeticException("negative polynomial power " + n);
		} else {
			for (int i = 1; i <= n; i++) {
				resul = resul.multiply(this);
			}
			resul.simplify();
			return resul;
		}
	} /* pow */

	/**
	 * Add another polynomial
	 *
	 * @param val
	 *            the other polynomial
	 * @return the sum of this with the other polynomial
	 * @since 2010-08-27
	 */
	public BigIntegerPoly add(final BigIntegerPoly val) {
		final BigIntegerPoly resul = new BigIntegerPoly();
		/*
		 * the degree of the result is the larger of the two degrees (before
		 * simplify() at least).
		 */
		final int nmax = degree() > val.degree() ? degree() : val.degree();
		for (int n = 0; n <= nmax; n++) {
			final BigInteger coef = at(n).add(val.at(n));
			resul.set(n, coef);
		}
		resul.simplify();
		return resul;
	} /* add */

	/**
	 * Subtract another polynomial
	 *
	 * @param val
	 *            the other polynomial
	 * @return the difference between this and the other polynomial
	 * @since 2008-10-25
	 */
	public BigIntegerPoly subtract(final BigIntegerPoly val) {
		final BigIntegerPoly resul = new BigIntegerPoly();
		/*
		 * the degree of the result is the larger of the two degrees (before
		 * simplify() at least).
		 */
		final int nmax = degree() > val.degree() ? degree() : val.degree();
		for (int n = 0; n <= nmax; n++) {
			final BigInteger coef = at(n).subtract(val.at(n));
			resul.set(n, coef);
		}
		resul.simplify();
		return resul;
	} /* subtract */

	/**
	 * Divide by another polynomial.
	 *
	 * @param val
	 *            the other polynomial
	 * @return A vector with [0] containg the polynomial of degree which is the
	 *         difference of the degree of this and the degree of val. [1] the
	 *         remainder polynomial.
	 *         This = returnvalue[0] + returnvalue[1]/val .
	 * @since 2012-03-01
	 */
	public BigIntegerPoly[] divideAndRemainder(final BigIntegerPoly val) {
		final BigIntegerPoly[] ret = new BigIntegerPoly[2];
		/*
		 * remove any high-order zeros. note that the clone() operation calls
		 * simplify().
		 */
		final BigIntegerPoly valSimpl = val.clone();
		final BigIntegerPoly thisSimpl = clone();

		/*
		 * catch the case with val equal to zero
		 */
		if (valSimpl.degree() == 0 && valSimpl.a.firstElement().compareTo(BigInteger.ZERO) == 0) {
			throw new ArithmeticException("Division through zero polynomial");
		}
		/*
		 * degree of this smaller than degree of val: remainder is this
		 */
		if (thisSimpl.degree() < valSimpl.degree()) {
			/*
			 * leading polynomial equals zero
			 */
			ret[0] = new BigIntegerPoly();
			ret[1] = thisSimpl;
		} else {
			/*
			 * long division. Highest degree by dividing the highest degree
			 * of this thru val. At this point an exception is thrown if the
			 * polynomial division cannot be done with integer coefficients.
			 */
			ret[0] = new BigIntegerPoly();
			final BigInteger[] newc = thisSimpl.a.lastElement().divideAndRemainder(valSimpl.a.lastElement());
			if (newc[1].compareTo(BigInteger.ZERO) != 0) {
				throw new ArithmeticException("Incompatible leading term in " + this + " / " + val);
			}
			ret[0].set(thisSimpl.degree() - valSimpl.degree(), newc[0]);

			/*
			 * recurrences: build this - val*(1-termresult) and feed this
			 * into another round of division. Have intermediate
			 * ret[0]+ret[1]/val.
			 */
			ret[1] = thisSimpl.subtract(ret[0].multiply(valSimpl));

			/*
			 * any remainder left ?
			 */
			if (ret[1].degree() < valSimpl.degree()) {
				;
			} else {
				final BigIntegerPoly rem[] = ret[1].divideAndRemainder(val);
				ret[0] = ret[0].add(rem[0]);
				ret[1] = rem[1];
			}
		}
		return ret;
	} /* divideAndRemainder */

	/**
	 * Print as a comma-separated list of coefficients.
	 *
	 * @return the representation a0,a1,a2,a3,...
	 * @since 2010-08-27
	 */
	@Override
	public String toString() {
		String str = new String();
		for (int n = 0; n < a.size(); n++) {
			if (n == 0) {
				str += a.elementAt(n).toString();
			} else {
				str += "," + a.elementAt(n).toString();
			}
		}
		if (str.length() == 0) {
			str = "0";
		}
		return str;
	} /* toString */

	/**
	 * Print as a polyomial in x.
	 *
	 * @return The representation a0+a1*x+a2*x^2+...
	 *         The terms with zero coefficients are not mentioned.
	 * @since 2008-10-26
	 */
	public String toPString() {
		String str = new String();
		for (int n = 0; n < a.size(); n++) {
			final BigInteger num = a.elementAt(n);
			if (num.compareTo(BigInteger.ZERO) != 0) {
				str += " ";
				if (num.compareTo(BigInteger.ZERO) > 0 && n > 0) {
					str += "+";
				}
				str += a.elementAt(n).toString();
				if (n > 0) {
					str += "*x";
					if (n > 1) {
						str += "^" + n;
					}
				}
			}
		}
		if (str.length() == 0) {
			str = "0";
		}
		return str;
	} /* toPString */

	/**
	 * Simplify the representation.
	 * Trailing values with zero coefficients (at high powers) are deleted.
	 */
	protected void simplify() {
		int n = a.size() - 1;
		if (n >= 0) {
			while (a.elementAt(n).compareTo(BigInteger.ZERO) == 0) {
				a.removeElementAt(n);
				if (--n < 0) {
					break;
				}
			}
		}
	} /* simplify */

	/**
	 * First derivative.
	 *
	 * @return The first derivative with respect to the indeterminate variable.
	 * @since 2008-10-26
	 */
	public BigIntegerPoly derive() {
		if (a.size() <= 1) {
			/*
			 * derivative of the constant is just zero
			 */
			return new BigIntegerPoly();
		} else {
			final BigIntegerPoly d = new BigIntegerPoly();
			for (int i = 1; i <= degree(); i++) {
				final BigInteger c = a.elementAt(i).multiply(new BigInteger("" + i));
				d.set(i - 1, c);
			}
			return d;
		}
	} /* derive */

	/**
	 * Truncate polynomial degree.
	 *
	 * @return The polynomial with all coefficients beyond deg set to zero.
	 * @since 2010-08-27
	 */
	public BigIntegerPoly trunc(final int newdeg) {
		final BigIntegerPoly t = new BigIntegerPoly();
		for (int i = 0; i <= newdeg; i++) {
			t.set(i, at(i));
		}
		t.simplify();
		return t;
	} /* trunc */

	/**
	 * Inverse Binomial transform.
	 *
	 * @param maxdeg
	 *            the maximum polynomial degree of the result
	 * @return the sequence of coefficients is the inverse binomial transform of
	 *         the original sequence.
	 * @since 2010-08-29
	 */
	public BigIntegerPoly binomialTInv(final int maxdeg) {
		final BigIntegerPoly r = new BigIntegerPoly();
		for (int i = 0; i <= maxdeg; i++) {
			BigInteger c = BigInteger.ZERO;
			for (int j = 0; j <= i && j < a.size(); j++) {
				if ((j + i) % 2 != 0) {
					c = c.subtract(a.elementAt(j).multiply(BigIntegerMath.binomial(i, j)));
				} else {
					c = c.add(a.elementAt(j).multiply(BigIntegerMath.binomial(i, j)));
				}
			}
			r.set(i, c);
		}
		r.simplify();
		return r;
	} /* binomialTInv */

	/**
	 * Compute the order of the root r.
	 *
	 * @return 1 for simple roots, 2 for order 2 etc., 0 if not a root
	 * @since 2010-08-27
	 */
	public int rootDeg(final BigInteger r) {
		int o = 0;
		BigIntegerPoly d = clone();
		BigInteger roo = d.valueOf(r);
		while (roo.compareTo(BigInteger.ZERO) == 0) {
			o++;
			d = d.derive();
			roo = d.valueOf(r);
		}
		return o;
	} /* rootDeg */

	/**
	 * Generate the integer roots of the polynomial.
	 *
	 * @return The vector of integer roots, without their multiplicity.
	 * @since 2010-08-27
	 */
	public Vector<BigInteger> iroots() {
		/* The vector of the roots */
		final Vector<BigInteger> res = new Vector<>();

		/*
		 * collect the zero
		 */
		if (a.firstElement().compareTo(BigInteger.ZERO) == 0) {
			res.add(BigInteger.ZERO);
		}

		/*
		 * collect the divisors of the constant element (or the reduced
		 * polynomial)
		 */
		final int l = ldegree();
		if (a.elementAt(l).compareTo(BigInteger.ZERO) != 0) {
			@SuppressWarnings("deprecation")
			final Vector<BigInteger> cand = BigIntegerMath.divisors(a.elementAt(l).abs());

			/* check the divisors (both signs) */
			for (int i = 0; i < cand.size(); i++) {
				BigInteger roo = valueOf(cand.elementAt(i));
				if (roo.compareTo(BigInteger.ZERO) == 0) {
					/* found a root cand[i] */
					res.add(cand.elementAt(i));
				}
				roo = valueOf(cand.elementAt(i).negate());
				if (roo.compareTo(BigInteger.ZERO) == 0) {
					res.add(cand.elementAt(i).negate());
				}
			}
		}
		return res;
	} /* iroots */

	/**
	 * Generate the factors which are 2nd degree polynomials.
	 *
	 * @return A (potentially empty) vector of factors, without multiplicity.
	 *         Only factors with non-zero absolute coefficient are generated.
	 *         This means the factors are of the form x^2+a*x+b=0 with nonzero
	 *         b.
	 * @throws Error
	 * @since 2012-03-01
	 */
	protected Vector<BigIntegerPoly> i2roots() throws Error {
		/*
		 * The vector of the factors to be returned
		 */
		final Vector<BigIntegerPoly> res = new Vector<>();

		if (degree() < 2) {
			return res;
		}

		final BigInteger bsco = a.firstElement().abs();
		@SuppressWarnings("deprecation")
		final Vector<BigInteger> b = BigIntegerMath.divisors(bsco);
		final BigInteger csco = a.lastElement().abs();
		@SuppressWarnings("deprecation")
		final Vector<BigInteger> c = BigIntegerMath.divisors(csco);

		/*
		 * Generate the floating point values of roots. To have some reasonable
		 * accuracy in the results, add zeros to the integer coefficients,
		 * scaled
		 * by the expected division with values of b (which are all <=
		 * a.firstele).
		 * Number of decimal digits in bsco by using a log2->log10 rough
		 * estimate
		 * and adding 6 safety digits
		 */
		final RatPoly thisDec = toRatPoly();
		final Vector<BigComplex> roo = thisDec.roots(6 + (int) (0.3 * bsco.bitCount()));

		final BigDecimal half = new BigDecimal("0.5");

		/*
		 * for each of the roots z try to see whether c*z^2+a*z+b=0 with integer
		 * a, b and c
		 * where b is restricted to a signed divisor of the constant
		 * coefficient.
		 * Solve z*(c*z+a)=-b or c*z+a = -b/z or -b/z-c*z = some integer a.
		 */
		for (final BigComplex z : roo) {
			for (final BigInteger bco : b) {
				for (final BigInteger cco : c) {
					/*
					 * the major reason to avoid the case b=0 is that this would
					 * require precaution of double counting below. Note that
					 * this
					 * case is already covered by using iroots().
					 */
					if (bco.signum() != 0) {
						for (int sig = -1; sig <= 1; sig += 2) {
							final BigInteger bcosig = sig > 0 ? bco : bco.negate();
							/*
							 * -a = b/z+c*z has real part b*Re(z)/|z|^2+c*Re(z)
							 * = Re z *( b/|z|^2+c)
							 */
							BigDecimal negA = BigDecimalMath.add(BigDecimalMath.divideRound(bcosig, z.norm()), cco);
							negA = negA.multiply(z.re);
							/*
							 * convert to a with round-to-nearest
							 */
							final BigInteger a = negA.negate().add(half).toBigInteger();

							/*
							 * test the polynomial remainder. if zero, add the
							 * term
							 * to the results.
							 */
							final BigIntegerPoly dtst = new BigIntegerPoly("" + bcosig + "," + a + "," + cco);
							try {
								final BigIntegerPoly[] rm = divideAndRemainder(dtst);
								if (rm[1].isZero()) {
									res.add(dtst);
								}
							} catch (final ArithmeticException ex) {}
						}
					}
				}
			}
		}

		return res;
	} /* i2roots */

	/**
	 * Test whether this polynomial value is zero.
	 *
	 * @return If this is a polynomial p(x)=0 for all x.
	 */
	public boolean isZero() {
		simplify();
		return a.size() == 0;
	}

	/**
	 * Factorization into integer polynomials.
	 * The current factorization detects only factors which are polynomials of
	 * order up to 2.
	 *
	 * @return The vector of factors. Factors with higher multiplicity are
	 *         represented by repetition.
	 * @throws Error
	 * @since 2012-03-01
	 */
	public Vector<BigIntegerPoly> ifactor() throws Error {
		/*
		 * this ought be entirely rewritten in terms of the LLL algorithm
		 */
		final Vector<BigIntegerPoly> fac = new Vector<>();

		/* collect integer roots (polynomial factors of degree 1) */
		final Vector<BigInteger> r = iroots();
		BigIntegerPoly[] res = new BigIntegerPoly[2];
		res[0] = this;
		for (final BigInteger i : r) {
			final int deg = rootDeg(i);
			/* construct the factor x-i */
			final BigIntegerPoly f = new BigIntegerPoly("" + i.negate() + ",1");
			for (int mu = 0; mu < deg; mu++) {
				fac.add(f);
				res = res[0].divideAndRemainder(f);
			}
		}

		/*
		 * collect factors which are polynomials of degree 2
		 */
		final Vector<BigIntegerPoly> pol2 = i2roots();
		for (final BigIntegerPoly i : pol2) {
			/*
			 * the internal loop catches cases with higher
			 * powers of individual polynomials (of actual degree 2 or 4...)
			 */
			while (res[0].degree() >= 2) {
				try {
					final BigIntegerPoly[] dtst = res[0].divideAndRemainder(i);
					if (dtst[1].isZero()) {
						fac.add(i);
						res = dtst;
					} else {
						break;
					}
				} catch (final ArithmeticException ex) {
					break;
				}
			}
		}

		/*
		 * add remaining factor, if not equal to 1
		 */
		if (res[0].degree() > 0 || res[0].a.firstElement().compareTo(BigInteger.ONE) != 0) {
			fac.add(res[0]);
		}
		return fac;
	} /* ifactor */

} /* BigIntegerPoly */
