package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.trigonometry.Sine;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates the sine of another pattern.
 */
public class SinePattern extends VisitorPattern {
	private final Pattern argument;

	public SinePattern(final Pattern argument) {
		this.argument = argument;
	}

	@Override
	public Optional<Map<String, Function>> visit(final Sine sine) {
		return argument.match(sine.getParameter());
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Sine(
				mathContext,
				argument.replace(mathContext, subFunctions)
		);
	}
}
