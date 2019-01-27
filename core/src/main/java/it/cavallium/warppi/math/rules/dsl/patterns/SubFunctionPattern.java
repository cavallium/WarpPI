package it.cavallium.warppi.math.rules.dsl.patterns;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.rules.dsl.Pattern;

import java.util.*;

/**
 * Matches and generates any function as a named sub-function.
 */
public class SubFunctionPattern implements Pattern {
	private final String name;

	public SubFunctionPattern(final String name) {
		this.name = name;
	}

	@Override
	public Optional<Map<String, Function>> match(final Function function) {
		final HashMap<String, Function> subFunctions = new HashMap<>();
		subFunctions.put(name, function);
		return Optional.of(subFunctions);
	}

	@Override
	public Function replace(final MathContext mathContext, final Map<String, Function> subFunctions) {
		return subFunctions.get(name);
	}

	@Override
	public Set<SubFunctionPattern> getSubFunctions() {
		return Collections.singleton(this);
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
