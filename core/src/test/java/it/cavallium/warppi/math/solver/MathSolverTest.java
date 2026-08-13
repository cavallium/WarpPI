package it.cavallium.warppi.math.solver;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Expression;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.functions.SumSubtraction;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.functions.DivisionRule;
import it.cavallium.warppi.math.rules.functions.ExpressionRule;
import it.cavallium.warppi.math.rules.functions.SumRule;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternRule;
import it.cavallium.warppi.math.rules.dsl.patterns.MultiplicationPattern;
import it.cavallium.warppi.math.rules.dsl.patterns.PowerPattern;
import it.cavallium.warppi.math.rules.dsl.patterns.SubFunctionPattern;
import it.cavallium.warppi.math.rules.dsl.patterns.SumPattern;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathSolverTest {
	@Test
	void addsAnIntegerAndFractionExactlyInAlgebraMode() throws InterruptedException, Error {
		final TestMathContext context = new TestMathContext();
		context.exactMode = true;
		context.add(new DivisionRule());
		context.add(new ExpressionRule());
		context.add(new SumRule());
		final Function input = new Expression(context, new Sum(context,
				new Number(context, 12),
				new Division(context, new Number(context, 3), new Number(context, 2))));
		final Function expected = new Division(context, new Number(context, 27), new Number(context, 2));

		final ObjectArrayList<ObjectArrayList<Function>> steps = new MathSolver(input).solveAllSteps();

		assertEquals(expected, steps.get(steps.size() - 1).get(0));
	}

	@Test
	void omitsTheInitialExpressionWhenNothingChanges() throws InterruptedException, Error {
		final TestMathContext context = new TestMathContext();
		final Function input = new Number(context, 2);

		assertTrue(new MathSolver(input).solveAllSteps().isEmpty());
	}

	@Test
	void deduplicatesRuleBranchesInStableOrder() throws InterruptedException, Error {
		final TestMathContext context = new TestMathContext();
		context.add(new Rule() {
			@Override
			public RuleType getRuleType() {
				return RuleType.EXPANSION;
			}

			@Override
			public ObjectArrayList<Function> execute(final Function function) {
				if (!(function instanceof SumSubtraction)) {
					return null;
				}
				final ObjectArrayList<Function> results = new ObjectArrayList<>();
				results.add(new Number(context, 2));
				results.add(new Number(context, 2));
				return results;
			}
		});
		final Function input = new SumSubtraction(context,
				new Number(context, 2), new Number(context, 0));

		final ObjectArrayList<ObjectArrayList<Function>> steps = new MathSolver(input).solveAllSteps();

		assertEquals(1, steps.size());
		assertEquals(1, steps.get(0).size());
		assertEquals(new Number(context, 2), steps.get(0).get(0));
	}

	@Test
	void rejectsAnInverseRuleCycleBeforeItRepeats() {
		final TestMathContext context = new TestMathContext();
		final Pattern base = new SubFunctionPattern("base");
		final Pattern firstExponent = new SubFunctionPattern("firstExponent");
		final Pattern secondExponent = new SubFunctionPattern("secondExponent");
		final Pattern expanded = new MultiplicationPattern(
				new PowerPattern(base, firstExponent),
				new PowerPattern(base, secondExponent));
		final Pattern reduced = new PowerPattern(base,
				new SumPattern(firstExponent, secondExponent));
		context.add(new PatternRule("combine powers", RuleType.REDUCTION, expanded, reduced));
		context.add(new PatternRule("split powers", RuleType.EXPANSION, reduced, expanded));

		final Variable a = variable(context, 'A');
		final Variable b = variable(context, 'B');
		final Variable c = variable(context, 'C');
		final Function input = new Multiplication(context,
				new Power(context, a, b), new Power(context, a, c));
		final Function expected = new Power(context, a, new Sum(context, b, c));

		final ObjectArrayList<ObjectArrayList<Function>> steps = assertTimeoutPreemptively(
				Duration.ofSeconds(1), () -> new MathSolver(input).solveAllSteps());

		assertEquals(1, steps.size());
		assertEquals(ObjectArrayList.wrap(new Function[] { expected }), steps.get(0));

		final ObjectArrayList<ObjectArrayList<Function>> alreadyReducedSteps = assertTimeoutPreemptively(
				Duration.ofSeconds(1), () -> new MathSolver(expected).solveAllSteps());
		assertTrue(alreadyReducedSteps.isEmpty());
	}

	@Test
	void boundsUniqueRewriteGrowth() {
		final TestMathContext context = new TestMathContext();
		context.add(new Rule() {
			@Override
			public RuleType getRuleType() {
				return RuleType.EXPANSION;
			}

			@Override
			public ObjectArrayList<Function> execute(final Function function) {
				if (!(function instanceof Number)) {
					return null;
				}
				final Number number = (Number) function;
				final ObjectArrayList<Function> result = new ObjectArrayList<>();
				result.add(new Number(context, number.getTerm().add(BigDecimal.ONE)));
				return result;
			}
		});

		final Error error = assertThrows(Error.class,
				() -> new MathSolver(new Number(context, 0)).solveAllSteps());

		assertEquals(Errors.TIMEOUT, error.id);
	}

	@Test
	void boundsBranchGrowthAfterDeduplication() {
		final TestMathContext context = new TestMathContext();
		context.add(new Rule() {
			@Override
			public RuleType getRuleType() {
				return RuleType.EXPANSION;
			}

			@Override
			public ObjectArrayList<Function> execute(final Function function) {
				if (!(function instanceof Variable)) {
					return null;
				}
				final ObjectArrayList<Function> results = new ObjectArrayList<>();
				for (int i = 0; i <= MathSolver.MAX_BRANCHES; i++) {
					results.add(new Number(context, i));
				}
				return results;
			}
		});

		final Error error = assertThrows(Error.class,
				() -> new MathSolver(variable(context, 'A')).solveAllSteps());

		assertEquals(Errors.TIMEOUT, error.id);
	}

	@Test
	void honorsThreadInterruption() {
		final TestMathContext context = new TestMathContext();
		Thread.currentThread().interrupt();
		try {
			assertThrows(InterruptedException.class,
					() -> new MathSolver(variable(context, 'A')).solveAllSteps());
		} finally {
			Thread.interrupted();
		}
	}

	private static Variable variable(final MathContext context, final char name) {
		return new Variable(context, name, V_TYPE.VARIABLE);
	}

	private static final class TestMathContext extends MathContext {
		private final ObjectArrayList<Rule>[] rules;

		@SuppressWarnings("unchecked")
		private TestMathContext() {
			rules = new ObjectArrayList[RuleType.values().length];
			for (final RuleType type : RuleType.values()) {
				rules[type.ordinal()] = new ObjectArrayList<>();
			}
		}

		private void add(final Rule rule) {
			rules[rule.getRuleType().ordinal()].add(rule);
		}

		@Override
		public ObjectArrayList<Rule> getAcceptableRules(final RuleType ruleType) {
			return rules[ruleType.ordinal()];
		}
	}
}
