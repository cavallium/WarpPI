package it.cavallium.warppi.math.rules;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Platform;
import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Expression;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;
import it.cavallium.warppi.math.rules.dsl.DslAggregateException;
import it.cavallium.warppi.math.rules.dsl.RulesDsl;
import it.cavallium.warppi.math.rules.dsl.errorutils.DslFilesException;
import it.cavallium.warppi.math.rules.functions.*;
import it.cavallium.warppi.math.solver.MathSolver;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public class RulesManager {

	public static ObjectArrayList<Rule>[] rules;

	private RulesManager() {}

	@SuppressWarnings({ "unchecked" })
	public static void initialize() {
		Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "RulesManager", "Loading the rules");
		RulesManager.rules = new ObjectArrayList[RuleType.values().length];
		for (final RuleType val : RuleType.values()) {
			RulesManager.rules[val.ordinal()] = new ObjectArrayList<>();
		}

		loadBuiltinRules();

		try {
			loadDslRules();
		} catch (IOException | DslFilesException e) {
			e.printStackTrace();
			if (e instanceof DslFilesException) {
				System.err.print(((DslFilesException) e).format());
			}
			Engine.getPlatform().exit(1);
		}
	}

	private static void loadBuiltinRules() {
		Stream.of(
				new DivisionRule(),
				new EmptyNumberRule(),
				new ExpressionRule(),
				new JokeRule(),
				new MultiplicationRule(),
				new NegativeRule(),
				new NumberRule(),
				new PowerRule(),
				new RootRule(),
				new SubtractionRule(),
				new SumRule(),
				new SumSubtractionRule(),
				new VariableRule()
		).forEach(RulesManager::addRule);
	}

	private static void loadDslRules() throws IOException, DslFilesException {
		final Platform platform = Engine.getPlatform();

		final DslFilesException fileErrors = new DslFilesException();
		for (final String path : platform.getRuleFilePaths()) {
			platform.getConsoleUtils().out().println(
					ConsoleUtils.OUTPUTLEVEL_NODEBUG,
					"RulesManager",
					"Found DSL rules file: " + path
			);

			final String source;
			try (final InputStream resource = platform.getStorageUtils().getResourceStream(path)) {
				source = platform.getStorageUtils().read(resource);
			}

			try {
				// This loop used to be written as RulesDsl.makeRules(source).forEach(RulesManager::addRule),
				// but the forEach method hangs on TeaVM.
				for (Rule rule : RulesDsl.makeRules(source)) {
					addRule(rule);
				}
			} catch (DslAggregateException e) {
				fileErrors.addFileErrors(path, source, e.getErrors());
			}
		}

		if (fileErrors.hasErrors()) {
			throw fileErrors;
		}
	}

	public static void warmUp() throws Error, InterruptedException {
		ObjectArrayList<Function> uselessResult = null;
		boolean uselessVariable = false;
		for (final RuleType val : RuleType.values()) {
			final ObjectArrayList<Rule> ruleList = RulesManager.rules[val.ordinal()];
			for (final Rule rule : ruleList) {
				String ruleName = "<null>";
				try {
					ruleName = rule.getRuleName();
					final ObjectArrayList<Function> uselessResult2 = rule.execute(RulesManager.generateUselessExpression());
					uselessVariable = (uselessResult == null ? new ObjectArrayList<>() : uselessResult).equals(uselessResult2);
					uselessResult = uselessResult2;
				} catch (final Exception e) {
					if (uselessVariable || true) {
						System.err.println("Exception thrown by rule '" + ruleName + "'!");
						e.printStackTrace();
					}
				}
			}
		}
		try {
			new MathSolver(RulesManager.generateUselessExpression()).solveAllSteps();
		} catch (InterruptedException | Error e) {
			e.printStackTrace();
		}
	}

	private static Function generateUselessExpression() {
		final MathContext mc = new MathContext();
		Function expr = new Expression(mc);
		expr = expr.setParameter(0, new Variable(mc, 'x', V_TYPE.VARIABLE));
		return expr;
	}

	public static void addRule(final Rule rule) {
		RulesManager.rules[rule.getRuleType().ordinal()].add(rule);
		Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "RulesManager", rule.getRuleName(), "Loaded as " + rule.getRuleType() + " rule");
	}
}
