package it.cavallium.warppi.gui.graphicengine;

/**
 *
 * @author andreacv
 */
public interface BinaryFont extends Skin {

	int getStringWidth(String text);

	int getCharacterWidth();

	int getCharacterHeight();
}
