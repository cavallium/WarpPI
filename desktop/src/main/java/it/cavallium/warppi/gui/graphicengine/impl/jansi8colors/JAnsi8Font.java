package it.cavallium.warppi.gui.graphicengine.impl.jansi8colors;

import java.io.IOException;

import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;

public class JAnsi8Font implements BinaryFont {

	@Override
	public void load(String file) throws IOException {

	}

	@Override
	public void initialize(GraphicEngine d) {
		// TODO Auto-generated method stub

	}

	@Override
	public void use(GraphicEngine d) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getStringWidth(String text) {
		return 5 * text.length();
	}

	@Override
	public int getCharacterWidth() {
		return 5;
	}

	@Override
	public int getCharacterHeight() {
		return 5;
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public int getSkinWidth() {
		return 0;
	}

	@Override
	public int getSkinHeight() {
		return 0;
	}

}
