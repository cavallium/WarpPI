package it.cavallium.warppi.math.functions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;

import org.nevec.rjm.BigDecimalMath;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockExponentialNotation;
import it.cavallium.warppi.gui.expression.blocks.BlockPower;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Number implements Function {

	private final MathContext root;
	protected BigDecimal term;

	public Number(final MathContext root, final BigInteger val) {
		this.root = root;
		term = new BigDecimal(val).setScale(Utils.scale, Utils.scaleMode2);
	}

	public Number(final MathContext root, final BigDecimal val) {
		this.root = root;
		term = val.setScale(Utils.scale, Utils.scaleMode2);
	}

	public Number(final MathContext root, final String s) throws Error {
		this(root, new BigDecimal(s).setScale(Utils.scale, Utils.scaleMode2));
	}

	public Number(final MathContext root, final int s) {
		this(root, BigDecimal.valueOf(s).setScale(Utils.scale, Utils.scaleMode2));
	}

	public Number(final MathContext root, final float s) {
		this(root, BigDecimal.valueOf(s).setScale(Utils.scale, Utils.scaleMode2));
	}

	public Number(final MathContext root, final double s) {
		this(root, BigDecimal.valueOf(s).setScale(Utils.scale, Utils.scaleMode2));
	}

	/**
	 * Copy
	 * @param n
	 * @param newContext
	 */
	public Number(Number old, MathContext newContext) {
		this.root = newContext;
		this.term = old.term;
	}

	public BigDecimal getTerm() {
		return term;
	}

	public void setTerm(final BigDecimal val) {
		term = val.setScale(Utils.scale, Utils.scaleMode2);
	}

	public Number add(final Number f) {
		final Number ret = new Number(root, getTerm().add(f.getTerm()));
		return ret;
	}

	public Number multiply(final Number f) {
		final Number ret = new Number(root, getTerm().multiply(f.getTerm()));
		return ret;
	}

	public Number divide(final Number f) throws Error {
		final Number ret = new Number(root, BigDecimalMath.divideRound(getTerm(), f.getTerm()));
		return ret;
	}

	public Number pow(final Number f) throws Error, InterruptedException {
		Number ret = new Number(root, BigDecimal.ONE);
		if (Utils.isIntegerValue(f.term)) {
			final BigInteger bi = f.term.toBigInteger().abs();
			for (BigInteger i = BigInteger.ZERO; i.compareTo(bi) < 0; i = i.add(BigInteger.ONE)) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				ret = ret.multiply(new Number(root, getTerm()));
			}
			if (f.term.signum() == -1) {
				ret = new Number(root, 1).divide(ret);
			}
		} else {
			ret.term = BigDecimalMath.pow(term, f.term);
		}
		return ret;
	}

	@Override
	public String toString() {
		final String sWith0 = getTerm().setScale(Utils.displayScale, Utils.scaleMode2).toPlainString();
		final String sExtendedWith0 = getTerm().toPlainString();
		//Remove trailing zeroes. Thanks to Kent, http://stackoverflow.com/questions/14984664/remove-trailing-zero-in-java
		String s = sWith0.indexOf(".") < 0 ? sWith0 : sWith0.replaceAll("0*$", "").replaceAll("\\.$", "");
		final String sExtended = sExtendedWith0.indexOf(".") < 0 ? sExtendedWith0 : sExtendedWith0.replaceAll("0*$", "").replaceAll("\\.$", "");

		if (sExtended.length() > s.length()) {
			s = s + "…";
		}

		if (root.exactMode == false) {
			final String cuttedNumber = s.split("\\.")[0];
			if (cuttedNumber.length() > 8) {
				return cuttedNumber.substring(0, 1) + "," + cuttedNumber.substring(1, 8) + "ℯ℮" + (cuttedNumber.length() - 1);
			}
		}
		return s;
	}

	@Override
	public Number clone() {
		return new Number(root, term);
	}

	@Override
	public Number clone(MathContext c) {
		return new Number(c, term);
	}

	@Override
	public ObjectArrayList<Function> simplify(final Rule rule) throws Error, InterruptedException {
		return rule.execute(this);
	}

	public int getNumberOfDecimalPlaces() {
		return Math.max(0, term.stripTrailingZeros().scale());
	}

	public boolean isInteger() {
		return getNumberOfDecimalPlaces() <= 0;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if (o != null & term != null) {
			if (o instanceof Number) {
				final BigDecimal nav = ((Number) o).getTerm();
				final boolean na1 = term.compareTo(BigDecimal.ZERO) == 0;
				final boolean na2 = nav.compareTo(BigDecimal.ZERO) == 0;
				if (na1 == na2) {
					if (na1 == true) {
						return true;
					}
				} else {
					return false;
				}
				return nav.compareTo(term) == 0;
			}
		}
		return false;
	}

	@Override
	public MathContext getMathContext() {
		return root;
	}

	/*
	 * @Override
	 * public void draw(int x, int y, Graphics g) {
	 * }
	 *
	 * @Override
	 * public int getHeight() {
	 * return Utils.getFontHeight();
	 * }
	 *
	 * @Override
	 * public int getWidth() {
	 * return 6*toString().length()-1;
	 * }
	 */

	public boolean canBeFactorized() {
		if (Utils.isIntegerValue(getTerm())) {
			return getTerm().toBigIntegerExact().compareTo(BigInteger.valueOf(1)) > 1;
		}
		return false;
	}

	/**
	 * @author programmingpraxis
	 * @return
	 */
	public LinkedList<BigInteger> getFactors() {
		BigInteger n = getTerm().toBigIntegerExact();
		final BigInteger two = BigInteger.valueOf(2);
		final BigInteger zero = BigInteger.ZERO;
		final LinkedList<BigInteger> fs = new LinkedList<>();

		final int comparedToZero = n.compareTo(zero);
		final int comparedToTwo = n.compareTo(two);
		if (comparedToZero == 0) {
			return fs;
		}
		if (comparedToTwo < 0) {
			if (comparedToZero > 0) {
				return fs;
			} else {
				fs.add(BigInteger.valueOf(-1));
				n = n.multiply(BigInteger.valueOf(-1));
			}
		}

		if (n.compareTo(two) < 0) {
			throw new IllegalArgumentException("must be greater than one");
		}

		while (n.mod(two).equals(BigInteger.ZERO)) {
			fs.add(two);
			n = n.divide(two);
		}

		if (n.compareTo(BigInteger.ONE) > 0) {
			BigInteger f = BigInteger.valueOf(3);
			while (f.compareTo(Utils.maxFactor) <= 0 && f.multiply(f).compareTo(n) <= 0) {
				if (n.mod(f).equals(BigInteger.ZERO)) {
					fs.add(f);
					n = n.divide(f);
				} else {
					f = f.add(two);
				}
			}
			fs.add(n);
		}

		return fs;
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		final String numberString = toString();
		if (numberString.contains("ℯ℮")) {
			final String[] numberParts = numberString.split("ℯ℮", 2);
			final BlockPower bp = new BlockExponentialNotation();
			final BlockContainer bpec = bp.getExponentContainer();
			for (final char c : numberParts[0].toCharArray()) {
				result.add(new BlockChar(c));
			}
			for (final char c : numberParts[1].toCharArray()) {
				bpec.appendBlockUnsafe(new BlockChar(c));
			} ;
			bpec.recomputeDimensions();
			bp.recomputeDimensions();
			result.add(bp);
			return result;
		} else {
			for (final char c : numberString.toCharArray()) {
				result.add(new BlockChar(c));
			}
		}
		return result;
	}

	@Override
	public Function setParameter(final int index, final Function var) throws IndexOutOfBoundsException {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public Function getParameter(final int index) throws IndexOutOfBoundsException {
		throw new IndexOutOfBoundsException();
	}
}
