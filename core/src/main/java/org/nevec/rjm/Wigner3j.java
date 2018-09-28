package org.nevec.rjm;

import java.math.BigInteger;
import java.util.Scanner;

import it.cavallium.warppi.util.Error;

/**
 * Exact representations of Wigner 3jm and 3nj values of half-integer arguments.
 *
 * @see R. J. Mathar, <a href="http://arxiv.org/abs/1102.5125">Corrigendum to
 *      "Universal factorzation fo 3n-j (j>2) symbols ..[J. Phys. A: Math. Gen.37 (2004) 3259]"
 *      </a>
 * @see R. J. Mathar, <a href="http://vixra.org/abs/1202.0093">Symmetries in
 *      Wigner 18-j and 21-j Symbols</a>
 * @since 2011-02-15
 * @author Richard J. Mathar
 */
public class Wigner3j {
	/**
	 * Test programs. This supports three types of direct evaluations:<br>
	 * java -cp . org.nevec.rjm.Wigner3j 3jm 2j1+1 2j2+1 2j3+1 2m1+1 2m2+1 2m3+1
	 * <br>
	 * java -cp . org.nevec.rjm.Wigner3j 6j 2j1+1 2j2+2 .. 2j6+1<br>
	 * java -cp . org.nevec.rjm.Wigner3j 9j 2j1+1 2j2+2 .. 2j9+1<br>
	 * The first command line argument is one of the three tags which determine
	 * whether a 3jm, a 6j or a 9j symbol will be computed. The other arguments
	 * are 6 or 9 integer values, which are the physical (half-integer) values
	 * multplied by 2 and augmented by 1. The order of the 6 or 9 values is as
	 * reading the corresponding standard symbol as first row, then second row
	 * (and for the 9j symbol) third row.
	 *
	 * @since 2011-02-15
	 * @author Richard J. Mathar
	 * @throws Error
	 */
	static public void main(final String args[]) throws Error {
		if (args[0].compareTo("6j") == 0) {
			try {
				final String m1 = "6";
				final String t1 = "1 2 -3 -1 5 6";
				final String t2 = "4 -5 3 -4 -2 -6";
				String j = "";
				for (int i = 1; i <= 6; i++) {
					j += args[i] + " ";
				}
				final BigSurdVec w = Wigner3j.wigner3j(m1, t1, t2, j);
				System.out.println(w.toString());
			} catch (final Exception e) {
				System.out.println(e.getMessage());
			}
		} else if (args[0].compareTo("9j") == 0) {
			try {
				final String m1 = "9";
				final String t1 = "1 3 2 4 6 5 7 9 8";
				final String t2 = "2 8 5 6 3 9 7 4 1";
				String j = "";
				for (int i = 1; i <= 9; i++) {
					j += args[i] + " ";
				}
				final BigSurdVec w = Wigner3j.wigner3j(m1, t1, t2, j);
				System.out.println(w.toString());
			} catch (final Exception e) {
				System.out.println(e.getMessage());
			}
		} else if (args[0].compareTo("3jm") == 0) {
			final int j1 = new Integer(args[1]).intValue();
			final int j2 = new Integer(args[2]).intValue();
			final int j3 = new Integer(args[3]).intValue();
			final int m1 = new Integer(args[4]).intValue();
			final int m2 = new Integer(args[5]).intValue();
			final int m3 = new Integer(args[6]).intValue();
			try {
				BigSurd w = Wigner3j.wigner3jm(j1, j2, j3, m1, m2, m3);
				System.out.println(w.toString());
				w = w.multiply(new BigSurd(j3 + 1, 1));
				System.out.println("CG factor sqrt" + (j3 + 1) + "sign " + (j2 - j2 - m3) / 2 + " " + w.toString());
			} catch (final Exception e) {
				System.out.println(e.getMessage());
			}
		} else {
			System.out.println("usage:");
			System.out.println(args[0] + " 6j 2j1+1 2j2+1 2j3+1 2j4+1 2j5+1 2j6+1");
			System.out.println(args[0] + " 9j 2j1+1 2j2+1 2j3+1 2j4+1 2j5+1 2j6+1.. 2j9+1 ");
			System.out.println(args[0] + " 3jm 2j1+1 2j2+1 2j3+1 2m1+1 2m2+1 2m3+1 ");
		}
	} /* Wigner3j.main */

