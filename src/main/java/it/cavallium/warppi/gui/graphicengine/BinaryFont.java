package it.cavallium.warppi.gui.graphicengine;

/**
 *
 * @author andreacv
 */
public interface BinaryFont extends Skin {

	public int getStringWidth(String text);

	public int getCharacterWidth();

	public int getCharacterHeight();
}
