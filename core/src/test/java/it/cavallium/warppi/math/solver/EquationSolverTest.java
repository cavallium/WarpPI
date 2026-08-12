package it.cavallium.warppi.math.solver;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquationSolverTest {
	private final EmptyMathContext context = new EmptyMathContext();

	@Test
	void automaticSolverUsesOnlyTheMarkedSolutionVariable() {
		final Variable x = variable('X', V_TYPE.SOLUTION);
		final Variable y = variable('Y', V_TYPE.VARIABLE);
		final Equation equation = equation(new Sum(context, x, y), number(2));
		final Equation expected = equation(x, new Subtraction(context, number(2), y));

		final ObjectArrayList<ObjectArrayList<Function>> steps = assertTimeoutPreemptively(
				Duration.ofSeconds(1), () -> new MathSolver(equation).solveAllSteps());

		assertEquals(1, steps.size());
		assertEquals(ObjectArrayList.wrap(new Function[] { expected }), steps.get(0));
	}

	@Test
	void automaticSolverDoesNotGuessBetweenMultipleUnknowns() throws InterruptedException, Error {
		final Equation equation = equation(
				new Sum(context, variable('X', V_TYPE.VARIABLE), variable('Y', V_TYPE.VARIABLE)),
				number(2));

		assertTrue(new MathSolver(equation).solveAllSteps().isEmpty());

		final Equation multipleSolutions = equation(
				new Sum(context, variable('X', V_TYPE.SOLUTION), variable('Y', V_TYPE.SOLUTION)),
				number(2));
		assertTrue(new MathSolver(multipleSolutions).solveAllSteps().isEmpty());
	}

	@Test
	void explicitSolveStepHonorsItsVariableArgument() {
		final Variable x = variable('X', V_TYPE.VARIABLE);
		final Variable y = variable('Y', V_TYPE.VARIABLE);
		final Equation equation = equation(new Sum(context, x, y), number(2));

		assertEquals(ObjectArrayList.wrap(new Equation[] {
				equation(x, new Subtraction(context, number(2), y))
		}), equation.solveStep('X'));
		assertEquals(ObjectArrayList.wrap(new Equation[] {
				equation(y, new Subtraction(context, number(2), x))
		}), equation.solveStep('Y'));
		assertTrue(equation.solveStep('Z').isEmpty());
	}

	@Test
	void constantsAreNeverSolveTargets() throws InterruptedException, Error {
		final Variable pi = variable(MathematicalSymbols.PI, V_TYPE.CONSTANT);
		final Variable x = variable('X', V_TYPE.SOLUTION);
		final Equation equation = equation(new Multiplication(context, pi, x), number(1));
		final Equation expected = equation(x, new Division(context, number(1), pi));

		final ObjectArrayList<ObjectArrayList<Function>> steps = new MathSolver(equation).solveAllSteps();

		assertEquals(1, steps.size());
		assertEquals(ObjectArrayList.wrap(new Function[] { expected }), steps.get(0));
		assertTrue(equation.solveStep(MathematicalSymbols.PI).isEmpty());
	}

	@Test
	void dividesOnlyByKnownNonZeroFactors() {
		final Variable x = variable('X', V_TYPE.SOLUTION);
		final Equation numeric = equation(
				new Multiplication(context, number(2), x), number(4));
		assertEquals(ObjectArrayList.wrap(new Equation[] {
				equation(x, new Division(context, number(4), number(2)))
		}), numeric.solveStep('X'));

		final Equation zero = equation(
				new Multiplication(context, number(0), x), number(0));
		assertTrue(zero.solveStep('X').isEmpty());

		final Equation symbolic = equation(
				new Multiplication(context, variable('A', V_TYPE.CONSTANT), x), number(1));
		assertTrue(symbolic.solveStep('X').isEmpty());
	}

	@Test
	void clearsOnlyKnownNonZeroDenominators() {
		final Variable x = variable('X', V_TYPE.SOLUTION);
		final Equation numeric = equation(new Division(context, x, number(2)), number(3));
		assertEquals(ObjectArrayList.wrap(new Equation[] {
				equation(x, new Multiplication(context, number(3), number(2)))
		}), numeric.solveStep('X'));

		final Equation symbolic = equation(
				new Division(context, x, variable('Y', V_TYPE.VARIABLE)), number(3));
		assertTrue(symbolic.solveStep('X').isEmpty());

		final Equation targetInDenominator = equation(
				new Division(context, number(1), x), number(3));
		assertTrue(targetInDenominator.solveStep('X').isEmpty());
	}

	private Equation equation(final Function left, final Function right) {
		return new Equation(context, left, right);
	}

	private Number number(final int value) {
		return new Number(context, value);
	}

	private Variable variable(final char name, final V_TYPE type) {
		return new Variable(context, name, type);
	}

	private static final class EmptyMathContext extends MathContext {
		private final ObjectArrayList<Rule> noRules = new ObjectArrayList<>();

		@Override
		public ObjectArrayList<Rule> getAcceptableRules(final RuleType ruleType) {
			return noRules;
		}
	}
}
