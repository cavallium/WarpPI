package it.cavallium.warppi.gui.expression.blocks;

public class BlockReference<T extends Block> {
	private final T block;
	private final BlockContainer container;
	private final int blockPosition;

	public BlockReference(final T block, final int blockPosition, final BlockContainer container) {
		this.block = block;
		this.blockPosition = blockPosition;
		this.container = container;
	}

	public T get() {
		return block;
	}

	public BlockContainer getContainer() {
		return container;
	}

	public int getIndex() {
		return blockPosition;
	}

	public BlockReference<?> getNextBlock() {
		return getBlockAtSafe(this.blockPosition + 1);
	}

	public boolean hasNextBlock() {
		return isInsideBounds(this.blockPosition + 1);
	}

	public BlockReference<?> getPreviousBlock() {
		return getBlockAtSafe(this.blockPosition - 1);
	}

	public boolean hasPreviousBlock() {
		return isInsideBounds(this.blockPosition - 1);
	}

	private BlockReference<?> getBlockAtSafe(final int i) {
		if (isInsideBounds(i)) {
			return container.getBlockAt(i);
		}
		return null;
	}

	private boolean isInsideBounds(final int i) {
		return i < container.getSize() && i >= 0;
	}

}
