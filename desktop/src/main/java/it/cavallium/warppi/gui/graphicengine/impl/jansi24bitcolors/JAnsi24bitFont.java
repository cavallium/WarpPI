package it.cavallium.warppi.gui.graphicengine.impl.jansi24bitcolors;

import java.io.IOException;

import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;

public class JAnsi24bitFont implements BinaryFont {

	@Override
	public void load(final String file) throws IOException {

	}

	@Override
	public void initialize(final GraphicEngine d) {
		// TODO Auto-generated method stub

	}

	@Override
	public void use(final GraphicEngine d) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getStringWidth(final String text) {
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
