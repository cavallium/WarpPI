package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternUtils;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Matches and generates a division of two other patterns.
 */
public class DivisionPattern extends VisitorPattern {
	private final Pattern dividend;
	private final Pattern divisor;

	public DivisionPattern(final Pattern dividend, final Pattern divisor) {
		this.dividend = dividend;
		this.divisor = divisor;
	}

	@Override
	public Boolean visit(final Division division, final Map<String, Function> subFunctions) {
		return dividend.match(division.getParameter1(), subFunctions)
			&& divisor.match(division.getParameter2(), subFunctions);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Division(
			mathContext,
			dividend.replace(mathContext, subFunctions),
			divisor.replace(mathContext, subFunctions)
		);
	}

	@Override
	public Set<SubFunctionPattern> getSubFunctions() {
		return PatternUtils.getSubFunctionsFrom(dividend, divisor);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof DivisionPattern)) {
			return false;
		}
		final DivisionPattern other = (DivisionPattern) o;
		return dividend.equals(other.dividend) && divisor.equals(other.divisor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dividend, divisor);
	}
}
