package it.cavallium.warppi.math.solver;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;

public final class SolverUtils {
	private SolverUtils() {
	}

	static CharArrayList findTargets(final Function function) {
		final Map<Character, V_TYPE> variables = variableTypes(function);
		final CharArrayList solutions = new CharArrayList();
		final CharArrayList unknowns = new CharArrayList();
		for (final Map.Entry<Character, V_TYPE> entry : variables.entrySet()) {
			if (entry.getValue() == V_TYPE.SOLUTION) {
				solutions.add(entry.getKey().charValue());
			} else if (entry.getValue() == V_TYPE.VARIABLE) {
				unknowns.add(entry.getKey().charValue());
			}
		}
		if (solutions.size() == 1) {
			return solutions;
		}
		if (solutions.size() > 1) {
			return new CharArrayList();
		}
		return unknowns.size() == 1 ? unknowns : new CharArrayList();
	}

	static int countVariable(final Function function, final char variable) {
		if (function == null) {
			return 0;
		}
		if (function instanceof Variable) {
			return ((Variable) function).getChar() == variable ? 1 : 0;
		}
		int count = 0;
		if (function instanceof FunctionOperator) {
			final FunctionOperator operator = (FunctionOperator) function;
			return countVariable(operator.getParameter1(), variable)
					+ countVariable(operator.getParameter2(), variable);
		}
		if (function instanceof FunctionSingle) {
			return countVariable(((FunctionSingle) function).getParameter(), variable);
		}
		if (function instanceof FunctionDynamic) {
			final FunctionDynamic dynamic = (FunctionDynamic) function;
			for (int i = 0; i < dynamic.getParametersLength(); i++) {
				count += countVariable(dynamic.getParameter(i), variable);
			}
		}
		return count;
	}

	static Variable findVariable(final Function function, final char variable) {
		if (function == null) {
			return null;
		}
		if (function instanceof Variable) {
			final Variable candidate = (Variable) function;
			return candidate.getChar() == variable ? candidate : null;
		}
		if (function instanceof FunctionOperator) {
			final FunctionOperator operator = (FunctionOperator) function;
			final Variable left = findVariable(operator.getParameter1(), variable);
			return left != null ? left : findVariable(operator.getParameter2(), variable);
		}
		if (function instanceof FunctionSingle) {
			return findVariable(((FunctionSingle) function).getParameter(), variable);
		}
		if (function instanceof FunctionDynamic) {
			final FunctionDynamic dynamic = (FunctionDynamic) function;
			for (int i = 0; i < dynamic.getParametersLength(); i++) {
				final Variable found = findVariable(dynamic.getParameter(i), variable);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	static boolean isProvablyNonZero(final Function function) {
		if (function instanceof Number) {
			return ((Number) function).getTerm().compareTo(BigDecimal.ZERO) != 0;
		}
		if (function instanceof Variable) {
			final Variable variable = (Variable) function;
			return variable.getType() == V_TYPE.CONSTANT
					&& (variable.getChar() == MathematicalSymbols.PI
							|| variable.getChar() == MathematicalSymbols.EULER_NUMBER);
		}
		return false;
	}

	// Compatibility helpers for equation solvers that are not enabled yet.
	static V_TYPE getVariableType(final Function function, final char variable) {
		return variableTypes(function).get(Character.valueOf(variable));
	}

	static void extractVariables(final Function function, final CharOpenHashSet variables) {
		for (final Character variable : variableTypes(function).keySet()) {
			variables.add(variable.charValue());
		}
	}

	private static Map<Character, V_TYPE> variableTypes(final Function function) {
		final Map<Character, V_TYPE> variables = new TreeMap<>();
		collectVariables(function, variables);
		return variables;
	}

	private static void collectVariables(final Function function,
			final Map<Character, V_TYPE> variables) {
		if (function == null) {
			return;
		}
		if (function instanceof Variable) {
			final Variable variable = (Variable) function;
			variables.merge(variable.getChar(), variable.getType(), SolverUtils::mergeTypes);
			return;
		}
		if (function instanceof FunctionOperator) {
			final FunctionOperator operator = (FunctionOperator) function;
			collectVariables(operator.getParameter1(), variables);
			collectVariables(operator.getParameter2(), variables);
		} else if (function instanceof FunctionSingle) {
			collectVariables(((FunctionSingle) function).getParameter(), variables);
		} else if (function instanceof FunctionDynamic) {
			final FunctionDynamic dynamic = (FunctionDynamic) function;
			for (int i = 0; i < dynamic.getParametersLength(); i++) {
				collectVariables(dynamic.getParameter(i), variables);
			}
		}
	}

	private static V_TYPE mergeTypes(final V_TYPE first, final V_TYPE second) {
		if (first == V_TYPE.SOLUTION || second == V_TYPE.SOLUTION) {
			return V_TYPE.SOLUTION;
		}
		if (first == V_TYPE.VARIABLE || second == V_TYPE.VARIABLE) {
			return V_TYPE.VARIABLE;
		}
		return V_TYPE.CONSTANT;
	}
}
