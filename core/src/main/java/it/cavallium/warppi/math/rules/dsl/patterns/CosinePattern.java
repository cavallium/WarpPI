package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.trigonometry.Cosine;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates the cosine of another pattern.
 */
public class CosinePattern extends VisitorPattern {
	private final Pattern argument;

	public CosinePattern(final Pattern argument) {
		this.argument = argument;
	}

	@Override
	public Optional<Map<String, Function>> visit(final Cosine cosine) {
		return argument.match(cosine.getParameter());
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Cosine(
				mathContext,
				argument.replace(mathContext, subFunctions)
		);
	}
}
