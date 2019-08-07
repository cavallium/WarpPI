package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Joke implements Function {

	public static final byte FISH = 0;
	public static final byte TORNADO = 1;
	public static final byte SHARKNADO = 2;
	@SuppressWarnings("unused")
	private static final String[] jokes = new String[] { "♓", "TORNADO", "SHARKNADO" };
	@SuppressWarnings("unused")
	private static final int[] jokesFont = new int[] { 4, -1, -1 };
	private final byte joke;
	private final MathContext root;

	public Joke(final MathContext root, final byte joke) {
		this.root = root;
		this.joke = joke;
	}

	@Override
	public ObjectArrayList<Function> simplify(final Rule rule) throws Error, InterruptedException {
		return rule.execute(this);
	}

	@Override
	public MathContext getMathContext() {
		return root;
	}

	@Override
	public Function clone() {
		return new Joke(root, joke);
	}

	@Override
	public Function clone(MathContext c) {
		return new Joke(c, joke);
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
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		// TODO Auto-generated method stub
		throw new Error(Errors.NOT_IMPLEMENTED, "Unknown function " + getClass().getSimpleName());
	}

	@Override
	public <T> T accept(final Function.Visitor<T> visitor) {
		return visitor.visit(this);
	}

}
