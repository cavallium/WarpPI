package it.cavallium.warppi.math.functions.trigonometry;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.Errors;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Cosine extends FunctionSingle {

	public Cosine(MathContext root, Function value) {
		super(root, value);
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FunctionSingle clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectArrayList<Block> toBlock(MathContext context) throws Error {
		// TODO Auto-generated method stub
		throw new Error(Errors.NOT_IMPLEMENTED, "Unknown function " + getClass().getSimpleName());
	}

}
