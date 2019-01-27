package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternRule;
import it.cavallium.warppi.math.rules.dsl.patterns.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static it.cavallium.warppi.math.rules.dsl.frontend.TokenType.*;

/**
 * Converts a list of tokens to a list of <code>PatternRule</code>s.
 */
public class Parser {
	private final List<Token> tokens;
	private int current = 0;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	public List<PatternRule> parse() {
		return rules();
	}

	// rules = { rule } , EOF ;
	private List<PatternRule> rules() {
		List<PatternRule> rules = new ArrayList<>();
		while (!atEnd()) {
			rules.add(rule());
		}
		return rules;
	}

	// rule = rule header , rule body ;
	// rule header = rule type , IDENTIFIER , COLON ;
	// rule body = pattern , ARROW , replacements ;
	private PatternRule rule() {
		RuleType type = ruleType();
		String name = matchOrFail(IDENTIFIER).lexeme;
		matchOrFail(COLON);
		Pattern target = pattern();
		matchOrFail(ARROW);
		List<Pattern> replacements = replacements();
		return new PatternRule(name, type, target, replacements);
	}

	// rule type = REDUCTION | EXPANSION | CALCULATION | EXISTENCE ;
	private RuleType ruleType() {
		switch (pop().type) {
			case REDUCTION:
				return RuleType.REDUCTION;
			case EXPANSION:
				return RuleType.EXPANSION;
			case CALCULATION:
				return RuleType.CALCULATION;
			case EXISTENCE:
				return RuleType.EXISTENCE;
		}
		throw new RuntimeException("Expected rule type");
	}

	// pattern = equation ;
	private Pattern pattern() {
		return equation();
	}

	// replacements = pattern
	//              | LEFT_BRACKET , patterns , RIGHT_BRACKET ;
	// patterns = [ pattern , { COMMA , pattern } , [ COMMA ] ] ;
	private List<Pattern> replacements() {
		if (match(LEFT_BRACKET) == null) {
			return Collections.singletonList(pattern());
		}

		if (match(RIGHT_BRACKET) != null) {
			return Collections.emptyList();
		} else {
			List<Pattern> pats = new ArrayList<>();
			do {
				pats.add(pattern());
			} while (match(COMMA) != null && peek().type != RIGHT_BRACKET);
			matchOrFail(RIGHT_BRACKET);
			return pats;
		}
	}

	// equation = sum , [ EQUALS , sum ] ;
	private Pattern equation() {
		Pattern pat = sum();
		if (match(EQUALS) != null) {
			pat = new EquationPattern(pat, sum());
		}
		return pat;
	}

	// sum = product , { ( PLUS | MINUS | PLUS_MINUS ) product } ;
	private Pattern sum() {
		return matchLeftAssoc(this::product, Map.ofEntries(
				Map.entry(PLUS, SumPattern::new),
				Map.entry(MINUS, SubtractionPattern::new),
				Map.entry(PLUS_MINUS, SumSubtractionPattern::new)
		));
	}

	// product = unary , { ( TIMES | DIVIDE ) unary } ;
	private Pattern product() {
		return matchLeftAssoc(this::unary, Map.ofEntries(
				Map.entry(TIMES, MultiplicationPattern::new),
				Map.entry(DIVIDE, DivisionPattern::new)
		));
	}

	// unary = ( PLUS | MINUS ) unary
	//       | power ;
	private Pattern unary() {
		if (match(PLUS) != null) {
			return unary();
		} else if (match(MINUS) != null) {
			return new NegativePattern(unary());
		} else {
			return power();
		}
	}

	// power = ( function | primary ) , [ POWER , power ] ;
	private Pattern power() {
		Pattern pat = functionOrPrimary();
		if (match(POWER) != null) {
			pat = new PowerPattern(pat, power());
		}
		return pat;
	}

	private Pattern functionOrPrimary() {
		Pattern function = tryFunction();
		return function != null ? function : primary();
	}

