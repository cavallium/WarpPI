package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Negative;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Optional;

/**
 * Matches and generates the negative of another pattern.
 */
public class NegativePattern extends VisitorPattern {
	private final Pattern inner;

	public NegativePattern(final Pattern inner) {
		this.inner = inner;
	}

	@Override
	public Optional<Map<String, Function>> visit(final Negative negative) {
		return inner.match(negative.getParameter());
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Negative(
				mathContext,
				inner.replace(mathContext, subFunctions)
		);
	}
}
