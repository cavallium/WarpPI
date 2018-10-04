package it.cavallium.warppi.gui.expression.blocks;

import java.io.Serializable;

public interface TreeBlock extends Serializable {
	TreeContainer getParentContainer();

	boolean hasParent();
}
