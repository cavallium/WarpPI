package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.equations.EquationsSystem;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Matches and generates a system of equations of multiple other patterns.
 */
public class EquationsSystemPattern extends VisitorPattern {
	private final Pattern[] patterns;

	public EquationsSystemPattern(final Pattern[] patterns) {
		this.patterns = patterns;
	}

	@Override
	public Boolean visit(final EquationsSystem equationsSystem, final Map<String, Function> subFunctions) {
		if (patterns.length != equationsSystem.getParametersLength()) {
			return false;
		}

		for (int i = 0; i < patterns.length; i++) {
			final Pattern curPattern = patterns[i];
			final Function curFunction = equationsSystem.getParameter(i);
			if (!curPattern.match(curFunction, subFunctions)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		final Function[] functions = Arrays.stream(patterns)
			.map(pattern -> pattern.replace(mathContext, subFunctions))
			.toArray(Function[]::new);
		return new EquationsSystem(mathContext, functions);
	}

	@Override
	public Stream<SubFunctionPattern> getSubFunctions() {
		return Stream.of(patterns)
			.flatMap(Pattern::getSubFunctions);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof EquationsSystemPattern)) {
			return false;
		}
		final EquationsSystemPattern other = (EquationsSystemPattern) o;
		return Arrays.equals(patterns, other.patterns);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(patterns);
	}
}