	/**
	 * The Wigner 3jm symbol (j1,j2,j3,m1,m2,m3). All arguments of the function
	 * are the actual parameters multiplied by 2, so they all allow an integer
	 * representation.
	 *
	 * @param j1
	 *            integer representing 2*j1
	 * @param j2
	 *            integer representing 2*j2
	 * @param j3
	 *            integer representing 2*j3
	 * @param m1
	 *            integer representing 2*m1
	 * @param m2
	 *            integer representing 2*m2
	 * @param m3
	 *            integer representing 2*m3
	 * @return The value of the symbol. Zero if any of the triangular
	 *         inequalities is violated or some parameters are out of range.
	 * @since 2011-02-13
	 * @author Richard J. Mathar
	 * @throws Error
	 */
	static public BigSurd wigner3jm(final int j1, final int j2, final int j3, final int m1, final int m2, final int m3)
			throws Error {
		final Rational J1 = new Rational(j1, 2);
		final Rational J2 = new Rational(j2, 2);
		final Rational J3 = new Rational(j3, 2);
		final Rational M1 = new Rational(m1, 2);
		final Rational M2 = new Rational(m2, 2);
		final Rational M3 = new Rational(m3, 2);
		return Wigner3j.wigner3jm(J1, J2, J3, M1, M2, M3);
	} /* wigner3jm */

