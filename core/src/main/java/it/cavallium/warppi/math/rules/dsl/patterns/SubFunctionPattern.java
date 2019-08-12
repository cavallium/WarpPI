package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.rules.dsl.Pattern;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Matches and generates any function as a named sub-function.
 * <p>
 * For a <code>Function</code> to match a <code>Pattern</code>, all <code>SubFunctionPattern</code>s with the same name
 * must capture equal sub-functions (according to the <code>equals</code> method).
 * For example, the <code>x + x</code> <code>Pattern</code> matches <code>2 + 2</code> and <code>2 + 2.0</code>,
 * but not <code>2 + 3</code>, while the <code>x + y</code> <code>Pattern</code> matches all three <code>Function</code>s.
 */
public class SubFunctionPattern implements Pattern {
	private final String name;

	public SubFunctionPattern(final String name) {
		this.name = name;
	}

	@Override
	public boolean match(final Function function, final Map<String, Function> subFunctions) {
		final Function existingSubFunction = subFunctions.putIfAbsent(name, function);
		return existingSubFunction == null || existingSubFunction.equals(function);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return subFunctions.get(name);
	}

	@Override
	public Stream<SubFunctionPattern> getSubFunctions() {
		return Stream.of(this);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof SubFunctionPattern)) {
			return false;
		}
		final SubFunctionPattern other = (SubFunctionPattern) o;
		return name.equals(other.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
