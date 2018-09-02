package org.warp.picalculator.math.functions;

import org.warp.picalculator.Error;
import org.warp.picalculator.gui.expression.blocks.Block;
import org.warp.picalculator.gui.expression.blocks.BlockChar;
import org.warp.picalculator.math.Function;
import org.warp.picalculator.math.FunctionOperator;
import org.warp.picalculator.math.MathContext;
import org.warp.picalculator.math.MathematicalSymbols;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Sum extends FunctionOperator {

	public Sum(MathContext root, Function value1, Function value2) {
		super(root, value1, value2);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Sum) {
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
	public Sum clone() {
		return new Sum(mathContext, parameter1, parameter2);
	}

	@Override
	public ObjectArrayList<Block> toBlock(MathContext context) throws Error {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		result.addAll(getParameter1().toBlock(context));
		result.add(new BlockChar(MathematicalSymbols.SUM));
		result.addAll(getParameter2().toBlock(context));
		return result;
	}

}
