package it.cavallium.warppi.gui.graphicengine;

import java.io.IOException;
import java.net.URISyntaxException;

public interface Skin {

	public void load(String file) throws IOException, URISyntaxException;

	public void initialize(GraphicEngine d);

	public boolean isInitialized();

	public void use(GraphicEngine d);

	public int getSkinWidth();

	public int getSkinHeight();
}
