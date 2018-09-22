package it.cavallium.warppi.gui.graphicengine;

import java.io.IOException;
import java.net.URISyntaxException;

public interface Skin {

	void load(String file) throws IOException, URISyntaxException;

	void initialize(GraphicEngine d);

	boolean isInitialized();

	void use(GraphicEngine d);

	int getSkinWidth();

	int getSkinHeight();
}
