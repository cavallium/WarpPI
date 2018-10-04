package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Variable implements Function {

	protected char var;
	protected final MathContext root;
	protected V_TYPE type = V_TYPE.CONSTANT;

	public Variable(final MathContext root, final char val, final V_TYPE type) {
		this.root = root;
		var = val;
		this.type = type;
	}

	public Variable(final MathContext root, final String s, final V_TYPE type) throws Error {
		this(root, s.charAt(0), type);
	}

	/**
	 * Copy
	 * @param old
	 * @param root
	 */
	public Variable(Variable old, MathContext root) {
		this.root = root;
		this.type = old.type;
		this.var = old.var;
	}

	public char getChar() {
		return var;
	}

	public Variable setChar(final char val) {
		return new Variable(root, val, type);
	}

	public V_TYPE getType() {
		return type;
	}

	public Variable setType(final V_TYPE typ) {
		return new Variable(root, var, typ);
	}

	@Override
	public String toString() {
		return "" + getChar();
	}

	public static class VariableValue {
		public final Variable v;
		public final Number n;

		public VariableValue(final Variable v, final Number n) {
			this.v = v;
			this.n = n;
		}

		/**
		 * Copy
		 * @param old
		 * @param newContext
		 */
		public VariableValue(VariableValue old, MathContext newContext) {
			this.v = new Variable(old.v, newContext);
			this.n = new Number(old.n, newContext);
		}
	}

	@Override
	public ObjectArrayList<Function> simplify(final Rule rule) throws Error, InterruptedException {
		return rule.execute(this);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof Variable) {
			return ((Variable) o).getChar() == var && ((Variable) o).getType() == type;
		}
		return false;
	}

	@Override
	public MathContext getMathContext() {
		return root;
	}

	@Override
	public Variable clone() {
		return new Variable(root, var, type);
	}

	@Override
	public Variable clone(MathContext c) {
		return new Variable(c, var, type);
	}

	public static enum V_TYPE {
		CONSTANT, VARIABLE, SOLUTION
	}

	@Override
	public Function setParameter(final int index, final Function var) throws IndexOutOfBoundsException {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public Function getParameter(final int index) throws IndexOutOfBoundsException {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		//TODO: Temporary solution. In near future Variables will be distint objects and they will have a color. So they will be no longer a BlockChar/FeatureChar
		result.add(new BlockChar(getChar()));
		return result;
	}
}
