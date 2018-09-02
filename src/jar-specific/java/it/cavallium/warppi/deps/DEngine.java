package it.cavallium.warppi.deps;

import it.cavallium.warppi.ClassUtils;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.cpu.CPUEngine;
import it.cavallium.warppi.gui.graphicengine.framebuffer.FBEngine;

public class DEngine {
	public static GraphicEngine newGPUEngine() {
		try {
			return (GraphicEngine) ClassUtils.newClassInstance("it.cavallium.warppi.gui.graphicengine.gpu.GPUEngine");
		} catch (NullPointerException ex) {
			return null;
		}
	}

	public static GraphicEngine newHeadless24bitEngine() {
		return new it.cavallium.warppi.gui.graphicengine.headless24bit.Headless24bitEngine();
	}

	public static GraphicEngine newHeadless256Engine() {
		return new it.cavallium.warppi.gui.graphicengine.headless256.Headless256Engine();
	}

	public static GraphicEngine newHeadless8Engine() {
		return new it.cavallium.warppi.gui.graphicengine.headless8.Headless8Engine();
	}

	public static GraphicEngine newCPUEngine() {
		return new CPUEngine();
	}

	public static GraphicEngine newFBEngine() {
		return new FBEngine();
	}

	public static GraphicEngine newHtmlEngine() {
		return null;
	}
}
