package it.cavallium.warppi.math.solver;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Error;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.deps.Platform.ConsoleUtils;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MathSolver {

	private final Function initialFunction;
	private final AtomicInteger stepState = new AtomicInteger(0);
	private int stepStateRepetitions = 0;
	private int consecutiveNullSteps = 0;

	private enum StepState {
		_1_CALCULATION, _2_EXPANSION, _3_CALCULATION, _4_REDUCTION
	}

	private final StepState[] stepStates = StepState.values();
	@SuppressWarnings("unchecked")
	private final ObjectArrayList<Function>[][] lastFunctions = new ObjectArrayList[2][stepStates.length];

	public MathSolver(Function initialFunction) {
		this.initialFunction = initialFunction;
	}

	@SuppressWarnings("unchecked")
	public ObjectArrayList<ObjectArrayList<Function>> solveAllSteps() throws InterruptedException, Error {
		final ObjectArrayList<ObjectArrayList<Function>> steps = new ObjectArrayList<>();
		ObjectArrayList<Function> lastFnc = null, currFnc = new ObjectArrayList<>();
		Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "Math Solver", "Solving all steps. Input: " + initialFunction.toString());
		currFnc.add(initialFunction);
		long stepNumber = 0;
		int initStepState = 0, endStepState = 0;
		final AtomicInteger stepState = new AtomicInteger(0);
		do {
			final ObjectArrayList<Function>[] currFncHistory = new ObjectArrayList[stepStates.length];
			final String stepName = "Step " + stepNumber;
			if (initStepState > endStepState) {
				for (int i = initStepState; i < stepStates.length; i++) {
					currFncHistory[i] = currFnc;
				}
				for (int i = 0; i <= initStepState; i++) {
					currFncHistory[i] = currFnc;
				}
			} else {
				for (int i = initStepState; i <= endStepState; i++) {
					currFncHistory[i] = currFnc;
				}
			}
			if (currFnc != null) {
				lastFunctions[1] = lastFunctions[0];
				lastFunctions[0] = currFncHistory;
			}
			lastFnc = currFnc;
			initStepState = stepState.get();
			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "Math Solver", stepName, "Starting step " + stepStates[initStepState] + ". Input: " + currFnc);
			final ObjectArrayList<Function> stepResult = solveStep(lastFnc, stepState);
			if (stepResult != null) {
				for (final Function result : stepResult) {
					Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, result.toString());
				}
				currFnc = stepResult;
				steps.add(currFnc);
			}
			endStepState = stepState.get();
			stepNumber++;

			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "Math Solver", stepName, "Step result: " + stepResult);
			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "Math Solver", stepName, "Step result details: Consecutive steps that did nothing: " + consecutiveNullSteps + ", this step did " + stepStateRepetitions + " simplifications.");
			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "Math Solver", stepName, "Next step state: " + stepStates[endStepState]);
			if (StaticVars.debugOn) {
				Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "Math Solver", stepName, currFnc + " is " + (checkEquals(currFnc, lastFunctions[0][endStepState]) ? "" : "not ") + "equals to [0]:" + lastFunctions[0][endStepState]);
			}
			if (StaticVars.debugOn) {
				Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "Math Solver", stepName, currFnc + " is " + (checkEquals(currFnc, lastFunctions[1][endStepState]) ? "" : "not ") + "equals to [1]:" + lastFunctions[1][endStepState]);
			}
		} while (consecutiveNullSteps < stepStates.length && !checkEquals(currFnc, lastFunctions[0][endStepState]) && !checkEquals(currFnc, lastFunctions[1][endStepState]));
		if (consecutiveNullSteps >= stepStates.length) {
			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "Math Solver", "Loop ended because " + consecutiveNullSteps + " >= " + stepStates.length);
		} else if (checkEquals(currFnc, lastFunctions[0][endStepState])) {
			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "Math Solver", "Loop ended because " + currFnc + " is equals to [0]:" + lastFunctions[0][endStepState]);
		} else {
			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "Math Solver", "Loop ended because " + currFnc + " is equals to [1]:" + lastFunctions[1][endStepState]);
		}
		return steps;
	}

	private boolean checkEquals(ObjectArrayList<Function> a, ObjectArrayList<Function> b) {
		if (a == null && b == null) {
			return true;
		} else if (a != null && b != null) {
			if (a.isEmpty() == b.isEmpty()) {
				int size;
				if ((size = a.size()) == b.size()) {
					for (int i = 0; i < size; i++) {
						if (a.get(i).equals(b.get(i)) == false) {
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private ObjectArrayList<Function> solveStep(ObjectArrayList<Function> fncs) throws InterruptedException, Error {
		return solveStep(fncs, stepState);
	}

	private ObjectArrayList<Function> solveStep(ObjectArrayList<Function> fncs, AtomicInteger stepState)
			throws InterruptedException, Error {
		final ObjectArrayList<Function> processedFncs = applyRules(fncs, RuleType.EXISTENCE); // Apply existence rules before everything
		if (processedFncs != null) {
			fncs = processedFncs;
		}
		RuleType currentAcceptedRules;
		switch (stepStates[stepState.get()]) {
			case _1_CALCULATION: {
				currentAcceptedRules = RuleType.CALCULATION;
				break;
			}
			case _2_EXPANSION: {
				currentAcceptedRules = RuleType.EXPANSION;
				break;
			}
			case _3_CALCULATION: {
				currentAcceptedRules = RuleType.CALCULATION;
				break;
			}
			case _4_REDUCTION: {
				currentAcceptedRules = RuleType.REDUCTION;
				break;
			}
			default:
				throw new RuntimeException("Unknown Step State");
		}
		final ObjectArrayList<Function> results = applyRules(fncs, currentAcceptedRules);
		switch (stepStates[stepState.get()]) {
			case _1_CALCULATION: {
				if (results == null) {
					stepState.incrementAndGet();
					consecutiveNullSteps++;
					stepStateRepetitions = 0;
				} else {
					consecutiveNullSteps = 0;
					stepStateRepetitions++;
					return results;
				}
				break;
			}
			case _2_EXPANSION: {
				if (results == null) {
					if (stepStateRepetitions == 0) {
						stepState.addAndGet(2);
						consecutiveNullSteps += 2;
					} else {
						stepState.incrementAndGet();
						consecutiveNullSteps++;
					}
					stepStateRepetitions = 0;
				} else {
					consecutiveNullSteps = 0;
					stepStateRepetitions++;
					return results;
				}
				break;
			}
			case _3_CALCULATION: {
				if (results == null) {
					stepState.incrementAndGet();
					consecutiveNullSteps++;
					stepStateRepetitions = 0;
				} else {
					consecutiveNullSteps = 0;
					stepStateRepetitions++;
					return results;
				}
				break;
			}
			case _4_REDUCTION: {
				if (results == null) {
					stepState.set(1);
					consecutiveNullSteps++;
					stepStateRepetitions = 0;
				} else {
					stepState.set(0);
					consecutiveNullSteps = 0;
					stepStateRepetitions++;
					return results;
				}
				break;
			}
			default:
				throw new RuntimeException("Unknown Step State");
		}
		return null;
	}

	private ObjectArrayList<Function> applyRules(ObjectArrayList<Function> fncs, RuleType currentAcceptedRules)
			throws InterruptedException, Error {
		final ObjectArrayList<Rule> rules = initialFunction.getMathContext().getAcceptableRules(currentAcceptedRules);
		ObjectArrayList<Function> results = null;
		ObjectArrayList<Rule> appliedRules = new ObjectArrayList<>();
		for (final Function fnc : fncs) {
			boolean didSomething = false;
			for (final Rule rule : rules) {
				List<Function> ruleResults = fnc.simplify(rule);
				if ((ruleResults != null && !ruleResults.isEmpty())) {
					if (results == null) {
						results = new ObjectArrayList<>();
					}
					results.addAll(ruleResults);
					appliedRules.add(rule);
					didSomething = true;
					break;
				}
			}
			if (!didSomething && fncs.size() > 1) {
				if (results == null) {
					results = new ObjectArrayList<>();
				}
				results.add(fnc);
			}
		}
		if (appliedRules.isEmpty())
			results = null;
		if (StaticVars.debugOn & results != null && !appliedRules.isEmpty()) {
			StringBuilder rulesStr = new StringBuilder();
			for (Rule r : appliedRules) {
				rulesStr.append(r.getRuleName());
				rulesStr.append(',');
			}
			if (rulesStr.length() > 0) {
				rulesStr.setLength(rulesStr.length() - 1);
			}
			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "Math Solver", currentAcceptedRules.toString(), "Applied rules: " + rulesStr);
		}
		return results;
	}
}
