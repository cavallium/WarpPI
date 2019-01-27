package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.functions.Subtraction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contains helper methods which are useful for writing patterns.
 */
public class PatternUtils {
	private PatternUtils() {}

	/**
	 * Gathers captured sub-functions from two matches, checking for equality
	 * of ones with the same name.
	 *
	 * @param match1 Sub-functions from one match.
	 * @param match2 Sub-functions from the other match.
	 * @return A <code>Map</code> containing all sub-functions, or an empty
	 *         <code>Optional</code> if the same name is used to refer to
	 *         non-equal sub-functions in the two matches.
	 */
	public static Optional<Map<String, Function>> mergeMatches(
			final Map<String, Function> match1,
			final Map<String, Function> match2
	) {
		if (!checkSubFunctionEquality(match1, match2)) {
			return Optional.empty();
		}

		final Map<String, Function> merged = new HashMap<>();
		merged.putAll(match1);
		merged.putAll(match2);
		return Optional.of(merged);
	}

	private static boolean checkSubFunctionEquality(
			final Map<String, Function> match1,
			final Map<String, Function> match2
	) {
		for (final Map.Entry<String, Function> leftSubFunction : match1.entrySet()) {
			final String key = leftSubFunction.getKey();
			if (match2.containsKey(key)
					&& !match2.get(key).equals(leftSubFunction.getValue())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Tries to match the given patterns against the two parameters of a <code>FunctionOperator</code>.
	 *
	 * @param functionOperator The <code>FunctionOperator</code> to be matched.
	 * @param pattern1 The <code>Pattern</code> used to match <code>functionOperator.parameter1</code>.
	 * @param pattern2 The <code>Pattern</code> used to match <code>functionOperator.parameter2</code>.
	 * @return The combined result of the two matches.
	 * @see #mergeMatches(Map, Map)
	 */
	public static Optional<Map<String, Function>> matchFunctionOperatorParameters(
			final FunctionOperator functionOperator,
			final Pattern pattern1,
			final Pattern pattern2
	) {
		return pattern1.match(functionOperator.getParameter1())
				.flatMap(match1 -> pattern2.match(functionOperator.getParameter2())
						.flatMap(match2 -> mergeMatches(match1, match2))
				);
	}
}
