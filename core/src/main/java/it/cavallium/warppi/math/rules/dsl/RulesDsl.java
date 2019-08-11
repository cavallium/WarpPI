package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.dsl.frontend.Lexer;
import it.cavallium.warppi.math.rules.dsl.frontend.Parser;
import it.cavallium.warppi.math.rules.dsl.patterns.SubFunctionPattern;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implements a domain-specific language (DSL) for the definition of {@link Rule}s.
 */
public class RulesDsl {
	private RulesDsl() {}

	/**
	 * Creates rules from DSL source code.
	 *
	 * @param source The source code.
	 * @return An unmodifiable list containing the rules defined in the DSL code.
	 * @throws DslAggregateException if the code contains any errors.
	 */
	public static List<Rule> makeRules(final String source) throws DslAggregateException {
		final List<DslError> errors = new ArrayList<>();

		final Lexer lexer = new Lexer(source, errors::add);
		final Parser parser = new Parser(lexer.lex(), errors::add);
		final List<PatternRule> rules = parser.parse();

		for (final PatternRule rule : rules) {
			undefinedSubFunctions(rule)
					.map(subFunc -> new UndefinedSubFunction(
						parser.getSubFunctionIdentifier(subFunc)
					))
					.forEach(errors::add);
		}

		if (!errors.isEmpty()) {
			throw new DslAggregateException(errors);
		}

		return Collections.unmodifiableList(rules);
	}

	/**
	 * Finds any sub-functions that are used in the replacement patterns of a <code>PatternRule</code>
	 * without being defined (captured) in the target pattern.
	 *
	 * @param rule The rule to analyze.
	 * @return A (possibly empty) <code>Stream</code> of undefined sub-functions.
	 */
	private static Stream<SubFunctionPattern> undefinedSubFunctions(final PatternRule rule) {
		final Set<SubFunctionPattern> defined = rule.getTarget().getSubFunctions().collect(Collectors.toSet());
		return rule.getReplacements().stream()
			.flatMap(Pattern::getSubFunctions)
			.filter(subFunc -> !defined.contains(subFunc));
	}
}
