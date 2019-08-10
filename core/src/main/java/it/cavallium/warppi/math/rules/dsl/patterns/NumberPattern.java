package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Matches and generates a specific number.
 */
public class NumberPattern extends VisitorPattern {
	private final BigDecimal value;

	public NumberPattern(final BigDecimal value) {
		this.value = value;
	}

	@Override
	public Boolean visit(final Number number, final Map<String, Function> subFunctions) {
		return number.getTerm().compareTo(value) == 0;
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Number(mathContext, value);
	}

	@Override
	public Set<SubFunctionPattern> getSubFunctions() {
		return Collections.emptySet();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof NumberPattern)) {
			return false;
		}
		final NumberPattern other = (NumberPattern) o;
		return value.compareTo(other.value) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}
}
