package it.cavallium.warppi.math.functions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.gui.expression.blocks.BlockNumericChar;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/** Decimal degrees rendered as degrees, minutes, and seconds. */
public final class Sexagesimal implements Function {
	private final MathContext context;
	private final boolean negative;
	private final BigInteger degrees;
	private final BigInteger minutes;
	private final BigDecimal seconds;

	public Sexagesimal(final MathContext context, final boolean negative, final BigInteger degrees,
			final BigInteger minutes, final BigDecimal seconds) {
		this.context = Objects.requireNonNull(context, "context");
		this.negative = negative;
		this.degrees = degrees;
		this.minutes = minutes;
		this.seconds = seconds;
	}

	@Override public Sexagesimal clone() { return clone(context); }
	@Override public Sexagesimal clone(final MathContext newContext) {
		return new Sexagesimal(newContext, negative, degrees, minutes, seconds);
	}
	@Override public MathContext getMathContext() { return context; }
	@Override public Function setParameter(final int index, final Function value) { throw new IndexOutOfBoundsException(); }
	@Override public Function getParameter(final int index) { throw new IndexOutOfBoundsException(); }
	@Override public ObjectArrayList<Function> simplify(final Rule rule) throws Error, InterruptedException {
		return rule.execute(this);
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext outputContext) throws Error {
		if (outputContext.losslessInputMode) {
			BigDecimal decimalDegrees = new BigDecimal(degrees)
					.add(new BigDecimal(minutes).divide(BigDecimal.valueOf(60), Utils.scale,
							Utils.scaleMode2))
					.add(seconds.divide(BigDecimal.valueOf(3600), Utils.scale, Utils.scaleMode2));
			if (negative)
				decimalDegrees = decimalDegrees.negate();
			return new Number(outputContext, decimalDegrees).toBlock(outputContext);
		}
		final ObjectArrayList<Block> blocks = new ObjectArrayList<>();
		appendNumber(blocks, (negative ? "-" : "") + degrees);
		blocks.add(new BlockChar('°'));
		appendNumber(blocks, minutes.toString());
		blocks.add(new BlockChar('′'));
		appendNumber(blocks, plain(seconds));
		blocks.add(new BlockChar('″'));
		return blocks;
	}

	private static void appendNumber(final ObjectArrayList<Block> blocks, final String value) {
		for (final char character : value.toCharArray())
			blocks.add(new BlockNumericChar(character));
	}

	private static String plain(final BigDecimal value) {
		final BigDecimal stripped = value.stripTrailingZeros();
		return stripped.scale() < 0 ? stripped.setScale(0).toPlainString() : stripped.toPlainString();
	}

	@Override public boolean equals(final Object other) {
		return other instanceof Sexagesimal value && negative == value.negative
				&& degrees.equals(value.degrees) && minutes.equals(value.minutes)
				&& seconds.compareTo(value.seconds) == 0;
	}
	@Override public int hashCode() { return Objects.hash(negative, degrees, minutes, seconds.stripTrailingZeros()); }
	@Override public String toString() { return (negative ? "-" : "") + degrees + "°" + minutes + "′" + plain(seconds) + "″"; }
	@Override public <Argument, Result> Result accept(final Function.Visitor<Argument, Result> visitor,
			final Argument argument) { return visitor.visit(this, argument); }
}