	/**
	 * Wigner 3jn symbol. For the 6j symbol, the input of the 3 lines is
	 * "1 2 3 1 5 6", "4 5 3 4 2 6" "2j1+1 2j2+1 2j3+1 2l1+1 2l2+1 2l3+1"
	 *
	 * @param m1
	 *            The information on the number of angular momenta.
	 * @param t1
	 *            The list of one half of the triads, indexing j, whitespace
	 *            separated
	 * @param t2
	 *            The list of the second half of the triads, indexing j,
	 *            whitespace separated
	 * @param j
	 *            The list of the integer values of the angular momenta. They
	 *            are actually the doubled j-values plus 1, whitespace
	 *            separated. Only as many as announced by the m1 parameter are
	 *            used; trailing numbers are ignored.
	 * @see A. Bar-Shalom and M. Klapisch,
	 *      <a href="https://doi.org/10.1016/0010-4655(88)90192-0">NJGRAF...
	 *      </a>, Comp. Phys Comm. 50 (3) (1988) 375
	 * @since 2011-02-13
	 * @since 2012-02-15 Upgraded return value to BigSurdVec
	 * @author Richard J. Mathar
	 * @throws Error
	 */
	static public BigSurdVec wigner3j(final String m1, final String t1, final String t2, final String j) throws Error {
		/*
		 * The first number in the line "m" is the number of angular momenta.
		 * The rest of the line is ignored.
		 */
		Scanner s = new Scanner(m1);
		final int m = s.nextInt();
		if (m % 3 != 0) {
			s.close();
			throw new IllegalArgumentException("Angular momenta " + m + " not a multiple of three.");
		}

		/*
		 * Scan the numbers in the line "j". Excess numbers beyond what has been
		 * announced in the "m" line are ignored.
		 */
		final int[] jvec = new int[m];
		final int[] tvec = new int[2 * m];

		s.close();

		/*
		 * the third row contains positive 2j+1.
		 */
		s = new Scanner(j);
		int ji = 0;
		while (s.hasNextInt() && ji < m) {
			jvec[ji++] = s.nextInt();
			if (jvec[ji - 1] < 1) {
				s.close();
				throw new IllegalArgumentException("Illegal value " + jvec[ji - 1] + " for 2j+1.");
			}
		}

		s.close();

		/*
		 * the first two rows contain signed values of indices into the j list
		 */
		s = new Scanner(t1);
		int ti = 0;
		while (s.hasNextInt()) {
			tvec[ti++] = s.nextInt();
		}

		s.close();

		s = new Scanner(t2);
		while (s.hasNextInt()) {
			tvec[ti++] = s.nextInt();
		}

		/*
		 * Basic sanity checks. All indices in the first two lines address a
		 * number in the third line, and each index occurs exactly twice.
		 */
		if (ji % 3 != 0) {
			s.close();
			throw new IllegalArgumentException("j-count " + ji + " not a multiple of three.");
		}
		if (ti != 2 * ji) {
			s.close();
			throw new IllegalArgumentException("triad-count " + ti + " not twice j-count " + ji);
		}

		final int[] jfreq = new int[m];
		for (ji = 0; ji < jfreq.length; ji++) {
			jfreq[ji] = 0;
		}

		/*
		 * maintain a 0-based index which shows where the j-value has its first
		 * and second occurrence in the flattened list of triads.
		 */
		final int[][] jhash = new int[m][2];

		for (ti = 0; ti < 2 * m; ti++) {
			final int t = tvec[ti];
			if (t == 0 || Math.abs(t) > jvec.length) {
				s.close();
				throw new IllegalArgumentException("Triad index " + t + " out of bounds");
			}
			if (jfreq[Math.abs(t) - 1] >= 2) {
				s.close();
				throw new IllegalArgumentException("Node " + t + " referenced more than twice");
			}
			jhash[Math.abs(t) - 1][jfreq[Math.abs(t) - 1]] = ti;
			jfreq[Math.abs(t) - 1]++;
		}

		/*
		 * Move on from the 2j+1 values of the input to the j-values. Subtract
		 * one and divide through 2.
		 */
		final Rational[] J = new Rational[jvec.length];
		for (ji = 0; ji < jvec.length; ji++) {
			J[ji] = new Rational(jvec[ji] - 1, 2);
		}

		/*
		 * Convert the 1-based indices to 0-based indices, loosing the sign
		 * information.
		 */
		final int[] triadidx = new int[tvec.length];
		for (ti = 0; ti < tvec.length; ti++) {
			triadidx[ti] = Math.abs(tvec[ti]) - 1;
		}

		/*
		 * The M-values are all null (undetermined) at the start.
		 */
		final Rational[] M = new Rational[J.length];
		s.close();
		return Wigner3j.wigner3j(tvec, J, M, triadidx);
	} /* wigner3j */

