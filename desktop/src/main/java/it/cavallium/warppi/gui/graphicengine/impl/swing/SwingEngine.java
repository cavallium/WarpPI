package it.cavallium.warppi.gui.graphicengine.impl.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.util.EventSubmitter;

public class SwingEngine implements GraphicEngine {

	private final int defaultWidth;
	private final int defaultHeight;
	private SwingWindow INSTANCE;
	public SwingRenderer r;
	public volatile BufferedImage g;
	public volatile boolean initialized;

	public SwingEngine(int defaultWidth, int defaultHeight) {
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	@Override
	public void setTitle(final String title) {
		INSTANCE.setTitle(title);
	}

	@Override
	public void setResizable(final boolean r) {
		INSTANCE.setResizable(r);
		if (!r)
			INSTANCE.setUndecorated(true);
	}

	@Override
	public void setDisplayMode(final int ww, final int wh) {
		INSTANCE.setSize(ww, wh);
		r.size = new int[] { ww, wh };
		SwingRenderer.canvas2d = new int[ww * wh];
		g = new BufferedImage(ww, wh, BufferedImage.TYPE_INT_RGB);
	}

	@Override
	public void create() {
		create(null);
	}

	@Override
	public void create(final Runnable onInitialized) {
		r = new SwingRenderer();
		g = new BufferedImage(r.size[0], r.size[1], BufferedImage.TYPE_INT_RGB);
		initialized = false;
		INSTANCE = new SwingWindow(r, g, this::destroyEngine, defaultWidth, defaultHeight);
		setResizable(WarpPI.getPlatform().getSettings().isDebugEnabled());
		INSTANCE.setVisible(true);
		initialized = true;
		if (onInitialized != null)
			onInitialized.run();
	}

	@Override
	public EventSubmitter<Integer[]> onResize() {
		return INSTANCE.onResize();
	}

	@Override
	public int getWidth() {
		return this.getSize()[0];
	}

	@Override
	public int getHeight() {
		return this.getSize()[1];
	}

	@Override
	public void destroy() {
		sendPowerOffSignal();
	}

	protected void destroyEngine() {
		initialized = false;
	}

	@Override
	public void start(final RenderingLoop d) {
		INSTANCE.setRenderingLoop(d);
		final Thread th = new Thread(() -> {
			try {
				double extratime = 0;
				while (initialized) {
					final long start = System.nanoTime();
					repaint();
					final long end = System.nanoTime();
					final double delta = end - start;
					if (extratime + delta < 50 * 1000d * 1000d) {
						Thread.sleep((long) Math.floor(50d - (extratime + delta) / 1000d / 1000d));
						extratime = 0;
					} else
						extratime += delta - 50d;
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
		if (WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getScreen() == null || WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().error != null && WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().error.length() > 0 || WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getScreen().mustBeRefreshed()) {
			INSTANCE.c.paintImmediately(0, 0, getWidth(), getHeight());
		}
	}

	@Override
	public void repaint() {
		INSTANCE.c.repaint();
	}

	public void subscribeExit(Runnable subscriber) {
		INSTANCE.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {

			}

			@Override
			public void windowClosing(WindowEvent e) {
				subscriber.run();
			}

			@Override
			public void windowClosed(WindowEvent e) {

			}

			@Override
			public void windowIconified(WindowEvent e) {

			}

			@Override
			public void windowDeiconified(WindowEvent e) {

			}

			@Override
			public void windowActivated(WindowEvent e) {

			}

			@Override
			public void windowDeactivated(WindowEvent e) {

			}
		});
	}

	public void sendPowerOffSignal() {
		INSTANCE.sendPowerOffSignal();
	}

	public abstract class Startable {
		public Startable() {
			force = false;
		}

		public Startable(final boolean force) {
			this.force = force;
		}

		public boolean force = false;

		public abstract void run();
	}

	@Override
	public int[] getSize() {
		return r.size.clone();
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public SwingRenderer getRenderer() {
		return r;
	}

	@Override
	public BinaryFont loadFont(final String fontName) throws IOException {
		return new SwingFont(fontName);
	}

	@Override
	public BinaryFont loadFont(final String path, final String fontName) throws IOException {
		return new SwingFont(path, fontName);
	}

	@Override
	public Skin loadSkin(final String file) throws IOException {
		return new SwingSkin(file);
	}

	@Override
	public boolean isSupported() {
		if (StaticVars.startupArguments.isEngineForced() && StaticVars.startupArguments.isCPUEngineForced() == false)
			return false;
		return (GraphicsEnvironment.isHeadless()) == false;
	}

	@Override
	public boolean doesRefreshPauses() {
		return true;
	}

	public void setAlphaChanged(final boolean val) {
		INSTANCE.setAlphaChanged(val);
	}

	public void setShiftChanged(final boolean val) {
		INSTANCE.setShiftChanged(val);
	}

	public Insets getInsets() {
		return INSTANCE.getInsets();
	}

	public void subscribeTouchDevice(MouseMotionListener mouseMotionListener, MouseListener mouseListener) {
		INSTANCE.c.addMouseListener(mouseListener);
		INSTANCE.c.addMouseMotionListener(mouseMotionListener);
	}
}
