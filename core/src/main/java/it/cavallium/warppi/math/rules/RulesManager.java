package it.cavallium.warppi.math.rules;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.Platform.StorageUtils;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Expression;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;
import it.cavallium.warppi.math.rules.dsl.DslAggregateException;
import it.cavallium.warppi.math.rules.dsl.RulesDsl;
import it.cavallium.warppi.math.rules.functions.*;
import it.cavallium.warppi.math.solver.MathSolver;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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

		if (Engine.getPlatform().isJavascript()) {
			Engine.getPlatform().loadPlatformRules();
		} else {
			try {
				loadDslRules();
			} catch (IOException | DslAggregateException e) {
				e.printStackTrace();
				Engine.getPlatform().exit(1);
			}
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

	private static void loadDslRules() throws IOException, DslAggregateException {
		final StorageUtils storageUtils = Engine.getPlatform().getStorageUtils();

		final File dslRulesPath = storageUtils.get("rules/dsl/");
		if (!dslRulesPath.exists()) {
			return;
		}

		for (final File file : storageUtils.walk(dslRulesPath)) {
			if (!file.toString().endsWith(".rules")) {
				continue;
			}

			Engine.getPlatform().getConsoleUtils().out().println(
					ConsoleUtils.OUTPUTLEVEL_NODEBUG,
					"RulesManager",
					"Found DSL rules file: " + file.getAbsolutePath()
			);

			final String source;
			try (final InputStream resource = storageUtils.getResourceStream(file.toString())) {
				source = storageUtils.read(resource);
			}

			RulesDsl.makeRules(source).forEach(RulesManager::addRule);
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
