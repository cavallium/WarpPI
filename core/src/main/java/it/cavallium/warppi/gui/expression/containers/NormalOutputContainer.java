package it.cavallium.warppi.gui.expression.containers;

import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class NormalOutputContainer extends OutputContainer {

	public NormalOutputContainer() {
		super();
	}

	public NormalOutputContainer(final boolean small) {
		super(small);
	}

	public NormalOutputContainer(final boolean small, final int minWidth, final int minHeight) {
		super(small, minWidth, minHeight);
	}

	public NormalOutputContainer(OutputContainer old) {
		super.roots.clear();
		super.roots.addAll(old.roots.clone());
	}
}
