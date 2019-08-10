package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.rules.dsl.patterns.SubFunctionPattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Recognizes and generates functions of some specific shape.
 */
public interface Pattern {
	/**
	 * Tries to match this pattern against a function and capture sub-functions.
	 *
	 * @param function The function to test the pattern against.
	 * @return The captured sub-functions, or an empty <code>Optional</code> if
	 *         the pattern doesn't match.
	 */
	default Optional<Map<String, Function>> match(Function function) {
		Map<String, Function> subFunctions = new HashMap<>();
		return match(function, subFunctions) ? Optional.of(subFunctions) : Optional.empty();
	}

	/**
	 * Tries to match this pattern against a function and capture sub-functions.
	 * <p>
	 * This overload is provided to allow for a more efficient implementation of matching, by mutating the given
	 * <code>Map</code> instead of creating and merging multiple ones.
	 * For all other purposes, use of the {@link #match(Function)} overload is recommended instead.
	 * <p>
	 * When the pattern matches, all captured sub-functions are added to the map (if not present already).
	 * If, instead, the pattern doesn't match, the contents of the map are undefined.
	 *
	 * @param function The function to test the pattern against.
	 * @param subFunctions The map used to capture sub-functions.
	 * @return <code>true</code> if the pattern matches, or <code>false</code> otherwise.
	 */
	boolean match(Function function, Map<String, Function> subFunctions);

	/**
	 * Creates a new function by filling in sub-functions within this pattern.
	 *
	 * @param mathContext The <code>MathContext</code> used to construct <code>Function</code>s.
	 * @param subFunctions A map of named sub-functions to be inserted into this pattern.
	 * @return The resulting function.
	 */
	Function replace(MathContext mathContext, Map<String, Function> subFunctions);

	/**
	 * @return The (possibly empty) <code>Set</code> of all sub-function patterns
	 *         found within this pattern and its children.
	 */
	Set<SubFunctionPattern> getSubFunctions();
}
