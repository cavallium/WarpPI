package it.cavallium.warppi.deps;

import it.cavallium.warppi.ClassUtils;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.html.HtmlEngine;

public class DEngine {
	public static GraphicEngine newCPUEngine() {
		return null;
	}
	public static GraphicEngine newGPUEngine() {
		return null;
	}
	public static GraphicEngine newHeadless24bitEngine() {
		return null;
	}
	public static GraphicEngine newHeadless256Engine() {
		return null;
	}
	public static GraphicEngine newHeadless8Engine() {
		return null;
	}
	public static GraphicEngine newFBEngine() {
		return null;
	}
	public static GraphicEngine newHtmlEngine() {
		return new HtmlEngine();
	}
}
