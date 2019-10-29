package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.trigonometry.Tangent;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Matches and generates the tangent of another pattern.
 */
public class TangentPattern extends VisitorPattern {
	private final Pattern argument;

	public TangentPattern(final Pattern argument) {
		this.argument = argument;
	}

	@Override
	public Boolean visit(final Tangent tangent, final Map<String, Function> subFunctions) {
		return argument.match(tangent.getParameter(), subFunctions);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return new Tangent(
			mathContext,
			argument.replace(mathContext, subFunctions)
		);
	}

	@Override
	public Stream<SubFunctionPattern> getSubFunctions() {
		return argument.getSubFunctions();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof TangentPattern)) {
			return false;
		}
		final TangentPattern other = (TangentPattern) o;
		return argument.equals(other.argument);
	}

	@Override
	public int hashCode() {
		return Objects.hash(argument);
	}
}
