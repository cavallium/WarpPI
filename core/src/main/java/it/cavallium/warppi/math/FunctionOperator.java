package it.cavallium.warppi.math;

import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class FunctionOperator implements Function {

	/**
	 * Create a new instance of FunctionOperator. The Math Context will be the
	 * same of <strong>value1</strong>'s.
	 *
	 * @throws NullPointerException
	 *             when value1 is null.
	 * @param value1
	 *            The parameter of this function.
	 * @param value2
	 *            The parameter of this function.
	 */
	public FunctionOperator(final Function value1, final Function value2) throws NullPointerException {
		mathContext = value1.getMathContext();
		parameter1 = value1;
		parameter2 = value2;
	}

	/**
	 * Create a new instance of FunctionOperator.
	 *
	 * @param value1
	 *            The parameter of this function.
	 * @param value2
	 *            The parameter of this function.
	 */
	public FunctionOperator(final MathContext mc, final Function value1, final Function value2) {
		mathContext = mc;
		parameter1 = value1;
		parameter2 = value2;
	}

	protected final MathContext mathContext;

	protected Function parameter1 = null;
	protected Function parameter2 = null;

	/**
	 *
	 * @return First parameter.
	 */
	public Function getParameter1() {
		return parameter1;
	}

	/**
	 *
	 * @return Second parameter.
	 */
	public Function getParameter2() {
		return parameter2;
	}

	/**
	 *
	 * @param var
	 *            First parameter.
	 * @return A new instance of this function.
	 */
	public FunctionOperator setParameter1(final Function var) {
		final FunctionOperator s = clone();
		s.parameter1 = var;
		return s;
	}

	/**
	 *
	 * @param var
	 *            Second parameter.
	 * @return A new instance of this function.
	 */
	public FunctionOperator setParameter2(final Function var) {
		final FunctionOperator s = clone();
		s.parameter2 = var;
		return s;
	}

	@Override
	public FunctionOperator setParameter(final int index, final Function var) throws IndexOutOfBoundsException {
		switch (index) {
			case 0:
				return setParameter1(var);
			case 1:
				return setParameter2(var);
			default:
				throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public Function getParameter(final int index) throws IndexOutOfBoundsException {
		switch (index) {
			case 0:
				return getParameter1();
			case 1:
				return getParameter2();
			default:
				throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public MathContext getMathContext() {
		return mathContext;
	}

	@Override
	public final ObjectArrayList<Function> simplify(final Rule rule) throws Error, InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();

		final ObjectArrayList<Function> simplifiedParam1 = parameter1.simplify(rule);
		final ObjectArrayList<Function> simplifiedParam2 = parameter2.simplify(rule);
		try {
			if (simplifiedParam1 == null & simplifiedParam2 == null)
				return rule.execute(this);
		} catch (final Exception e) {
			final Error err = new Error(Errors.ERROR, "Error while executing rule '" + rule.getRuleName() + "'!\n" + e.getMessage());
			err.initCause(e);
			throw err;
		}

		if (Thread.interrupted())
			throw new InterruptedException();
		final ObjectArrayList<Function> result = new ObjectArrayList<>();

		final ObjectArrayList<Function> l1 = new ObjectArrayList<>();
		final ObjectArrayList<Function> l2 = new ObjectArrayList<>();
		if (Thread.interrupted())
			throw new InterruptedException();
		if (simplifiedParam1 == null)
			l1.add(parameter1);
		else {
			if (Thread.interrupted())
				throw new InterruptedException();
			l1.addAll(simplifiedParam1);
		}
		if (Thread.interrupted())
			throw new InterruptedException();
		if (simplifiedParam2 == null)
			l2.add(parameter2);
		else {
			if (Thread.interrupted())
				throw new InterruptedException();
			l2.addAll(simplifiedParam2);
		}

		final Function[][] results = Utils.joinFunctionsResults(l1, l2);

		for (final Function[] f : results)
			result.add(setParameter1(f[0]).setParameter2(f[1]));

		return result;
	}

	@Override
	public abstract FunctionOperator clone();

	@Override
	public int hashCode() {
		return parameter1.hashCode() + 7 * parameter2.hashCode() + 883 * super.hashCode();
	}

	@Override
	public abstract boolean equals(Object o);

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getParameter1() + "," + getParameter2() + ")";
	}
}
