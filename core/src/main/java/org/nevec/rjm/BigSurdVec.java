package org.nevec.rjm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Vector;

import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Utils;

/**
 * A BigSurdVec represents an algebraic sum or differences of values which each
 * term an instance of BigSurd. This mainly means that sums or differences of
 * two BigSurd (or two BigSurdVec) can be represented (exactly) as a BigSurdVec.
 *
 * @since 2012-02-15
 * @author Richard J. Mathar
 */
public class BigSurdVec implements Comparable<BigSurdVec> {
	/**
	 * The value of zero.
	 */
	static public BigSurdVec ZERO = new BigSurdVec();

	/**
	 * The value of one.
	 */
	static public BigSurdVec ONE = new BigSurdVec(BigSurd.ONE);

	/**
	 * Internal representation: Each term as a single BigSurd. The value zero is
	 * represented by an empty vector.
	 */
	Vector<BigSurd> terms;

	/**
	 * Default ctor, which represents the zero.
	 *
	 * @since 2012-02-15
	 */
	public BigSurdVec() {
		terms = new Vector<>();
	} /* ctor */

	/**
	 * ctor given the value of a BigSurd.
	 *
	 * @param a
	 *            The value to be represented by this vector.
	 * @since 2012-02-15
	 */
	public BigSurdVec(final BigSurd a) {
		terms = new Vector<>(1);
		terms.add(a);
	} /* ctor */

