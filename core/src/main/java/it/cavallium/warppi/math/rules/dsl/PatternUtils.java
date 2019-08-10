package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.rules.dsl.patterns.SubFunctionPattern;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Contains helper methods which are useful for writing patterns.
 */
public class PatternUtils {
	private PatternUtils() {}

	/**
	 * Gathers all sub-function patterns from multiple patterns.
	 *
	 * @param patterns The patterns from which sub-functions are gathered.
	 * @return The union of the return values of {@link Pattern#getSubFunctions()} for each pattern.
	 */
	public static Set<SubFunctionPattern> getSubFunctionsFrom(final Pattern... patterns) {
		return Arrays.stream(patterns)
				.map(Pattern::getSubFunctions)
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}
}