	/**
	 * Wigner 3jn symbol. Computes sum_{mi} (-1)^(j1-m1+j2-m2+...)
	 * triad(triadidx[0..2])*triad(triadidx[3..5])*... where each factor is a
	 * Wigner-3jm symbol with each sign of m_i occurring once at the
	 * corresponding l-value.
	 *
	 * @param triadidx
	 *            0-based indices into the list of J
	 * @param J
	 *            The list of J-values
	 * @param M
	 *            The list of M-values associated with the J. This contains null
	 *            where the parameter has not yet been set by an outer loop.
	 * @since 2011-02-13
	 * @since 2012-02-15 Upgraded to return BigSurdVec
	 * @author Richard J. Mathar
	 * @throws Error
	 */
	static private BigSurdVec wigner3j(final int[] tvec, final Rational[] J, final Rational[] M, final int[] triadidx)
			throws Error {
		/*
		 * The result of the computation. The sum over all m-combinations of the
		 * triads.
		 */
		BigSurdVec res = new BigSurdVec();

		/*
		 * First step is to monitor the triangular conditions on the J. If at
		 * least one is violated, the result is zero. Loop over the triads.
		 */
		for (int t = 0; t < triadidx.length; t += 3) {
			/* Ensure |J[t]-J[t+1]| <= J[t+2] <= J[t]+J[t+1] */
			if (J[triadidx[t]].subtract(J[triadidx[t + 1]]).abs().compareTo(J[triadidx[t + 2]]) > 0) {
				return res;
			}
			if (J[triadidx[t]].add(J[triadidx[t + 1]]).compareTo(J[triadidx[t + 2]]) < 0) {
				return res;
			}
		}

		/*
		 * the index of the preferred member of the triad list. Preference given
		 * to those dangling in triads where alreaday two others are fixed, then
		 * to members where at least one is fixed, then to smallest associated
		 * J-values.
		 */
		int freeM = -1;
		int freeMrank = -1;
		for (int i = 0; i < triadidx.length; i++) {
			/*
			 * found an m-value which has not yet been summed over.
			 */
			if (M[triadidx[i]] == null) {
				/*
				 * two cases: value is fixed implicitly because already two
				 * others values are set in the triad. or it is still to
				 * maintain its own explicit loop.
				 */
				final int triadn = i / 3;
				final int triadr = i % 3;
				/*
				 * the neighbors in the triad have indices triadn*3+ (tiradr+1)
				 * mod 3 and triadn*3+(triadr+2) mod3
				 */
				final int nei1 = 3 * triadn + (triadr + 1) % 3;
				final int nei2 = 3 * triadn + (triadr + 2) % 3;

				/*
				 * found a candidate for which the two other values are already
				 * set.
				 */
				if (M[triadidx[nei1]] != null && M[triadidx[nei2]] != null) {
					freeM = i;
					break;
				} else {
					/*
					 * rough work load estimator: basically (2J1+1)*(2J2+1)
					 */
					Rational wt = J[triadidx[i]].multiply(2).add(1);
					if (M[triadidx[nei1]] == null) {
						wt = wt.multiply(J[triadidx[nei1]].multiply(2).add(1));
					}
					if (M[triadidx[nei2]] == null) {
						wt = wt.multiply(J[triadidx[nei2]].multiply(2).add(1));
					}
					final int thiswt = wt.intValue();
					if (freeM < 0 || thiswt < freeMrank) {
						freeM = i;
						freeMrank = thiswt;
					}
				}
			}
		}

		if (freeM >= 0) {
			/*
			 * found an m-value which has not yet been summed over.
			 */
			if (M[triadidx[freeM]] == null) {
				final Rational[] childM = new Rational[M.length];
				for (int ji = 0; ji < M.length; ji++) {
					if (M[ji] != null) {
						childM[ji] = M[ji];
					}
				}

				/*
				 * two cases: value is fixed implicitly because already two
				 * others values are set in the triad. or it is still to
				 * maintain its own explicit loop.
				 */
				final int triadn = freeM / 3;
				final int triadr = freeM % 3;
				/*
				 * the neighbors in the triad have indices triadn*3+ (triadr+1)
				 * mod 3 and triadn*3+(triadr+2) mod3
				 */
				final int nei1 = 3 * triadn + (triadr + 1) % 3;
				final int nei2 = 3 * triadn + (triadr + 2) % 3;
				if (M[triadidx[nei1]] == null || M[triadidx[nei2]] == null) {
					/*
					 * The J-value is J[triadidx[freeM]]. Loop from -J to +J,
					 * the allowed range.
					 */
					Rational newm = J[triadidx[freeM]].negate();
					while (newm.compareTo(J[triadidx[freeM]]) <= 0) {
						childM[triadidx[freeM]] = tvec[freeM] > 0 ? newm : newm.negate();
						res = res.add(Wigner3j.wigner3j(tvec, J, childM, triadidx));
						newm = newm.add(Rational.ONE);
					}
				} else {
					/*
					 * Set its value and the value at its companion j-value. Sum
					 * of the three m-values in the triad is to be zero for a
					 * non-zero contribution.
					 */
					Rational m1 = M[triadidx[nei1]];
					Rational m2 = M[triadidx[nei2]];
					/*
					 * negate if these are the second occurrences of the J in
					 * the triads
					 */
					if (tvec[nei1] < 0) {
						m1 = m1.negate();
					}
					if (tvec[nei2] < 0) {
						m2 = m2.negate();
					}
					/* m3 = -(m1+m2) */
					final Rational newm = tvec[freeM] > 0 ? m1.add(m2).negate() : m1.add(m2);
					/*
					 * No contribution if the m-value enforced by the other two
					 * entries is outside the range -|J|..|J| enforced by its
					 * associated J-value. One could essentially remove this
					 * branching and let wigner3j() decide on this, but this is
					 * inefficient.
					 */
					if (newm.abs().compareTo(J[triadidx[freeM]]) <= 0) {
						childM[triadidx[freeM]] = newm;
						res = res.add(Wigner3j.wigner3j(tvec, J, childM, triadidx));
					}
					/*
					 * zero contribution if this m-value cannot be set to any
					 * value compatible with the triangular conditions.
					 */
				}
				return res;
			}
		}

		/*
		 * reached the bottom of the loop where all M-values are assigned. Build
		 * the product over all Wigner-3jm values and the associated sign.
		 */
		res = BigSurdVec.ONE;
		for (int ji = 0; ji < triadidx.length; ji += 3) {
			Rational m1 = M[triadidx[ji]];
			Rational m2 = M[triadidx[ji + 1]];
			Rational m3 = M[triadidx[ji + 2]];
			/*
			 * negate if these are associated with in-flowing vectors in the
			 * triads
			 */
			if (tvec[ji] < 0) {
				m1 = m1.negate();
			}
			if (tvec[ji + 1] < 0) {
				m2 = m2.negate();
			}
			if (tvec[ji + 2] < 0) {
				m3 = m3.negate();
			}
			res = res.multiply(Wigner3j.wigner3jm(J[triadidx[ji]], J[triadidx[ji + 1]], J[triadidx[ji + 2]], m1, m2, m3));

			/*
			 * if a partial product yields zero, the total product is zero, too,
			 * and offers an early exit.
			 */
			if (res.signum() == 0) {
				return BigSurdVec.ZERO;
			}
		}
		/*
		 * The overal sign is product_{J-Mpairs} (-1)^(J-M). This is an integer
		 * because all the J-M are integer.
		 */
		Rational sig = new Rational();
		for (int ji = 0; ji < J.length; ji++) {
			sig = sig.add(J[ji]).subtract(M[ji]);
		}
		/*
		 * sign depends on the sum being even or odd. We assume that "sig" is
		 * integer and look only at the numerator
		 */
		if (sig.a.abs().testBit(0)) {
			res = res.negate();
		}
		return res;
	} /* wigner3j */

