package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.trigonometry.Sine;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Matches and generates the sine of another pattern.
 */
public class SinePattern extends VisitorPattern {
	private final Pattern argument;

	public SinePattern(final Pattern argument) {
		this.argument = argument;
	}

	@Override
	public Boolean visit(final Sine sine, final Map<String, Function> subFunctions) {
		return argument.match(sine.getParameter(), subFunctions);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Sine(
			mathContext,
			argument.replace(mathContext, subFunctions)
		);
	}

	@Override
	public Set<SubFunctionPattern> getSubFunctions() {
		return argument.getSubFunctions();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof SinePattern)) {
			return false;
		}
		final SinePattern other = (SinePattern) o;
		return argument.equals(other.argument);
	}

	@Override
	public int hashCode() {
		return Objects.hash(argument);
	}
}
