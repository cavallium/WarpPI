package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.equations.EquationsSystem;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternUtils;
import it.cavallium.warppi.math.rules.dsl.VisitorPattern;

import java.util.*;

/**
 * Matches and generates a system of equations of multiple other patterns.
 */
public class EquationsSystemPattern extends VisitorPattern {
	private final Pattern[] patterns;

	public EquationsSystemPattern(final Pattern[] patterns) {
		this.patterns = patterns;
	}

	@Override
	public Optional<Map<String, Function>> visit(final EquationsSystem equationsSystem) {
		if (patterns.length != equationsSystem.getParametersLength()) {
			return Optional.empty();
		}

		Optional<Map<String, Function>> subFunctions = Optional.of(Collections.emptyMap());
		for (int i = 0; i < patterns.length && subFunctions.isPresent(); i++) {
			final Pattern curPattern = patterns[i];
			final Function curFunction = equationsSystem.getParameter(i);
			subFunctions = subFunctions
					.flatMap(prevMatch -> curPattern.match(curFunction)
							.flatMap(curMatch -> PatternUtils.mergeMatches(prevMatch, curMatch))
					);
		}
		return subFunctions;
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		final Function[] functions = Arrays.stream(patterns)
				.map(pattern -> pattern.replace(mathContext, subFunctions))
				.toArray(Function[]::new);
		return new EquationsSystem(mathContext, functions);
	}

	@Override
	public Set<SubFunctionPattern> getSubFunctions() {
		return PatternUtils.getSubFunctionsFrom(patterns);
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
