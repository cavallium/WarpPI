package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.trigonometry.Cosine;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Matches and generates the cosine of another pattern.
 */
public class CosinePattern extends VisitorPattern {
	private final Pattern argument;

	public CosinePattern(final Pattern argument) {
		this.argument = argument;
	}

	@Override
	public Boolean visit(final Cosine cosine, final Map<String, Function> subFunctions) {
		return argument.match(cosine.getParameter(), subFunctions);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Cosine(
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
		if (!(o instanceof CosinePattern)) {
			return false;
		}
		final CosinePattern other = (CosinePattern) o;
		return argument.equals(other.argument);
	}

	@Override
	public int hashCode() {
		return Objects.hash(argument);
	}
}
