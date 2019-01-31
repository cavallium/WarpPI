package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.dsl.DslError;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternRule;
import it.cavallium.warppi.math.rules.dsl.patterns.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static it.cavallium.warppi.math.rules.dsl.frontend.TokenType.*;

/**
 * Converts a list of tokens to a list of <code>PatternRule</code>s.
 */
public class Parser {
	private static final Map<TokenType, RuleType> ruleTypes = Map.ofEntries(
			Map.entry(REDUCTION, RuleType.REDUCTION),
			Map.entry(EXPANSION, RuleType.EXPANSION),
			Map.entry(CALCULATION, RuleType.CALCULATION),
			Map.entry(EXISTENCE, RuleType.EXISTENCE)
	);

	private final List<Token> tokens;
	private final Consumer<? super DslError> errorReporter;
	private int current = 0;

	// For error reporting
	private String currentRuleName;
	private final Map<String, Map<SubFunctionPattern, List<Token>>> subFunctionIdentifiers = new HashMap<>();

	public Parser(final List<Token> tokens, final Consumer<? super DslError> errorReporter) {
		this.tokens = tokens;
		this.errorReporter = errorReporter;
	}

	public List<PatternRule> parse() {
		return rules();
	}

	public List<Token> getSubFunctionIdentifiers(final String ruleName, final SubFunctionPattern subFunction) {
		return subFunctionIdentifiers.get(ruleName).get(subFunction);
	}

	// rules = { rule } , EOF ;
	private List<PatternRule> rules() {
		final List<PatternRule> rules = new ArrayList<>();
		while (!atEnd()) {
			try {
				rules.add(rule());
			} catch (final SyntaxException e) {
				errorReporter.accept(e.getError());
				synchronizeTo(ruleTypes.keySet()); // Skip to the next rule to minimize "false" errors
			}
		}
		return rules;
	}

	// rule = rule header , rule body ;
	// rule header = rule type , IDENTIFIER , COLON ;
	// rule body = pattern , ARROW , replacements ;
	private PatternRule rule() throws SyntaxException {
		final RuleType type = ruleType();
		final String name = matchOrFail(IDENTIFIER).lexeme;
		currentRuleName = name; // This field must be set before calling pattern() and replacements()
		matchOrFail(COLON);
		final Pattern target = pattern();
		matchOrFail(ARROW);
		final List<Pattern> replacements = replacements();
		return new PatternRule(name, type, target, replacements);
	}

	// rule type = REDUCTION | EXPANSION | CALCULATION | EXISTENCE ;
	private RuleType ruleType() throws SyntaxException {
		final Token curToken = pop();
		if (!ruleTypes.containsKey(curToken.type)) {
			throw new SyntaxException(
					new UnexpectedToken(curToken, ruleTypes.keySet())
			);
		}
		return ruleTypes.get(curToken.type);
	}

	// pattern = equation ;
	private Pattern pattern() throws SyntaxException {
		return equation();
	}

	// replacements = pattern
	//              | LEFT_BRACKET , patterns , RIGHT_BRACKET ;
	// patterns = [ pattern , { COMMA , pattern } , [ COMMA ] ] ;
	private List<Pattern> replacements() throws SyntaxException {
		if (match(LEFT_BRACKET) == null) {
			return Collections.singletonList(pattern());
		}

		if (match(RIGHT_BRACKET) != null) {
			return Collections.emptyList();
		} else {
			final List<Pattern> pats = new ArrayList<>();
			do {
				pats.add(pattern());
			} while (match(COMMA) != null && peek().type != RIGHT_BRACKET);
			matchOrFail(RIGHT_BRACKET);
			return pats;
		}
	}

	// equation = sum , [ EQUALS , sum ] ;
	private Pattern equation() throws SyntaxException {
		Pattern pat = sum();
		if (match(EQUALS) != null) {
			pat = new EquationPattern(pat, sum());
		}
		return pat;
	}

	// sum = product , { ( PLUS | MINUS | PLUS_MINUS ) product } ;
	private Pattern sum() throws SyntaxException {
		return matchLeftAssoc(this::product, Map.ofEntries(
				Map.entry(PLUS, SumPattern::new),
				Map.entry(MINUS, SubtractionPattern::new),
				Map.entry(PLUS_MINUS, SumSubtractionPattern::new)
		));
	}

