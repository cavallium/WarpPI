package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternUtils;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Matches and generates a multiplication of two other patterns.
 */
public class MultiplicationPattern extends VisitorPattern {
	private final Pattern left;
	private final Pattern right;

	public MultiplicationPattern(final Pattern left, final Pattern right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public Boolean visit(final Multiplication multiplication, final Map<String, Function> subFunctions) {
		return left.match(multiplication.getParameter1(), subFunctions)
			&& right.match(multiplication.getParameter2(), subFunctions);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Multiplication(
			mathContext,
			left.replace(mathContext, subFunctions),
			right.replace(mathContext, subFunctions)
		);
	}

	@Override
	public Set<SubFunctionPattern> getSubFunctions() {
		return PatternUtils.getSubFunctionsFrom(left, right);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof MultiplicationPattern)) {
			return false;
		}
		final MultiplicationPattern other = (MultiplicationPattern) o;
		return left.equals(other.left) && right.equals(other.right);
	}

	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}
}