	// function = ( ARCCOS | ARCSIN | ARCTAN | COS | SIN | SQRT | TAN ) , LEFT_PAREN , sum , RIGHT_PAREN
	//          | ( LOG | ROOT ) LEFT_PAREN , sum , COMMA , sum , RIGHT_PAREN ;
	private Pattern tryFunction() {
		final Map<TokenType, Function<Pattern, Pattern>> oneArg = Map.ofEntries(
				Map.entry(ARCCOS, ArcCosinePattern::new),
				Map.entry(ARCSIN, ArcSinePattern::new),
				Map.entry(ARCTAN, ArcTangentPattern::new),
				Map.entry(COS, CosinePattern::new),
				Map.entry(SIN, SinePattern::new),
				Map.entry(SQRT, arg -> new RootPattern(new NumberPattern(new BigDecimal(2)), arg)),
				Map.entry(TAN, TangentPattern::new)
		);
		final Map<TokenType, BiFunction<Pattern, Pattern, Pattern>> twoArg = Map.ofEntries(
				Map.entry(LOG, LogarithmPattern::new),
				Map.entry(ROOT, RootPattern::new)
		);

		final TokenType curType = peek().type;
		if (oneArg.containsKey(curType)) {
			pop();
			return oneArgFunction(oneArg.get(curType));
		} else if (twoArg.containsKey(curType)) {
			pop();
			return twoArgFunction(twoArg.get(curType));
		}

		return null;
	}

	private Pattern oneArgFunction(final Function<Pattern, Pattern> constructor) {
		matchOrFail(LEFT_PAREN);
		final Pattern arg = pattern();
		matchOrFail(RIGHT_PAREN);
		return constructor.apply(arg);
	}

	private Pattern twoArgFunction(final BiFunction<Pattern, Pattern, Pattern> constructor) {
		matchOrFail(LEFT_PAREN);
		final Pattern firstArg = pattern();
		matchOrFail(COMMA);
		final Pattern secondArg = pattern();
		matchOrFail(RIGHT_PAREN);
		return constructor.apply(firstArg, secondArg);
	}

	// primary = NUMBER | constant | IDENTIFIER | UNDEFINED
	//         | LEFT_PAREN sum RIGHT_PAREN ;
	// constant = PI | E ;
	private Pattern primary() {
		Token curToken = pop();
		switch (curToken.type) {
			case PI:
				return new ConstantPattern(MathematicalSymbols.PI);
			case E:
				return new ConstantPattern(MathematicalSymbols.EULER_NUMBER);
			case UNDEFINED:
				return new UndefinedPattern();
			case NUMBER:
				return new NumberPattern(new BigDecimal(curToken.lexeme));
			case IDENTIFIER:
				return new SubFunctionPattern(curToken.lexeme);
			case LEFT_PAREN:
				final Pattern grouped = sum();
				matchOrFail(RIGHT_PAREN);
				return grouped;
		}
		throw new RuntimeException("Unexpected " + curToken);
	}

	private Pattern matchLeftAssoc(
			final Supplier<Pattern> operandParser,
			final Map<TokenType, BiFunction<Pattern, Pattern, Pattern>> operators
	) {
		Pattern pat = operandParser.get();
		while (operators.containsKey(peek().type)) {
			final Token operatorToken = pop();
			final BiFunction<Pattern, Pattern, Pattern> constructor = operators.get(operatorToken.type);
			pat = constructor.apply(pat, operandParser.get());
		}
		return pat;
	}

	private Token matchOrFail(final TokenType expectedType) {
		final Token matched = match(expectedType);
		if (matched == null) {
			throw new RuntimeException("Expected " + expectedType);
		}
		return matched;
	}

	private Token match(final TokenType expectedType) {
		final Token curToken = tokens.get(current);
		if (curToken.type == expectedType) {
			current++;
			return curToken;
		} else {
			return null;
		}
	}

	private Token pop() {
		final Token curToken = tokens.get(current);
		current++;
		return curToken;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private boolean atEnd() {
		return tokens.get(current).type == EOF;
	}
}
