package it.cavallium.warppi.math.functions.equations;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class EquationsSystemPart extends FunctionSingle {

	public EquationsSystemPart(MathContext root, Equation equazione) {
		super(root, equazione);
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EquationsSystemPart clone() {
		return new EquationsSystemPart(mathContext, (Equation) parameter);
	}

	@Override
	public ObjectArrayList<Block> toBlock(MathContext context) throws Error {
		// TODO Auto-generated method stub
		return null;
	}

}
