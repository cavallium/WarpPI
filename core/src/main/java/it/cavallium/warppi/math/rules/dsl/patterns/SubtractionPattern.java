package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Matches and generates a subtraction of two other patterns.
 */
public class SubtractionPattern extends VisitorPattern {
	private final Pattern left;
	private final Pattern right;

	public SubtractionPattern(final Pattern left, final Pattern right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public Boolean visit(final Subtraction subtraction, final Map<String, Function> subFunctions) {
		return left.match(subtraction.getParameter1(), subFunctions)
			&& right.match(subtraction.getParameter2(), subFunctions);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Subtraction(
			mathContext,
			left.replace(mathContext, subFunctions),
			right.replace(mathContext, subFunctions)
		);
	}

	@Override
	public Stream<SubFunctionPattern> getSubFunctions() {
		return Stream.of(left, right)
			.flatMap(Pattern::getSubFunctions);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof SubtractionPattern)) {
			return false;
		}
		final SubtractionPattern other = (SubtractionPattern) o;
		return left.equals(other.left) && right.equals(other.right);
	}

	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}
}
