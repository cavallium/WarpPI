package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Negative;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Matches and generates the negative of another pattern.
 */
public class NegativePattern extends VisitorPattern {
	private final Pattern inner;

	public NegativePattern(final Pattern inner) {
		this.inner = inner;
	}

	@Override
	public Boolean visit(final Negative negative, final Map<String, Function> subFunctions) {
		return inner.match(negative.getParameter(), subFunctions);
	}

	@Override
	public Boolean visit(final Number number, final Map<String, Function> subFunctions) {
		final BigDecimal value = number.getTerm();
		if (value.signum() >= 0) {
			return false;
		}
		final Number absoluteValue = new Number(number.getMathContext(), value.abs());
		return inner.match(absoluteValue, subFunctions);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		final Function newInner = inner.replace(mathContext, subFunctions);

		if (newInner instanceof Number) {
			return ((Number) newInner).multiply(new Number(mathContext, -1));
		} else {
			return new Negative(
				mathContext,
				inner.replace(mathContext, subFunctions)
			);
		}
	}

	@Override
	public Stream<SubFunctionPattern> getSubFunctions() {
		return inner.getSubFunctions();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof NegativePattern)) {
			return false;
		}
		final NegativePattern other = (NegativePattern) o;
		return inner.equals(other.inner);
	}

	@Override
	public int hashCode() {
		return Objects.hash(inner);
	}
}
