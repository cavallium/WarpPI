package it.cavallium.warppi.math;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.functions.Variable.VariableValue;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.RulesManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MathContext {

	public AngleMode angleMode = AngleMode.DEG;
	public boolean exactMode = false;
	public ObjectArrayList<Function> f;
	public ObjectArrayList<Function> f2;
	public ObjectArrayList<VariableValue> variablesValues;
	public int resultsCount;

	public MathContext() {
		f = new ObjectArrayList<>();
		f2 = new ObjectArrayList<>();
		variablesValues = new ObjectArrayList<>();
		resultsCount = 0;
	}

	@Deprecated
	public Function parseString(String string) throws Error {
		return null;
		/*
		if (string.contains("{")) {
			if (!string.startsWith("{")) {
				throw new Error(Errors.SYNTAX_ERROR);
			}
			final String[] parts = string.substring(1).split("\\{");
			final EquationsSystem s = new EquationsSystem(this);
			for (final String part : parts) {
				s.appendParameter(parseEquationString(part));
			}
			return s;
		} else if (string.contains("=")) {
			return parseEquationString(string);
		} else {
			return new Expression(this, string);
		}
		*/
	}

	@Deprecated
	public Function parseEquationString(String string) throws Error {
		return null;
		/*
		final String[] parts = string.split("=");
		if (parts.length == 1) {
			return new Equation(this, new Expression(this, parts[0]), new Number(this, BigInteger.ZERO));
		} else if (parts.length == 2) {
			return new Equation(this, new Expression(this, parts[0]), new Expression(this, parts[1]));
		} else {
			throw new Error(Errors.SYNTAX_ERROR);
		}*/
	}

	@Deprecated
	public ObjectArrayList<Function> solveExpression(ObjectArrayList<Function> input) throws Error {
		return null;
		/*
		ObjectArrayList<Function> results = new ObjectArrayList<>();
		final ObjectArrayList<Function> partialResults = new ObjectArrayList<>();
		for (final Function f : input) {
			if (f instanceof Equation) {
				throw new IllegalArgumentException("Not an expression!");
			} else {
				results.add(f);
				while (Utils.allSolved(results) == false) {
					for (final Function itm : results) {
						if (itm.isSimplified() == false) {
							final long t1 = System.currentTimeMillis();
							final List<Function> dt = itm.simplify();
							final long t2 = System.currentTimeMillis();
							if (!Utils.debugOn & (t2 - t1 >= 3000)) {
								throw new Error(Errors.TIMEOUT);
							}
							partialResults.addAll(dt);
						} else {
							partialResults.add(itm);
						}
					}
					results = new ObjectArrayList<>(partialResults);
					partialResults.clear();
				}
			}
		}
		return results;
		*/
	}

	public Function getChild() {
		return f.get(0);
	}

	public void init() {
		if (f == null & f2 == null) {
			f = new ObjectArrayList<>();
			f2 = new ObjectArrayList<>();
			variablesValues = new ObjectArrayList<>();
			resultsCount = 0;
		}
	}

	/*public void solve(EquationScreen equationScreen, char letter) throws Error {
		if (Calculator.currentSession == 0 && Calculator.sessions[0] instanceof EquationScreen) {
			EquationScreen es = (EquationScreen) Calculator.sessions[0];
			ObjectArrayList<Function> f = es.f;
			if (f instanceof Equation) {
				List<Function> results = ((Equation)f).solve(letter);
				Collections.reverse(results);
				// add elements to al, including duplicates
				Set<Function> hs = new LinkedHashSet<>();
				hs.addAll(results);
				results.clear();
				results.addAll(hs);
				es.f2 = results;
				for (Function rf : es.f2) {
					rf.generateGraphics();
				}
			}
		}
	}*/

	@Override
	@Deprecated
	public MathContext clone() {
//		MathContext mc = new MathContext();
//		mc.angleMode = this.angleMode;
//		mc.exactMode = this.exactMode;
//		mc.f = this.f;
//		mc.f2 = this.f2;
//		mc.variablesValues = this.variablesValues;
//		mc.resultsCount = this.resultsCount;
//		return mc;
		throw new UnsupportedOperationException();
	}

	public ObjectArrayList<Rule> getAcceptableRules(RuleType currentAcceptedRules) {
		return RulesManager.rules[currentAcceptedRules.ordinal()];
	}
}
