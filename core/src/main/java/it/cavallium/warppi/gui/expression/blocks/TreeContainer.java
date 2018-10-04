package it.cavallium.warppi.gui.expression.blocks;

import java.io.Serializable;

public interface TreeContainer extends Serializable {
	TreeBlock getParentBlock();

	boolean hasParent();
}
