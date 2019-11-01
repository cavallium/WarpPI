package it.cavallium.warppi.gui;

import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;

public class RenderContext extends ScreenContext {
    private final Renderer renderer;

    public RenderContext(GraphicEngine graphicEngine, Renderer renderer, int width, int height) {
        super(graphicEngine, width, height);
        this.renderer = renderer;
    }

    public Renderer getRenderer() {
        return renderer;
    }
}
