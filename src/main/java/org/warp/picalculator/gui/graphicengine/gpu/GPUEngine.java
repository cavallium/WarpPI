package org.warp.picalculator.gui.graphicengine.gpu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import org.warp.picalculator.Main;
import org.warp.picalculator.Utils;
import org.warp.picalculator.gui.graphicengine.BinaryFont;
import org.warp.picalculator.gui.graphicengine.GraphicEngine;
import org.warp.picalculator.gui.graphicengine.RenderingLoop;
import org.warp.picalculator.gui.graphicengine.Skin;

import com.jogamp.opengl.GLProfile;

public class GPUEngine implements GraphicEngine {

	private volatile boolean initialized = false;
	private volatile boolean created = false;
	private NEWTWindow wnd;
	private RenderingLoop d;
	private GPURenderer r;
	int[] size = new int[] { Main.screenSize[0], Main.screenSize[1] };
	private final CopyOnWriteArrayList<BinaryFont> registeredFonts = new CopyOnWriteArrayList<BinaryFont>();

	@Override
	public int[] getSize() {
		return size;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void setTitle(String title) {
		wnd.window.setTitle(title);
	}

	@Override
	public void setResizable(boolean r) {
		if (!r) {
			wnd.window.setPosition(0, 0);
		}
		wnd.window.setResizable(r);
		wnd.window.setUndecorated(!r);
		wnd.window.setPointerVisible(r);
	}

	@Override
	public void setDisplayMode(int ww, int wh) {
		size[0] = ww;
		size[1] = wh;
		wnd.window.setSize((Utils.debugOn & Utils.debugWindow2x) ? ww * 2 : ww, (Utils.debugOn & Utils.debugWindow2x) ? wh * 2 : wh);
	}

	@Override
	public void create() {
		create(null);
	}
	
	@Override
	public void create(Runnable onInitialized) {
		created = true;
		r = new GPURenderer();
		wnd = new NEWTWindow(this);
		wnd.create();
		setDisplayMode(Main.screenSize[0], Main.screenSize[1]);
		setResizable(Utils.debugOn & !Utils.debugThirdScreen);
		initialized = true;
		wnd.onInitialized = onInitialized;
	}

	@Override
	public boolean wasResized() {
		return Main.screenSize[0] != size[0] | Main.screenSize[1] != size[1];
	}

	@Override
	public int getWidth() {
		return size[0];
	}

	@Override
	public int getHeight() {
		return size[1];
	}

	@Override
	public void destroy() {
		initialized = false;
		created = false;
		wnd.window.destroy();
	}

	@Override
	public void start(RenderingLoop d) {
		this.d = d;
		wnd.window.setVisible(true);
	}

	@Override
	public void repaint() {
		if (d != null & r != null && GPURenderer.gl != null) {
			d.refresh();
		}
	}

	@Override
	public GPURenderer getRenderer() {
		return r;
	}

	@Override
	public BinaryFont loadFont(String file) throws IOException {
		return new GPUFont(this, file);
	}

	@Override
	public Skin loadSkin(String file) throws IOException {
		return new GPUSkin(this, file);
	}

	@Override
	public void waitUntilExit() {
		try {
			do {
				Thread.sleep(500);
			} while (initialized | created);
		} catch (final InterruptedException e) {

		}
	}

	@Override
	public boolean isSupported() {
		if (Utils.forceEngine != null && Utils.forceEngine != "gpu") return false;
		if (Utils.headlessOverride) return false;
		boolean available = false;
		boolean errored = false;
		try {
			available = GLProfile.isAvailable(GLProfile.GL2ES1);
        } catch (Exception ex) {
        	errored = true;
        	System.err.println(ex.getMessage());
        }
		if (!available && !errored) {
			System.err.println(GLProfile.glAvailabilityToString());
		}
		return available;
	}

	@Override
	public boolean doesRefreshPauses() {
		return false;
	}
	
	public void registerFont(GPUFont gpuFont) {
		registeredFonts.add(gpuFont);
	}

	@Override
	public boolean supportsFontRegistering() {
		return true;
	}

	@Override
	public CopyOnWriteArrayList<BinaryFont> getRegisteredFonts() {
		return registeredFonts;
	}

}
