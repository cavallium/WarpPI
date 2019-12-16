package it.cavallium.warppi.gui;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;

public class ScreenContext {
    private final GraphicEngine graphicEngine;
    private final int width;
    private final int height;

    public ScreenContext(GraphicEngine graphicEngine, int width, int height) {
        this.graphicEngine = graphicEngine;
        this.width = width;
        this.height = height;
    }

    public GraphicEngine getGraphicEngine() {
        return graphicEngine;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
