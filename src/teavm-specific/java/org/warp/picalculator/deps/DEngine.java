package org.warp.picalculator.deps;

import org.warp.picalculator.ClassUtils;
import org.warp.picalculator.gui.graphicengine.GraphicEngine;
import org.warp.picalculator.gui.graphicengine.html.HtmlEngine;

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
