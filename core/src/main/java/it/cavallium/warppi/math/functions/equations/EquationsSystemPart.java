package it.cavallium.warppi.math.functions.equations;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class EquationsSystemPart extends FunctionSingle {

	public EquationsSystemPart(final MathContext root, final Equation equazione) {
		super(root, equazione);
	}

	@Override
	public boolean equals(final Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EquationsSystemPart clone() {
		return new EquationsSystemPart(mathContext, (Equation) (parameter == null ? null : parameter.clone()));
	}

	@Override
	public EquationsSystemPart clone(MathContext c) {
		return new EquationsSystemPart(c, (Equation) (parameter == null ? null : parameter.clone(c)));
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T accept(final Function.Visitor<T> visitor) {
		return visitor.visit(this);
	}

}
