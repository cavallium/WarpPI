package org.warp.picalculator.deps;

import org.warp.picalculator.ClassUtils;
import org.warp.picalculator.gui.graphicengine.GraphicEngine;
import org.warp.picalculator.gui.graphicengine.cpu.CPUEngine;
import org.warp.picalculator.gui.graphicengine.framebuffer.FBEngine;

public class DEngine {
	public static GraphicEngine newGPUEngine() {
		try {
			return (GraphicEngine) ClassUtils.newClassInstance("org.warp.picalculator.gui.graphicengine.gpu.GPUEngine");
		} catch (NullPointerException ex) {
			return null;
		}
	}

	public static GraphicEngine newHeadless24bitEngine() {
		return new org.warp.picalculator.gui.graphicengine.headless24bit.Headless24bitEngine();
	}

	public static GraphicEngine newHeadless256Engine() {
		return new org.warp.picalculator.gui.graphicengine.headless256.Headless256Engine();
	}

	public static GraphicEngine newHeadless8Engine() {
		return new org.warp.picalculator.gui.graphicengine.headless8.Headless8Engine();
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
