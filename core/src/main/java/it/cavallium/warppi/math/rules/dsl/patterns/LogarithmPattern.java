package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Logarithm;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternUtils;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Matches and generates a logarithm of base and argument patterns.
 */
public class LogarithmPattern extends VisitorPattern {
	private final Pattern base;
	private final Pattern argument;

	public LogarithmPattern(final Pattern base, final Pattern argument) {
		this.base = base;
		this.argument = argument;
	}

	@Override
	public Optional<Map<String, Function>> visit(final Logarithm logarithm) {
		return PatternUtils.matchFunctionOperatorParameters(logarithm, base, argument);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Logarithm(
				mathContext,
				base.replace(mathContext, subFunctions),
				argument.replace(mathContext, subFunctions)
		);
	}

	@Override
	public Set<SubFunctionPattern> getSubFunctions() {
		return PatternUtils.getSubFunctionsFrom(base, argument);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof LogarithmPattern)) {
			return false;
		}
		final LogarithmPattern other = (LogarithmPattern) o;
		return base.equals(other.base) && argument.equals(other.argument);
	}

	@Override
	public int hashCode() {
		return Objects.hash(base, argument);
	}
}