package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Logarithm;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

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
	public Boolean visit(final Logarithm logarithm, final Map<String, Function> subFunctions) {
		return base.match(logarithm.getParameter1(), subFunctions)
			&& argument.match(logarithm.getParameter2(), subFunctions);
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
	public Stream<SubFunctionPattern> getSubFunctions() {
		return Stream.of(base, argument)
			.flatMap(Pattern::getSubFunctions);
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
