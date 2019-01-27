package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.dsl.frontend.Lexer;
import it.cavallium.warppi.math.rules.dsl.frontend.Parser;

import java.util.Collections;
import java.util.List;

public class RulesDsl {
	private RulesDsl() {}

	public static List<Rule> makeRules(final String source) {
		final Lexer lexer = new Lexer(source);
		final Parser parser = new Parser(lexer.lex());
		return Collections.unmodifiableList(parser.parse());
	}
}
