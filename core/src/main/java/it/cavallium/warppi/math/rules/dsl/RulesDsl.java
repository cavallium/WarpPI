package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.dsl.frontend.Lexer;
import it.cavallium.warppi.math.rules.dsl.frontend.Parser;
import it.cavallium.warppi.math.rules.dsl.patterns.SubFunctionPattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RulesDsl {
	private RulesDsl() {}

	public static List<Rule> makeRules(final String source) {
		final List<DslException> errors = new ArrayList<>();

		final Lexer lexer = new Lexer(source, errors::add);
		final Parser parser = new Parser(lexer.lex(), errors::add);
		final List<PatternRule> rules = parser.parse();

		for (final PatternRule rule : rules) {
			checkSubFunctionsDefined(rule);
		}

		if (!errors.isEmpty()) {
			throw new RuntimeException("Errors in DSL source code");
		}

		return Collections.unmodifiableList(rules);
	}

	/**
	 * Verifies that all sub-functions in the replacement patterns of a <code>PatternRule</code>
	 * are defined (captured) in the target pattern.
	 *
	 * @param rule The rule to check.
	 * @throws RuntimeException if any replacement pattern uses undefined sub-functions.
	 */
	private static void checkSubFunctionsDefined(final PatternRule rule) {
		final Set<SubFunctionPattern> defined = rule.getTarget().getSubFunctions();
		for (final Pattern replacement : rule.getReplacements()) {
			if (!defined.containsAll(replacement.getSubFunctions())) {
				throw new RuntimeException("Undefined sub-function(s) in replacements for " + rule.getRuleName());
			}
		}
	}
}
