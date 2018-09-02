package it.cavallium.warppi.gui;

public interface GraphicalElement {

	/**
	 * Recompute element's dimension parameters, like <strong>width</strong>,
	 * <strong>height</strong>, <strong>line</strong> or
	 * <strong>length</strong>.
	 */
	public void recomputeDimensions();

	/**
	 * 
	 * @return Width of the element.
	 */
	public int getWidth();

	/**
	 * 
	 * @return Height of the element.
	 */
	public int getHeight();

	/**
	 * 
	 * @return Position of the vertical alignment line of the element, relative
	 *         to itself.
	 */
	public int getLine();
}