	/**
	 * ctor given two values, which (when added) represent this number a+b.
	 *
	 * @param a
	 *            The value to be represented by the first term of the vector.
	 * @param b
	 *            The value to be represented by the second term of the vector.
	 * @since 2012-02-15
	 */
	public BigSurdVec(final BigSurd a, final BigSurd b) {
		terms = new Vector<>(2);
		terms.add(a);
		terms.add(b);
		try {
			normalize();
		} catch (final Error e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} /* ctor */

	/**
	 * Combine terms that can be written as a single surd. This unites for
	 * example the terms sqrt(90) and sqrt(10) to 4*sqrt(10).
	 *
	 * @throws Error
	 *
	 * @since 2012-02-15
	 */
	protected void normalize() throws Error {
		/*
		 * nothing to be done if at most one term
		 */
		if (terms.size() <= 1) {
			return;
		}

		final Vector<BigSurd> newter = new Vector<>();
		newter.add(terms.firstElement());
		/*
		 * add j-th element to the existing vector and combine were possible
		 */
		for (int j = 1; j < terms.size(); j++) {
			final BigSurd todo = terms.elementAt(j);
			boolean merged = false;
			for (int ex = 0; ex < newter.size(); ex++) {
				BigSurd v = newter.elementAt(ex);
				/*
				 * try to merge terms[j] and newter[ex]. todo = r * v with r a
				 * rational number is needed. Replaces v with v+todo = v*(1+r)
				 * if this reduction works.
				 */
				final BigSurd r = todo.divide(v);
				if (r.isRational()) {
					/* compute r+1 */
					final Rational newpref = r.toRational().add(1);
					/*
					 * eliminate accidental zeros; overwrite with v*(1+r).
					 */
					if (newpref.compareTo(Rational.ZERO) == 0) {
						newter.removeElementAt(ex);
					} else {
						v = v.multiply(newpref);
						newter.setElementAt(v, ex);
					}
					merged = true;
					break;
				}
			}
			/*
			 * append if none of the existing elements matched
			 */
			if (!merged) {
				newter.add(todo);
			}
		}

		/* overwrite old version */
		terms = newter;
	} /* normalize */

	/**
	 * Compare algebraic value with oth. Returns -1, 0 or +1 depending on
	 * whether this is smaller, equal to or larger than oth.
	 *
	 * @param oth
	 *            The value with which this is to be compared.
	 * @return 0 or +-1.
	 * @since 2012-02-15
	 */
	@Override
	public int compareTo(final BigSurdVec oth) {
		BigSurdVec diff;
		try {
			diff = this.subtract(oth);
			return diff.signum();
		} catch (final Error e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	} /* compareTo */

	/**
	 * Sign function. Returns -1, 0 or +1 depending on whether this is smaller,
	 * equal to or larger than zero.
	 *
	 * @return 0 or +-1.
	 * @throws Error
	 * @since 2012-02-15
	 */
	public int signum() throws Error {
		/*
		 * the case of zero is unique, because no (reduced) vector of surds
		 * other than the one element 0 itself can add/subtract to zero.
		 */
		if (terms.size() == 0) {
			return 0;
		}

		/*
		 * if there is one term: forward to the signum function of BigSurd
		 */
		if (terms.size() == 1) {
			return terms.firstElement().signum();
		}

		/*
		 * if all terms have a common sign: take that one offsig is the index of
		 * the first "offending" term in the sense that its sign doese not agree
		 * with the term[0].
		 */
		final int sig0 = terms.elementAt(0).signum();
		int offsig = 1;
		for (; offsig < terms.size(); offsig++) {
			if (terms.elementAt(offsig).signum() != sig0) {
				break;
			}
		}
		if (offsig >= terms.size()) {
			return sig0;
		}

		/*
		 * if there are two terms (now known to have different sign): forward to
		 * the comparison of the two elements as BigSurds
		 */
		if (terms.size() == 2) {
			return terms.elementAt(0).compareTo(terms.elementAt(1).negate());
		}

		/*
		 * if there are three terms, move the one with the offending sign to the
		 * other side and square both sides (which looses the sign) to remove
		 * all but one surds. The difference of the squared sides contains at
		 * most two terms, which reduces to the case above. t(0)+t(offbar) <>
		 * -t(offs)
		 */
		if (terms.size() == 3) {
			BigSurdVec lhs;
			if (offsig == 2) {
				lhs = new BigSurdVec(terms.elementAt(0), terms.elementAt(1));
			} else {
				lhs = new BigSurdVec(terms.elementAt(0), terms.elementAt(2));
			}
			lhs = lhs.sqr();
			/*
			 * Strange line: this line isn't used, but it's present in this
			 * code!
			 *
			 *
			 *
			 * BigSurd rhs = new BigSurd(terms.elementAt(offsig).sqr(),
			 * Rational.ONE);
			 *
			 *
			 *
			 */
			if (lhs.compareTo(lhs) > 0) {
				/*
				 * dominating sign was t(0)+t(offbar)
				 */
				return terms.elementAt(0).signum();
			} else {
				return terms.elementAt(offsig).signum();
			}
		}

		/*
		 * for a larger number of terms: take a floating point representation
		 * with a small but correct number of digits, and resume with the sign
		 * of that one.
		 */
		return floatValue() > 0. ? 1 : -1;

	} /* signum */

	/**
	 * Construct an approximate floating point representation
	 *
	 * @param mc
	 *            The intended accuracy of the result.
	 * @return A truncated version with the precision described by mc
	 */
	public BigDecimal BigDecimalValue(final MathContext mc) {
		/*
		 * simple cases with one term forwarded to the BigSurd class
		 */
		if (terms.size() == 0) {
			return BigDecimal.ZERO;
		} else if (terms.size() == 1) {
			return terms.firstElement().BigDecimalValue(mc);
		}

		/*
		 * To reduce cancellation errors, loop over increasing local precision
		 * until we are stable to the required result. Keep the old (less
		 * precise) estimate in res[0], and the newer, more precise in res[1].
		 */
		final BigDecimal[] res = new BigDecimal[2];
		res[0] = BigDecimal.ZERO;
		for (int addpr = 1;; addpr += 3) {
			final MathContext locmc = new MathContext(mc.getPrecision() + addpr, mc.getRoundingMode());
			res[1] = BigDecimal.ZERO;
			for (final BigSurd j : terms) {
				res[1] = BigDecimalMath.addRound(res[1], j.BigDecimalValue(locmc));
			}
			if (addpr > 1) {
				final BigDecimal err = res[1].subtract(res[0]).abs();
				final int prec = BigDecimalMath.err2prec(res[1], err);
				if (prec > mc.getPrecision()) {
					break;
				}
			}
			res[0] = res[1];
		}
		return BigDecimalMath.scalePrec(res[1], mc);

	} /* BigDecimalValue */

	/**
	 * Construct an approximate floating point representation
	 *
	 * @return A truncated version with the precision described by mc
	 */
	public double doubleValue() {
		final BigDecimal bd = BigDecimalValue(MathContext.DECIMAL128);
		return bd.doubleValue();
	} /* doubleValue */

	/**
	 * Construct an approximate floating point representation
	 *
	 * @return A truncated version with the precision described by mc
	 */
	public double floatValue() {
		final BigDecimal bd = BigDecimalValue(MathContext.DECIMAL64);
		return bd.floatValue();
	} /* floatValue */

	/**
	 * Add two vectors algebraically.
	 *
	 * @param val
	 *            The value to be added to this.
	 * @return The new value representing this+val.
	 * @throws Error
	 */
	public BigSurdVec add(final BigSurdVec val) throws Error {
		final BigSurdVec sum = new BigSurdVec();
		/*
		 * concatenate the vectors and eliminate common overlaps
		 */
		for (final BigSurd term : terms) {
			if (term.compareTo(BigSurd.ZERO) != 0) {
				sum.terms.add(term);
			}
		}
		for (final BigSurd term : val.terms) {
			if (term.compareTo(BigSurd.ZERO) != 0) {
				sum.terms.add(term);
			}
		}
		sum.normalize();
		return sum;
	} /* add */

	/**
	 * Add two vectors algebraically.
	 *
	 * @param val
	 *            The value to be added to this.
	 * @return The new value representing this+val.
	 * @throws Error
	 */
	public BigSurdVec add(final BigSurd val) throws Error {
		final BigSurdVec sum = new BigSurdVec();
		/*
		 * concatenate the vectors and eliminate common overlaps
		 */
		sum.terms.addAll(terms);
		sum.terms.add(val);
		sum.normalize();
		return sum;
	} /* add */

	/**
	 * Subtract another number.
	 *
	 * @param val
	 *            The value to be subtracted from this.
	 * @return The new value representing this-val.
	 * @throws Error
	 */
	public BigSurdVec subtract(final BigSurdVec val) throws Error {
		final BigSurdVec sum = new BigSurdVec();
		/*
		 * concatenate the vectors and eliminate common overlaps
		 */
		sum.terms.addAll(terms);
		for (final BigSurd s : val.terms) {
			sum.terms.add(s.negate());
		}
		sum.normalize();
		return sum;
	} /* subtract */

	/**
	 * Subtract another number.
	 *
	 * @param val
	 *            The value to be subtracted from this.
	 * @return The new value representing this-val.
	 * @throws Error
	 */
	public BigSurdVec subtract(final BigSurd val) throws Error {
		final BigSurdVec sum = new BigSurdVec();
		/*
		 * concatenate the vectors and eliminate common overlaps
		 */
		sum.terms.addAll(terms);
		sum.terms.add(val.negate());
		sum.normalize();
		return sum;
	} /* subtract */

	/**
	 * Compute the negative.
	 *
	 * @return -this.
	 * @since 2012-02-15
	 */
	public BigSurdVec negate() {
		/*
		 * accumulate the negated elements of term one by one
		 */
		final BigSurdVec resul = new BigSurdVec();
		for (final BigSurd s : terms) {
			resul.terms.add(s.negate());
		}
		/*
		 * no normalization step here, because the negation of all terms does
		 * not introduce new common factors
		 */
		return resul;
	} /* negate */

	/**
	 * Compute the square.
	 *
	 * @return this value squared.
	 * @throws Error
	 * @since 2012-02-15
	 */
	public BigSurdVec sqr() throws Error {
		/*
		 * Binomial expansion. First the sum of the terms squared, then 2 times
		 * the mixed products.
		 */
		final BigSurdVec resul = new BigSurdVec();
		for (int i = 0; i < terms.size(); i++) {
			resul.terms.add(new BigSurd(terms.elementAt(i).sqr(), Rational.ONE));
		}
		for (int i = 0; i < terms.size() - 1; i++) {
			for (int j = i + 1; j < terms.size(); j++) {
				resul.terms.add(terms.elementAt(i).multiply(terms.elementAt(j)).multiply(2));
			}
		}
		resul.normalize();
		return resul;
	} /* sqr */

	/**
	 * Multiply by another square root.
	 *
	 * @param val
	 *            a second number of this type.
	 * @return the product of this with the val.
	 * @throws Error
	 * @since 2011-02-12
	 */
	public BigSurdVec multiply(final BigSurd val) throws Error {
		final BigSurdVec resul = new BigSurdVec();
		for (final BigSurd s : terms) {
			resul.terms.add(s.multiply(val));
		}
		resul.normalize();
		return resul;
	} /* multiply */

	public BigSurdVec multiply(final BigSurdVec val) throws Error {
		BigSurdVec resul = new BigSurdVec();
		for (final BigSurd s : terms) {
			resul.terms.add(s);
		}
		for (final BigSurd s : val.terms) {
			resul = resul.multiply(s);
		}
		return resul;
	} /* multiply */

	public BigSurdVec divide(final BigSurd val) throws Error {
		final BigSurdVec resul = new BigSurdVec();
		for (final BigSurd s : terms) {
			resul.terms.add(s.divide(val));
		}
		resul.normalize();
		return resul;
	} /* multiply */

	public BigSurdVec divide(final BigSurdVec val) throws Error {
		BigSurdVec resul = new BigSurdVec();
		resul.terms = terms;
		for (final BigSurd s : val.terms) {
			resul = resul.divide(s);
		}
		return resul;
	} /* divide */

	/**
	 * True if the value is rational. Equivalent to the indication whether a
	 * conversion to a Rational can be exact.
	 *
	 * @since 2011-02-12
	 */
	public boolean isRational() {
		boolean val = false;
		for (final BigSurd s : terms) {
			val = s.isRational();
			if (val == false) {
				break;
			}
		}
		return val;
	} /* BigSurdVec.isRational */

	/**
	 * True if the value is BigInteger. Equivalent to the indication whether a
	 * conversion to a BigInteger can be exact.
	 *
	 * @since 2011-02-12
	 */
	public boolean isBigInteger() {
		boolean val = false;
		for (final BigSurd s : terms) {
			val = s.isBigInteger();
			if (val == false) {
				break;
			}
		}
		return val;
	} /* BigSurdVec.isRational */

	/**
	 * Convert to a rational value if possible
	 *
	 * @since 2012-02-15
	 */
	public Rational toRational() {
		Rational rat = Rational.ZERO;
		if (isRational() == false) {
			throw new ArithmeticException("Undefined conversion " + toString() + " to Rational.");
		}
		for (final BigSurd s : terms) {
			rat = rat.add(s.pref);
		}
		return rat;
	} /* BigSurd.toRational */

	/**
	 * Convert to a BigInteger value if possible
	 *
	 * @since 2012-02-15
	 */
	public BigInteger toBigInteger() {
		BigDecimal tmp = BigDecimal.ZERO.setScale(Utils.scale, Utils.scaleMode);
		if (isBigInteger() == false) {
			throw new ArithmeticException("Undefined conversion " + toString() + " to Rational.");
		}
		for (final BigSurd s : terms) {
			tmp = BigDecimalMath.addRound(tmp, s.pref.BigDecimalValue(new MathContext(Utils.scale, Utils.scaleMode2)));
		}
		return tmp.toBigInteger();
	} /* BigSurd.toRational */

	/**
	 * Convert to a BigDecimal value if possible
	 *
	 * @since 2012-02-15
	 */
	public BigDecimal toBigDecimal() {
		BigDecimal tmp = BigDecimal.ZERO.setScale(Utils.scale, Utils.scaleMode);
		for (final BigSurd s : terms) {
			tmp = BigDecimalMath.addRound(tmp, s.BigDecimalValue(new MathContext(Utils.scale, Utils.scaleMode2)));
		}
		return tmp;
	} /* BigSurd.toBigDecimal */

	/**
	 * Return a string in the format (number/denom)*()^(1/2). If the
	 * discriminant equals 1, print just the prefactor.
	 *
	 * @return the human-readable version in base 10
	 * @since 2012-02-16
	 */
	@Override
	public String toString() {
		/*
		 * simple cases with one term forwarded to the BigSurd class
		 */
		if (terms.size() == 0) {
			return new String("0");
		} else {
			String s = new String();
			for (int t = 0; t < terms.size(); t++) {
				final BigSurd bs = terms.elementAt(t);
				if (bs.signum() > 0) {
					s += "+";
				}
				s += bs.toString();
			}
			return s;
		}
	} /* toString */

	public String toFancyString() {
		if (terms.size() == 0) {
			return new String("0");
		} else {
			BigInteger denominator = BigInteger.ONE;
			for (int i = 0; i < terms.size(); i++) {
				denominator = denominator.multiply(terms.elementAt(i).pref.b);
			}
			String s = "";
			if (denominator.compareTo(BigInteger.ONE) != 0) {
				s += "(";
			}
			for (int t = 0; t < terms.size(); t++) {
				final BigSurd bs = terms.elementAt(t);
				if (bs.signum() > 0 && t > 0) {
					s += "+";
				}
				if (bs.isBigInteger()) {
					s += bs.BigDecimalValue(new MathContext(Utils.scale, Utils.scaleMode2)).toBigInteger().toString();
				} else if (bs.isRational()) {
					s += bs.toRational().toString();
				} else {
					final BigInteger numerator = bs.pref.multiply(denominator).numer();
					if (numerator.compareTo(BigInteger.ONE) != 0) {
						s += numerator.toString();
						s += "*";
						// s += "("; Radice quadrata. non servono le parentesi.
					}
					s += "Ⓐ";
					if (bs.disc.isInteger()) {
						s += bs.disc.toString();
					} else {
						s += "(" + bs.disc.toString() + ")";
					}
					if (numerator.compareTo(BigInteger.ONE) != 0) {
						// s += ")"; Radice quadrata. non servono le parentesi.
					}
				}
			}
			if (denominator.compareTo(BigInteger.ONE) != 0) {
				s += ")";
				s += "/";
				s += denominator;
			}
			return s;
		}
	}

} /* BigSurdVec */