	/**
	 * The Wigner 3jm symbol (j1,j2,j3,m1,m2,m3). Warning: there is no check
	 * that each argument is indeed half-integer.
	 *
	 * @param j1
	 *            integer or half-integer j1
	 * @param j2
	 *            integer or half-integer j2
	 * @param j3
	 *            integer or half-integer j3
	 * @param m1
	 *            integer or half-integer m1
	 * @param m2
	 *            integer or half-integer m2
	 * @param m3
	 *            integer or half-integer m3
	 * @return The value of the symbol. Zero if any of the triangular
	 *         inequalities is violated or some parameters are out of range.
	 * @since 2011-02-13
	 * @author Richard J. Mathar
	 * @throws Error
	 */
	static protected BigSurd wigner3jm(final Rational j1, final Rational j2, final Rational j3, final Rational m1,
			final Rational m2, final Rational m3) throws Error {
		/*
		 * Check that m1+m2+m3 = 0
		 */
		if (m1.add(m2).add(m3).signum() != 0) {
			return BigSurd.ZERO;
		}

		/*
		 * Check that j1+j2+j3 is integer
		 */
		if (j1.add(j2).add(j3).isBigInteger() == false) {
			return BigSurd.ZERO;
		}

		/*
		 * Check that |j1-j2|<=j3 <= |j1+j2|
		 */
		final Rational j1m2 = j1.subtract(j2);
		if (j1m2.abs().compareTo(j3) > 0) {
			return BigSurd.ZERO;
		}
		final Rational j1p2 = j1.add(j2);
		if (j1p2.abs().compareTo(j3) < 0) {
			return BigSurd.ZERO;
		}

		/*
		 * Check that |m_i| <= j_i
		 */
		if (m1.abs().compareTo(j1) > 0 || m2.abs().compareTo(j2) > 0 || m3.abs().compareTo(j3) > 0) {
			return BigSurd.ZERO;
		}

		/*
		 * Check that m_i-j_i are integer.
		 */
		if (!m1.subtract(j1).isBigInteger() || !m2.subtract(j2).isBigInteger() || !m3.subtract(j3).isBigInteger()) {
			return BigSurd.ZERO;
		}

		/*
		 * (-)^(j1-j2-m3)*delta(-m3,m1+m2)*sqrt[ (j3+j1-j2)! (j3-j1+j2)!
		 * (j1+j2-j3)! /(j1+j2+j3+1)!
		 * *(j3-m)!*(j3+m)!(j1-m1)!*(j1+m1)!*(j2-m2)!*(j2+m2)!] *sum_k
		 * (-1)^k/[k!(j1+j2-j3-k)!(j1-m1-k)!(j2+m2-k)!(j3-j2+m1+k)!)*(j3-j1-m2+k
		 * )!]
		 */

		/*
		 * It is tacitly assumed that all the major j_i, m_i values are in the
		 * integer range. This is implicitly plausible since otherwise the
		 * execution times of the following loop over the k-values would be
		 * immense.
		 */
		int j1j2jk = j1p2.subtract(j3).intValue();
		int j1m1k = j1.subtract(m1).intValue();
		int j2m2k = j2.add(m2).intValue();
		int jj2m1k = j3.subtract(j2).add(m1).intValue();
		int jj1m2k = j3.subtract(j1).subtract(m2).intValue();

		int k = Math.max(0, -jj2m1k);
		k = Math.max(k, -jj1m2k);
		if (k > 0) {
			j1j2jk -= k;
			j1m1k -= k;
			j2m2k -= k;
			jj2m1k += k;
			jj1m2k += k;
		}

		final Factorial f = new Factorial();
		Rational sumk = new Rational();
		while (true) {
			final BigInteger d = f.at(k).multiply(f.at(j1j2jk)).multiply(f.at(j1m1k)).multiply(f.at(j2m2k)).multiply(f.at(jj2m1k)).multiply(f.at(jj1m2k));
			if (k % 2 == 0) {
				sumk = sumk.add(new Rational(BigInteger.ONE, d));
			} else {
				sumk = sumk.subtract(new Rational(BigInteger.ONE, d));
			}
			j1j2jk--;
			j1m1k--;
			j2m2k--;
			jj2m1k++;
			jj1m2k++;
			if (j1j2jk < 0 || j1m1k < 0 || j2m2k < 0) {
				break;
			}
			k++;
		}
		/*
		 * sign factor (-1)^(j1-j2-m3)
		 */
		if (j1m2.subtract(m3).intValue() % 2 != 0) {
			sumk = sumk.negate();
		}

		k = j1m2.add(j3).intValue();
		BigInteger s = f.at(k);
		k = j3.subtract(j1m2).intValue();
		s = s.multiply(f.at(k));
		k = j1p2.subtract(j3).intValue();
		s = s.multiply(f.at(k));
		k = j3.add(m3).intValue();
		s = s.multiply(f.at(k));
		k = j3.subtract(m3).intValue();
		s = s.multiply(f.at(k));
		k = j1.add(m1).intValue();
		s = s.multiply(f.at(k));
		k = j1.subtract(m1).intValue();
		s = s.multiply(f.at(k));
		k = j2.add(m2).intValue();
		s = s.multiply(f.at(k));
		k = j2.subtract(m2).intValue();
		s = s.multiply(f.at(k));
		k = j1p2.add(j3).intValue();
		k++;
		final Rational disc = new Rational(s, f.at(k));
		return new BigSurd(sumk, disc);
	} /* wigner3jm */

} /* Wigner3j */
