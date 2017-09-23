package org.warp.picalculator.gui.graphicengine.cpu;

import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.warp.picalculator.Main;
import org.warp.picalculator.Utils;
import org.warp.picalculator.gui.DisplayManager;
import org.warp.picalculator.gui.graphicengine.BinaryFont;
import org.warp.picalculator.gui.graphicengine.GraphicEngine;
import org.warp.picalculator.gui.graphicengine.Renderer;
import org.warp.picalculator.gui.graphicengine.RenderingLoop;
import org.warp.picalculator.gui.graphicengine.Skin;

public class CPUEngine implements GraphicEngine {

	private SwingWindow INSTANCE;
	public final CPURenderer r = new CPURenderer();
	public BufferedImage g = new BufferedImage(r.size[0], r.size[1], BufferedImage.TYPE_INT_RGB);
	public volatile boolean initialized = false;

	@Override
	public void setTitle(String title) {
		INSTANCE.setTitle(title);
	}

	@Override
	public void setResizable(boolean r) {
		INSTANCE.setResizable(r);
		if (!r) {
			INSTANCE.setUndecorated(true);
		}
	}

	@Override
	public void setDisplayMode(final int ww, final int wh) {
		INSTANCE.setSize(ww, wh);
		r.size = new int[] { ww, wh };
		r.canvas2d = new int[ww * wh];
		g = new BufferedImage(ww, wh, BufferedImage.TYPE_INT_ARGB);
		INSTANCE.wasResized = false;
	}
	
	@Override
	public void create() {
		create(null);
	}
	
	@Override
	public void create(Runnable onInitialized) {
		INSTANCE = new SwingWindow(this);
		setResizable(Utils.debugOn & !Utils.debugThirdScreen);
		setDisplayMode(Main.screenSize[0], Main.screenSize[1]);
		INSTANCE.setVisible(true);
		initialized = true;
		if (onInitialized != null)
			onInitialized.run();
	}

	@Override
	public boolean wasResized() {
		if (INSTANCE.wasResized) {
			r.size = new int[] { INSTANCE.getWidth(), INSTANCE.getHeight() };
			r.canvas2d = new int[r.size[0] * r.size[1]];
			g = new BufferedImage(r.size[0], r.size[1], BufferedImage.TYPE_INT_ARGB);
			INSTANCE.wasResized = false;
			return true;
		}
		return false;
	}

	@Override
	public int getWidth() {
		return INSTANCE.getWidth() - Main.screenPos[0];
	}

	@Override
	public int getHeight() {
		return INSTANCE.getHeight() - Main.screenPos[1];
	}

	@Override
	public void destroy() {
		initialized = false;
		INSTANCE.setVisible(false);
		INSTANCE.dispose();
	}

	@Override
	public void start(RenderingLoop d) {
		INSTANCE.setRenderingLoop(d);
		final Thread th = new Thread(() -> {
			try {
				double extratime = 0;
				while (initialized) {
					final long start = System.currentTimeMillis();
					repaint();
					final long end = System.currentTimeMillis();
					final double delta = (end - start) / 1000d;
					final int deltaInt = (int) Math.floor(delta);
					final int extraTimeInt = (int) Math.floor(extratime);
					if (extraTimeInt + deltaInt < 50) {
						Thread.sleep(50 - (extraTimeInt + deltaInt));
						extratime = 0;
					} else {
						extratime += delta - 50d;
					}
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		});
		th.setName("CPU rendering thread");
		th.setDaemon(true);
		th.start();
	}

	@Deprecated()
	public void refresh() {
		if (DisplayManager.screen == null || (DisplayManager.error != null && DisplayManager.error.length() > 0) || DisplayManager.screen == null || DisplayManager.screen.mustBeRefreshed()) {
			INSTANCE.c.repaint();
		}
	}

	@Override
	public void repaint() {
		INSTANCE.c.repaint();
	}

	public abstract class Startable {
		public Startable() {
			force = false;
		}

		public Startable(boolean force) {
			this.force = force;
		}

		public boolean force = false;

		public abstract void run();
	}

	@Override
	public int[] getSize() {
		return r.size;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public CPURenderer getRenderer() {
		return r;
	}

	@Override
	public BinaryFont loadFont(String file) throws IOException {
		return new CPUFont(file);
	}

	@Override
	public Skin loadSkin(String file) throws IOException {
		return new CPUSkin(file);
	}

	@Override
	public void waitUntilExit() {
		try {
			do {
				Thread.sleep(500);
			} while (initialized);
		} catch (final InterruptedException e) {

		}
	}

	@Override
	public boolean isSupported() {
		if (Utils.forceEngine != null && Utils.forceEngine != "cpu") return false;
		return (Utils.headlessOverride || GraphicsEnvironment.isHeadless()) == false;
	}

	@Override
	public boolean doesRefreshPauses() {
		return true;
	}
}
