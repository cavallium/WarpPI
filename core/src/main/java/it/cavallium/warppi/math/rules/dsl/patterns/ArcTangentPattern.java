package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.trigonometry.ArcTangent;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Matches and generates the arctangent of another pattern.
 */
public class ArcTangentPattern extends VisitorPattern {
	private final Pattern argument;

	public ArcTangentPattern(final Pattern argument) {
		this.argument = argument;
	}

	@Override
	public Boolean visit(final ArcTangent arcTangent, final Map<String, Function> subFunctions) {
		return argument.match(arcTangent.getParameter(), subFunctions);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new ArcTangent(
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
		if (!(o instanceof ArcTangentPattern)) {
			return false;
		}
		final ArcTangentPattern other = (ArcTangentPattern) o;
		return argument.equals(other.argument);
	}

	@Override
	public int hashCode() {
		return Objects.hash(argument);
	}
}
