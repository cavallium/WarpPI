package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.parser.features.interfaces.FeatureDouble;

public abstract class FeatureDoubleImpl implements FeatureDouble {
	private Object child_1;
	private Object child_2;

	public FeatureDoubleImpl(final Object child1, final Object child2) {
		child_1 = child1;
		child_2 = child2;
	}

	@Override
	public Object getChild1() {
		return child_1;
	}

	@Override
	public void setChild1(final Object obj) {
		child_1 = obj;
	}

	@Override
	public Object getChild2() {
		return child_2;
	}

	@Override
	public void setChild2(final Object obj) {
		child_2 = obj;
	}

	protected Function getFunction1() {
		return (Function) child_1;
	}

	protected Function getFunction2() {
		return (Function) child_2;
	}
}