	// product = unary , { ( TIMES | DIVIDE ) unary } ;
	private Pattern product() throws SyntaxException {
		return matchLeftAssoc(this::unary, Map.ofEntries(
				Map.entry(TIMES, MultiplicationPattern::new),
				Map.entry(DIVIDE, DivisionPattern::new)
		));
	}

	// unary = ( PLUS | MINUS ) unary
	//       | power ;
	private Pattern unary() throws SyntaxException {
		if (match(PLUS) != null) {
			return unary();
		} else if (match(MINUS) != null) {
			return new NegativePattern(unary());
		} else {
			return power();
		}
	}

	// power = ( function | primary ) , [ POWER , power ] ;
	private Pattern power() throws SyntaxException {
		Pattern pat = functionOrPrimary();
		if (match(POWER) != null) {
			pat = new PowerPattern(pat, power());
		}
		return pat;
	}

	private Pattern functionOrPrimary() throws SyntaxException {
		final Pattern function = tryFunction();
		return function != null ? function : primary();
	}

	// function = ( ARCCOS | ARCSIN | ARCTAN | COS | SIN | SQRT | TAN ) , LEFT_PAREN , sum , RIGHT_PAREN
	//          | ( LOG | ROOT ) LEFT_PAREN , sum , COMMA , sum , RIGHT_PAREN ;
	private Pattern tryFunction() throws SyntaxException {
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

	private Pattern oneArgFunction(final Function<Pattern, Pattern> constructor) throws SyntaxException {
		matchOrFail(LEFT_PAREN);
		final Pattern arg = pattern();
		matchOrFail(RIGHT_PAREN);
		return constructor.apply(arg);
	}

	private Pattern twoArgFunction(final BiFunction<Pattern, Pattern, Pattern> constructor) throws SyntaxException {
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
	private Pattern primary() throws SyntaxException {
		final Token curToken = pop();
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
				final SubFunctionPattern subFunction = new SubFunctionPattern(curToken.lexeme);
				saveSubFunctionIdentifier(subFunction, curToken);
				return subFunction;
			case LEFT_PAREN:
				final Pattern grouped = sum();
				matchOrFail(RIGHT_PAREN);
				return grouped;
		}
		throw new SyntaxException(new UnexpectedToken(curToken));
	}

	private void saveSubFunctionIdentifier(final SubFunctionPattern subFunction, final Token curToken) {
		final Map<SubFunctionPattern, List<Token>> ruleMap = subFunctionIdentifiers.computeIfAbsent(
				currentRuleName,
				key -> new HashMap<>()
		);
		final List<Token> subFunctionList = ruleMap.computeIfAbsent(
				subFunction,
				key -> new ArrayList<>()
		);
		subFunctionList.add(curToken);
	}

	private Pattern matchLeftAssoc(
			final PatternParser operandParser,
			final Map<TokenType, BiFunction<Pattern, Pattern, Pattern>> operators
	) throws SyntaxException {
		Pattern pat = operandParser.parse();
		while (operators.containsKey(peek().type)) {
			final Token operatorToken = pop();
			final BiFunction<Pattern, Pattern, Pattern> constructor = operators.get(operatorToken.type);
			pat = constructor.apply(pat, operandParser.parse());
		}
		return pat;
	}

	private Token matchOrFail(final TokenType expectedType) throws SyntaxException {
		final Token matched = match(expectedType);
		if (matched == null) {
			throw new SyntaxException(
					new UnexpectedToken(tokens.get(current), expectedType)
			);
		}
		return matched;
	}

	private Token match(final TokenType expectedType) {
		final Token curToken = tokens.get(current);
		if (curToken.type != expectedType) {
			return null;
		}
		current++;
		return curToken;
	}

	private void synchronizeTo(final Set<TokenType> types) {
		while (!atEnd() && !types.contains(tokens.get(current).type)) {
			current++;
		}
	}

	private Token pop() throws SyntaxException {
		final Token curToken = tokens.get(current);
		if (atEnd()) {
			throw new SyntaxException(new UnexpectedToken(curToken)); // Avoid popping EOF
		}
		current++;
		return curToken;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private boolean atEnd() {
		return tokens.get(current).type == EOF;
	}

	@FunctionalInterface
	private interface PatternParser {
		Pattern parse() throws SyntaxException;
	}
}
