package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.SumSubtraction;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternUtils;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Matches and generates a sum/subtraction (±) of two other patterns.
 */
public class SumSubtractionPattern extends VisitorPattern {
	private final Pattern left;
	private final Pattern right;

	public SumSubtractionPattern(final Pattern left, final Pattern right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public Optional<Map<String, Function>> visit(final SumSubtraction sumSubtraction) {
		return PatternUtils.matchFunctionOperatorParameters(sumSubtraction, left, right);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new SumSubtraction(
				mathContext,
				left.replace(mathContext, subFunctions),
				right.replace(mathContext, subFunctions)
		);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof SumSubtractionPattern)) {
			return false;
		}
		final SumSubtractionPattern other = (SumSubtractionPattern) o;
		return left.equals(other.left) && right.equals(other.right);
	}

	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}
}
