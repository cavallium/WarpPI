package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.parser.features.interfaces.FeatureSingle;

public abstract class FeatureSingleImpl implements FeatureSingle {
	private Object child;

	public FeatureSingleImpl(final Object child) {
		this.child = child;
	}

	@Override
	public Object getChild() {
		return child;
	}

	protected Function getFunction1() {
		return (Function) child;
	}

	@Override
	public void setChild(final Object obj) {
		child = obj;
	}
}
