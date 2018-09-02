package org.warp.picalculator.gui.graphicengine.framebuffer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.concurrent.Semaphore;

import org.warp.picalculator.MmapByteBuffer;
import org.warp.picalculator.StaticVars;
import org.warp.picalculator.TestJNI;
import org.warp.picalculator.Utils;
import org.warp.picalculator.flow.BehaviorSubject;
import org.warp.picalculator.flow.Observable;
import org.warp.picalculator.gui.graphicengine.BinaryFont;
import org.warp.picalculator.gui.graphicengine.GraphicEngine;
import org.warp.picalculator.gui.graphicengine.Renderer;
import org.warp.picalculator.gui.graphicengine.RenderingLoop;
import org.warp.picalculator.gui.graphicengine.Skin;
import org.warp.picalculator.gui.graphicengine.cpu.CPUFont;
import org.warp.picalculator.gui.graphicengine.cpu.CPUSkin;

public class FBEngine implements GraphicEngine {

	private static final int FB_DISPLAY_WIDTH = 320;
	private static final int FB_DISPLAY_HEIGHT = 480;
	private static final int FB_DISPLAY_BPP = 32;
	private static final int WIDTH = 480;
	private static final int HEIGHT = 320;
	private static final int[] SIZE = new int[] { WIDTH, HEIGHT };
	private BehaviorSubject<Integer[]> onResize;
	private final TestJNI jni = new TestJNI();
	public FBRenderer r;
	private MappedByteBuffer fb;
	MmapByteBuffer realFb;
	private RandomAccessFile fbFileRW;
	public volatile boolean initialized = false;
	public Semaphore exitSemaphore = new Semaphore(0);
	
	@Override
	public int[] getSize() {
		return SIZE;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void setTitle(String title) {}

	@Override
	public void setResizable(boolean r) {}

	@Override
	public void setDisplayMode(int ww, int wh) {}

	@Override
	public void create(Runnable onInitialized) {
		onResize = BehaviorSubject.create(new Integer[] {SIZE[0], SIZE[1]});
		realFb = jni.retrieveBuffer();
		final long fbLen = realFb.getLength();
		fb = (MappedByteBuffer) ByteBuffer.allocateDirect((int) fbLen);
		
		r = new FBRenderer(this, fb);

		initialized = true;
		if (onInitialized != null) {
			onInitialized.run();
		}
	}

	@Override
	public Observable<Integer[]> onResize() {
		return onResize;
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public void destroy() {
		try {
			fbFileRW.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(RenderingLoop d) {
		final Thread th = new Thread(() -> {
			try {
				double extratime = 0;
				while (initialized) {
					final long start = System.currentTimeMillis();
					d.refresh();
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

	private int _________________TMP = 0;

	@Override
	public void repaint() {
		if (_________________TMP % 100 == 0) {
			System.out.println(System.currentTimeMillis());
			_________________TMP = 0;
		}
		_________________TMP++;
		realFb.getBuffer().clear();
		realFb.getBuffer().put(fb);
		for (int i = 0; i < fb.capacity() / 2; i++) {
			realFb.getBuffer().put(i, (byte) (_________________TMP < 50 ? 0xFF : 0xF0));
		}
		for (int i = fb.capacity() / 2; i < fb.capacity(); i++) {
			realFb.getBuffer().put(i, (byte) (0x18));
		}
	}

	@Override
	public Renderer getRenderer() {
		return r;
	}

	@Override
	public BinaryFont loadFont(String fontName) throws IOException {
		return new CPUFont(fontName);
	}

	@Override
	public BinaryFont loadFont(String path, String fontName) throws IOException {
		return new CPUFont(path, fontName);
	}

	@Override
	public Skin loadSkin(String file) throws IOException {
		return new CPUSkin(file);
	}

	@Override
	public void waitForExit() {
		try {
			exitSemaphore.acquire();
		} catch (final InterruptedException e) {}
	}

	@Override
	public boolean isSupported() {
		if (StaticVars.startupArguments.isEngineForced() && StaticVars.startupArguments.isFrameBufferEngineForced() == false) {
			return false;
		}
		if (StaticVars.startupArguments.isHeadlessEngineForced()) {
			return false;
		}
		/*
		File fbFile = new File("/dev/fb1");
		try {
			fbFileRW = new RandomAccessFile(fbFile, "rw");
			fbFileRW.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, fbFile.length());
			fbFileRW.close();
			return true;
		} catch (IOException ex) {
			System.err.println("Cannot read framebuffer fb1.");
			ex.printStackTrace();
		}
		*/
		return false;
	}

	@Override
	public boolean doesRefreshPauses() {
		return true;
	}

}
