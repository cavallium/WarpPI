package it.cavallium.warppi.gui;

public interface GraphicalElement {

	/**
	 * Recompute element's dimension parameters, like <strong>width</strong>,
	 * <strong>height</strong>, <strong>line</strong> or
	 * <strong>length</strong>.
	 */
	void recomputeDimensions();

	/**
	 *
	 * @return Width of the element.
	 */
	int getWidth();

	/**
	 *
	 * @return Height of the element.
	 */
	int getHeight();

	/**
	 *
	 * @return Position of the vertical alignment line of the element, relative
	 *         to itself.
	 */
	int getLine();
}
