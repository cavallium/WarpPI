package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.gui.expression.blocks.BlockParenthesis;
import it.cavallium.warppi.math.*;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Multiplication extends FunctionOperator {

	public Multiplication(final MathContext root, final Function value1, final Function value2) {
		super(root, value1, value2);
		/*if (value1 instanceof Variable && value2 instanceof Variable == false) {
			parameter1 = value2;
			parameter2 = value1;
		}*/
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof Multiplication) {
			final FunctionOperator f = (FunctionOperator) o;
			if (parameter1.equals(f.getParameter1()) && parameter2.equals(f.getParameter2())) {
				return true;
			} else if (parameter1.equals(f.getParameter2()) && parameter2.equals(f.getParameter1())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Multiplication clone() {
		return new Multiplication(mathContext, parameter1 == null ? null : parameter1.clone(), parameter2 == null ? null : parameter2.clone());
	}

	@Override
	public Multiplication clone(MathContext c) {
		return new Multiplication(c, parameter1 == null ? null : parameter1.clone(c), parameter2 == null ? null : parameter2.clone(c));
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		final Function par1 = getParameter1();
		final Function par2 = getParameter2();
		final ObjectArrayList<Block> sub1 = par1.toBlock(context);
		final ObjectArrayList<Block> sub2 = par2.toBlock(context);
		final Block nearLeft = sub1.get(sub1.size() - 1);
		final Block nearRight = sub2.get(0);

		if (par1 instanceof Number && ((Number) par1).equals(new Number(context, -1))) {
			result.add(new BlockChar(MathematicalSymbols.MINUS));
			if (new Expression(context, par2).parenthesisNeeded()) {
				final ObjectArrayList<Block> parBlocks = par2.toBlock(context);
				final BlockParenthesis par = new BlockParenthesis(parBlocks);
				result.add(par);
			} else {
				result.addAll(sub2);
			}
			return result;
		} else {
			if (new Expression(context, par1).parenthesisNeeded()) {
				final ObjectArrayList<Block> parBlocks = par1.toBlock(context);
				final BlockParenthesis par = new BlockParenthesis(parBlocks);
				result.add(par);
			} else {
				result.addAll(sub1);
			}
			if (nearLeft instanceof BlockChar && nearRight instanceof BlockChar && !(par2 instanceof Negative) && !(par1 instanceof Number && par2 instanceof Number) && !(par1 instanceof Number && par2 instanceof Multiplication && ((Multiplication)par2).getParameter1() instanceof Number)) {

			} else {
				result.add(new BlockChar(MathematicalSymbols.MULTIPLICATION));
			}
			if (new Expression(context, par2).parenthesisNeeded()) {
				final ObjectArrayList<Block> parBlocks = par2.toBlock(context);
				final BlockParenthesis par = new BlockParenthesis(parBlocks);
				result.add(par);
			} else {
				result.addAll(sub2);
			}
			return result;
		}
	}

	@Override
	public <T> T accept(final Function.Visitor<T> visitor) {
		return visitor.visit(this);
	}

	public boolean isNegative() {
		return parameter1.equals(new Number(getMathContext(), -1)) || parameter2.equals(new Number(getMathContext(), -1));
	}

	public Function toPositive() {
		if (parameter1.equals(new Number(getMathContext(), -1))) {
			return parameter2;
		} else if (parameter2.equals(new Number(getMathContext(), -1))) {
			return parameter2;
		} else {
			return null;
		}
	}

	public static Multiplication newNegative(final MathContext context, final Function value2) {
		return new Multiplication(context, new Number(context, -1), value2);
	}
}