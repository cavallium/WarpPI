package it.cavallium.warppi.math.solver;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import it.cavallium.warppi.Platform;
import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MathSolver {
	static final int MAX_STEPS = 128;
	static final int MAX_BRANCHES = 128;
	private static final int MAX_PASSES = MAX_STEPS * 6;

	private final Function initialFunction;

	private enum StepState {
		CALCULATION(RuleType.CALCULATION),
		EXPANSION(RuleType.EXPANSION),
		POST_EXPANSION_CALCULATION(RuleType.CALCULATION),
		REDUCTION(RuleType.REDUCTION);

		private final RuleType ruleType;

		StepState(final RuleType ruleType) {
			this.ruleType = ruleType;
		}

		private StepState next() {
			switch (this) {
				case CALCULATION:
					return EXPANSION;
				case EXPANSION:
					return POST_EXPANSION_CALCULATION;
				case POST_EXPANSION_CALCULATION:
					return REDUCTION;
				default:
					throw new IllegalStateException("Reduction is the final solver phase.");
			}
		}
	}

	public MathSolver(final Function initialFunction) {
		this.initialFunction = Objects.requireNonNull(initialFunction, "initialFunction");
	}

	public ObjectArrayList<ObjectArrayList<Function>> solveAllSteps() throws InterruptedException, Error {
		final ObjectArrayList<ObjectArrayList<Function>> steps = new ObjectArrayList<>();
		ObjectArrayList<Function> current = new ObjectArrayList<>();
		current.add(initialFunction);

		final Set<Set<Function>> seenStates = new HashSet<>();
		seenStates.add(new HashSet<>(current));
		StepState state = StepState.CALCULATION;
		final ConsoleUtils console = getConsoleUtils();
		final boolean verbose = console != null
				&& console.getOutputLevel() >= ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE;
		if (verbose) {
			console.out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE,
					"Math Solver", "Input: " + initialFunction);
		}

		for (int pass = 0; pass < MAX_PASSES; pass++) {
			checkInterrupted();
			ObjectArrayList<Function> next = applyRules(current, RuleType.EXISTENCE, seenStates);
			if (next == null) {
				next = applyRules(current, state.ruleType, seenStates);
				if (next == null) {
					if (state == StepState.REDUCTION) {
						next = solveEquations(current, seenStates);
						if (next == null) {
							return steps;
						}
						state = StepState.CALCULATION;
					} else {
						state = state.next();
						continue;
					}
				}
				if (state == StepState.REDUCTION) {
					state = StepState.CALCULATION;
				}
			}

			if (steps.size() >= MAX_STEPS) {
				throw limitExceeded("step", MAX_STEPS);
			}
			current = next;
			steps.add(current);
			if (verbose) {
				console.out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE,
						"Math Solver", "Step " + steps.size(), current.toString());
			}
		}

		throw limitExceeded("pass", MAX_PASSES);
	}

	private ObjectArrayList<Function> solveEquations(final ObjectArrayList<Function> functions,
			final Set<Set<Function>> seenStates) throws InterruptedException, Error {
		for (int branchIndex = 0; branchIndex < functions.size(); branchIndex++) {
			checkInterrupted();
			final Function function = functions.get(branchIndex);
			if (!(function instanceof Equation)) {
				continue;
			}
			for (final SolveMethod technique : SolveMethod.techniques) {
				final ObjectArrayList<Equation> solutions = technique.solve((Equation) function);
				if (solutions.isEmpty()) {
					continue;
				}
				final ObjectArrayList<Function> candidate = replaceBranch(
						functions, branchIndex, solutions, "Equation solver");
				final Set<Function> state = new HashSet<>(candidate);
				if (seenStates.add(state)) {
					return candidate;
				}
			}
		}
		return null;
	}

	private ObjectArrayList<Function> applyRules(final ObjectArrayList<Function> functions,
			final RuleType ruleType, final Set<Set<Function>> seenStates) throws InterruptedException, Error {
		final ObjectArrayList<Rule> rules = initialFunction.getMathContext().getAcceptableRules(ruleType);
		if (rules == null || rules.isEmpty()) {
			return null;
		}

		for (int branchIndex = 0; branchIndex < functions.size(); branchIndex++) {
			checkInterrupted();
			final Function function = functions.get(branchIndex);
			for (final Rule rule : rules) {
				final List<Function> ruleResults = function.simplify(rule);
				if (ruleResults == null || ruleResults.isEmpty()) {
					continue;
				}
				final ObjectArrayList<Function> candidate = replaceBranch(
						functions, branchIndex, ruleResults, "Rule " + rule.getRuleName());
				final Set<Function> candidateState = new HashSet<>(candidate);
				if (seenStates.contains(candidateState)) {
					continue;
				}
				if (ruleType == RuleType.EXPANSION
						&& isImmediatelyReversible(candidate, functions)) {
					continue;
				}
				seenStates.add(candidateState);
				return candidate;
			}
		}
		return null;
	}

	private boolean isImmediatelyReversible(final ObjectArrayList<Function> expanded,
			final ObjectArrayList<Function> original) throws InterruptedException, Error {
		final ObjectArrayList<Rule> reductionRules = initialFunction.getMathContext()
				.getAcceptableRules(RuleType.REDUCTION);
		if (reductionRules == null || reductionRules.isEmpty()) {
			return false;
		}
		final Set<Function> originalState = new HashSet<>(original);
		for (int branchIndex = 0; branchIndex < expanded.size(); branchIndex++) {
			checkInterrupted();
			final Function function = expanded.get(branchIndex);
			for (final Rule rule : reductionRules) {
				final List<Function> results = function.simplify(rule);
				if (results != null && !results.isEmpty()
						&& originalState.equals(new HashSet<>(replaceBranch(
							expanded, branchIndex, results, "Rule " + rule.getRuleName())))) {
					return true;
				}
			}
		}
		return false;
	}

	private ObjectArrayList<Function> replaceBranch(final ObjectArrayList<Function> functions,
			final int branchIndex, final List<? extends Function> replacements,
			final String source) throws Error {
		final LinkedHashSet<Function> uniqueResults = new LinkedHashSet<>();
		for (int i = 0; i < branchIndex; i++) {
			addBranch(uniqueResults, functions.get(i));
		}
		for (final Function result : replacements) {
			if (result == null) {
				throw new Error(Errors.ERROR,
						source + " returned a null branch.");
			}
			addBranch(uniqueResults, result);
		}
		for (int i = branchIndex + 1; i < functions.size(); i++) {
			addBranch(uniqueResults, functions.get(i));
		}
		return new ObjectArrayList<>(uniqueResults);
	}

	private void addBranch(final Set<Function> results, final Function result) throws Error {
		if (results.add(result) && results.size() > MAX_BRANCHES) {
			throw limitExceeded("branch", MAX_BRANCHES);
		}
	}

	private static void checkInterrupted() throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
	}

	private static Error limitExceeded(final String resource, final int limit) {
		return new Error(Errors.TIMEOUT,
				"Math solver " + resource + " limit exceeded (" + limit + ").");
	}

	private static ConsoleUtils getConsoleUtils() {
		final Platform platform = WarpPI.getPlatform();
		return platform == null ? null : platform.getConsoleUtils();
	}
}
