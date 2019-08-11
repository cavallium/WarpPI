package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.dsl.DslError;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternRule;
import it.cavallium.warppi.math.rules.dsl.patterns.*;
import it.cavallium.warppi.util.MapFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static it.cavallium.warppi.math.rules.dsl.frontend.TokenType.*;

/**
 * Converts a list of {@link Token}s to a list of {@link PatternRule}s.
 */
public class Parser {
	private static final Map<TokenType, RuleType> RULE_TYPES = MapFactory.fromEntries(
		MapFactory.entry(REDUCTION, RuleType.REDUCTION),
		MapFactory.entry(EXPANSION, RuleType.EXPANSION),
		MapFactory.entry(CALCULATION, RuleType.CALCULATION),
		MapFactory.entry(EXISTENCE, RuleType.EXISTENCE)
	);

	private final List<Token> tokens;
	private final Consumer<? super DslError> errorReporter;
	private int currentIndex = 0;

	// For error reporting
	// An IdentityHashMap is used to distinguish SubFunctionPatterns even if they're identical (equal)
	private final IdentityHashMap<SubFunctionPattern, Token> subFunctionIdentifiers = new IdentityHashMap<>();

	/**
	 * Constructs a <code>Parser</code> that will produce a list of {@link PatternRule}s from the the given list of {@link Token}s.
	 *
	 * @param tokens        the list of <code>Token</code>s to process.
	 * @param errorReporter a <code>Consumer</code> used to report each <code>DslError</code> that the
	 *                      <code>Parser</code> finds within the source string.
	 */
	public Parser(final List<Token> tokens, final Consumer<? super DslError> errorReporter) {
		this.tokens = tokens;
		this.errorReporter = errorReporter;
	}

	/**
	 * Runs the <code>Parser</code>.
	 * <p>
	 * This method can only be called once per instance.
	 *
	 * @return the list of all valid <code>PatternRule</code>s constructed from the given tokens.
	 * 	       If any errors are reported, this list should not be considered to represent a valid set of DSL rules,
	 * 		   but each rule can still be analyzed to look for undefined sub-functions in replacement patterns and
	 * 		   report them (which may allow the user to fix more errors before having to rerun the <code>Lexer</code>
	 * 		   and <code>Parser</code>).
	 */
	public List<PatternRule> parse() {
		return rules();
	}

	/**
	 * Retrieves the <code>IDENTIFIER</code> token which corresponds to the given <code>SubFunctionPattern</code>.
	 * <p>
	 * The information returned by this method can be used to point out the location of sub-function related errors
	 * within the DSL source code.
	 *
	 * @param subFunction a <code>SubFunctionsPattern</code> from one of the rules returned by this <code>Parser</code>
	 *                    instance. While <code>SubFunctionPattern</code>s with the same name are considered equal,
	 *                    this method can distinguish between them, in order to return the exact identifier which led
	 *                    to the creation of the specified <code>SubFunctionPattern</code> object.
	 * @return the <code>Token</code> (of type <code>IDENTIFIER</code>) which corresponds to the given
	 * 	       <code>SubFunctionPattern</code>.
	 */
	public Token getSubFunctionIdentifier(final SubFunctionPattern subFunction) {
		return subFunctionIdentifiers.get(subFunction);
	}

	// rules = { rule } , EOF ;
	private List<PatternRule> rules() {
		final List<PatternRule> rules = new ArrayList<>();
		while (!atEnd()) {
			try {
				rules.add(rule());
			} catch (final SyntaxException e) {
				errorReporter.accept(e.getError());
				synchronizeTo(RULE_TYPES.keySet()); // Skip to the next rule to minimize "false" errors
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
		matchOrFail(COLON);
		final Pattern target = pattern();
		matchOrFail(ARROW);
		final List<Pattern> replacements = replacements();
		return new PatternRule(name, type, target, replacements);
	}

	// rule type = REDUCTION | EXPANSION | CALCULATION | EXISTENCE ;
	private RuleType ruleType() throws SyntaxException {
		final Token curToken = pop();
		if (!RULE_TYPES.containsKey(curToken.type)) {
			throw new SyntaxException(
					new UnexpectedToken(curToken, RULE_TYPES.keySet())
			);
		}
		return RULE_TYPES.get(curToken.type);
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
		return matchLeftAssoc(this::product, MapFactory.fromEntries(
				MapFactory.entry(PLUS, SumPattern::new),
				MapFactory.entry(MINUS, SubtractionPattern::new),
				MapFactory.entry(PLUS_MINUS, SumSubtractionPattern::new)
		));
	}

	// product = unary , { ( TIMES | DIVIDE ) unary } ;
	private Pattern product() throws SyntaxException {
		return matchLeftAssoc(this::unary, MapFactory.fromEntries(
				MapFactory.entry(TIMES, MultiplicationPattern::new),
				MapFactory.entry(DIVIDE, DivisionPattern::new)
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

	// power = ( function | primary ) , [ POWER , unary ] ;
	private Pattern power() throws SyntaxException {
		Pattern pat = functionOrPrimary();
		if (match(POWER) != null) {
			pat = new PowerPattern(pat, unary());
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
		final Map<TokenType, Function<Pattern, Pattern>> oneArg = MapFactory.fromEntries(
				MapFactory.entry(ARCCOS, ArcCosinePattern::new),
				MapFactory.entry(ARCSIN, ArcSinePattern::new),
				MapFactory.entry(ARCTAN, ArcTangentPattern::new),
				MapFactory.entry(COS, CosinePattern::new),
				MapFactory.entry(SIN, SinePattern::new),
				MapFactory.entry(SQRT, arg -> new RootPattern(new NumberPattern(new BigDecimal(2)), arg)),
				MapFactory.entry(TAN, TangentPattern::new)
		);
		final Map<TokenType, BiFunction<Pattern, Pattern, Pattern>> twoArg = MapFactory.fromEntries(
				MapFactory.entry(LOG, LogarithmPattern::new),
				MapFactory.entry(ROOT, RootPattern::new)
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
				subFunctionIdentifiers.put(subFunction, curToken);
				return subFunction;
			case LEFT_PAREN:
				final Pattern grouped = sum();
				matchOrFail(RIGHT_PAREN);
				return grouped;
		}
		throw new SyntaxException(new UnexpectedToken(curToken));
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
					new UnexpectedToken(tokens.get(currentIndex), expectedType)
			);
		}
		return matched;
	}

	private Token match(final TokenType expectedType) {
		final Token curToken = tokens.get(currentIndex);
		if (curToken.type != expectedType) {
			return null;
		}
		currentIndex++;
		return curToken;
	}

	private void synchronizeTo(final Set<TokenType> types) {
		while (!atEnd() && !types.contains(tokens.get(currentIndex).type)) {
			currentIndex++;
		}
	}

	private Token pop() throws SyntaxException {
		final Token curToken = tokens.get(currentIndex);
		if (atEnd()) {
			throw new SyntaxException(new UnexpectedToken(curToken)); // Avoid popping EOF
		}
		currentIndex++;
		return curToken;
	}

	private Token peek() {
		return tokens.get(currentIndex);
	}

	private boolean atEnd() {
		return tokens.get(currentIndex).type == EOF;
	}

	@FunctionalInterface
	private interface PatternParser {
		Pattern parse() throws SyntaxException;
	}
}
