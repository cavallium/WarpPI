package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.dsl.frontend.Lexer;
import it.cavallium.warppi.math.rules.dsl.frontend.Parser;
import it.cavallium.warppi.math.rules.dsl.patterns.SubFunctionPattern;

import java.util.*;

public class RulesDsl {
	private RulesDsl() {}

	public static List<Rule> makeRules(final String source) {
		final List<DslException> errors = new ArrayList<>();

		final Lexer lexer = new Lexer(source, errors::add);
		final Parser parser = new Parser(lexer.lex(), errors::add);
		final List<PatternRule> rules = parser.parse();

		for (final PatternRule rule : rules) {
			undefinedSubFunctions(rule).stream()
					.flatMap(subFunc -> parser.getSubFunctionIdentifiers(rule.getRuleName(), subFunc).stream())
					.map(UndefinedSubFunctionException::new)
					.forEach(errors::add);
		}

		if (!errors.isEmpty()) {
			throw new RuntimeException("Errors in DSL source code");
		}

		return Collections.unmodifiableList(rules);
	}

	/**
	 * Finds any sub-functions that are used in the replacement patterns of a <code>PatternRule</code>
	 * without being defined (captured) in the target pattern.
	 *
	 * @param rule The rule to analyze.
	 * @return The (possibly empty) set of undefined sub-functions.
	 */
	private static Set<SubFunctionPattern> undefinedSubFunctions(final PatternRule rule) {
		final Set<SubFunctionPattern> defined = rule.getTarget().getSubFunctions();
		final Set<SubFunctionPattern> undefined = new HashSet<>();
		for (final Pattern replacement : rule.getReplacements()) {
			undefined.addAll(replacement.getSubFunctions());
		}
		undefined.removeAll(defined);
		return undefined;
	}


}
